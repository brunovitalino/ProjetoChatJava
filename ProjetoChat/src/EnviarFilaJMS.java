import java.io.*;
import java.util.*;
import javax.jms.*;
import javax.naming.*;

public class EnviarFilaJMS
{
    //String filaName; //Queue1, 2 ou 3.
    String salaDestino;
    String usuarioDestino;
    
	public EnviarFilaJMS(String salaDestino, String usuarioDestino)
	{
		this.salaDestino = salaDestino;
		this.usuarioDestino = usuarioDestino;
	}
	
    public void enviar(String msg)
    {
    	try
    	{
			Hashtable properties = new Hashtable();
			properties.put(Context.INITIAL_CONTEXT_FACTORY,"org.exolab.jms.jndi.InitialContextFactory");
			properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");
	
			Context context = new InitialContext(properties);
	
			QueueConnectionFactory qfactory = (QueueConnectionFactory) context.lookup("ConnectionFactory");
	
			QueueConnection qconnection = qfactory.createQueueConnection();
			QueueSession qsession = qconnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
	
			MapMessage mapMessage = qsession.createMapMessage();
			String mensagem = usuarioDestino+"|"+msg;
			mapMessage.setString(salaDestino, mensagem);
			System.out.println("filaEnv mensagem: " + mensagem);
	      
			javax.jms.Queue dest = (javax.jms.Queue) context.lookup("queue1");
			QueueSender sender = qsession.createSender(dest);
			sender.send(mapMessage);
	        
			context.close();
			qconnection.close();
    	}
    	catch(Exception e)
    	{
    		//e.printStackTrace();
    		System.out.println("JMS esta offline.");
    	}
    }
}

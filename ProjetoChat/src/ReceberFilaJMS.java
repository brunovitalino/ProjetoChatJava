import java.io.*;
import java.util.*;
import java.util.Queue;

import javax.jms.*;
import javax.naming.*;
 
public class ReceberFilaJMS
{
	//String nomeFila; //Queue1, 2 ou 3.
	private String sala;
	private String usuario;
    //String mensagem;
	private Queue<String> filaMensagens = new LinkedList<String>();
	private Map<String, Queue<String>> mensagensRelacao = new HashMap<String, Queue<String>>();
	
	public ReceberFilaJMS(String sala, String usuario)
	{
		//this.nomeFila = nomeFila;
		this.sala = sala;
		this.usuario = usuario;
		//this.mensagem = mensagem;
	}
	
	private void receber()
	{
		try
		{
			Hashtable properties = new Hashtable();
			properties.put(Context.INITIAL_CONTEXT_FACTORY,"org.exolab.jms.jndi.InitialContextFactory");
			properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");
	
			Context context = new InitialContext(properties);
	
			QueueConnectionFactory qfactory = (QueueConnectionFactory) context.lookup("ConnectionFactory");
			//
			QueueConnection qconnection = qfactory.createQueueConnection();
			QueueSession qsession = qconnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

			qconnection.start();
	
		    javax.jms.Queue dest1 = (javax.jms.Queue)context.lookup("queue1");
	        QueueReceiver qreceiver1 = qsession.createReceiver(dest1);
			QueueSender qsender1 = qsession.createSender(dest1);
	        
		    javax.jms.Queue dest2 = (javax.jms.Queue)context.lookup("queue2");
	        QueueReceiver qreceiver2 = qsession.createReceiver(dest2);
			QueueSender qsender2 = qsession.createSender(dest2);
	        //	                
			MapMessage mapMessage = null;
	
			while (true)
			{
				mapMessage = (MapMessage) qreceiver1.receiveNoWait(); //receiver(1) tenta receber alguma msg durante 1ms,
				//ou receiveNoWait() que tenta receber alguma msg caso haja alguma.
		        //Thread.sleep(2000);
				System.out.println("0- mapMessage: " + mapMessage);
				if (mapMessage!=null)
				{
					System.out.println("1- mapMessage.getString("+sala+"): " + mapMessage.getString(sala));
					if ( mapMessage.getString(sala)!=null && !mapMessage.getString(sala).isEmpty() )
					{
						System.out.println("2-------------------------------");
						//System.out.println("mapMessage.getString("+sala+"): " + mapMessage.getString(sala));
						if ( existe(usuario, mapMessage.getString(sala)) )
						{
							//System.out.println(" existe("+usuario+", mapMessage.getString("+sala+") ): " + existe(usuario, mapMessage.getString(sala)));
						
							String mensagem = extrairMensagem( mapMessage.getString(sala) );
							filaMensagens.add(mensagem);
							//System.out.println( mapMessage.getString(usuario) );
						}
						else
						{
							qsender2.send(mapMessage);
						}
					}
					else
					{
						qsender2.send(mapMessage);
					}
				}
				else
				{
					break;
				}	
			}
			//Passar os elementos da queue2 para a queue1.
			while ( true )
			{	
				mapMessage = (MapMessage) qreceiver2.receiveNoWait(); //receiver(1) tenta receber alguma msg durante 1s,
				//ou receiveNoWait() que tenta receber alguma msg caso haja alguma.
		        if (mapMessage!=null)
				{
					qsender1.send(mapMessage);
				}
				else
				{
					break;
				}      
			}
			//qreceiver1.close();
			//qreceiver2.close();
			//qsender1.close();
			//qsender2.close();
			//qsession.close();
			qconnection.close();
			context.close();
		}
		catch(Exception e)
		{
    		//e.printStackTrace();
    		System.out.println("JMS esta offline.");
        }
	}
	
	public Queue<String> getFila()
	{
		System.out.println("inicio");
		receber();
		System.out.println("fim");
	    return filaMensagens;
		
	}
	
	private boolean existe(String escolhido, String msg)
	{
		//Caractere que é colocado apos a ultima letra de nome usuario. Ex. usuario|msg
		int indexCaractereBarra = msg.indexOf('|');
		//Usaremos o proprio numero de index do caractereBarra, como uma quantidade a ser percorrida para achar o nome de usuario.
		//Ex: user|msg. caractereBarra possui index 4. E 4 eh a quantidade de caracteres a ser percorrido desde o index 0. Substring(0, 4).
		String user = msg.substring(0, indexCaractereBarra).toString();
		
		if (escolhido.equals(user))
		{
			return true;
		}
		else
		{
			return false;
		}		
	}
	
	private String extrairMensagem(String msg)
	{
		int indexCaractereBarra = msg.indexOf("|");
		//Temos o ex. user|msg. Nele o indexCaractereBarra eh 4. Para extrair a msg iniciamos desse index+1 e percorremos os caracteres restantes (lenght).
		msg = msg.substring(indexCaractereBarra+1, msg.length());
		return msg;
	}
	
}

import java.time.LocalTime;
import java.util.Queue;
import java.util.TreeMap;

public class RemocaoDeMensagens implements Runnable
{
	private TreeMap<String, Queue<String>> mensagens;
	private int velhoTempo;
	
	public RemocaoDeMensagens(TreeMap<String, Queue<String>> mensagens, int velhoTempo)
	{
		this.mensagens = mensagens;
		this.velhoTempo = velhoTempo;
	}
	
	public void run()
	{
		LocalTime tempoLocal;
		int novoTempo;
		int resultado;
		
		do
		{
			tempoLocal = LocalTime.now();
			novoTempo = tempoLocal.toSecondOfDay();
			//Essa condicao garante remocao de msgs enviadas antes da meia noite 00h.
			if ( velhoTempo > novoTempo )
			{	//Soma com os segundos totais de um dia completo.
				novoTempo = novoTempo + 86400;
			}
			resultado = novoTempo - velhoTempo;
		}
		while ( resultado<300 ); //300seg = 5min
	}
}

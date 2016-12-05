
import java.util.ArrayList;
import java.util.Queue;
import java.util.TreeMap;

import net.jini.core.entry.Entry;

public class Message implements Entry
{
	//Cada posicao possui [Sala, Usuarios]
	public TreeMap<String, ArrayList<String>> salasList;
	//Cada posicao possui [Sala_destino, (Usuario, Mensagens)]
	public TreeMap<String, TreeMap<String, Queue<String>>> mensagensSalas;
	//Cada posicao possui [Usuario_destino, (Usuario_origem, Mensagens privadas)]
	public TreeMap<String, TreeMap<String, Queue<String>>> mensagensUsuarios;
	//msg ou criacao ou remocao
	public TipoOperacao tipoOperacao;
	//publico ou privado
	public TipoDestino tipoDestino;
	//nome da sala ou cliente
	public String nomeRemetente;
	//nome da sala ou cliente
	public String nomeDestino;
	//conteudo da msg
	public String conteudo;
	
	public Message()
	{
		
	}
}
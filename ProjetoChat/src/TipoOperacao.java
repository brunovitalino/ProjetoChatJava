
public enum TipoOperacao
{
	AGUARDANDO_TAREFA(1),
	CONECTAR_USUARIO(2),
	DESCONECTAR_USUARIO(3),
	ENTRAR_SALA(4),
	SAIR_SALA(5),
	WISPAR_USUARIO(6),
	NOVA_MENSAGEM(7),
	REMOVER_MENSAGEM(8),
	RETORNO(9);
	
	private int valor;
	
	TipoOperacao(int valor)
	{
		this.valor = valor;
	}
	
	public int getValor()
	{
		return valor;
	}
}
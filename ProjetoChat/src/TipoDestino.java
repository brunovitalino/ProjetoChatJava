
public enum TipoDestino
{
	SALA(1),
	USUARIO(2);
	
	private int _valor;
	
	TipoDestino(int valor)
	{
		this._valor = valor;
	}
	
	public int getValor()
	{
		return _valor;
	}
}
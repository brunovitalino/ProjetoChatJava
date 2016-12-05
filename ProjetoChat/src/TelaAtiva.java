
public class TelaAtiva
{
	private TipoDestino _tipoTela;
	private String _nomeTela;
	
	public TelaAtiva(TipoDestino tipoTela, String nomeTela)
	{
		this.setTipoTela(tipoTela);
		this.setNomeTela(nomeTela);
	}

	TipoDestino getTipoTela() {
		return _tipoTela;
	}

	void setTipoTela(TipoDestino _tipoTela) {
		this._tipoTela = _tipoTela;
	}

	String getNomeTela() {
		return _nomeTela;
	}

	void setNomeTela(String _nomeTela) {
		this._nomeTela = _nomeTela;
	}
}

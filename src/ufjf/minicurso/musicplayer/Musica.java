package ufjf.minicurso.musicplayer;

/** Classe para armazenar os dados de cada música **/
public class Musica {
	private String endereco;
	private String titulo;
	private String duracao;
	private String artista;

	public Musica(String endereco, String titulo, String duracao, String artista) {
		this.endereco = endereco;
		this.titulo = titulo;
		this.duracao = duracao;
		this.artista = artista;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDuracao() {
		return duracao;
	}

	public void setDuracao(String duracao) {
		this.duracao = duracao;
	}

	public String getArtista() {
		return artista;
	}

	public void setArtista(String artista) {
		this.artista = artista;
	}

}

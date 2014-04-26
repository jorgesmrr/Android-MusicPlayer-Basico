package ufjf.minicurso.musicplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/** Classe para criar a Activity principal e controlar as intera��es do usu�rio **/
public class MusicListActivity extends ListActivity implements
		OnSeekBarChangeListener, OnCompletionListener {
	/** Objeto respons�vel por reproduzir as m�sicas **/
	private MediaPlayer player = new MediaPlayer();

	/** Armazena o view que exibe os dados da m�sica atual **/
	private View viewMusicaAtual;

	/** Lista com os dados de cada m�sica **/
	private List<Musica> musicas;

	/** Armazena a m�sica que est� tocando ou que est� pausada **/
	private Musica musicaAtual = null;

	/** Associa-se ao View da nossa barra de procura **/
	private SeekBar barraProcura;

	/** Utilizado para resumir a musica na posi�ao correta ao resumir a Activity **/
	private int duracao;

	/**
	 * Respons�vel por enviar umamensagem em uma Thread separada, independente
	 * da UI Thread
	 **/
	private Handler duracaoHandler = new Handler();

	/**
	 * Respons�vel por executar determinada tarefa (nesse caso chamar o m�todo
	 * que atualiza a barra
	 **/
	Runnable run = new Runnable() {
		@Override
		public void run() {
			try {
				// Se alguma m�sica est� tocando...
				if (player.isPlaying()) {
					// ... chama o m�todo para atualizar a barra
					atualizarBarra();
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	};

	/** M�todo para atualizar a barra **/
	public void atualizarBarra() {
		// Atualiza a barra se o player estiver tocando
		try {
			if (player.isPlaying()) {
				barraProcura.setProgress(player.getCurrentPosition());
				// Solicita que nosso Handler execute nosso Runnable daqui a 1
				// segundo
				duracaoHandler.postDelayed(run, 1000);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/** M�todo executado quando a Activity � criada **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Configura o layout desta Activity
		setContentView(R.layout.activity_music_list);

		// Inicializa��o da lista
		musicas = new ArrayList<Musica>();

		// Endere�o da pasta de m�sicas, no cart�o SD
		File pasta = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

		// Imprime o endere�o
		Log.v("Minicurso", pasta.getAbsolutePath());

		// Pega a lista de "sub-diret�rios" (arquivos ou pastas dentro do
		// endere�o)
		File arquivos[] = pasta.listFiles();

		// Se o endere�o n�o � v�lido...
		if (arquivos == null) {
			// ...n�o faz mais nada
			Log.v("Minicurso", "Endereco invalido");
			return;
		}

		for (int i = 0; i < arquivos.length
				&& arquivos[i].getName().endsWith(".mp3"); i++) {

			// Se o diret�rio � uma pasta...
			if (arquivos[i].listFiles() != null)
				// ...vai para o pr�ximo
				continue;

			// Armazena o endere�o completo do arquivo
			String arquivo = pasta.getAbsolutePath() + "/"
					+ arquivos[i].getName();

			// MediaMetadataRetriever � uma interface unificada para obter
			// metadata sobre um arquivo de m�dia
			MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
			metaRetriever.setDataSource(arquivo);

			// Obt�m o titulo
			String titulo = metaRetriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			// Se n�o tem a tag de t�tulo adota o nome do arquivo como titulo
			// (mas sem o ".mp3")
			if (titulo == null)
				titulo = arquivos[i].getName().substring(0,
						arquivos[i].getName().length() - 5);

			// Obt�m o artista
			String artista = metaRetriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

			// Obt�m a dura��o
			String duracaoMusica = metaRetriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

			// Formata a dura��o para o formato mm:ss
			long dur = Long.parseLong(duracaoMusica);
			String seconds = String.valueOf((dur % 60000) / 1000);
			String minutes = String.valueOf(dur / 60000);
			if (seconds.length() == 1) {
				duracaoMusica = "0" + minutes + ":0" + seconds;
			} else {
				duracaoMusica = "0" + minutes + ":" + seconds;
			}

			// Libera a mem�ria alocada internamente
			metaRetriever.release();

			// Adiciona um objeto Musica com as informa��es obtidas � lista de
			// m�sicas
			musicas.add(new Musica(arquivo, titulo, duracaoMusica, artista));
			Log.v("Minicurso", "Musica: " + titulo);
		}

		// Se n�o encontrou nenhuma m�sica...
		if (musicas.size() == 0) {
			// ... n�o faz mais nada
			Log.v("Minicurso", "Nenhuma musica no diretorio");
			return;
		}

		// Providencia o adaptador � ListView
		setListAdapter(new MusicListAdapter(this, R.layout.item_musica, musicas));

		// Localiza a barra de procura
		barraProcura = (SeekBar) findViewById(R.id.barra_duracao);
		// Define esta Activity como respons�vel por lidar com as mudan�as da
		// barra
		barraProcura.setOnSeekBarChangeListener(this);

	}

	/** M�todo executado quando o usu�rio toca em um item da ListView **/
	@Override
	protected void onListItemClick(ListView lista, View viewMusica,
			int posicao, long id) {		
		tocar(musicas.get(posicao), viewMusica);
		((ImageView) findViewById(R.id.imv_tocar))
				.setImageResource(R.drawable.ic_action_pause);		
	}

	/** M�todo executado sempre que o MediaPlayer termina de tocar uma m�sica **/
	@Override
	public void onCompletion(MediaPlayer player) {
		proxima(null);
	}

	/** M�todo para pular para a m�sica anterior **/
	public void anterior(View v) {
		// Obt�m o �ndice da m�sica atual e calcula o �ndice da p�xima m�sica
		int indiceMusicaAtual = musicas.indexOf(musicaAtual);
		int indiceMusicaAnterior;
		if (indiceMusicaAtual == 0)
			indiceMusicaAnterior = musicas.size() - 1;
		else
			indiceMusicaAnterior = indiceMusicaAtual - 1;

		// Obt�m o View que representa a pr�xima m�sica
		View viewMusica = getListView().getChildAt(indiceMusicaAnterior);

		// Toca a pr�xima m�sica
		tocar(musicas.get(indiceMusicaAnterior), viewMusica);

		((ImageView) findViewById(R.id.imv_tocar))
				.setImageResource(R.drawable.ic_action_pause);
	}

	/** M�todo para tocar a primeira m�sica ou pausar/resumir a m�sica atual **/
	public void tocarPausar(View v) {
		if (player.isPlaying()) {
			// Se estiver tocando, pausa a m�sica.
			player.pause();
			((ImageView) v).setImageResource(R.drawable.ic_action_play);
		} else if (viewMusicaAtual != null) {
			// Se n�o estiver tocando mas alguma outra m�sica j� foi tocada, o
			// player est� pausado. Basta inici�-lo.
			player.start();
			((ImageView) v).setImageResource(R.drawable.ic_action_pause);
		} else {
			// Se n�o estiver tocando e for a primeira vez que uma m�sica ser�
			// tocada, inicia a primeira m�sica da lista.
			((ImageView) v).setImageResource(R.drawable.ic_action_pause);
			View viewMusica = getListView().getChildAt(0);
			tocar(musicas.get(0), viewMusica);
		}
	}

	/** M�todo para pular para a pr�xima m�sica **/
	public void proxima(View v) {
		// Obt�m o �ndice da m�sica atual e calcula o �ndice da p�xima m�sica
		int indiceMusicaAtual = musicas.indexOf(musicaAtual);
		int indiceProximaMusica;
		if (indiceMusicaAtual == musicas.size() - 1)
			indiceProximaMusica = 0;
		else
			indiceProximaMusica = indiceMusicaAtual + 1;

		// Obt�m o View que representa a pr�xima m�sica
		View viewMusica = getListView().getChildAt(indiceProximaMusica);

		// Toca a pr�xima m�sica
		tocar(musicas.get(indiceProximaMusica), viewMusica);

		((ImageView) findViewById(R.id.imv_tocar))
				.setImageResource(R.drawable.ic_action_pause);
	}

	/** M�todo para tocar uma m�sica **/
	public void tocar(Musica musica, View viewMusica) {
		try {

			// Para a m�sica, se estiver tocando
			try {
				if (player.isPlaying())
					player.stop();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}

			// Libera os recursos do Media Player
			player.release();

			// Configura o Media Player e toca a m�sica
			player = new MediaPlayer();
			player.setOnCompletionListener(this);
			player.setDataSource(musica.getEndereco());
			player.prepare();
			player.setVolume(1f, 1f);
			player.setLooping(false);
			player.start();

			// Atualiza os Views
			if (viewMusicaAtual != null)
				viewMusicaAtual.setBackgroundResource(R.drawable.cartao_branco);
			viewMusica.setBackgroundResource(R.drawable.cartao_azul);
			viewMusicaAtual = viewMusica;
			musicaAtual = musica;

			// Atualiza a barra
			barraProcura.setMax(player.getDuration());
			atualizarBarra();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/** M�todo executado antes de a Activity ser finalizada **/
	@Override
	protected void onStop() {
		super.onStop();
		try {
			player.stop();
			player.release();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/** M�todo executado quando o usu�rio retorna � Activity **/
	@Override
	protected void onResume() {
		super.onResume();
		if (duracao > 0)
			try {
				player = new MediaPlayer();
				player.setOnCompletionListener(this);

				player.setDataSource(musicaAtual.getEndereco());

				player.prepare();
				player.setVolume(1f, 1f);
				player.setLooping(false);
				player.seekTo(duracao);
				player.start();

				viewMusicaAtual.setBackgroundResource(R.drawable.cartao_azul);

				barraProcura.setMax(player.getDuration());
				atualizarBarra();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/** M�todo executado quando a Activity � colocada em segundo plano **/
	@Override
	protected void onPause() {
		super.onPause();

		((ImageView) findViewById(R.id.imv_tocar))
				.setImageResource(R.drawable.ic_action_play);

		try {
			if (player.isPlaying()) {
				duracao = player.getCurrentPosition();
				player.stop();
				player.release();
				return;
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/** M�todo executado sempre que a posi��o da barra muda **/
	@Override
	public void onProgressChanged(SeekBar barra, int progresso,
			boolean doUsuario) {
		// Nada para fazer aqui
	}

	/** M�todo executado quando o usu�rio come�a a alterar a barra **/
	@Override
	public void onStartTrackingTouch(SeekBar barra) {
		// Nada para fazer aqui
	}

	/** M�todo executado quando o usu�rio deixa de alterar a barra **/
	@Override
	public void onStopTrackingTouch(SeekBar barra) {
		// Pula para a posi��o selecionada pelo usu�rio
		// A barra est� dividida pela dura��o da m�sica, em milisegundos,
		// por isso a propor��o � a mesma.
		player.seekTo(barra.getProgress());
	}

}

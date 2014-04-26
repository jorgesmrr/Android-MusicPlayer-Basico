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

/** Classe para criar a Activity principal e controlar as interações do usuário **/
public class MusicListActivity extends ListActivity implements
		OnSeekBarChangeListener, OnCompletionListener {
	/** Objeto responsável por reproduzir as músicas **/
	private MediaPlayer player = new MediaPlayer();

	/** Armazena o view que exibe os dados da música atual **/
	private View viewMusicaAtual;

	/** Lista com os dados de cada música **/
	private List<Musica> musicas;

	/** Armazena a música que está tocando ou que está pausada **/
	private Musica musicaAtual = null;

	/** Associa-se ao View da nossa barra de procura **/
	private SeekBar barraProcura;

	/** Utilizado para resumir a musica na posiçao correta ao resumir a Activity **/
	private int duracao;

	/**
	 * Responsável por enviar umamensagem em uma Thread separada, independente
	 * da UI Thread
	 **/
	private Handler duracaoHandler = new Handler();

	/**
	 * Responsável por executar determinada tarefa (nesse caso chamar o método
	 * que atualiza a barra
	 **/
	Runnable run = new Runnable() {
		@Override
		public void run() {
			try {
				// Se alguma música está tocando...
				if (player.isPlaying()) {
					// ... chama o método para atualizar a barra
					atualizarBarra();
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	};

	/** Método para atualizar a barra **/
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

	/** Método executado quando a Activity é criada **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Configura o layout desta Activity
		setContentView(R.layout.activity_music_list);

		// Inicialização da lista
		musicas = new ArrayList<Musica>();

		// Endereço da pasta de músicas, no cartão SD
		File pasta = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

		// Imprime o endereço
		Log.v("Minicurso", pasta.getAbsolutePath());

		// Pega a lista de "sub-diretórios" (arquivos ou pastas dentro do
		// endereço)
		File arquivos[] = pasta.listFiles();

		// Se o endereço não é válido...
		if (arquivos == null) {
			// ...não faz mais nada
			Log.v("Minicurso", "Endereco invalido");
			return;
		}

		for (int i = 0; i < arquivos.length
				&& arquivos[i].getName().endsWith(".mp3"); i++) {

			// Se o diretório é uma pasta...
			if (arquivos[i].listFiles() != null)
				// ...vai para o próximo
				continue;

			// Armazena o endereço completo do arquivo
			String arquivo = pasta.getAbsolutePath() + "/"
					+ arquivos[i].getName();

			// MediaMetadataRetriever é uma interface unificada para obter
			// metadata sobre um arquivo de mídia
			MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
			metaRetriever.setDataSource(arquivo);

			// Obtém o titulo
			String titulo = metaRetriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			// Se não tem a tag de título adota o nome do arquivo como titulo
			// (mas sem o ".mp3")
			if (titulo == null)
				titulo = arquivos[i].getName().substring(0,
						arquivos[i].getName().length() - 5);

			// Obtém o artista
			String artista = metaRetriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

			// Obtém a duração
			String duracaoMusica = metaRetriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

			// Formata a duração para o formato mm:ss
			long dur = Long.parseLong(duracaoMusica);
			String seconds = String.valueOf((dur % 60000) / 1000);
			String minutes = String.valueOf(dur / 60000);
			if (seconds.length() == 1) {
				duracaoMusica = "0" + minutes + ":0" + seconds;
			} else {
				duracaoMusica = "0" + minutes + ":" + seconds;
			}

			// Libera a memória alocada internamente
			metaRetriever.release();

			// Adiciona um objeto Musica com as informações obtidas à lista de
			// músicas
			musicas.add(new Musica(arquivo, titulo, duracaoMusica, artista));
			Log.v("Minicurso", "Musica: " + titulo);
		}

		// Se não encontrou nenhuma música...
		if (musicas.size() == 0) {
			// ... não faz mais nada
			Log.v("Minicurso", "Nenhuma musica no diretorio");
			return;
		}

		// Providencia o adaptador à ListView
		setListAdapter(new MusicListAdapter(this, R.layout.item_musica, musicas));

		// Localiza a barra de procura
		barraProcura = (SeekBar) findViewById(R.id.barra_duracao);
		// Define esta Activity como responsável por lidar com as mudanças da
		// barra
		barraProcura.setOnSeekBarChangeListener(this);

	}

	/** Método executado quando o usuário toca em um item da ListView **/
	@Override
	protected void onListItemClick(ListView lista, View viewMusica,
			int posicao, long id) {		
		tocar(musicas.get(posicao), viewMusica);
		((ImageView) findViewById(R.id.imv_tocar))
				.setImageResource(R.drawable.ic_action_pause);		
	}

	/** Método executado sempre que o MediaPlayer termina de tocar uma música **/
	@Override
	public void onCompletion(MediaPlayer player) {
		proxima(null);
	}

	/** Método para pular para a música anterior **/
	public void anterior(View v) {
		// Obtém o índice da música atual e calcula o índice da póxima música
		int indiceMusicaAtual = musicas.indexOf(musicaAtual);
		int indiceMusicaAnterior;
		if (indiceMusicaAtual == 0)
			indiceMusicaAnterior = musicas.size() - 1;
		else
			indiceMusicaAnterior = indiceMusicaAtual - 1;

		// Obtém o View que representa a próxima música
		View viewMusica = getListView().getChildAt(indiceMusicaAnterior);

		// Toca a próxima música
		tocar(musicas.get(indiceMusicaAnterior), viewMusica);

		((ImageView) findViewById(R.id.imv_tocar))
				.setImageResource(R.drawable.ic_action_pause);
	}

	/** Método para tocar a primeira música ou pausar/resumir a música atual **/
	public void tocarPausar(View v) {
		if (player.isPlaying()) {
			// Se estiver tocando, pausa a música.
			player.pause();
			((ImageView) v).setImageResource(R.drawable.ic_action_play);
		} else if (viewMusicaAtual != null) {
			// Se não estiver tocando mas alguma outra música já foi tocada, o
			// player está pausado. Basta iniciá-lo.
			player.start();
			((ImageView) v).setImageResource(R.drawable.ic_action_pause);
		} else {
			// Se não estiver tocando e for a primeira vez que uma música será
			// tocada, inicia a primeira música da lista.
			((ImageView) v).setImageResource(R.drawable.ic_action_pause);
			View viewMusica = getListView().getChildAt(0);
			tocar(musicas.get(0), viewMusica);
		}
	}

	/** Método para pular para a próxima música **/
	public void proxima(View v) {
		// Obtém o índice da música atual e calcula o índice da póxima música
		int indiceMusicaAtual = musicas.indexOf(musicaAtual);
		int indiceProximaMusica;
		if (indiceMusicaAtual == musicas.size() - 1)
			indiceProximaMusica = 0;
		else
			indiceProximaMusica = indiceMusicaAtual + 1;

		// Obtém o View que representa a próxima música
		View viewMusica = getListView().getChildAt(indiceProximaMusica);

		// Toca a próxima música
		tocar(musicas.get(indiceProximaMusica), viewMusica);

		((ImageView) findViewById(R.id.imv_tocar))
				.setImageResource(R.drawable.ic_action_pause);
	}

	/** Método para tocar uma música **/
	public void tocar(Musica musica, View viewMusica) {
		try {

			// Para a música, se estiver tocando
			try {
				if (player.isPlaying())
					player.stop();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}

			// Libera os recursos do Media Player
			player.release();

			// Configura o Media Player e toca a música
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

	/** Método executado antes de a Activity ser finalizada **/
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

	/** Método executado quando o usuário retorna à Activity **/
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

	/** Método executado quando a Activity é colocada em segundo plano **/
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

	/** Método executado sempre que a posição da barra muda **/
	@Override
	public void onProgressChanged(SeekBar barra, int progresso,
			boolean doUsuario) {
		// Nada para fazer aqui
	}

	/** Método executado quando o usuário começa a alterar a barra **/
	@Override
	public void onStartTrackingTouch(SeekBar barra) {
		// Nada para fazer aqui
	}

	/** Método executado quando o usuário deixa de alterar a barra **/
	@Override
	public void onStopTrackingTouch(SeekBar barra) {
		// Pula para a posição selecionada pelo usuário
		// A barra está dividida pela duração da música, em milisegundos,
		// por isso a proporção é a mesma.
		player.seekTo(barra.getProgress());
	}

}

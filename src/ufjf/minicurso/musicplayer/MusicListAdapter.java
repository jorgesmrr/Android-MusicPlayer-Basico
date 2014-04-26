package ufjf.minicurso.musicplayer;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapters n�o s�o usados somente por ListViews, mas tamb�m por outros Views
 * que extendem a classe AdapterView e.g. Spinner, GridView, Gallery e StackView
 **/
public class MusicListAdapter extends ArrayAdapter<Musica> {

	// Context geralmente fornece informa��es sobre o estado do objeto ou da
	// aplica��o. �til, por exemplo, para criar novos objetos.
	Context context;
	int layoutResourceId;
	List<Musica> musicas = null;

	/** M�todo construtor **/
	public MusicListAdapter(Context context, int layoutResourceId,
			List<Musica> musicas) {
		super(context, layoutResourceId, musicas);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.musicas = musicas;
	}

	/**
	 * M�todo que ir� constrir cada View para a ListView associada a este
	 * Adapter
	 **/
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		MusicHolder holder = null;

		// Se ainda n�o foi constru�do este view, configuramos seu layout
		if (row == null) {
			holder = new MusicHolder();
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder.txtNome = (TextView) row.findViewById(R.id.txtTitulo);
			holder.txtDuracao = (TextView) row.findViewById(R.id.txtDuracao);
			holder.txtArtista = (TextView) row.findViewById(R.id.txtArtista);
			row.setTag(holder);
			// Sen�o obtemos o layout j� definido
		} else {
			holder = (MusicHolder) row.getTag();
		}

		// Define os valores a serem exibidos
		holder.txtDuracao.setText(musicas.get(position).getDuracao());
		holder.txtNome.setText(musicas.get(position).getTitulo());
		holder.txtArtista.setText(musicas.get(position).getArtista());

		// Retorna o View constru�do
		return row;
	}

	/** Classe para melhor performance na constru��odos Views **/
	static class MusicHolder {
		public TextView txtNome, txtDuracao, txtArtista;
	}
}
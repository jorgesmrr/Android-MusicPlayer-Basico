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
 * Adapters não são usados somente por ListViews, mas também por outros Views
 * que extendem a classe AdapterView e.g. Spinner, GridView, Gallery e StackView
 **/
public class MusicListAdapter extends ArrayAdapter<Musica> {

	// Context geralmente fornece informações sobre o estado do objeto ou da
	// aplicação. Útil, por exemplo, para criar novos objetos.
	Context context;
	int layoutResourceId;
	List<Musica> musicas = null;

	/** Método construtor **/
	public MusicListAdapter(Context context, int layoutResourceId,
			List<Musica> musicas) {
		super(context, layoutResourceId, musicas);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.musicas = musicas;
	}

	/**
	 * Método que irá constrir cada View para a ListView associada a este
	 * Adapter
	 **/
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		MusicHolder holder = null;

		// Se ainda não foi construído este view, configuramos seu layout
		if (row == null) {
			holder = new MusicHolder();
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder.txtNome = (TextView) row.findViewById(R.id.txtTitulo);
			holder.txtDuracao = (TextView) row.findViewById(R.id.txtDuracao);
			holder.txtArtista = (TextView) row.findViewById(R.id.txtArtista);
			row.setTag(holder);
			// Senão obtemos o layout já definido
		} else {
			holder = (MusicHolder) row.getTag();
		}

		// Define os valores a serem exibidos
		holder.txtDuracao.setText(musicas.get(position).getDuracao());
		holder.txtNome.setText(musicas.get(position).getTitulo());
		holder.txtArtista.setText(musicas.get(position).getArtista());

		// Retorna o View construído
		return row;
	}

	/** Classe para melhor performance na construçãodos Views **/
	static class MusicHolder {
		public TextView txtNome, txtDuracao, txtArtista;
	}
}
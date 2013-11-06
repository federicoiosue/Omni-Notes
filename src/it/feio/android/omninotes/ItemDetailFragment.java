package it.feio.android.omninotes;

import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.DbHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import it.feio.android.omninotes.R;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {

    private Note note;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		if (getArguments().containsKey(Constants.INTENT_KEY)
				&& getArguments().getString(Constants.INTENT_KEY) != null) {
			DbHelper db = new DbHelper(getActivity().getApplicationContext());
			note = db.getNote(Integer.parseInt(getArguments().getString(Constants.INTENT_KEY)));
		}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        if (note != null) {
        	((TextView) rootView.findViewById(R.id.last_modification)).append(note.getlastModification());
        	((EditText) rootView.findViewById(R.id.title)).setText(note.getTitle());
        	((EditText) rootView.findViewById(R.id.content)).setText(note.getContent());
        }

        return rootView;
    }
}

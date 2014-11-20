package it.feio.android.omninotes.models.adapters;

import android.content.Context;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

public class NoteCardAdapter extends CardArrayRecyclerViewAdapter {
    private final Context mActivity;
    private final List<Card> cards;

    public NoteCardAdapter(Context context, List<Card> cards) {
        super(context, cards);
        this.mActivity = context;
        this.cards = cards;
    }


    @Override
    public CardRecyclerView getCardRecyclerView() {
        return super.getCardRecyclerView();
    }
}

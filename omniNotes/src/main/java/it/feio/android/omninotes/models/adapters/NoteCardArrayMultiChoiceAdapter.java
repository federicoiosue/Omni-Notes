package it.feio.android.omninotes.models.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnCABItemClickedListener;
import it.feio.android.omninotes.models.views.NoteCard;
import it.feio.android.omninotes.utils.Navigation;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayMultiChoiceAdapter;
import it.gmariotti.cardslib.library.view.base.CardViewWrapper;

public class NoteCardArrayMultiChoiceAdapter extends CardArrayMultiChoiceAdapter {

    private ActionMode mActionMode;
    private OnCABItemClickedListener onCABItemClickedListener;
    private List<Card> cards;
    private List<Note> notes;

    private NoteCardArrayMultiChoiceAdapter(Context context, List<Card> cards) {
        super(context, cards);
        this.cards = cards;
    }

    public NoteCardArrayMultiChoiceAdapter(Context context, List<Card> cards, List<Note> notes) {
        super(context, cards);
        this.notes = notes;
        this.cards = cards;
    }

    public void setOnActionItemClickedListener(OnCABItemClickedListener onCABItemClickedListener) {
        this.onCABItemClickedListener = onCABItemClickedListener;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mActionMode = mode;
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        prepareActionModeMenu();
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        onCABItemClickedListener.onCABItemClicked(item);
        return false;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked, CardViewWrapper cardView, Card card) {
        mActionMode.setTitle(String.valueOf(getSelectedCards().size()));
        prepareActionModeMenu();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        super.onDestroyActionMode(mode);
        mActionMode = null;
    }

    private void prepareActionModeMenu() {
        Menu menu = mActionMode.getMenu();
        int navigation = Navigation.getNavigation();
        boolean showArchive = navigation == Navigation.NOTES || navigation == Navigation.REMINDERS
                || navigation == Navigation.CATEGORY;
        boolean showUnarchive = navigation == Navigation.ARCHIVED || navigation == Navigation.CATEGORY;

        if (navigation == Navigation.TRASH) {
            menu.findItem(R.id.menu_untrash).setVisible(true);
            menu.findItem(R.id.menu_delete).setVisible(true);
        } else {
            if (getSelectedCards().size() == 1) {
                menu.findItem(R.id.menu_share).setVisible(true);
                menu.findItem(R.id.menu_merge).setVisible(false);
                menu.findItem(R.id.menu_archive)
                        .setVisible(showArchive && !((NoteCard) getSelectedCards().get(0)).getNote().isArchived
                                ());
                menu.findItem(R.id.menu_unarchive)
                        .setVisible(showUnarchive && ((NoteCard) getSelectedCards().get(0)).getNote().isArchived
                                ());
            } else {
                menu.findItem(R.id.menu_share).setVisible(false);
                menu.findItem(R.id.menu_merge).setVisible(true);
                menu.findItem(R.id.menu_archive).setVisible(showArchive);
                menu.findItem(R.id.menu_unarchive).setVisible(showUnarchive);

            }
            menu.findItem(R.id.menu_category).setVisible(true);
            menu.findItem(R.id.menu_tags).setVisible(true);
            menu.findItem(R.id.menu_trash).setVisible(true);
        }
        menu.findItem(R.id.menu_select_all).setVisible(true);
    }

    public List<Note> getSelectedNotes() {
        List<Note> notes = new ArrayList<Note>();
        for (Card card : getSelectedCards()) {
            Note note = ((NoteCard) card).getNote();
            notes.add(note);
        }
        return notes;
    }

    public void selectAll() {
        for (int i = 0; i < cards.size(); i++) {
            getCardListView().setItemChecked(i, true);
        }
    }

    public int getPosition(Note note) {
        for (Card card : cards) {
            if (((NoteCard) card).getNote().get_id() == note.get_id()) {
                return getPosition(card);
            }
        }
        return -1;
    }

    public Note getItemNote(int position) {
        return ((NoteCard)getItem(position)).getNote();
    }

    public void remove(Note note) {
        int index = notes.indexOf(note);
        notes.remove(index);
        cards.remove(index);
    }
}
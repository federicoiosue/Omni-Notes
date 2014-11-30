package it.feio.android.omninotes.models.adapters;

import android.app.Activity;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AbsListView;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnCABItemClickedListener;
import it.feio.android.omninotes.models.views.NoteCard;
import it.feio.android.omninotes.utils.Navigation;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;

import java.util.ArrayList;
import java.util.List;


public class NoteCardArrayMultiChoiceAdapter extends CardArrayAdapter implements AbsListView.MultiChoiceModeListener {

    private final Activity activity;
    private OnCABItemClickedListener onCABItemClickedListener;
    private List<Card> cards;
    private List<Note> notes = new ArrayList<Note>();
    private SparseBooleanArray mSelectedItemsIds = new SparseBooleanArray();
    private LayoutInflater inflater;
    private ActionMode mActionMode;


    private NoteCardArrayMultiChoiceAdapter(Activity activity, List<Card> cards) {
        super(activity, cards);
        this.cards = cards;
        this.activity = activity;
    }


    public NoteCardArrayMultiChoiceAdapter(Activity activity, List<Card> cards, List<Note> notes) {
        this(activity, cards);
        this.notes = notes;
    }


    public void setOnActionItemClickedListener(OnCABItemClickedListener onCABItemClickedListener) {
        this.onCABItemClickedListener = onCABItemClickedListener;
    }


    public ActionMode getActionMode() {
        return mActionMode;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        int bgColor = mSelectedItemsIds.get(position) ? mContext.getResources().getColor(R.color
                .kitkat_gray_dark) : mContext.getResources().getColor(android.R.color
                .transparent);
        convertView.findViewById(R.id.card_layout).setBackgroundColor(bgColor);
        return convertView;
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
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
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        prepareActionModeMenu();
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mSelectedItemsIds.clear();
        mActionMode = null;
        notifyDataSetChanged();
    }


    private void prepareActionModeMenu() {
        Menu menu = mActionMode.getMenu();
        int navigation = Navigation.getNavigation();
        boolean showArchive = navigation == Navigation.NOTES || navigation == Navigation.REMINDERS
                || navigation == Navigation.CATEGORY;
        boolean showUnarchive = navigation == Navigation.ARCHIVE || navigation == Navigation.CATEGORY;

        if (navigation == Navigation.TRASH) {
            menu.findItem(R.id.menu_untrash).setVisible(true);
            menu.findItem(R.id.menu_delete).setVisible(true);
        } else {
            if (getSelectedNotes().size() == 1) {
                menu.findItem(R.id.menu_share).setVisible(true);
                menu.findItem(R.id.menu_merge).setVisible(false);
                menu.findItem(R.id.menu_archive)
                        .setVisible(showArchive && !getSelectedNotes().get(0).isArchived
                                ());
                menu.findItem(R.id.menu_unarchive)
                        .setVisible(showUnarchive && getSelectedNotes().get(0).isArchived
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
        List<Note> selectedNotes = new ArrayList<Note>();
        for (int i = 0; i < mSelectedItemsIds.size(); i++) {
            if (mSelectedItemsIds.valueAt(i)) selectedNotes.add(notes.get(i));
        }
        return selectedNotes;
    }


    public void selectNote(Note note, boolean selected) {
        if (selected)
            mSelectedItemsIds.put(notes.indexOf(note), selected);
        else
            mSelectedItemsIds.delete(notes.indexOf(note));
        mActionMode.setTitle(String.valueOf(getSelectedNotes().size()));
        notifyDataSetChanged();
    }


    public void toggleSelection(Note note) {
        selectNote(note, !mSelectedItemsIds.get(notes.indexOf(note)));
    }


    public void selectAll() {
//        for (int i = 0; i < cards.size(); i++) {
//            getCardListView().setItemChecked(i, true);
//        }
        for (int i = 0; i < notes.size(); i++) {
            mSelectedItemsIds.put(i, true);
        }
        mActionMode.setTitle(String.valueOf(getSelectedNotes().size()));
        notifyDataSetChanged();
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
        return ((NoteCard) getItem(position)).getNote();
    }


    public void remove(Note note) {
        Card cardToRemove = null;
        for (Card card : cards) {
            if (((NoteCard) card).getNote().get_id() == note.get_id()) {
                cardToRemove = card;
            }
        }
        if (cardToRemove != null) {
            remove(cardToRemove);
        }
    }


    /**
     * Replaces a card
     */
    public void replace(Card card, int position) {
        if (cards.indexOf(card) != -1) {
            cards.remove(position);
        } else {
            position = cards.size();
        }
        cards.add(position, card);
    }
}
package it.feio.android.omninotes;

import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.DbHelper;
import it.feio.android.omninotes.R;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ItemDetailFragment}.
 */
public class ItemDetailActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(Constants.INTENT_KEY,
                    getIntent().getStringExtra(Constants.INTENT_KEY));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.save).setVisible(true);
		menu.findItem(R.id.share).setVisible(true);
		menu.findItem(R.id.delete).setVisible(true);
		menu.findItem(R.id.sort).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}
	

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                goHome();
        }
        return super.onOptionsItemSelected(item);
    }


	private boolean goHome() {
		NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));		
        return true;
	}


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.save:
				saveNote();
				break;
			case R.id.share:
				shareNote();
				break;
			case R.id.delete:
				deleteNote();
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}


	private void deleteNote() {
		
		// Confirm dialog creation
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(R.string.delete_note_confirmation)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						// Simply return to the previous activity/fragment if it was a new note
						if (getIntent().getStringExtra(Constants.INTENT_KEY) == null) {
							goHome();
							return;
						}
						
						// Create note object						
						int _id = Integer.parseInt(getIntent().getStringExtra(Constants.INTENT_KEY));
						Note note = new Note();
						note.set_id(_id);
						
						// Deleting note using DbHelper
						DbHelper db = new DbHelper(getApplicationContext());
						db.deleteNote(note);		
						
						// Informs the user about update
						Log.d(Constants.TAG, "Deleted note with id '" + _id + "'");
						Toast.makeText(getApplicationContext(), getResources().getText(R.string.note_deleted), Toast.LENGTH_SHORT).show();
				        goHome();
						return;
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}


	private void saveNote() {
		// Changed fields
		String title = ((EditText)findViewById(R.id.title)).getText().toString();
		String content = ((EditText)findViewById(R.id.content)).getText().toString();
		
		// Logging operation
		Log.d(Constants.TAG, "Saving new note titled: " + title);
		
		// New note object
		Note note = new Note();
		// If it's an editing operation also _id field will be filled
		int id = 0;
		if (getIntent().getExtras().containsKey(Constants.INTENT_KEY) && getIntent().getStringExtra(Constants.INTENT_KEY) != null) {
			id = Integer.parseInt(getIntent().getStringExtra(Constants.INTENT_KEY));
		}
		note.set_id(id);
		note.setTitle(title);
		note.setContent(content);
		
		// Saving changes to the note
		DbHelper db = new DbHelper(this);
		long _id = db.updateNote(note);
		
		// Informs the user about update
		Log.d(Constants.TAG, "New note saved with title '" + note.getTitle() + "' and id '" + _id + "'");
		Toast.makeText(this, getResources().getText(R.string.note_updated), Toast.LENGTH_SHORT).show();

		// Go back on stack
        goHome();
	}
	
	
	private void shareNote() {
		// Changed fields
		String title = ((EditText)findViewById(R.id.title)).getText().toString();
		String content = ((EditText)findViewById(R.id.content)).getText().toString();

		// Definition of shared content
		String text = title + System.getProperty("line.separator") + content
				+ System.getProperty("line.separator") + System.getProperty("line.separator")
				+ getResources().getString(R.string.shared_content_sign);
		
		// Prepare sharing intent
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("*/*");		
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_message_chooser)));
	}
}

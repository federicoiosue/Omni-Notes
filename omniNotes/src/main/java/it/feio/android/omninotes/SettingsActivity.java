package it.feio.android.omninotes;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.analitica.AnalyticsHelper;
import it.feio.android.omninotes.async.DataBackupIntentService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends ActionBarActivity implements FolderChooserDialog.FolderCallback {

	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.crouton_handle) ViewGroup croutonViewContainer;

	private List<Fragment> backStack = new ArrayList<>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		ButterKnife.bind(this);
		initUI();
		getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
	}


	void initUI() {
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}


	void switchToScreen(String key) {
		SettingsFragment sf = new SettingsFragment();
		Bundle b = new Bundle();
		b.putString(SettingsFragment.XML_NAME, key);
		sf.setArguments(b);
		backStack.add(getFragmentManager().findFragmentById(R.id.content_frame));
		replaceFragment(sf);
	}


	private void replaceFragment(Fragment sf) {
		getFragmentManager().beginTransaction().setCustomAnimations(R.animator.fade_in, R.animator.fade_out,
				R.animator.fade_in, R.animator.fade_out).replace(R.id.content_frame, sf).commit();
	}


	@Override
	public void onBackPressed() {
		if (backStack.size() > 0) {
			replaceFragment(backStack.remove(backStack.size() - 1));
		} else {
			super.onBackPressed();
		}
	}


	public void showMessage(int messageId, Style style) {
		showMessage(getString(messageId), style);
	}


	public void showMessage(String message, Style style) {
		// ViewGroup used to show Crouton keeping compatibility with the new Toolbar
		Crouton.makeText(this, message, style, croutonViewContainer).show();
	}


	@Override
	public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
		new MaterialDialog.Builder(this)
				.title(R.string.data_import_message_warning)
				.content(folder.getName())
				.positiveText(R.string.confirm)
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog materialDialog) {
						((OmniNotes)getApplication()).getAnalyticsHelper().trackEvent(AnalyticsHelper.CATEGORIES.SETTING,
								"settings_import_data");
						Intent service = new Intent(getApplicationContext(), DataBackupIntentService.class);
						service.setAction(DataBackupIntentService.ACTION_DATA_IMPORT_LEGACY);
						service.putExtra(DataBackupIntentService.INTENT_BACKUP_NAME, folder.getAbsolutePath());
						startService(service);
					}
				}).build().show();
	}
}

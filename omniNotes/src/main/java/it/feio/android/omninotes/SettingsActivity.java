package it.feio.android.omninotes;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class SettingsActivity extends AppCompatActivity {

    @Bind(R.id.crouton_handle) ViewGroup croutonViewContainer;
    @Bind(R.id.outer_toolbar) AppBarLayout outerToolbar;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

		ButterKnife.bind(this);
        toolbar = (Toolbar) outerToolbar.findViewById(R.id.toolbar);

        initUI();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }


    void initUI() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    void switchToScreen(String key) {
        SettingsFragment sf = new SettingsFragment();
        Bundle b = new Bundle();
        b.putString(SettingsFragment.XML_NAME, key);
        sf.setArguments(b);
        replaceFragment(sf);
    }

    private void replaceFragment(Fragment sf) {
        getFragmentManager().beginTransaction().setCustomAnimations(R.animator.fade_in, R.animator.fade_out,
                R.animator.fade_in, R.animator.fade_out)
                .remove(getFragmentManager()
                        .findFragmentById(R.id.content_frame))
                .add(R.id.content_frame, sf)
                .addToBackStack(null)
                .commit();
    }


	public void showMessage(int messageId, Style style) {
		showMessage(getString(messageId), style);
	}


	public void showMessage(String message, Style style) {
		// ViewGroup used to show Crouton keeping compatibility with the new Toolbar
		Crouton.makeText(this, message, style, croutonViewContainer).show();
	}
}

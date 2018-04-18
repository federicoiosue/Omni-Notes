/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class SettingsActivity extends ActionBarActivity {

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
}

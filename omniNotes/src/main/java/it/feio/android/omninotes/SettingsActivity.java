package it.feio.android.omninotes;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private List<Fragment> backStack = new ArrayList<Fragment>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initUI();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    void initUI() {
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    void switchToScreen(String key) {
        SettingsFragment sf = new SettingsFragment();
        Bundle b = new Bundle();
        b.putString(SettingsFragment.XML_NAME, key);
        sf.setArguments(b);
        backStack.add(getFragmentManager().findFragmentById(R.id.content_frame));
        getFragmentManager().beginTransaction().replace(R.id.content_frame, sf).commit();
    }


    @Override
    public void onBackPressed() {
        if (backStack.size() > 0) {
            getFragmentManager().beginTransaction().replace(R.id.content_frame, backStack.remove(backStack.size() - 1))
                    .commit();
        } else {
            super.onBackPressed();
        }
    }
}

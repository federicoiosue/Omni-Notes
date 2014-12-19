package it.feio.android.omninotes.models;

import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.omninotes.R;


public class ONStyle {

    public static final int DURATION_INFINITE = -1;
    public static final int DURATION_SHORT = 1000;
    public static final int DURATION_MEDIUM = 1650;
    public static final int DURATION_LONG = 2300;

    public static final Configuration CONFIGURATION;
    public static final Style ALERT;
    public static final Style WARN;
    public static final Style CONFIRM;
    public static final Style INFO;

    public static final int AlertRed = R.color.alert;
    public static final int WarnOrange = R.color.warning;
    public static final int ConfirmGreen = R.color.confirm;
    public static final int InfoYellow = R.color.info;


    static {
        CONFIGURATION = new Configuration.Builder()
                .setDuration(DURATION_MEDIUM)
                .setInAnimation(R.animator.fade_in)
                .setOutAnimation(R.animator.fade_out)
                .build();
        ALERT = new Style.Builder()
                .setBackgroundColor(AlertRed)
                .setHeight(LayoutParams.MATCH_PARENT)
                .setGravity(Gravity.CENTER)
                .setConfiguration(CONFIGURATION)
                .build();
        WARN = new Style.Builder()
                .setBackgroundColor(WarnOrange)
                .setHeight(LayoutParams.MATCH_PARENT)
                .setGravity(Gravity.CENTER)
                .setConfiguration(CONFIGURATION)
                .build();
        CONFIRM = new Style.Builder()
                .setBackgroundColor(ConfirmGreen)
                .setHeight(LayoutParams.MATCH_PARENT)
                .setTextAppearance(R.style.crouton_text)
                .setConfiguration(CONFIGURATION)
                .build();
        INFO = new Style.Builder()
                .setBackgroundColor(InfoYellow)
                .setHeight(LayoutParams.MATCH_PARENT)
                .setGravity(Gravity.CENTER)
                .setConfiguration(CONFIGURATION)
                .build();
    }
}
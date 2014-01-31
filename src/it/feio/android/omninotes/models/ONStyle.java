package it.feio.android.omninotes.models;

import android.widget.LinearLayout.LayoutParams;
import it.feio.android.omninotes.R;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ONStyle {
public static final int DURATION_INFINITE = -1;
public static final Style ALERT;
public static final Style WARN;
public static final Style CONFIRM;
public static final Style INFO;

public static final int AlertRed = R.color.alert;
public static final int WarnOrange= R.color.warning;
public static final int ConfirmGreen = R.color.confirm;
public static final int InfoYellow = R.color.info;

private static final int DURATION_SHORT  = 1500;
private static final int DURATION_MEDIUM = 2500;
private static final int DURATION_LONG   = 5500;


static {
    ALERT   = new Style.Builder()
                .setBackgroundColor(AlertRed)
//                .setDuration(DURATION_LONG)
                .setHeight(LayoutParams.WRAP_CONTENT)
                .setConfiguration(new Configuration.Builder().setDuration(DURATION_LONG).build())
                .build();
    WARN    = new Style.Builder()
//                .setDuration(DURATION_MEDIUM)
                .setBackgroundColor(WarnOrange)
                .setHeight(LayoutParams.WRAP_CONTENT)
                .setConfiguration(new Configuration.Builder().setDuration(DURATION_MEDIUM).build())
                .build();
    CONFIRM = new Style.Builder()
//                .setDuration(DURATION_MEDIUM)
                .setBackgroundColor(ConfirmGreen)
                .setHeight(LayoutParams.WRAP_CONTENT)
                .setConfiguration(new Configuration.Builder().setDuration(DURATION_MEDIUM).build())
                .build();
    INFO    = new Style.Builder()
//                .setDuration(DURATION_MEDIUM)
                .setBackgroundColor(InfoYellow)
                .setHeight(LayoutParams.WRAP_CONTENT)
                .setConfiguration(new Configuration.Builder().setDuration(DURATION_MEDIUM).build())
                .build();
}
}
package it.feio.android.omninotes.utils;

import android.support.v4.view.ViewGroupCompat;

import com.neopixl.pixlui.components.textview.TextView;

public class ChecklistManager {

	public static TextView convert(TextView v) {
		TextView returnView = null;
		
		String text = v.getText().toString();
		String[] lines = text.split("\n");
		
//		ViewGroupCompat vg
		for (String line : lines) {
			
		}
		
		return returnView;		
	}
}

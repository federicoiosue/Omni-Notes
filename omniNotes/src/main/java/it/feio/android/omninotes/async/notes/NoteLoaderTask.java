/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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
package it.feio.android.omninotes.async.notes;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnNotesLoadedListener;
import roboguice.util.Ln;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class NoteLoaderTask extends AsyncTask<Object, Void, ArrayList<Note>> {

	private final WeakReference<Fragment> mFragmentReference;
	private final Activity mActivity;
	private OnNotesLoadedListener mOnNotesLoadedListener;


    public NoteLoaderTask(Fragment mFragment,
                          OnNotesLoadedListener mOnNotesLoadedListener) {
        mFragmentReference = new WeakReference<>(mFragment);
        mActivity = mFragment.getActivity();
        this.mOnNotesLoadedListener = mOnNotesLoadedListener;
    }

	@SuppressWarnings("unchecked")
	@Override
	protected ArrayList<Note> doInBackground(Object... params) {
		ArrayList<Note> notes = new ArrayList<>();
		String methodName = params[0].toString();
		Object methodArgs = params[1];
		DbHelper db = DbHelper.getInstance(mActivity);

		// If null argument an empty list will be returned
		if (methodArgs == null) {
			return notes;
		}

		// Checks the argument class with reflection
		@SuppressWarnings("rawtypes")
		Class[] paramClass = new Class[1];
		if (Boolean.class.isAssignableFrom(methodArgs.getClass())) {
			paramClass[0] = Boolean.class;
		} else {
			paramClass[0] = String.class;
		}

		// Retrieves and calls the right method
		Method method;
		try {
			method = db.getClass().getDeclaredMethod(methodName, paramClass);
			notes = (ArrayList<Note>) method.invoke(db,
					paramClass[0].cast(methodArgs));
		} catch (Exception e) {
			Ln.e(e, "Error retrieving notes");
		}

		return notes;
	}

	@Override
	protected void onPostExecute(ArrayList<Note> notes) {
		super.onPostExecute(notes);
		if (isAlive()) {
			mOnNotesLoadedListener.onNotesLoaded(notes);
		}
	}

    private boolean isAlive() {
        return mFragmentReference.get() != null
                && mFragmentReference.get().getActivity() != null
                && !mFragmentReference.get().getActivity().isFinishing()
                && mFragmentReference.get().isAdded();
    }
}

/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.async;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnNotesLoadedListener;
import it.feio.android.omninotes.utils.Constants;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

public class NoteLoaderTask extends AsyncTask<Object, Void, ArrayList<Note>> {

	private final WeakReference<Fragment> mFragmentReference;
	private OnNotesLoadedListener mOnNotesLoadedListener;

	public NoteLoaderTask(Fragment mFragment,
			OnNotesLoadedListener mOnNotesLoadedListener) {
		mFragmentReference = new WeakReference<Fragment>(mFragment);
		this.mOnNotesLoadedListener = mOnNotesLoadedListener;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ArrayList<Note> doInBackground(Object... params) {
		ArrayList<Note> notes = new ArrayList<Note>();
		String methodName = params[0].toString();
		Object methodArgs = params[1];
		DbHelper db = new DbHelper(mFragmentReference.get().getActivity());

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
			Log.e(Constants.TAG, "Error retrieving notes", e);
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
		if (mFragmentReference.get() != null
				&& mFragmentReference.get().getActivity() != null
				&& !mFragmentReference.get().getActivity().isFinishing()
				&& mFragmentReference.get().isAdded()) {
			return true;
		}
		return false;
	}
}

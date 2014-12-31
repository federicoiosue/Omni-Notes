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
package it.feio.android.omninotes.utils;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import roboguice.util.Ln;

public class ImportExportExcel {
	
	private DbHelper db;
	private Context context;
	private boolean silentMode = false;

	public ImportExportExcel(Context context) {
		this.context = context;
	}

	public ImportExportExcel(DbHelper db) {
		this.db = db;
		this.silentMode = true;
	}

	public Boolean exportDataToCSV(String path) {

		Ln.e("excel", "in exportDatabasecsv()");
		Boolean returnCode = false;

		String csvHeader = "";
		String csvValues = "";

		try {

			if (db == null) {
				db = DbHelper.getInstance(context);
				db.getReadableDatabase();
			}

			String fileName = Constants.EXPORT_FILE_NAME + ".csv";
			File outFile = new File(path, fileName);
			FileWriter fileWriter = new FileWriter(outFile, false);
			Ln.e("after FileWriter :file name", outFile.toString());
			BufferedWriter out = new BufferedWriter(fileWriter);

			List<Note> notesList = db.getAllNotes(false);
			Ln.i("Exporting " + notesList.size() + " notes");

			csvHeader += "\"" + "Id" + "\",";
			csvHeader += "\"" + "Creation" + "\",";
			csvHeader += "\"" + "Last Modification" + "\",";
			csvHeader += "\"" + "Title" + "\",";
			csvHeader += "\"" + "Content" + "\",";
			csvHeader += "\"" + "Archived" + "\";\n";

			if (notesList.size() > 0) {
				out.write(csvHeader);
				for (Note note : notesList) {
					csvValues = "\"" + note.get_id() + "\",";
					csvValues += "\"" + note.getCreation() + "\",";
					csvValues += "\"" + note.getLastModification() + "\",";
					csvValues += "\"" + textEncode(note.getTitle()) + "\",";
					csvValues += "\"" + textEncode(note.getContent()) + "\",";
					csvValues += "\"" + note.isArchived() + "\";\n";

					out.write(csvValues);
					Ln.v("Note values are: " + csvValues);
				}

			}
			out.close();
			db.close();
			returnCode = true;
		} catch (Exception e) {
			returnCode = false;
			Ln.e("Error exportin csv: " + e.getMessage());
		}

		db.close();
		return returnCode;
	}

	public boolean importDataFromCSV(String path) {
		boolean returnCode = false;
		boolean flag_is_header = true;

		String fileName = Constants.EXPORT_FILE_NAME + ".csv";
		File file = new File(path, fileName);
		if (!file.exists()) {
			Ln.w("File to import doesn't exists");
			if (!silentMode)
				Toast.makeText(context, context.getString(R.string.file_not_exists), Toast.LENGTH_SHORT).show();
			return false;
		}
		BufferedReader bufRdr = null;
		try {
			bufRdr = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = null;

		try {
			if (db == null) {
				db = DbHelper.getInstance(context);
				db.getWritableDatabase();
			}
			StringBuffer lineBuf = new StringBuffer();
			Note note;
			while ((line = bufRdr.readLine()) != null) {
				
				if (flag_is_header) {
					flag_is_header = false;
					continue;
				}

				lineBuf.append(line).append(System.getProperty("line.separator"));
				if (line.lastIndexOf("\";") != line.length() - 2) {
					continue;
				} 
				line = lineBuf.substring(1, lineBuf.length() - 3).toString();
				lineBuf.setLength(0);
				
				
				// Split substring (cutted out first and last quotes) on column separator string
				String[] insertValues = line.split("\",\"");
				
				// Creation of note
				note = new Note();
				note.set_id(Integer.parseInt(insertValues[0]));
				note.setCreation(Long.parseLong(insertValues[1]));
				note.setLastModification(Long.parseLong(insertValues[2]));
				note.setTitle(textDecode(insertValues[3]));
				note.setContent(textDecode(insertValues[4]));
				note.setArchived(Boolean.parseBoolean(insertValues[5]));
				
				// Database inserting
				db.updateNote(note, false);
				
				returnCode = true;
			}
			db.close();
			bufRdr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnCode;
	}
	
	
	private String textEncode(String text) {
		String textEncoded = null;
		textEncoded = text.replace("\"", "\"\"");
		return textEncoded;
	}
	
	
	private String textDecode(String text) {
		String textDecoded = null;
		textDecoded = text.replace("\"\"", "\"");
		return textDecoded;
	}

}

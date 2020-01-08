/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.widget.Toast;
import exceptions.ImportException;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.GeocodeHelper;
import it.feio.android.omninotes.utils.ReminderHelper;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.omninotes.utils.notifications.NotificationChannels;
import it.feio.android.omninotes.utils.notifications.NotificationsHelper;
import it.feio.android.springpadimporter.Importer;
import it.feio.android.springpadimporter.models.SpringpadAttachment;
import it.feio.android.springpadimporter.models.SpringpadComment;
import it.feio.android.springpadimporter.models.SpringpadElement;
import it.feio.android.springpadimporter.models.SpringpadItem;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;


public class SpringImportHelper {

  public static final String ACTION_DATA_IMPORT_SPRINGPAD = "action_data_import_springpad";
  public static final String EXTRA_SPRINGPAD_BACKUP = "extra_springpad_backup";
  private final Context context;


  private int importedSpringpadNotes, importedSpringpadNotebooks;

  public SpringImportHelper (Context context) {
    this.context = context;
  }


  /**
   * Imports notes and notebooks from Springpad exported archive
   */
  public synchronized void importDataFromSpringpad (Intent intent, NotificationsHelper mNotificationsHelper) {
    String backupPath = intent.getStringExtra(EXTRA_SPRINGPAD_BACKUP);
    Importer importer = new Importer();
    try {
      importer.setZipProgressesListener(percentage -> mNotificationsHelper.setMessage(context
          .getString(R.string.extracted) + " " + percentage + "%").show());
      importer.doImport(backupPath);
      // Updating notification
      updateImportNotification(importer, mNotificationsHelper);
    } catch (ImportException e) {
      new NotificationsHelper(context)
          .createNotification(NotificationChannels.NotificationChannelNames.Backups,
              R.drawable.ic_emoticon_sad_white_24dp,
              context.getString(R.string.import_fail) + ": " + e.getMessage(), null).setLedActive().show();
      return;
    }
    List<SpringpadElement> elements = importer.getSpringpadNotes();

    // If nothing is retrieved it will exit
    if (elements == null || elements.size() == 0) {
      return;
    }

    // These maps are used to associate with post processing notes to categories (notebooks)
    HashMap<String, Category> categoriesWithUuid = new HashMap<>();

    // Adds all the notebooks (categories)
    for (SpringpadElement springpadElement : importer.getNotebooks()) {
      Category cat = new Category();
      cat.setName(springpadElement.getName());
      cat.setColor(String.valueOf(Color.parseColor("#F9EA1B")));
      DbHelper.getInstance().updateCategory(cat);
      categoriesWithUuid.put(springpadElement.getUuid(), cat);

      // Updating notification
      importedSpringpadNotebooks++;
      updateImportNotification(importer, mNotificationsHelper);
    }
    // And creates a default one for notes without notebook
    Category defaulCategory = new Category();
    defaulCategory.setName("Springpad");
    defaulCategory.setColor(String.valueOf(Color.parseColor("#F9EA1B")));
    DbHelper.getInstance().updateCategory(defaulCategory);

    // And then notes are created
    Note note;
    Attachment mAttachment = null;
    Uri uri;
    for (SpringpadElement springpadElement : importer.getNotes()) {
      note = new Note();

      // Title
      note.setTitle(springpadElement.getName());

      // Content dependent from type of Springpad note
      StringBuilder content = new StringBuilder();
      content.append(TextUtils.isEmpty(springpadElement.getText()) ? "" : Html.fromHtml(springpadElement
          .getText()));
      content.append(TextUtils.isEmpty(springpadElement.getDescription()) ? "" : springpadElement
          .getDescription());

      // Some notes could have been exported wrongly
      if (springpadElement.getType() == null) {
        Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
        continue;
      }

      if (springpadElement.getType().equals(SpringpadElement.TYPE_VIDEO)) {
        try {
          content.append(System.getProperty("line.separator")).append(springpadElement.getVideos().get(0));
        } catch (IndexOutOfBoundsException e) {
          content.append(System.getProperty("line.separator")).append(springpadElement.getUrl());
        }
      }
      if (springpadElement.getType().equals(SpringpadElement.TYPE_TVSHOW)) {
        content.append(System.getProperty("line.separator")).append(
            TextUtils.join(", ", springpadElement.getCast()));
      }
      if (springpadElement.getType().equals(SpringpadElement.TYPE_BOOK)) {
        content.append(System.getProperty("line.separator")).append("Author: ")
               .append(springpadElement.getAuthor()).append(System.getProperty("line.separator"))
               .append("Publication date: ").append(springpadElement.getPublicationDate());
      }
      if (springpadElement.getType().equals(SpringpadElement.TYPE_RECIPE)) {
        content.append(System.getProperty("line.separator")).append("Ingredients: ")
               .append(springpadElement.getIngredients()).append(System.getProperty("line.separator"))
               .append("Directions: ").append(springpadElement.getDirections());
      }
      if (springpadElement.getType().equals(SpringpadElement.TYPE_BOOKMARK)) {
        content.append(System.getProperty("line.separator")).append(springpadElement.getUrl());
      }
      if (springpadElement.getType().equals(SpringpadElement.TYPE_BUSINESS)
          && springpadElement.getPhoneNumbers() != null) {
        content.append(System.getProperty("line.separator")).append("Phone number: ")
               .append(springpadElement.getPhoneNumbers().getPhone());
      }
      if (springpadElement.getType().equals(SpringpadElement.TYPE_PRODUCT)) {
        content.append(System.getProperty("line.separator")).append("Category: ")
               .append(springpadElement.getCategory()).append(System.getProperty("line.separator"))
               .append("Manufacturer: ").append(springpadElement.getManufacturer())
               .append(System.getProperty("line.separator")).append("Price: ")
               .append(springpadElement.getPrice());
      }
      if (springpadElement.getType().equals(SpringpadElement.TYPE_WINE)) {
        content.append(System.getProperty("line.separator")).append("Wine type: ")
               .append(springpadElement.getWine_type()).append(System.getProperty("line.separator"))
               .append("Varietal: ").append(springpadElement.getVarietal())
               .append(System.getProperty("line.separator")).append("Price: ")
               .append(springpadElement.getPrice());
      }
      if (springpadElement.getType().equals(SpringpadElement.TYPE_ALBUM)) {
        content.append(System.getProperty("line.separator")).append("Artist: ")
               .append(springpadElement.getArtist());
      }
      for (SpringpadComment springpadComment : springpadElement.getComments()) {
        content.append(System.getProperty("line.separator")).append(springpadComment.getCommenter())
               .append(" commented at 0").append(springpadComment.getDate()).append(": ")
               .append(springpadElement.getArtist());
      }

      note.setContent(content.toString());

      // Checklists
      if (springpadElement.getType().equals(SpringpadElement.TYPE_CHECKLIST)) {
        StringBuilder sb = new StringBuilder();
        String checkmark;
        for (SpringpadItem mSpringpadItem : springpadElement.getItems()) {
          checkmark = mSpringpadItem.getComplete() ? it.feio.android.checklistview.interfaces.Constants
              .CHECKED_SYM
              : it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM;
          sb.append(checkmark).append(mSpringpadItem.getName()).append(System.getProperty("line.separator"));
        }
        note.setContent(sb.toString());
        note.setChecklist(true);
      }

      // Tags
      String tags = springpadElement.getTags().size() > 0 ? "#"
          + TextUtils.join(" #", springpadElement.getTags()) : "";
      if (note.isChecklist()) {
        note.setTitle(note.getTitle() + tags);
      } else {
        note.setContent(note.getContent() + System.getProperty("line.separator") + tags);
      }

      // Address
      String address = springpadElement.getAddresses() != null ? springpadElement.getAddresses().getAddress()
          : "";
      if (!TextUtils.isEmpty(address)) {
        try {
          double[] coords = GeocodeHelper.getCoordinatesFromAddress(context, address);
          note.setLatitude(coords[0]);
          note.setLongitude(coords[1]);
        } catch (IOException e) {
          LogDelegate.e("An error occurred trying to resolve address to coords during Springpad " +
              "import");
        }
        note.setAddress(address);
      }

      // Reminder
      if (springpadElement.getDate() != null) {
        note.setAlarm(springpadElement.getDate().getTime());
      }

      // Creation, modification, category
      note.setCreation(springpadElement.getCreated().getTime());
      note.setLastModification(springpadElement.getModified().getTime());

      // Image
      String image = springpadElement.getImage();
      if (!TextUtils.isEmpty(image)) {
        try {
          File file = StorageHelper.createNewAttachmentFileFromHttp(context, image);
          uri = Uri.fromFile(file);
          String mimeType = StorageHelper.getMimeType(uri.getPath());
          mAttachment = new Attachment(uri, mimeType);
        } catch (MalformedURLException e) {
          uri = Uri.parse(importer.getWorkingPath() + image);
          mAttachment = StorageHelper.createAttachmentFromUri(context, uri, true);
        } catch (IOException e) {
          LogDelegate.e("Error retrieving Springpad online image");
        }
        if (mAttachment != null) {
          note.addAttachment(mAttachment);
        }
        mAttachment = null;
      }

      // Other attachments
      for (SpringpadAttachment springpadAttachment : springpadElement.getAttachments()) {
        // The attachment could be the image itself so it's jumped
        if (image != null && image.equals(springpadAttachment.getUrl())) {
          continue;
        }

        if (TextUtils.isEmpty(springpadAttachment.getUrl())) {
          continue;
        }

        // Tries first with online images
        try {
          File file = StorageHelper.createNewAttachmentFileFromHttp(context, springpadAttachment.getUrl());
          uri = Uri.fromFile(file);
          String mimeType = StorageHelper.getMimeType(uri.getPath());
          mAttachment = new Attachment(uri, mimeType);
        } catch (MalformedURLException e) {
          uri = Uri.parse(importer.getWorkingPath() + springpadAttachment.getUrl());
          mAttachment = StorageHelper.createAttachmentFromUri(context, uri, true);
        } catch (IOException e) {
          LogDelegate.e("Error retrieving Springpad online image");
        }
        if (mAttachment != null) {
          note.addAttachment(mAttachment);
        }
        mAttachment = null;
      }

      // If the note has a category is added to the map to be post-processed
      if (springpadElement.getNotebooks().size() > 0) {
        note.setCategory(categoriesWithUuid.get(springpadElement.getNotebooks().get(0)));
      } else {
        note.setCategory(defaulCategory);
      }

      // The note is saved
      DbHelper.getInstance().updateNote(note, false);
      ReminderHelper.addReminder(context, note);

      // Updating notification
      importedSpringpadNotes++;
      updateImportNotification(importer, mNotificationsHelper);
    }

    // Delete temp data
    try {
      importer.clean();
    } catch (IOException e) {
      LogDelegate.w("Springpad import temp files not deleted");
    }
  }


  private void updateImportNotification (Importer importer, NotificationsHelper mNotificationsHelper) {
    mNotificationsHelper.setMessage(
        importer.getNotebooksCount() + " " + context.getString(R.string.categories) + " ("
            + importedSpringpadNotebooks + " " + context.getString(R.string.imported) + "), "
            + +importer.getNotesCount() + " " + context.getString(R.string.notes) + " ("
            + importedSpringpadNotes + " " + context.getString(R.string.imported) + ")").show();
  }
}

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

package it.feio.android.omninotes.utils;

import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SHORTCUT;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_KEY;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PRETTIFIED_DATES;
import static java.util.Collections.singletonList;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.models.Note;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ShortcutHelper {

  /**
   * Adding shortcut on Home screen
   */
  public static void addShortcut(Context context, Note note) {
    String shortcutTitle =
        note.getTitle().length() > 0 ? note.getTitle() : DateHelper.getFormattedDate(note
                .getCreation(),
            OmniNotes.getSharedPreferences().getBoolean(PREF_PRETTIFIED_DATES, true));

    if (Build.VERSION.SDK_INT < 26) {
      createShortcutPreOreo(context, note, shortcutTitle);
    } else {
      createShortcutPostOreo(context, note, shortcutTitle);
    }
  }

  @TargetApi(VERSION_CODES.O)
  private static void createShortcutPostOreo(Context context, Note note, String shortcutTitle) {
    ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

    if (shortcutManager.isRequestPinShortcutSupported()) {
      Uri uri = new Uri.Builder().scheme("app")
          .authority("omninotes")
          .appendQueryParameter("id", String.valueOf(note.get_id()))
          .build();
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);

      ShortcutInfo pinShortcutInfo = new ShortcutInfo
          .Builder(context, String.valueOf(note.get_id()))
          .setIcon(createShortcutIcon(context, note))
          .setIntent(intent)
          .setShortLabel(shortcutTitle)
          .build();
      Intent pinnedShortcutCallbackIntent = shortcutManager
          .createShortcutResultIntent(pinShortcutInfo);
      //Get notified when a shortcut is pinned successfully//
      PendingIntent successCallback = PendingIntent
          .getBroadcast(context, 0, pinnedShortcutCallbackIntent, 0);
      shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.getIntentSender()
      );
    }
  }

  @SneakyThrows
  @TargetApi(VERSION_CODES.O)
  private static Icon createShortcutIcon(Context context, Note note) {
    if (note.getAttachmentsList().isEmpty()) {
      return Icon.createWithResource(context, R.drawable.ic_shortcut);
    } else {
      return Icon.createWithBitmap(
          BitmapHelper.getBitmapFromAttachment(context, note.getAttachmentsList().get(0), 64, 64));
    }
  }

  private static void createShortcutPreOreo(Context context, Note note, String shortcutTitle) {
    Intent shortcutIntent = new Intent(context, MainActivity.class);
    shortcutIntent.putExtra(INTENT_KEY, note.get_id());
    shortcutIntent.setAction(ACTION_SHORTCUT);

    Intent addIntent = new Intent();
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutTitle);
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
        Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_shortcut));
    addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

    context.sendBroadcast(addIntent);
  }

  /**
   * Removes note shortcut from home launcher
   */
  public static void removeShortcut(Context context, Note note) {
    if (Build.VERSION.SDK_INT < 26) {
      removeShortcutPreOreo(context, note);
    } else {
      removeShortcutPostOreo(context, note);
    }
  }

  @TargetApi(VERSION_CODES.O)
  private static void removeShortcutPostOreo(Context context, Note note) {
    ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
    shortcutManager.getPinnedShortcuts().stream()
        .filter(ps -> ps.getId().equals(String.valueOf(note.get_id())))
        .forEach(ps -> shortcutManager.disableShortcuts(singletonList(ps.getId()),
            context.getString(R.string.shortcut_disabled)));
  }

  private static void removeShortcutPreOreo(Context context, Note note) {
    Intent shortcutIntent = new Intent(context, MainActivity.class);
    shortcutIntent.putExtra(INTENT_KEY, note.get_id());
    shortcutIntent.setAction(ACTION_SHORTCUT);

    Intent addIntent = new Intent();
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
    String shortcutTitle =
        note.getTitle().length() > 0 ? note.getTitle() : DateHelper.getFormattedDate(note
                .getCreation(),
            OmniNotes.getSharedPreferences().getBoolean(PREF_PRETTIFIED_DATES, true));

    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutTitle);

    addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
    context.sendBroadcast(addIntent);
  }

}

/*
 * Copyright (C) 2013-2024 Federico Iosue (federico@iosue.it)
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

public final class ConstantsBase {

  public static final String DATABASE_NAME = "omni-notes";
  public static final String APP_STORAGE_DIRECTORY_SB_SYNC = "db_sync";
  public static final String COMMUNITY_LINK = "https://t.me/omninotes";

  // Notes swipe
  public static final int SWIPE_MARGIN = 30;
  public static final int SWIPE_OFFSET = 100;

  // Floating action button
  public static final int FAB_ANIMATION_TIME = 250;

  // Notes content masking
  public static final String MASK_CHAR = "*";

  public static final int THUMBNAIL_SIZE = 300;

  public static final String DATE_FORMAT_SORTABLE = "yyyyMMdd_HHmmss_SSS";
  public static final String DATE_FORMAT_SORTABLE_OLD = "yyyyMMddHHmmss";
  public static final String DATE_FORMAT_EXPORT = "yyyy.MM.dd-HH.mm";

  public static final String INTENT_KEY = "note_id";
  public static final String INTENT_NOTE = "note";
  public static final String GALLERY_TITLE = "gallery_title";
  public static final String GALLERY_CLICKED_IMAGE = "gallery_clicked_image";
  public static final String GALLERY_IMAGES = "gallery_images";
  public static final String INTENT_CATEGORY = "category";
  public static final String INTENT_GOOGLE_NOW = "com.google.android.gm.action.AUTO_SEND";
  public static final String INTENT_WIDGET = "widget_id";
  public static final String INTENT_UPDATE_DASHCLOCK = "update_dashclock";

  // Custom intent actions
  public static final String ACTION_START_APP = "action_start_app";
  public static final String ACTION_RESTART_APP = "action_restart_app";
  public static final String ACTION_DISMISS = "action_dismiss";
  public static final String ACTION_SNOOZE = "action_snooze";
  public static final String ACTION_POSTPONE = "action_postpone";
  public static final String ACTION_PINNED = "action_pinned";
  public static final String ACTION_SHORTCUT = "action_shortcut";
  public static final String ACTION_WIDGET = "action_widget";
  public static final String ACTION_WIDGET_TAKE_PHOTO = "action_widget_take_photo";
  public static final String ACTION_WIDGET_SHOW_LIST = "action_widget_show_list";
  public static final String ACTION_SHORTCUT_WIDGET = "action_shortcut_widget";
  public static final String ACTION_NOTIFICATION_CLICK = "action_notification_click";
  public static final String ACTION_MERGE = "action_merge";
  public static final String ACTION_FAB_TAKE_PHOTO = "action_fab_take_photo";
  /**
   * Used to quickly add a note, save, and perform backPress (eg. Tasker+Pushbullet) *
   */
  public static final String ACTION_SEND_AND_EXIT = "action_send_and_exit";
  public static final String ACTION_SEARCH_UNCOMPLETE_CHECKLISTS = "action_search_uncomplete_checklists";

  public static final String PREF_LANG = "settings_language";
  public static final String PREF_LAST_UPDATE_CHECK = "last_update_check";
  public static final String PREF_NAVIGATION = "navigation";
  public static final String PREF_SORTING_COLUMN = "sorting_column";
  public static final String PREF_PASSWORD = "password";
  public static final String PREF_PASSWORD_QUESTION = "password_question";
  public static final String PREF_PASSWORD_ANSWER = "password_answer";
  public static final String PREF_KEEP_CHECKED = "keep_checked";
  public static final String PREF_KEEP_CHECKMARKS = "show_checkmarks";
  public static final String PREF_EXPANDED_VIEW = "expanded_view";
  public static final String PREF_COLORS_APP_DEFAULT = "strip";
  public static final String PREF_WIDGET_PREFIX = "widget_";
  public static final String PREF_SHOW_UNCATEGORIZED = "settings_show_uncategorized";
  public static final String PREF_AUTO_LOCATION = "settings_auto_location";
  public static final String PREF_FILTER_PAST_REMINDERS = "settings_filter_past_reminders";
  public static final String PREF_FILTER_ARCHIVED_IN_CATEGORIES = "settings_filter_archived_in_categories";
  public static final String PREF_DYNAMIC_MENU = "settings_dynamic_menu";
  public static final String PREF_CURRENT_APP_VERSION = "settings_current_app_version";
  public static final String PREF_FAB_EXPANSION_BEHAVIOR = "settings_fab_expansion_behavior";
  public static final String PREF_ATTACHMENTS_ON_BOTTOM = "settings_attachments_on_bottom";
  public static final String PREF_SNOOZE_DEFAULT = "10";
  public static final String PREF_TOUR_COMPLETE = "pref_tour_complete";
  public static final String PREF_ENABLE_SWIPE = "settings_enable_swipe";
  public static final String PREF_SEND_ANALYTICS = "settings_send_analytics";
  public static final String PREF_PRETTIFIED_DATES = "settings_prettified_dates";
  public static final String PREF_ENABLE_AUTOBACKUP = "settings_enable_autobackup";
  public static final String PREF_ENABLE_FILE_LOGGING = "settings_enable_file_logging";
  public static final String PREF_BACKUP_FOLDER_URI = "backup_folder";

  public static final String MIME_TYPE_IMAGE = "image/jpeg";
  public static final String MIME_TYPE_AUDIO = "audio/amr";
  public static final String MIME_TYPE_VIDEO = "video/mp4";
  public static final String MIME_TYPE_SKETCH = "image/png";
  public static final String MIME_TYPE_FILES = "file/*";

  public static final String MIME_TYPE_IMAGE_EXT = ".jpeg";
  public static final String MIME_TYPE_AUDIO_EXT = ".amr";
  public static final String MIME_TYPE_VIDEO_EXT = ".mp4";
  public static final String MIME_TYPE_SKETCH_EXT = ".png";
  public static final String MIME_TYPE_CONTACT_EXT = ".vcf";

  public static final String TIMESTAMP_UNIX_EPOCH = "0";
  public static final String TIMESTAMP_UNIX_EPOCH_FAR = "18464193800000";

  public static final int MENU_SORT_GROUP_ID = 11998811;

  public static final String MERGED_NOTES_SEPARATOR = "----------------------";
  public static final String PROPERTIES_PARAMS_SEPARATOR = ",";

  public static final String AUTO_BACKUP_DIR = "_autobackup";

  private ConstantsBase() {
    // Private constructor to prevent instantiation of this utility class
    throw new AssertionError("Cannot be instantiated");
  }
}
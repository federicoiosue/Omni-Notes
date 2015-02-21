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

public interface Constants {

    final String TAG = "Omni Notes";
    final String DATABASE_NAME = "omni-notes";
    final String PACKAGE = "it.feio.android.omninotes";
    final String PREFS_NAME = PACKAGE + "_preferences";
    final String APP_STORAGE_DIRECTORY = TAG;
    final String APP_STORAGE_DIRECTORY_ATTACHMENTS = "attachments";
    final String APP_STORAGE_DIRECTORY_SB_SYNC = "db_sync";

    final String DEV_EMAIL = "federico.iosue@gmail.com";

    // Used for updates retrieval
    final String PS_METADATA_FETCHER_URL = "http://www.iosue.it/federico/apps/PSMetadataFetcher/get_app_data.php?url=";
    final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";
    static long UPDATE_MIN_FREQUENCY = 24 * 60 * 60 * 1000; // 1 day
    final String DRIVE_FOLDER_LAST_BUILD = "http://goo.gl/R10Tr5";

    // Notes swipe
    final int SWIPE_MARGIN = 30;
    final int SWIPE_OFFSET = 100;

    // Floating action button
    final int FAB_ANIMATION_TIME = 200;

    // Notes content masking
    final String MASK_CHAR = "*";

    final int THUMBNAIL_SIZE = 300;

    final String DATE_FORMAT_SHORT_DATE = "d MMM yyyy";
    final String DATE_FORMAT_SORTABLE = "yyyyMMdd_HHmmss_SSS";
    final String DATE_FORMAT_SORTABLE_OLD = "yyyyMMddHHmmss";
    final String DATE_FORMAT_EXPORT = "yyyy.MM.dd-HH.mm";

    final boolean LOAD_NOTES_SYNC = true;

    final String INTENT_KEY = "note_id";
    final String INTENT_NOTE = "note";
    final String INTENT_MANAGING_SHARE = "managing_share"; // Used when handling received data
    final String INTENT_IMAGE = "image";
    final String GALLERY_TITLE = "gallery_title";
    final String GALLERY_CLICKED_IMAGE = "gallery_clicked_image";
    final String GALLERY_IMAGES = "gallery_images";
    final int INTENT_ALARM_CODE = 12345;
    final String INTENT_TAG = "tag";
    final String INTENT_DETAIL_RESULT_CODE = "detail_result_code";
    final String INTENT_DETAIL_RESULT_MESSAGE = "detail_result_message";
    final String INTENT_GOOGLE_NOW = "com.google.android.gm.action.AUTO_SEND";
    final String INTENT_WIDGET = "widget_id";
    final String INTENT_UPDATE_DASHCLOCK = "update_dashclock";

    // Custom intent actions
    final String ACTION_START_APP = "action_start_app";
    final String ACTION_RESTART_APP = "action_restart_app";
    final String ACTION_DISMISS = "action_dismiss";
    final String ACTION_SNOOZE = "action_snooze";
    final String ACTION_POSTPONE = "action_postpone";
    final String ACTION_SHORTCUT = "action_shortcut";
    final String ACTION_WIDGET = "action_widget";
    final String ACTION_TAKE_PHOTO = "action_widget_take_photo";
    final String ACTION_WIDGET_SHOW_LIST = "action_widget_show_list";
    final String ACTION_NOTIFICATION_CLICK = "action_notification_click";
    final String ACTION_MERGE = "action_merge";
    /**
     * Used to quickly add a note, save, and perform backPress (eg. Tasker+Pushbullet) *
     */
    final String ACTION_SEND_AND_EXIT = "action_send_and_exit";

    final String MESSAGE = "message";

    final String PREF_FIRST_RUN = "first_run";
    final String PREF_LAST_UPDATE_CHECK = "last_update_check";
    final String PREF_NAVIGATION = "navigation";
    final String PREF_SORTING_COLUMN = "sorting_column";
    final String PREF_SORTING_ORDER = "sorting_direction";
    final String PREF_PASSWORD = "password";
    final String PREF_PASSWORD_QUESTION = "password_question";
    final String PREF_PASSWORD_ANSWER = "password_answer";
    final String PREF_RATE_DISMISSED = "rate_dismissed";
    final String PREF_LAUNCH_COUNT = "launch_count";
    final String PREF_FIRST_LAUNCH = "first_launch";
    final String PREF_KEEP_CHECKED = "keep_checked";
    final String PREF_KEEP_CHECKMARKS = "show_checkmarks";
    final String PREF_TOUR_PREFIX = "tour_";
    final String PREF_EXPANDED_VIEW = "expanded_view";
    final String PREF_COLORS_APP_DEFAULT = "strip";
    final String PREF_WIDGET_PREFIX = "widget_";
    final String PREF_SHOW_UNCATEGORIZED = "settings_show_uncategorized";
    final String PREF_AUTO_LOCATION = "settings_auto_location";
    final String PREF_FILTER_PAST_REMINDERS = "settings_filter_past_reminders";
    final String PREF_DYNAMIC_MENU = "settings_dynamic_menu";
    final String PREF_CURRENT_APP_VERSION = "settings_current_app_version";

    final String EXPORT_FILE_NAME = TAG;

    final String MIME_TYPE_IMAGE = "image/jpeg";
    final String MIME_TYPE_AUDIO = "audio/3gpp";
    final String MIME_TYPE_VIDEO = "video/mp4";
    final String MIME_TYPE_SKETCH = "image/png";
    final String MIME_TYPE_FILES = "file/*";

    final String MIME_TYPE_IMAGE_EXT = ".jpeg";
    final String MIME_TYPE_AUDIO_EXT = ".3gpp";
    final String MIME_TYPE_VIDEO_EXT = ".mp4";
    final String MIME_TYPE_SKETCH_EXT = ".png";

    final int ERROR_NOTE_NOT_DELETED = -1;
    final int ERROR_ATTACHMENTS_NOT_DELETED = -2;

    final String SECURITY_ALGORITHM = "MD5";

    final String TIMESTAMP_UNIX_EPOCH = "0";
    final String TIMESTAMP_UNIX_EPOCH_FAR = "18464193800000";

    final int MENU_SORT_GROUP_ID = 11998811;
}

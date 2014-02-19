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
package it.feio.android.omninotes.utils;

public interface Constants {

	final String TAG = "Omni Notes";
	final String DATABASE_NAME = "omni-notes";
	final String PREFS_FILE_NAME = "omni-notes.prefs";
	final String APP_STORAGE_DIRECTORY = TAG;
	final String APP_STORAGE_DIRECTORY_ATTACHMENTS = "attachments";
	
	// Splash screen timer
	static int SPLASH_TIME_OUT = 1400;
	static long SPLASH_MIN_OFFSET = 60*60*1000; // 1 hour

	final String DEV_EMAIL = "federico.iosue@gmail.com";
	
	// Used for updates retrieval
	final String PS_METADATA_FETCHER_URL = "http://www.iosue.it/federico/apps/PSMetadataFetcher/get_app_data.php?url=";
	final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";
	static long UPDATE_MIN_FREQUENCY = 24*60*60*1000; // 1 hour
	
	// Used for ACRA
	final String ACRA_MAILER_URL = "http://www.iosue.it/federico/apps/acramailer/acra.php?email=";
	
	final int THUMBNAIL_SIZE = 300;  

	final String DATE_SEPARATOR = "/";
	final String TIME_SEPARATOR = ":";
	
	final String DATE_FORMAT_EU = "dd/MM/yyyy HH:mm";
	final String DATE_FORMAT_ISO8601  = "YYYY-MM-DD HH:mm:SS.SSS";
	final String DATE_FORMAT_SHORT  = "d MMM HH:mm";
//	final String DATE_FORMAT_SHORT_12  = "d MMM hh:mma";
	final String DATE_FORMAT_SHORT_DATE  = "d MMM yyyy";
	final String DATE_FORMAT_SHORT_TIME  = "HH" + TIME_SEPARATOR + "mm";
//	final String DATE_FORMAT_SHORT_TIME_12  = "hh" + TIME_SEPARATOR + "mma";
	final String DATE_FORMAT_SORTABLE  = "yyyyMMddHHmmssS";
	final String DATE_FORMAT_SORTABLE_OLD  = "yyyyMMddHHmmss";
	final String DATE_FORMAT_EXPORT  = "yyyy.MM.dd-HH.mm";
	
    final String INTENT_KEY = "note_id";
    final String INTENT_NOTE = "note";
	final String INTENT_MANAGING_SHARE = "managing_share"; // Used when handling received data 
	final String INTENT_IMAGE = "image";
	final int INTENT_ALARM_CODE = 12345;
	final String INTENT_BACKUP_NAME = "backup_name";
	final String INTENT_BACKUP_INCLUDE_SETTINGS = "backup_include_settings";
    final String INTENT_TAG = "tag";
    final String INTENT_DETAIL_RESULT_CODE = "detail_result_code";
    final String INTENT_DETAIL_RESULT_MESSAGE = "detail_result_message";
    
    // Custom intent actions
    final String ACTION_DATA_EXPORT = "action_data_export";
    final String ACTION_DATA_IMPORT = "action_data_import";
	final String ACTION_DATA_DELETE = "action_data_delete";
	final String ACTION_START_APP = "action_start_app";
   	final String ACTION_DISMISS = "action_dismiss"; 
	final String ACTION_SNOOZE = "action_snooze";
	final String ACTION_SHORTCUT = "action_shortcut";
	
	final String MESSAGE = "message";

    final String PREF_FIRST_RUN = "first_run";
    final String PREF_LAST_UPDATE_CHECK = "last_update_check";
    final String PREF_NAVIGATION = "navigation";
    final String PREF_SORTING_COLUMN = "sorting_column";
    final String PREF_SORTING_ORDER = "sorting_direction";
    final String PREF_PASSWORD = "password";
    final String PREF_RATE_DISMISSED = "rate_dismissed";
    final String PREF_LAUNCH_COUNT = "launch_count";
    final String PREF_FIRST_LAUNCH = "first_launch";
    final String PREF_KEEP_CHECKED = "keep_checked";
    final String PREF_KEEP_CHECKMARKS = "show_checkmarks";
	final String PREF_INSTRUCTIONS_PREFIX = "tour_";
	final String PREF_EXPANDED_VIEW = "expanded_view";
    
    final String EXPORT_FILE_NAME = TAG;
    
    final String MIME_TYPE_IMAGE = "image/jpeg";
    final String MIME_TYPE_AUDIO = "audio/3gp";
    final String MIME_TYPE_VIDEO = "video/mp4";
    
    final String MIME_TYPE_IMAGE_EXT = ".jpeg";
    final String MIME_TYPE_AUDIO_EXT = ".3gp";
    final String MIME_TYPE_VIDEO_EXT = ".mp4";
    
    final int ERROR_NOTE_NOT_DELETED = -1;
    final int ERROR_ATTACHMENTS_NOT_DELETED = -2;
	
	final String SECURITY_ALGORITHM = "MD5";
	
	final String TIMESTAMP_NEVER = "13910051406040";	// I really don't believe we'll reach 10/17/2410 1:10:06 AM
}

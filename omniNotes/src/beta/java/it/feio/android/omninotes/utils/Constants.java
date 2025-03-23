package it.feio.android.omninotes.utils;

public final class Constants extends ConstantsBase {

  public static final String TAG = "Omni Notes Beta";
  public static final String EXTERNAL_STORAGE_FOLDER = "Omni Notes";
  public static final String PACKAGE = "it.feio.android.omninotes";

  public static final String CHANNEL_BACKUPS_ID = PACKAGE + ".backups";
  public static final String CHANNEL_REMINDERS_ID = PACKAGE + ".reminders";
  public static final String CHANNEL_PINNED_ID = PACKAGE + ".pinned";

  private Constants() {
    // Private constructor to prevent instantiation of this utility class
    throw new AssertionError("Cannot be instantiated");
  }
}
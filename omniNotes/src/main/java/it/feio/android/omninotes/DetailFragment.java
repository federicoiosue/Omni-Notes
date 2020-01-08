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
package it.feio.android.omninotes;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static androidx.core.view.ViewCompat.animate;
import static it.feio.android.omninotes.BaseActivity.TRANSITION_HORIZONTAL;
import static it.feio.android.omninotes.BaseActivity.TRANSITION_VERTICAL;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_FAB_TAKE_PHOTO;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_MERGE;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_NOTIFICATION_CLICK;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SHORTCUT;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SHORTCUT_WIDGET;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_WIDGET;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_WIDGET_SHOW_LIST;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_WIDGET_TAKE_PHOTO;
import static it.feio.android.omninotes.utils.ConstantsBase.GALLERY_CLICKED_IMAGE;
import static it.feio.android.omninotes.utils.ConstantsBase.GALLERY_IMAGES;
import static it.feio.android.omninotes.utils.ConstantsBase.GALLERY_TITLE;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_GOOGLE_NOW;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_KEY;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_WIDGET;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_AUDIO;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_AUDIO_EXT;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_FILES;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_IMAGE;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_IMAGE_EXT;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_SKETCH;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_SKETCH_EXT;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_VIDEO;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_VIDEO_EXT;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_ATTACHMENTS_ON_BOTTOM;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_AUTO_LOCATION;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_COLORS_APP_DEFAULT;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_KEEP_CHECKED;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_KEEP_CHECKMARKS;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PASSWORD;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PRETTIFIED_DATES;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_WIDGET_PREFIX;
import static it.feio.android.omninotes.utils.ConstantsBase.SWIPE_MARGIN;
import static it.feio.android.omninotes.utils.ConstantsBase.SWIPE_OFFSET;
import static it.feio.android.omninotes.utils.ConstantsBase.THUMBNAIL_SIZE;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.neopixl.pixlui.components.edittext.EditText;
import com.neopixl.pixlui.components.textview.TextView;
import com.pushbullet.android.extension.MessagingExtension;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.checklistview.exceptions.ViewNotSupportedException;
import it.feio.android.checklistview.interfaces.CheckListChangedListener;
import it.feio.android.checklistview.models.CheckListView;
import it.feio.android.checklistview.models.CheckListViewItem;
import it.feio.android.checklistview.models.ChecklistManager;
import it.feio.android.omninotes.async.AttachmentTask;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.async.bus.PushbulletReplyEvent;
import it.feio.android.omninotes.async.bus.SwitchFragmentEvent;
import it.feio.android.omninotes.async.notes.NoteProcessorDelete;
import it.feio.android.omninotes.async.notes.SaveNoteTask;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.AttachmentsHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.PermissionsHelper;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.helpers.date.RecurrenceHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.models.adapters.AttachmentAdapter;
import it.feio.android.omninotes.models.adapters.CategoryRecyclerViewAdapter;
import it.feio.android.omninotes.models.adapters.PlacesAutoCompleteAdapter;
import it.feio.android.omninotes.models.listeners.RecyclerViewItemClickSupport;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;
import it.feio.android.omninotes.models.listeners.OnNoteSaved;
import it.feio.android.omninotes.models.listeners.OnReminderPickedListener;
import it.feio.android.omninotes.models.views.ExpandableHeightGridView;
import it.feio.android.omninotes.utils.AlphaManager;
import it.feio.android.omninotes.utils.ConnectionManager;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Display;
import it.feio.android.omninotes.utils.FileHelper;
import it.feio.android.omninotes.utils.FileProviderHelper;
import it.feio.android.omninotes.utils.GeocodeHelper;
import it.feio.android.omninotes.utils.IntentChecker;
import it.feio.android.omninotes.utils.KeyboardUtils;
import it.feio.android.omninotes.utils.PasswordHelper;
import it.feio.android.omninotes.utils.ReminderHelper;
import it.feio.android.omninotes.utils.ShortcutHelper;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.omninotes.utils.TagsHelper;
import it.feio.android.omninotes.utils.TextHelper;
import it.feio.android.omninotes.utils.date.DateUtils;
import it.feio.android.omninotes.utils.date.ReminderPickers;
import it.feio.android.pixlui.links.TextLinkClickListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import rx.Observable;


public class DetailFragment extends BaseFragment implements OnReminderPickedListener, OnTouchListener,
    OnAttachingFileListener, TextWatcher, CheckListChangedListener, OnNoteSaved,
    OnGeoUtilResultListener {

  private static final int TAKE_PHOTO = 1;
  private static final int TAKE_VIDEO = 2;
  private static final int SET_PASSWORD = 3;
  private static final int SKETCH = 4;
  private static final int CATEGORY = 5;
  private static final int DETAIL = 6;
  private static final int FILES = 7;
  boolean goBack = false;
  @BindView(R.id.detail_root)
  ViewGroup root;
  @BindView(R.id.detail_title)
  EditText title;
  @BindView(R.id.detail_content)
  EditText content;
  @BindView(R.id.detail_attachments_above)
  ViewStub attachmentsAbove;
  @BindView(R.id.detail_attachments_below)
  ViewStub attachmentsBelow;
  @Nullable
  @BindView(R.id.gridview)
  ExpandableHeightGridView mGridView;
  @BindView(R.id.location)
  TextView locationTextView;
  @BindView(R.id.detail_timestamps)
  View timestampsView;
  @BindView(R.id.reminder_layout)
  LinearLayout reminderLayout;
  @BindView(R.id.reminder_icon)
  ImageView reminderIcon;
  @BindView(R.id.datetime)
  TextView datetime;
  @BindView(R.id.detail_tile_card)
  View titleCardView;
  @BindView(R.id.content_wrapper)
  ScrollView scrollView;
  @BindView(R.id.creation)
  TextView creationTextView;
  @BindView(R.id.last_modification)
  TextView lastModificationTextView;
  @BindView(R.id.title_wrapper)
  View titleWrapperView;
  @BindView(R.id.tag_marker)
  View tagMarkerView;
  @BindView(R.id.detail_wrapper)
  ViewManager detailWrapperView;
  @BindView(R.id.snackbar_placeholder)
  View snackBarPlaceholder;
  private View toggleChecklistView;
  private Uri attachmentUri;
  private AttachmentAdapter mAttachmentAdapter;
  private MaterialDialog attachmentDialog;
  private Note note;
  private Note noteTmp;
  private Note noteOriginal;
  // Audio recording
  private String recordName;
  private MediaRecorder mRecorder = null;
  private MediaPlayer mPlayer = null;
  private boolean isRecording = false;
  private View isPlayingView = null;
  private Bitmap recordingBitmap;
  private ChecklistManager mChecklistManager;
  // Values to print result
  private String exitMessage;
  private Style exitCroutonStyle = ONStyle.CONFIRM;
  // Flag to check if after editing it will return to ListActivity or not
  // and in the last case a Toast will be shown instead than Crouton
  private boolean afterSavedReturnsToList = true;
  private boolean showKeyboard = false;
  private boolean swiping;
  private int startSwipeX;
  private SharedPreferences prefs;
  private boolean orientationChanged;
  private long audioRecordingTimeStart;
  private long audioRecordingTime;
  private DetailFragment mFragment;
  private Attachment sketchEdited;
  private int contentLineCounter = 1;
  private int contentCursorPosition;
  private ArrayList<String> mergedNotesIds;
  private MainActivity mainActivity;
  TextLinkClickListener textLinkClickListener = new TextLinkClickListener() {
    @Override
    public void onTextLinkClick (View view, final String clickedString, final String url) {
      new MaterialDialog.Builder(mainActivity)
          .content(clickedString)
          .negativeColorRes(R.color.colorPrimary)
          .positiveText(R.string.open)
          .negativeText(R.string.copy)
          .onPositive((dialog, which) -> {
            boolean error = false;
            Intent intent = null;
            try {
              intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
              intent.addCategory(Intent.CATEGORY_BROWSABLE);
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } catch (NullPointerException e) {
              error = true;
            }

            if (intent == null
                || error
                || !IntentChecker
                .isAvailable(
                    mainActivity,
                    intent,
                    new String[]{PackageManager.FEATURE_CAMERA})) {
              mainActivity.showMessage(R.string.no_application_can_perform_this_action,
                  ONStyle.ALERT);

            } else {
              startActivity(intent);
            }
          })
          .onNegative((dialog, which) -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                mainActivity
                    .getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("text label",
                clickedString);
            clipboard.setPrimaryClip(clip);
          }).build().show();
      View clickedView = noteTmp.isChecklist() ? toggleChecklistView : content;
      clickedView.clearFocus();
      KeyboardUtils.hideKeyboard(clickedView);
      new Handler().post(() -> {
        View clickedView1 = noteTmp.isChecklist() ? toggleChecklistView : content;
        KeyboardUtils.hideKeyboard(clickedView1);
      });
    }
  };
  private boolean activityPausing;

  @Override
  public void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mFragment = this;
  }

  @Override
  public void onAttach (Context context) {
    super.onAttach(context);
    EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.CHILDREN));
  }

  @Override
  public void onStop () {
    super.onStop();
    GeocodeHelper.stop();
  }

  @Override
  public void onResume () {
    super.onResume();
    activityPausing = false;
    EventBus.getDefault().register(this);
  }

  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_detail, container, false);
    ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onActivityCreated (Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mainActivity = (MainActivity) getActivity();

    prefs = mainActivity.prefs;

    mainActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
    mainActivity.getToolbar().setNavigationOnClickListener(v -> navigateUp());

    // Force the navigation drawer to stay opened if tablet mode is on, otherwise has to stay closed
    if (NavigationDrawerFragment.isDoublePanelActive()) {
      mainActivity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
    } else {
      mainActivity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    // Restored temp note after orientation change
    if (savedInstanceState != null) {
      noteTmp = savedInstanceState.getParcelable("noteTmp");
      note = savedInstanceState.getParcelable("note");
      noteOriginal = savedInstanceState.getParcelable("noteOriginal");
      attachmentUri = savedInstanceState.getParcelable("attachmentUri");
      orientationChanged = savedInstanceState.getBoolean("orientationChanged");
    }

    // Added the sketched image if present returning from SketchFragment
    if (mainActivity.sketchUri != null) {
      Attachment mAttachment = new Attachment(mainActivity.sketchUri, MIME_TYPE_SKETCH);
      addAttachment(mAttachment);
      mainActivity.sketchUri = null;
      // Removes previous version of edited image
      if (sketchEdited != null) {
        noteTmp.getAttachmentsList().remove(sketchEdited);
        sketchEdited = null;
      }
    }

    init();

    setHasOptionsMenu(true);
    setRetainInstance(false);
  }

  @Override
  public void onSaveInstanceState (Bundle outState) {
    if (noteTmp != null) {
      noteTmp.setTitle(getNoteTitle());
      noteTmp.setContent(getNoteContent());
      outState.putParcelable("noteTmp", noteTmp);
      outState.putParcelable("note", note);
      outState.putParcelable("noteOriginal", noteOriginal);
      outState.putParcelable("attachmentUri", attachmentUri);
      outState.putBoolean("orientationChanged", orientationChanged);
    }
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onPause () {
    super.onPause();

    //to prevent memory leak fragment keep refernce of event but until deregister
    EventBus.getDefault().unregister(this);

    activityPausing = true;

    // Checks "goBack" value to avoid performing a double saving
    if (!goBack) {
      saveNote(this);
    }

    if (toggleChecklistView != null) {
      KeyboardUtils.hideKeyboard(toggleChecklistView);
      content.clearFocus();
    }
  }

  @Override
  public void onConfigurationChanged (Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (getResources().getConfiguration().orientation != newConfig.orientation) {
      orientationChanged = true;
    }
  }

  private void init () {

    // Handling of Intent actions
    handleIntents();

    if (noteOriginal == null) {
      noteOriginal = getArguments().getParcelable(INTENT_NOTE);
    }

    if (note == null) {
      note = new Note(noteOriginal);
    }

    if (noteTmp == null) {
      noteTmp = new Note(note);
    }

    if (noteTmp.isLocked() && !noteTmp.isPasswordChecked()) {
      checkNoteLock(noteTmp);
      return;
    }

    initViews();
  }

  /**
   * Checks note lock and password before showing note content
   */
  private void checkNoteLock (Note note) {
    // If note is locked security password will be requested
    if (note.isLocked()
        && prefs.getString(PREF_PASSWORD, null) != null
        && !prefs.getBoolean("settings_password_access", false)) {
      PasswordHelper.requestPassword(mainActivity, passwordConfirmed -> {
        switch (passwordConfirmed) {
          case SUCCEED:
            noteTmp.setPasswordChecked(true);
            init();
            break;
          case FAIL:
            goBack = true;
            goHome();
            break;
          case RESTORE:
            goBack = true;
            goHome();
            PasswordHelper.resetPassword(mainActivity);
            break;
        }
      });
    } else {
      noteTmp.setPasswordChecked(true);
      init();
    }
  }

  private void handleIntents () {
    Intent i = mainActivity.getIntent();

    if (IntentChecker.checkAction(i, ACTION_MERGE)) {
      noteOriginal = new Note();
      note = new Note(noteOriginal);
      noteTmp = getArguments().getParcelable(INTENT_NOTE);
      if (i.getStringArrayListExtra("merged_notes") != null) {
        mergedNotesIds = i.getStringArrayListExtra("merged_notes");
      }
    }

    // Action called from home shortcut
    if (IntentChecker.checkAction(i, ACTION_SHORTCUT, ACTION_NOTIFICATION_CLICK)) {
      afterSavedReturnsToList = false;
      noteOriginal = DbHelper.getInstance().getNote(i.getLongExtra(INTENT_KEY, 0));
      // Checks if the note pointed from the shortcut has been deleted
      try {
        note = new Note(noteOriginal);
        noteTmp = new Note(noteOriginal);
      } catch (NullPointerException e) {
        mainActivity.showToast(getText(R.string.shortcut_note_deleted), Toast.LENGTH_LONG);
        mainActivity.finish();
      }
    }

    // Check if is launched from a widget
    if (IntentChecker.checkAction(i, ACTION_WIDGET, ACTION_WIDGET_TAKE_PHOTO)) {

      afterSavedReturnsToList = false;
      showKeyboard = true;

      //  with tags to set tag
      if (i.hasExtra(INTENT_WIDGET)) {
        String widgetId = i.getExtras().get(INTENT_WIDGET).toString();
        if (widgetId != null) {
          String sqlCondition = prefs.getString(PREF_WIDGET_PREFIX + widgetId, "");
          String categoryId = TextHelper.checkIntentCategory(sqlCondition);
          if (categoryId != null) {
            Category category;
            try {
              category = DbHelper.getInstance().getCategory(parseLong(categoryId));
              noteTmp = new Note();
              noteTmp.setCategory(category);
            } catch (NumberFormatException e) {
              LogDelegate.e("Category with not-numeric value!", e);
            }
          }
        }
      }

      // Sub-action is to take a photo
      if (IntentChecker.checkAction(i, ACTION_WIDGET_TAKE_PHOTO)) {
        takePhoto();
      }
    }

    if (IntentChecker.checkAction(i, ACTION_FAB_TAKE_PHOTO)) {
      takePhoto();
    }

    // Handles third party apps requests of sharing
    if (IntentChecker.checkAction(i, Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE, INTENT_GOOGLE_NOW)
        && i.getType() != null) {

      afterSavedReturnsToList = false;

      if (noteTmp == null) {
        noteTmp = new Note();
      }

      // Text title
      String title = i.getStringExtra(Intent.EXTRA_SUBJECT);
      if (title != null) {
        noteTmp.setTitle(title);
      }

      // Text content
      String content = i.getStringExtra(Intent.EXTRA_TEXT);
      if (content != null) {
        noteTmp.setContent(content);
      }

      importAttachments(i);

    }

    if (IntentChecker.checkAction(i, Intent.ACTION_MAIN, ACTION_WIDGET_SHOW_LIST, ACTION_SHORTCUT_WIDGET, ACTION_WIDGET)) {
      showKeyboard = true;
    }

    i.setAction(null);
  }

  private void importAttachments (Intent i) {

    if (!i.hasExtra(Intent.EXTRA_STREAM)) {
      return;
    }

    if (i.getExtras().get(Intent.EXTRA_STREAM) instanceof Uri) {
      Uri uri = i.getParcelableExtra(Intent.EXTRA_STREAM);
      // Google Now passes Intent as text but with audio recording attached the case must be handled like this
      if (!INTENT_GOOGLE_NOW.equals(i.getAction())) {
        String name = FileHelper.getNameFromUri(mainActivity, uri);
        new AttachmentTask(this, uri, name, this).execute();
      }
    } else {
      ArrayList<Uri> uris = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
      for (Uri uriSingle : uris) {
        String name = FileHelper.getNameFromUri(mainActivity, uriSingle);
        new AttachmentTask(this, uriSingle, name, this).execute();
      }
    }
  }

  @SuppressLint("NewApi")
  private void initViews () {

    // Sets onTouchListener to the whole activity to swipe notes
    root.setOnTouchListener(this);

    // Color of tag marker if note is tagged a function is active in preferences
    setTagMarkerColor(noteTmp.getCategory());

    initViewTitle();

    initViewContent();

    initViewLocation();

    initViewAttachments();

    initViewReminder();

    initViewFooter();
  }

  private void initViewFooter () {
    // Footer dates of creation...
    String creation = DateHelper.getFormattedDate(noteTmp.getCreation(), prefs.getBoolean(PREF_PRETTIFIED_DATES, true));
    creationTextView.append(creation.length() > 0 ? getString(R.string.creation) + " " + creation : "");
    if (creationTextView.getText().length() == 0) {
      creationTextView.setVisibility(View.GONE);
    }

    // ... and last modification
    String lastModification = DateHelper.getFormattedDate(noteTmp.getLastModification(), prefs.getBoolean(
        PREF_PRETTIFIED_DATES, true));
    lastModificationTextView.append(lastModification.length() > 0 ? getString(R.string.last_update) + " " +
        lastModification : "");
    if (lastModificationTextView.getText().length() == 0) {
      lastModificationTextView.setVisibility(View.GONE);
    }
  }

  private void initViewReminder () {
    reminderLayout.setOnClickListener(v -> {
      ReminderPickers reminderPicker = new ReminderPickers(mainActivity, mFragment);
      reminderPicker.pick(DateUtils.getPresetReminder(noteTmp.getAlarm()), noteTmp
          .getRecurrenceRule());
    });

    reminderLayout.setOnLongClickListener(v -> {
      MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
          .content(R.string.remove_reminder)
          .positiveText(R.string.ok)
          .onPositive((dialog1, which) -> {
            ReminderHelper.removeReminder(OmniNotes.getAppContext(), noteTmp);
            noteTmp.setAlarm(null);
            reminderIcon.setImageResource(R.drawable.ic_alarm_black_18dp);
            datetime.setText("");
          }).build();
      dialog.show();
      return true;
    });

    // Reminder
    String reminderString = initReminder(noteTmp);
    if (!TextUtils.isEmpty(reminderString)) {
      reminderIcon.setImageResource(R.drawable.ic_alarm_add_black_18dp);
      datetime.setText(reminderString);
    }
  }

  private void initViewLocation () {

    DetailFragment detailFragment = this;

    if (isNoteLocationValid()) {
      if (TextUtils.isEmpty(noteTmp.getAddress())) {
        //FIXME: What's this "sasd"?
        GeocodeHelper.getAddressFromCoordinates(new Location("sasd"), detailFragment);
      } else {
        locationTextView.setText(noteTmp.getAddress());
        locationTextView.setVisibility(View.VISIBLE);
      }
    }

    // Automatic location insertion
    if (prefs.getBoolean(PREF_AUTO_LOCATION, false) && noteTmp.get_id() == null) {
      getLocation(detailFragment);
    }

    locationTextView.setOnClickListener(v -> {
      String uriString = "geo:" + noteTmp.getLatitude() + ',' + noteTmp.getLongitude()
          + "?q=" + noteTmp.getLatitude() + ',' + noteTmp.getLongitude();
      Intent locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
      if (!IntentChecker.isAvailable(mainActivity, locationIntent, null)) {
        uriString = "http://maps.google.com/maps?q=" + noteTmp.getLatitude() + ',' + noteTmp
            .getLongitude();
        locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
      }
      startActivity(locationIntent);
    });
    locationTextView.setOnLongClickListener(v -> {
      MaterialDialog.Builder builder = new MaterialDialog.Builder(mainActivity);
      builder.content(R.string.remove_location);
      builder.positiveText(R.string.ok);
      builder.onPositive(new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
          noteTmp.setLatitude("");
          noteTmp.setLongitude("");
          fade(locationTextView, false);
        }
      });
      MaterialDialog dialog = builder.build();
      dialog.show();
      return true;
    });
  }

  private void getLocation (OnGeoUtilResultListener onGeoUtilResultListener) {
    PermissionsHelper.requestPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION, R.string
        .permission_coarse_location, snackBarPlaceholder, () -> GeocodeHelper.getLocation
        (onGeoUtilResultListener));
  }

  private void initViewAttachments () {

    // Attachments position based on preferences
    if (prefs.getBoolean(PREF_ATTACHMENTS_ON_BOTTOM, false)) {
      attachmentsBelow.inflate();
    } else {
      attachmentsAbove.inflate();
    }
    mGridView = root.findViewById(R.id.gridview);

    // Some fields can be filled by third party application and are always shown
    mAttachmentAdapter = new AttachmentAdapter(mainActivity, noteTmp.getAttachmentsList(), mGridView);

    // Initialzation of gridview for images
    mGridView.setAdapter(mAttachmentAdapter);
    mGridView.autoresize();

    // Click events for images in gridview (zooms image)
    mGridView.setOnItemClickListener((parent, v, position, id) -> {
      Attachment attachment = (Attachment) parent.getAdapter().getItem(position);
      Uri sharableUri = FileProviderHelper.getShareableUri(attachment);
      Intent attachmentIntent;
      if (MIME_TYPE_FILES.equals(attachment.getMime_type())) {

        attachmentIntent = new Intent(Intent.ACTION_VIEW);
        attachmentIntent.setDataAndType(sharableUri, StorageHelper.getMimeType(mainActivity,
            sharableUri));
        attachmentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent
            .FLAG_GRANT_WRITE_URI_PERMISSION);
        if (IntentChecker.isAvailable(mainActivity.getApplicationContext(), attachmentIntent, null)) {
          startActivity(attachmentIntent);
        } else {
          mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.WARN);
        }

        // Media files will be opened in internal gallery
      } else if (MIME_TYPE_IMAGE.equals(attachment.getMime_type())
          || MIME_TYPE_SKETCH.equals(attachment.getMime_type())
          || MIME_TYPE_VIDEO.equals(attachment.getMime_type())) {
        // Title
        noteTmp.setTitle(getNoteTitle());
        noteTmp.setContent(getNoteContent());
        String title1 = TextHelper.parseTitleAndContent(mainActivity,
            noteTmp)[0].toString();
        // Images
        int clickedImage = 0;
        ArrayList<Attachment> images = new ArrayList<>();
        for (Attachment mAttachment : noteTmp.getAttachmentsList()) {
          if (MIME_TYPE_IMAGE.equals(mAttachment.getMime_type())
              || MIME_TYPE_SKETCH.equals(mAttachment.getMime_type())
              || MIME_TYPE_VIDEO.equals(mAttachment.getMime_type())) {
            images.add(mAttachment);
            if (mAttachment.equals(attachment)) {
              clickedImage = images.size() - 1;
            }
          }
        }
        // Intent
        attachmentIntent = new Intent(mainActivity, GalleryActivity.class);
        attachmentIntent.putExtra(GALLERY_TITLE, title1);
        attachmentIntent.putParcelableArrayListExtra(GALLERY_IMAGES, images);
        attachmentIntent.putExtra(GALLERY_CLICKED_IMAGE, clickedImage);
        startActivity(attachmentIntent);

      } else if (MIME_TYPE_AUDIO.equals(attachment.getMime_type())) {
        playback(v, attachment.getUri());
      }

    });

    mGridView.setOnItemLongClickListener((parent, v, position, id) -> {
      // To avoid deleting audio attachment during playback
      if (mPlayer != null) {
        return false;
      }
      List<String> items = Arrays.asList(getResources().getStringArray(R.array.attachments_actions));
      if (!MIME_TYPE_SKETCH.equals(mAttachmentAdapter.getItem(position).getMime_type())) {
        items = items.subList(0, items.size() - 1);
      }
      Attachment attachment = mAttachmentAdapter.getItem(position);
      new MaterialDialog.Builder(mainActivity)
          .title(attachment.getName() + " (" + AttachmentsHelper.getSize(attachment) + ")")
          .items(items.toArray(new String[items.size()]))
          .itemsCallback((materialDialog, view, i, charSequence) ->
              performAttachmentAction(position, i))
          .build()
          .show();
      return true;
    });
  }

  /**
   * Performs an action when long-click option is selected
   *
   * @param i item index
   */
  private void performAttachmentAction (int attachmentPosition, int i) {
    switch (getResources().getStringArray(R.array.attachments_actions_values)[i]) {
      case "share":
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Attachment attachment = mAttachmentAdapter.getItem(attachmentPosition);
        shareIntent.setType(StorageHelper.getMimeType(OmniNotes.getAppContext(), attachment.getUri()));
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProviderHelper.getShareableUri(attachment));
        if (IntentChecker.isAvailable(OmniNotes.getAppContext(), shareIntent, null)) {
          startActivity(shareIntent);
        } else {
          mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.WARN);
        }
        break;
      case "delete":
        removeAttachment(attachmentPosition);
        mAttachmentAdapter.notifyDataSetChanged();
        mGridView.autoresize();
        break;
      case "delete all":
        new MaterialDialog.Builder(mainActivity)
            .title(R.string.delete_all_attachments)
            .positiveText(R.string.confirm)
            .onPositive((materialDialog, dialogAction) -> removeAllAttachments())
            .build()
            .show();
        break;
      case "edit":
        takeSketch(mAttachmentAdapter.getItem(attachmentPosition));
        break;
      default:
        LogDelegate.w("No action available");
    }
  }

  private void initViewTitle () {
    title.setText(noteTmp.getTitle());
    title.gatherLinksForText();
    title.setOnTextLinkClickListener(textLinkClickListener);
    // To avoid dropping here the  dragged checklist items
    title.setOnDragListener((v, event) -> {
//					((View)event.getLocalState()).setVisibility(View.VISIBLE);
      return true;
    });
    //When editor action is pressed focus is moved to last character in content field
    title.setOnEditorActionListener((v, actionId, event) -> {
      content.requestFocus();
      content.setSelection(content.getText().length());
      return false;
    });
    requestFocus(title);
  }

  private void initViewContent () {

    content.setText(noteTmp.getContent());
    content.gatherLinksForText();
    content.setOnTextLinkClickListener(textLinkClickListener);
    // Avoids focused line goes under the keyboard
    content.addTextChangedListener(this);

    // Restore checklist
    toggleChecklistView = content;
    if (noteTmp.isChecklist()) {
      noteTmp.setChecklist(false);
      AlphaManager.setAlpha(toggleChecklistView, 0);
      toggleChecklist2();
    }
  }

  /**
   * Force focus and shows soft keyboard. Only happens if it's a new note, without shared content. {@link showKeyboard}
   * is used to check if the note is created from shared content.
   */
  @SuppressWarnings("JavadocReference")
  private void requestFocus (final EditText view) {
    if (note.get_id() == null && !noteTmp.isChanged(note) && showKeyboard) {
      KeyboardUtils.showKeyboard(view);
    }
  }

  /**
   * Colors tag marker in note's title and content elements
   */
  private void setTagMarkerColor (Category tag) {

    String colorsPref = prefs.getString("settings_colors_app", PREF_COLORS_APP_DEFAULT);

    // Checking preference
    if (!"disabled".equals(colorsPref)) {

      // Choosing target view depending on another preference
      ArrayList<View> target = new ArrayList<>();
      if ("complete".equals(colorsPref)) {
        target.add(titleWrapperView);
        target.add(scrollView);
      } else {
        target.add(tagMarkerView);
      }

      // Coloring the target
      if (tag != null && tag.getColor() != null) {
        for (View view : target) {
          view.setBackgroundColor(parseInt(tag.getColor()));
        }
      } else {
        for (View view : target) {
          view.setBackgroundColor(Color.parseColor("#00000000"));
        }
      }
    }
  }

  private void displayLocationDialog () {
    getLocation(new OnGeoUtilResultListenerImpl(mainActivity, mFragment, noteTmp));
  }

  @Override
  public void onLocationRetrieved (Location location) {
    if (location == null) {
      mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
    }
    if (location != null) {
      noteTmp.setLatitude(location.getLatitude());
      noteTmp.setLongitude(location.getLongitude());
      if (!TextUtils.isEmpty(noteTmp.getAddress())) {
        locationTextView.setVisibility(View.VISIBLE);
        locationTextView.setText(noteTmp.getAddress());
      } else {
        GeocodeHelper.getAddressFromCoordinates(location, mFragment);
      }
    }
  }

  @Override
  public void onLocationUnavailable () {
    mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
  }

  @Override
  public void onAddressResolved (String address) {
    if (TextUtils.isEmpty(address)) {
      if (!isNoteLocationValid()) {
        mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
        return;
      }
      address = noteTmp.getLatitude() + ", " + noteTmp.getLongitude();
    }
    if (!GeocodeHelper.areCoordinates(address)) {
      noteTmp.setAddress(address);
    }
    locationTextView.setVisibility(View.VISIBLE);
    locationTextView.setText(address);
    fade(locationTextView, true);
  }

  @Override
  public void onCoordinatesResolved (Location location, String address) {
    if (location != null) {
      noteTmp.setLatitude(location.getLatitude());
      noteTmp.setLongitude(location.getLongitude());
      noteTmp.setAddress(address);
      locationTextView.setVisibility(View.VISIBLE);
      locationTextView.setText(address);
      fade(locationTextView, true);
    } else {
      mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
    }
  }

  @Override
  public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_detail, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu (Menu menu) {

    // Closes search view if left open in List fragment
    MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
    if (searchMenuItem != null) {
      searchMenuItem.collapseActionView();
    }

    boolean newNote = noteTmp.get_id() == null;

    menu.findItem(R.id.menu_checklist_on).setVisible(!noteTmp.isChecklist());
    menu.findItem(R.id.menu_checklist_off).setVisible(noteTmp.isChecklist());
    menu.findItem(R.id.menu_lock).setVisible(!noteTmp.isLocked());
    menu.findItem(R.id.menu_unlock).setVisible(noteTmp.isLocked());
    // If note is trashed only this options will be available from menu
    if (noteTmp.isTrashed()) {
      menu.findItem(R.id.menu_untrash).setVisible(true);
      menu.findItem(R.id.menu_delete).setVisible(true);
      // Otherwise all other actions will be available
    } else {
      // Temporary removed until fixed on Oreo and following
//			menu.findItem(R.ID.menu_add_shortcut).setVisible(!newNote);
      menu.findItem(R.id.menu_archive).setVisible(!newNote && !noteTmp.isArchived());
      menu.findItem(R.id.menu_unarchive).setVisible(!newNote && noteTmp.isArchived());
      menu.findItem(R.id.menu_trash).setVisible(!newNote);
    }
  }

  @SuppressLint("NewApi")
  private boolean goHome () {
    stopPlaying();

    // The activity has managed a shared intent from third party app and
    // performs a normal onBackPressed instead of returning back to ListActivity
    if (!afterSavedReturnsToList) {
      if (!TextUtils.isEmpty(exitMessage)) {
        mainActivity.showToast(exitMessage, Toast.LENGTH_SHORT);
      }
      mainActivity.finish();

    } else {

      if (!TextUtils.isEmpty(exitMessage) && exitCroutonStyle != null) {
        mainActivity.showMessage(exitMessage, exitCroutonStyle);
      }

      // Otherwise the result is passed to ListActivity
      if (mainActivity != null && mainActivity.getSupportFragmentManager() != null) {
        mainActivity.getSupportFragmentManager().popBackStack();
        if (mainActivity.getSupportFragmentManager().getBackStackEntryCount() == 1) {
          mainActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
          if (mainActivity.getDrawerToggle() != null) {
            mainActivity.getDrawerToggle().setDrawerIndicatorEnabled(true);
          }
          EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.PARENT));
        }
      }
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item) {

    if (isOptionsItemFastClick()) {
      return true;
    }

    switch (item.getItemId()) {
      case R.id.menu_attachment:
        showAttachmentsPopup();
        break;
      case R.id.menu_tag:
        addTags();
        break;
      case R.id.menu_category:
        categorizeNote();
        break;
      case R.id.menu_share:
        shareNote();
        break;
      case R.id.menu_checklist_on:
      case R.id.menu_checklist_off:
        toggleChecklist();
        break;
      case R.id.menu_lock:
      case R.id.menu_unlock:
        lockNote();
        break;
      case R.id.menu_add_shortcut:
        addShortcut();
        break;
      case R.id.menu_archive:
        archiveNote(true);
        break;
      case R.id.menu_unarchive:
        archiveNote(false);
        break;
      case R.id.menu_trash:
        trashNote(true);
        break;
      case R.id.menu_untrash:
        trashNote(false);
        break;
      case R.id.menu_discard_changes:
        discard();
        break;
      case R.id.menu_delete:
        deleteNote();
        break;
      case R.id.menu_note_info:
        showNoteInfo();
        break;
      default:
        LogDelegate.w("Invalid menu option selected");
    }

    ((OmniNotes) getActivity().getApplication()).getAnalyticsHelper().trackActionFromResourceId(getActivity(),
        item.getItemId());

    return super.onOptionsItemSelected(item);
  }

  private void showNoteInfo () {
    noteTmp.setTitle(getNoteTitle());
    noteTmp.setContent(getNoteContent());
    Intent intent = new Intent(getContext(), NoteInfosActivity.class);
    intent.putExtra(INTENT_NOTE, (android.os.Parcelable) noteTmp);
    startActivity(intent);

  }

  private void navigateUp () {
    afterSavedReturnsToList = true;
    saveAndExit(this);
  }

  /**
   *
   */
  private void toggleChecklist () {

    // In case checklist is active a prompt will ask about many options
    // to decide hot to convert back to simple text
    if (!noteTmp.isChecklist()) {
      toggleChecklist2();
      return;
    }

    // If checklist is active but no items are checked the conversion in done automatically
    // without prompting user
    if (mChecklistManager.getCheckedCount() == 0) {
      toggleChecklist2(true, false);
      return;
    }

    // Inflate the popup_layout.XML
    LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
    final View layout = inflater.inflate(R.layout.dialog_remove_checklist_layout,
        getView().findViewById(R.id.layout_root));

    // Retrieves options checkboxes and initialize their values
    final CheckBox keepChecked = layout.findViewById(R.id.checklist_keep_checked);
    final CheckBox keepCheckmarks = layout.findViewById(R.id.checklist_keep_checkmarks);
    keepChecked.setChecked(prefs.getBoolean(PREF_KEEP_CHECKED, true));
    keepCheckmarks.setChecked(prefs.getBoolean(PREF_KEEP_CHECKMARKS, true));

    new MaterialDialog.Builder(mainActivity)
        .customView(layout, false)
        .positiveText(R.string.ok)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            prefs.edit()
                 .putBoolean(PREF_KEEP_CHECKED, keepChecked.isChecked())
                 .putBoolean(PREF_KEEP_CHECKMARKS, keepCheckmarks.isChecked())
                 .apply();
            toggleChecklist2();
          }
        }).build().show();
  }

  /**
   * Toggles checklist view
   */
  private void toggleChecklist2 () {
    boolean keepChecked = prefs.getBoolean(PREF_KEEP_CHECKED, true);
    boolean showChecks = prefs.getBoolean(PREF_KEEP_CHECKMARKS, true);
    toggleChecklist2(keepChecked, showChecks);
  }

  @SuppressLint("NewApi")
  private void toggleChecklist2 (final boolean keepChecked, final boolean showChecks) {
    // Get instance and set options to convert EditText to CheckListView

    mChecklistManager = mChecklistManager == null ? new ChecklistManager(mainActivity) : mChecklistManager;
    int checkedItemsBehavior = Integer.valueOf(prefs.getString("settings_checked_items_behavior", String.valueOf
        (it.feio.android.checklistview.Settings.CHECKED_HOLD)));
    mChecklistManager
        .showCheckMarks(showChecks)
        .newEntryHint(getString(R.string.checklist_item_hint))
        .keepChecked(keepChecked)
        .undoBarContainerView(scrollView)
        .moveCheckedOnBottom(checkedItemsBehavior);

    // Links parsing options
    mChecklistManager.setOnTextLinkClickListener(textLinkClickListener);
    mChecklistManager.addTextChangedListener(mFragment);
    mChecklistManager.setCheckListChangedListener(mFragment);

    // Switches the views
    View newView = null;
    try {
      newView = mChecklistManager.convert(toggleChecklistView);
    } catch (ViewNotSupportedException e) {
      LogDelegate.e("Error switching checklist view", e);
    }

    // Switches the views
    if (newView != null) {
      mChecklistManager.replaceViews(toggleChecklistView, newView);
      toggleChecklistView = newView;
      animate(toggleChecklistView).alpha(1).scaleXBy(0).scaleX(1).scaleYBy(0).scaleY(1);
      noteTmp.setChecklist(!noteTmp.isChecklist());
    }
  }

  /**
   * Categorize note choosing from a list of previously created categories
   */
  private void categorizeNote () {

    String currentCategory = noteTmp.getCategory() != null ? String.valueOf(noteTmp.getCategory().getId()) : null;
    final List<Category> categories = Observable.from(DbHelper.getInstance().getCategories()).map(category -> {
      if (String.valueOf(category.getId()).equals(currentCategory)) {
        category.setCount(category.getCount() + 1);
      }
      return category;
    }).toList().toBlocking().single();

    final MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
        .title(R.string.categorize_as)
        .adapter(new CategoryRecyclerViewAdapter(mainActivity, categories), null)
        .positiveText(R.string.add_category)
        .positiveColorRes(R.color.colorPrimary)
        .negativeText(R.string.remove_category)
        .negativeColorRes(R.color.colorAccent)
        .onPositive((dialog1, which) -> {
          Intent intent = new Intent(mainActivity, CategoryActivity.class);
          intent.putExtra("noHome", true);
          startActivityForResult(intent, CATEGORY);
        })
        .onNegative((dialog12, which) -> {
          noteTmp.setCategory(null);
          setTagMarkerColor(null);
        }).build();

    RecyclerViewItemClickSupport.addTo(dialog.getRecyclerView()).setOnItemClickListener((recyclerView, position, v) -> {
      noteTmp.setCategory(categories.get(position));
      setTagMarkerColor(categories.get(position));
      dialog.dismiss();
    });

    dialog.show();

  }

    private void showAttachmentsPopup () {
    LayoutInflater inflater = mainActivity.getLayoutInflater();
    final View layout = inflater.inflate(R.layout.attachment_dialog, null);

    attachmentDialog = new MaterialDialog.Builder(mainActivity)
        .autoDismiss(false)
        .customView(layout, false)
        .build();
    attachmentDialog.show();

    // Camera
    android.widget.TextView cameraSelection = layout.findViewById(R.id.camera);
    cameraSelection.setOnClickListener(new AttachmentOnClickListener());
    // Audio recording
    android.widget.TextView recordingSelection = layout.findViewById(R.id.recording);
    toggleAudioRecordingStop(recordingSelection);
    recordingSelection.setOnClickListener(new AttachmentOnClickListener());
    // Video recording
    android.widget.TextView videoSelection = layout.findViewById(R.id.video);
    videoSelection.setOnClickListener(new AttachmentOnClickListener());
    // Files
    android.widget.TextView filesSelection = layout.findViewById(R.id.files);
    filesSelection.setOnClickListener(new AttachmentOnClickListener());
    // Sketch
    android.widget.TextView sketchSelection = layout.findViewById(R.id.sketch);
    sketchSelection.setOnClickListener(new AttachmentOnClickListener());
    // Location
    android.widget.TextView locationSelection = layout.findViewById(R.id.location);
    locationSelection.setOnClickListener(new AttachmentOnClickListener());
    // Time
    android.widget.TextView timeStampSelection = layout.findViewById(R.id.timestamp);
    timeStampSelection.setOnClickListener(new AttachmentOnClickListener());
    // Desktop note with PushBullet
    android.widget.TextView pushbulletSelection = layout.findViewById(R.id.pushbullet);
    pushbulletSelection.setVisibility(View.VISIBLE);
    pushbulletSelection.setOnClickListener(new AttachmentOnClickListener());
  }

  private void takePhoto () {
    // Checks for camera app available
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (!IntentChecker.isAvailable(mainActivity, intent, new String[]{PackageManager.FEATURE_CAMERA})) {
      mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.ALERT);

      return;
    }
    // Checks for created file validity
    File f = StorageHelper.createNewAttachmentFile(mainActivity, MIME_TYPE_IMAGE_EXT);
    if (f == null) {
      mainActivity.showMessage(R.string.error, ONStyle.ALERT);
      return;
    }
    attachmentUri = FileProviderHelper.getFileProvider(f);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, attachmentUri);
    startActivityForResult(intent, TAKE_PHOTO);
  }

  private void takeVideo () {
    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    if (!IntentChecker.isAvailable(mainActivity, takeVideoIntent, new String[]{PackageManager.FEATURE_CAMERA})) {
      mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.ALERT);

      return;
    }
    // File is stored in custom ON folder to speedup the attachment
    File f = StorageHelper.createNewAttachmentFile(mainActivity, MIME_TYPE_VIDEO_EXT);
    if (f == null) {
      mainActivity.showMessage(R.string.error, ONStyle.ALERT);
      return;
    }
    attachmentUri = FileProviderHelper.getFileProvider(f);
    takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, attachmentUri);
    String maxVideoSizeStr = "".equals(prefs.getString("settings_max_video_size",
        "")) ? "0" : prefs.getString("settings_max_video_size", "");
    long maxVideoSize = parseLong(maxVideoSizeStr) * 1024L * 1024L;
    takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
    startActivityForResult(takeVideoIntent, TAKE_VIDEO);
  }

  private void takeSketch (Attachment attachment) {

    File f = StorageHelper.createNewAttachmentFile(mainActivity, MIME_TYPE_SKETCH_EXT);
    if (f == null) {
      mainActivity.showMessage(R.string.error, ONStyle.ALERT);
      return;
    }
    attachmentUri = Uri.fromFile(f);

    // Forces portrait orientation to this fragment only
    mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    // Fragments replacing
    FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
    mainActivity.animateTransition(transaction, TRANSITION_HORIZONTAL);
    SketchFragment mSketchFragment = new SketchFragment();
    Bundle b = new Bundle();
    b.putParcelable(MediaStore.EXTRA_OUTPUT, attachmentUri);
    if (attachment != null) {
      b.putParcelable("base", attachment.getUri());
    }
    mSketchFragment.setArguments(b);
    transaction.replace(R.id.fragment_container, mSketchFragment, mainActivity.FRAGMENT_SKETCH_TAG)
               .addToBackStack(mainActivity.FRAGMENT_DETAIL_TAG).commit();
  }

  private void addTimestamp () {
    Editable editable = content.getText();
    int position = content.getSelectionStart();
    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    String dateStamp = dateFormat.format(new Date().getTime()) + " ";
    if (noteTmp.isChecklist()) {
      if (mChecklistManager.getFocusedItemView() != null) {
        editable = mChecklistManager.getFocusedItemView().getEditText().getEditableText();
        position = mChecklistManager.getFocusedItemView().getEditText().getSelectionStart();
      } else {
        ((CheckListView) toggleChecklistView)
            .addItem(dateStamp, false, mChecklistManager.getCount());
      }
    }
    String leadSpace = position == 0 ? "" : " ";
    dateStamp = leadSpace + dateStamp;
    editable.insert(position, dateStamp);
    Selection.setSelection(editable, position + dateStamp.length());
  }

  @SuppressLint("NewApi")
  @Override
  public void onActivityResult (int requestCode, int resultCode, Intent intent) {
    // Fetch uri from activities, store into adapter and refresh adapter
    Attachment attachment;
    if (resultCode == Activity.RESULT_OK) {
      switch (requestCode) {
        case TAKE_PHOTO:
          attachment = new Attachment(attachmentUri, MIME_TYPE_IMAGE);
          addAttachment(attachment);
          mAttachmentAdapter.notifyDataSetChanged();
          mGridView.autoresize();
          break;
        case TAKE_VIDEO:
          attachment = new Attachment(attachmentUri, MIME_TYPE_VIDEO);
          addAttachment(attachment);
          mAttachmentAdapter.notifyDataSetChanged();
          mGridView.autoresize();
          break;
        case FILES:
          onActivityResultManageReceivedFiles(intent);
          break;
        case SET_PASSWORD:
          noteTmp.setPasswordChecked(true);
          lockUnlock();
          break;
        case SKETCH:
          attachment = new Attachment(attachmentUri, MIME_TYPE_SKETCH);
          addAttachment(attachment);
          mAttachmentAdapter.notifyDataSetChanged();
          mGridView.autoresize();
          break;
        case CATEGORY:
          mainActivity.showMessage(R.string.category_saved, ONStyle.CONFIRM);
          Category category = intent.getParcelableExtra("category");
          noteTmp.setCategory(category);
          setTagMarkerColor(category);
          break;
        case DETAIL:
          mainActivity.showMessage(R.string.note_updated, ONStyle.CONFIRM);
          break;
        default:
          LogDelegate.e("Wrong element choosen: " + requestCode);
      }
    }
  }

  private void onActivityResultManageReceivedFiles (Intent intent) {
    List<Uri> uris = new ArrayList<>();
    if (Build.VERSION.SDK_INT > 16 && intent.getClipData() != null) {
      for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
        uris.add(intent.getClipData().getItemAt(i).getUri());
      }
    } else {
      uris.add(intent.getData());
    }
    for (Uri uri : uris) {
      String name = FileHelper.getNameFromUri(mainActivity, uri);
      new AttachmentTask(this, uri, name, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }

  /**
   * Discards changes done to the note and eventually delete new attachments
   */
  private void discard () {
    new MaterialDialog.Builder(mainActivity)
        .content(R.string.undo_changes_note_confirmation)
        .positiveText(R.string.ok)
        .onPositive((dialog, which) -> {
          if (!noteTmp.getAttachmentsList().equals(note.getAttachmentsList())) {
            for (Attachment newAttachment : noteTmp.getAttachmentsList()) {
              if (!note.getAttachmentsList().contains(newAttachment)) {
                StorageHelper.delete(mainActivity, newAttachment.getUri().getPath());
              }
            }
          }

          goBack = true;

          if (noteTmp.equals(noteOriginal)) {
            goHome();
          }

          if (noteOriginal.get_id() != null) {
            new SaveNoteTask(mFragment, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, noteOriginal);
            MainActivity.notifyAppWidgets(mainActivity);
          } else {
            goHome();
          }
        }).build().show();
  }

  @SuppressLint("NewApi")
  private void archiveNote (boolean archive) {
    // Simply go back if is a new note
    if (noteTmp.get_id() == null) {
      goHome();
      return;
    }

    noteTmp.setArchived(archive);
    goBack = true;
    exitMessage = archive ? getString(R.string.note_archived) : getString(R.string.note_unarchived);
    exitCroutonStyle = archive ? ONStyle.WARN : ONStyle.INFO;
    saveNote(this);
  }

  @SuppressLint("NewApi")
  private void trashNote (boolean trash) {
    // Simply go back if is a new note
    if (noteTmp.get_id() == null) {
      goHome();
      return;
    }

    noteTmp.setTrashed(trash);
    goBack = true;
    exitMessage = trash ? getString(R.string.note_trashed) : getString(R.string.note_untrashed);
    exitCroutonStyle = trash ? ONStyle.WARN : ONStyle.INFO;
    if (trash) {
      ShortcutHelper.removeshortCut(OmniNotes.getAppContext(), noteTmp);
      ReminderHelper.removeReminder(OmniNotes.getAppContext(), noteTmp);
    } else {
      ReminderHelper.addReminder(OmniNotes.getAppContext(), note);
    }
    saveNote(this);
  }

  private void deleteNote () {
    new MaterialDialog.Builder(mainActivity)
        .content(R.string.delete_note_confirmation)
        .positiveText(R.string.ok)
        .onPositive((dialog, which) -> {
          mainActivity.deleteNote(noteTmp);
          LogDelegate.d("Deleted note with ID '" + noteTmp.get_id() + "'");
          mainActivity.showMessage(R.string.note_deleted, ONStyle.ALERT);
          goHome();
        }).build().show();
  }

  public void saveAndExit (OnNoteSaved mOnNoteSaved) {
    if (isAdded()) {
      exitMessage = getString(R.string.note_updated);
      exitCroutonStyle = ONStyle.CONFIRM;
      goBack = true;
      saveNote(mOnNoteSaved);
    }
  }

  /**
   * Save new notes, modify them or archive
   */
  void saveNote (OnNoteSaved mOnNoteSaved) {

    // Changed fields
    noteTmp.setTitle(getNoteTitle());
    noteTmp.setContent(getNoteContent());

    // Check if some text or attachments of any type have been inserted or is an empty note
    if (goBack && TextUtils.isEmpty(noteTmp.getTitle()) && TextUtils.isEmpty(noteTmp.getContent())
        && noteTmp.getAttachmentsList().size() == 0) {
      LogDelegate.d("Empty note not saved");
      exitMessage = getString(R.string.empty_note_not_saved);
      exitCroutonStyle = ONStyle.INFO;
      goHome();
      return;
    }

    if (saveNotNeeded()) {
      exitMessage = "";
      if (goBack) {
        goHome();
      }
      return;
    }

    noteTmp.setAttachmentsListOld(note.getAttachmentsList());

    new SaveNoteTask(mOnNoteSaved, lastModificationUpdatedNeeded()).executeOnExecutor(AsyncTask
        .THREAD_POOL_EXECUTOR, noteTmp);
  }

  /**
   * Checks if nothing is changed to avoid committing if possible (check)
   */
  private boolean saveNotNeeded () {
    if (noteTmp.get_id() == null && prefs.getBoolean(PREF_AUTO_LOCATION, false)) {
      note.setLatitude(noteTmp.getLatitude());
      note.setLongitude(noteTmp.getLongitude());
    }
    return !noteTmp.isChanged(note) || (noteTmp.isLocked() && !noteTmp.isPasswordChecked());
  }

  /**
   * Checks if only tag, archive or trash status have been changed and then force to not update last modification date*
   */
  private boolean lastModificationUpdatedNeeded () {
    note.setCategory(noteTmp.getCategory());
    note.setArchived(noteTmp.isArchived());
    note.setTrashed(noteTmp.isTrashed());
    note.setLocked(noteTmp.isLocked());
    return noteTmp.isChanged(note);
  }

  @Override
  public void onNoteSaved (Note noteSaved) {
    MainActivity.notifyAppWidgets(OmniNotes.getAppContext());
    if (!activityPausing) {
      EventBus.getDefault().post(new NotesUpdatedEvent(Collections.singletonList(noteSaved)));
      deleteMergedNotes(mergedNotesIds);
      if (noteTmp.getAlarm() != null && !noteTmp.getAlarm().equals(note.getAlarm())) {
        ReminderHelper.showReminderMessage(String.valueOf(noteTmp.getAlarm()));
      }
    }
    note = new Note(noteSaved);
    if (goBack) {
      goHome();
    }
  }

  private void deleteMergedNotes (List<String> mergedNotesIds) {
    ArrayList<Note> notesToDelete = new ArrayList<>();
    if (mergedNotesIds != null) {
      for (String mergedNoteId : mergedNotesIds) {
        Note note = new Note();
        note.set_id(Long.valueOf(mergedNoteId));
        notesToDelete.add(note);
      }
      new NoteProcessorDelete(notesToDelete).process();
    }
  }

  private String getNoteTitle () {
    if (title != null && !TextUtils.isEmpty(title.getText())) {
      return title.getText().toString();
    } else {
      return "";
    }
  }

  private String getNoteContent () {
    String contentText = "";
    if (!noteTmp.isChecklist()) {
      // Due to checklist library introduction the returned EditText class is no more a
      // com.neopixl.pixlui.components.edittext.EditText but a standard android.widget.EditText
      View contentView = root.findViewById(R.id.detail_content);
      if (contentView instanceof EditText) {
        contentText = ((EditText) contentView).getText().toString();
      } else if (contentView instanceof android.widget.EditText) {
        contentText = ((android.widget.EditText) contentView).getText().toString();
      }
    } else {
      if (mChecklistManager != null) {
        mChecklistManager.keepChecked(true).showCheckMarks(true);
        contentText = mChecklistManager.getText();
      }
    }
    return contentText;
  }

  /**
   * Updates share intent
   */
  private void shareNote () {
    Note sharedNote = new Note(noteTmp);
    sharedNote.setTitle(getNoteTitle());
    sharedNote.setContent(getNoteContent());
    mainActivity.shareNote(sharedNote);
  }

  /**
   * Notes locking with security password to avoid viewing, editing or deleting from unauthorized
   */
  private void lockNote () {
    LogDelegate.d("Locking or unlocking note " + note.get_id());

    // If security password is not set yes will be set right now
    if (prefs.getString(PREF_PASSWORD, null) == null) {
      Intent passwordIntent = new Intent(mainActivity, PasswordActivity.class);
      startActivityForResult(passwordIntent, SET_PASSWORD);
      return;
    }

    // If password has already been inserted will not be asked again
    if (noteTmp.isPasswordChecked() || prefs.getBoolean("settings_password_access", false)) {
      lockUnlock();
      return;
    }

    // Password will be requested here
    PasswordHelper.requestPassword(mainActivity, passwordConfirmed -> {
      switch (passwordConfirmed) {
        case SUCCEED:
          lockUnlock();
          break;
        default:
          break;
      }
    });
  }

  private void lockUnlock () {
    // Empty password has been set
    if (prefs.getString(PREF_PASSWORD, null) == null) {
      mainActivity.showMessage(R.string.password_not_set, ONStyle.WARN);
      return;
    }
    mainActivity.showMessage(R.string.save_note_to_lock_it, ONStyle.INFO);
    mainActivity.supportInvalidateOptionsMenu();
    noteTmp.setLocked(!noteTmp.isLocked());
    noteTmp.setPasswordChecked(true);
  }

  /**
   * Used to set actual reminder state when initializing a note to be edited
   */
  private String initReminder (Note note) {
    if (noteTmp.getAlarm() == null) {
      return "";
    }
    long reminder = parseLong(note.getAlarm());
    String rrule = note.getRecurrenceRule();
    if (!TextUtils.isEmpty(rrule)) {
      return RecurrenceHelper.getNoteRecurrentReminderText(reminder, rrule);
    } else {
      return RecurrenceHelper.getNoteReminderText(reminder);
    }
  }

  /**
   * Audio recordings playback
   */
  private void playback (View v, Uri uri) {
    // Some recording is playing right now
    if (mPlayer != null && mPlayer.isPlaying()) {
      if (isPlayingView != v) {
        // If the audio actually played is NOT the one from the click view the last one is played
        stopPlaying();
        isPlayingView = v;
        startPlaying(uri);
        replacePlayingAudioBitmap(v);
      } else {
        // Otherwise just stops playing
        stopPlaying();
      }
    } else {
      // If nothing is playing audio just plays
      isPlayingView = v;
      startPlaying(uri);
      replacePlayingAudioBitmap(v);
    }
  }

  private void replacePlayingAudioBitmap (View v) {
    Drawable d = ((ImageView) v.findViewById(R.id.gridview_item_picture)).getDrawable();
    if (BitmapDrawable.class.isAssignableFrom(d.getClass())) {
      recordingBitmap = ((BitmapDrawable) d).getBitmap();
    } else {
      recordingBitmap = ((BitmapDrawable) d.getCurrent()).getBitmap();
    }
    ((ImageView) v.findViewById(R.id.gridview_item_picture)).setImageBitmap(ThumbnailUtils
        .extractThumbnail(BitmapFactory.decodeResource(mainActivity.getResources(),
            R.drawable.stop), THUMBNAIL_SIZE, THUMBNAIL_SIZE));
  }

  private void startPlaying (Uri uri) {
    if (mPlayer == null) {
      mPlayer = new MediaPlayer();
    }
    try {
      mPlayer.setDataSource(mainActivity, uri);
      mPlayer.prepare();
      mPlayer.start();
      mPlayer.setOnCompletionListener(mp -> {
        mPlayer = null;
        if (isPlayingView != null) {
          ((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture)).setImageBitmap
              (recordingBitmap);
          recordingBitmap = null;
          isPlayingView = null;
        }
      });
    } catch (IOException e) {
      LogDelegate.e("prepare() failed", e);
      mainActivity.showMessage(R.string.error, ONStyle.ALERT);
    }
  }

  private void stopPlaying () {
    if (mPlayer != null) {
      if (isPlayingView != null) {
        ((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture)).setImageBitmap(recordingBitmap);
      }
      isPlayingView = null;
      recordingBitmap = null;
      mPlayer.release();
      mPlayer = null;
    }
  }

  private void startRecording (View v) {
    PermissionsHelper.requestPermission(getActivity(), Manifest.permission.RECORD_AUDIO,
        R.string.permission_audio_recording, snackBarPlaceholder, () -> {

          isRecording = true;
          toggleAudioRecordingStop(v);

          File f = StorageHelper.createNewAttachmentFile(mainActivity, MIME_TYPE_AUDIO_EXT);
          if (f == null) {
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);
            return;
          }
          if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setAudioEncodingBitRate(96000);
            mRecorder.setAudioSamplingRate(44100);
          }
          recordName = f.getAbsolutePath();
          mRecorder.setOutputFile(recordName);

          try {
            audioRecordingTimeStart = Calendar.getInstance().getTimeInMillis();
            mRecorder.prepare();
            mRecorder.start();
          } catch (IOException | IllegalStateException e) {
            LogDelegate.e("prepare() failed", e);
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);
          }
        });
  }

  private void toggleAudioRecordingStop (View v) {
    if (isRecording) {
      ((android.widget.TextView) v).setText(getString(R.string.stop));
      ((android.widget.TextView) v).setTextColor(Color.parseColor("#ff0000"));
    }
  }

  private void stopRecording () {
    isRecording = false;
    if (mRecorder != null) {
      mRecorder.stop();
      audioRecordingTime = Calendar.getInstance().getTimeInMillis() - audioRecordingTimeStart;
      mRecorder.release();
      mRecorder = null;
    }
  }

  private void fade (final View v, boolean fadeIn) {

    int anim = R.animator.fade_out_support;
    int visibilityTemp = View.GONE;

    if (fadeIn) {
      anim = R.animator.fade_in_support;
      visibilityTemp = View.VISIBLE;
    }

    final int visibility = visibilityTemp;

    // Checks if user has left the app
    if (mainActivity != null) {
      Animation mAnimation = AnimationUtils.loadAnimation(mainActivity, anim);
      mAnimation.setAnimationListener(new AnimationListener() {
        @Override
        public void onAnimationStart (Animation animation) {
          // Nothing to do
        }

        @Override
        public void onAnimationRepeat (Animation animation) {
          // Nothing to do
        }

        @Override
        public void onAnimationEnd (Animation animation) {
          v.setVisibility(visibility);
        }
      });
      v.startAnimation(mAnimation);
    }
  }

  /**
   * Adding shortcut on Home screen
   */
  private void addShortcut () {
    ShortcutHelper.addShortcut(OmniNotes.getAppContext(), noteTmp);
    mainActivity.showMessage(R.string.shortcut_added, ONStyle.INFO);
  }

  @SuppressLint("NewApi")
  @Override
  public boolean onTouch (View v, MotionEvent event) {
    int x = (int) event.getX();
    int y = (int) event.getY();

    switch (event.getAction()) {

      case MotionEvent.ACTION_DOWN:
        LogDelegate.v("MotionEvent.ACTION_DOWN");
        int w;

        Point displaySize = Display.getUsableSize(mainActivity);
        w = displaySize.x;

        if (x < SWIPE_MARGIN || x > w - SWIPE_MARGIN) {
          swiping = true;
          startSwipeX = x;
        }

        break;

      case MotionEvent.ACTION_UP:
        LogDelegate.v("MotionEvent.ACTION_UP");
        if (swiping) {
          swiping = false;
        }
        break;

      case MotionEvent.ACTION_MOVE:
        if (swiping) {
          LogDelegate.v("MotionEvent.ACTION_MOVE at position " + x + ", " + y);
          if (Math.abs(x - startSwipeX) > SWIPE_OFFSET) {
            swiping = false;
            FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
            mainActivity.animateTransition(transaction, TRANSITION_VERTICAL);
            DetailFragment mDetailFragment = new DetailFragment();
            Bundle b = new Bundle();
            b.putParcelable(INTENT_NOTE, new Note());
            mDetailFragment.setArguments(b);
            transaction.replace(R.id.fragment_container, mDetailFragment,
                mainActivity.FRAGMENT_DETAIL_TAG).addToBackStack(mainActivity
                .FRAGMENT_DETAIL_TAG).commit();
          }
        }
        break;

      default:
        LogDelegate.e("Wrong element choosen: " + event.getAction());
    }

    return true;
  }

  @Override
  public void onAttachingFileErrorOccurred (Attachment mAttachment) {
    mainActivity.showMessage(R.string.error_saving_attachments, ONStyle.ALERT);
    if (noteTmp.getAttachmentsList().contains(mAttachment)) {
      removeAttachment(mAttachment);
      mAttachmentAdapter.notifyDataSetChanged();
      mGridView.autoresize();
    }
  }

  private void addAttachment (Attachment attachment) {
    noteTmp.addAttachment(attachment);
  }

  private void removeAttachment (Attachment mAttachment) {
    noteTmp.removeAttachment(mAttachment);
  }

  private void removeAttachment (int position) {
    noteTmp.removeAttachment(noteTmp.getAttachmentsList().get(position));
  }

  private void removeAllAttachments () {
    noteTmp.setAttachmentsList(new ArrayList<>());
    mAttachmentAdapter = new AttachmentAdapter(mainActivity, new ArrayList<>(), mGridView);
    mGridView.invalidateViews();
    mGridView.setAdapter(mAttachmentAdapter);
  }

  @Override
  public void onAttachingFileFinished (Attachment mAttachment) {
    addAttachment(mAttachment);
    mAttachmentAdapter.notifyDataSetChanged();
    mGridView.autoresize();
  }

  @Override
  public void onReminderPicked (long reminder) {
    noteTmp.setAlarm(reminder);
    if (mFragment.isAdded()) {
      reminderIcon.setImageResource(R.drawable.ic_alarm_black_18dp);
      datetime.setText(RecurrenceHelper.getNoteReminderText(reminder));
    }
  }

  @Override
  public void onRecurrenceReminderPicked (String recurrenceRule) {
    noteTmp.setRecurrenceRule(recurrenceRule);
    if (!TextUtils.isEmpty(recurrenceRule)) {
      LogDelegate.d("Recurrent reminder set: " + recurrenceRule);
      datetime.setText(RecurrenceHelper.getNoteRecurrentReminderText(Long.parseLong(noteTmp.getAlarm()), recurrenceRule));
    }
  }

  @Override
  public void onTextChanged (CharSequence s, int start, int before, int count) {
    scrollContent();
  }

  @Override
  public void beforeTextChanged (CharSequence s, int start, int count, int after) {
    // Nothing to do
  }

  @Override
  public void afterTextChanged (Editable s) {
    // Nothing to do
  }

  @Override
  public void onCheckListChanged () {
    scrollContent();
  }

  private void scrollContent () {
    if (noteTmp.isChecklist()) {
      if (mChecklistManager.getCount() > contentLineCounter) {
        scrollView.scrollBy(0, 60);
      }
      contentLineCounter = mChecklistManager.getCount();
    } else {
      if (content.getLineCount() > contentLineCounter) {
        scrollView.scrollBy(0, 60);
      }
      contentLineCounter = content.getLineCount();
    }
  }

  /**
   * Add previously created tags to content
   */
  private void addTags () {
    contentCursorPosition = getCursorIndex();

    final List<Tag> tags = TagsHelper.getAllTags();

    if (tags.isEmpty()) {
      mainActivity.showMessage(R.string.no_tags_created, ONStyle.WARN);
      return;
    }

    final Note currentNote = new Note();
    currentNote.setTitle(getNoteTitle());
    currentNote.setContent(getNoteContent());
    Integer[] preselectedTags = TagsHelper.getPreselectedTagsArray(currentNote, tags);

    // Dialog and events creation
    MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
        .title(R.string.select_tags)
        .positiveText(R.string.ok)
        .items(TagsHelper.getTagsArray(tags))
        .itemsCallbackMultiChoice(preselectedTags, (dialog1, which, text) -> {
          dialog1.dismiss();
          tagNote(tags, which, currentNote);
          return false;
        }).build();
    dialog.show();
  }

  private void tagNote (List<Tag> tags, Integer[] selectedTags, Note note) {
    Pair<String, List<Tag>> taggingResult = TagsHelper.addTagToNote(tags, selectedTags, note);

    StringBuilder sb;
    if (noteTmp.isChecklist()) {
      CheckListViewItem mCheckListViewItem = mChecklistManager.getFocusedItemView();
      if (mCheckListViewItem != null) {
        sb = new StringBuilder(mCheckListViewItem.getText());
        sb.insert(contentCursorPosition, " " + taggingResult.first + " ");
        mCheckListViewItem.setText(sb.toString());
        mCheckListViewItem.getEditText().setSelection(contentCursorPosition + taggingResult.first.length()
            + 1);
      } else {
        title.append(" " + taggingResult.first);
      }
    } else {
      sb = new StringBuilder(getNoteContent());
      if (content.hasFocus()) {
        sb.insert(contentCursorPosition, " " + taggingResult.first + " ");
        content.setText(sb.toString());
        content.setSelection(contentCursorPosition + taggingResult.first.length() + 1);
      } else {
        if (getNoteContent().trim().length() > 0) {
          sb.append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"));
        }
        sb.append(taggingResult.first);
        content.setText(sb.toString());
      }
    }

    // Removes unchecked tags
    if (!taggingResult.second.isEmpty()) {
      if (noteTmp.isChecklist()) {
        toggleChecklist2(true, true);
      }
      Pair<String, String> titleAndContent = TagsHelper.removeTag(getNoteTitle(), getNoteContent(),
          taggingResult.second);
      title.setText(titleAndContent.first);
      content.setText(titleAndContent.second);
      if (noteTmp.isChecklist()) {
        toggleChecklist2();
      }
    }
  }

  private int getCursorIndex () {
    if (!noteTmp.isChecklist()) {
      return content.getSelectionStart();
    } else {
      CheckListViewItem mCheckListViewItem = mChecklistManager.getFocusedItemView();
      if (mCheckListViewItem != null) {
        return mCheckListViewItem.getEditText().getSelectionStart();
      } else {
        return 0;
      }
    }
  }

  /**
   * Used to check currently opened note from activity to avoid openind multiple times the same one
   */
  public Note getCurrentNote () {
    return note;
  }

  private boolean isNoteLocationValid () {
    return noteTmp.getLatitude() != null
        && noteTmp.getLatitude() != 0
        && noteTmp.getLongitude() != null
        && noteTmp.getLongitude() != 0;
  }

  public void startGetContentAction () {
    Intent filesIntent;
    filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
    filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
    filesIntent.setType("*/*");
    startActivityForResult(filesIntent, FILES);
  }

  private void askReadExternalStoragePermission () {
    PermissionsHelper.requestPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE,
        R.string.permission_external_storage_detail_attachment,
        snackBarPlaceholder, this::startGetContentAction);
  }

  public void onEventMainThread (PushbulletReplyEvent pushbulletReplyEvent) {
    String text = getNoteContent() + System.getProperty("line.separator") + pushbulletReplyEvent.message;
    content.setText(text);
  }

  private static class OnGeoUtilResultListenerImpl implements OnGeoUtilResultListener {

    private final WeakReference<MainActivity> mainActivityWeakReference;
    private final WeakReference<DetailFragment> detailFragmentWeakReference;
    private final WeakReference<Note> noteTmpWeakReference;

    OnGeoUtilResultListenerImpl (MainActivity activity, DetailFragment mFragment, Note noteTmp) {
      mainActivityWeakReference = new WeakReference<>(activity);
      detailFragmentWeakReference = new WeakReference<>(mFragment);
      noteTmpWeakReference = new WeakReference<>(noteTmp);
    }

    @Override
    public void onAddressResolved (String address) {
      // Nothing to do
    }

    @Override
    public void onCoordinatesResolved (Location location, String address) {
      // Nothing to do
    }

    @Override
    public void onLocationUnavailable () {
      mainActivityWeakReference.get().showMessage(R.string.location_not_found, ONStyle.ALERT);
    }

    @Override
    public void onLocationRetrieved (Location location) {

      if (!checkWeakReferences()) {
        return;
      }

      if (location == null) {
        return;
      }
      if (!ConnectionManager.internetAvailable(mainActivityWeakReference.get())) {
        noteTmpWeakReference.get().setLatitude(location.getLatitude());
        noteTmpWeakReference.get().setLongitude(location.getLongitude());
        onAddressResolved("");
        return;
      }
      LayoutInflater inflater = mainActivityWeakReference.get().getLayoutInflater();
      View v = inflater.inflate(R.layout.dialog_location, null);
      final AutoCompleteTextView autoCompView = v.findViewById(R.id
          .auto_complete_location);
      autoCompView.setHint(mainActivityWeakReference.get().getString(R.string.search_location));
      autoCompView.setAdapter(new PlacesAutoCompleteAdapter(mainActivityWeakReference.get(), R.layout
          .simple_text_layout));
      final MaterialDialog dialog = new MaterialDialog.Builder(mainActivityWeakReference.get())
          .customView(autoCompView, false)
          .positiveText(R.string.use_current_location)
          .onPositive((dialog1, which) -> {
            if (TextUtils.isEmpty(autoCompView.getText().toString())) {
              noteTmpWeakReference.get().setLatitude(location.getLatitude());
              noteTmpWeakReference.get().setLongitude(location.getLongitude());
              GeocodeHelper.getAddressFromCoordinates(location, detailFragmentWeakReference.get());
            } else {
              GeocodeHelper.getCoordinatesFromAddress(autoCompView.getText().toString(),
                  detailFragmentWeakReference.get());
            }
          })
          .build();
      autoCompView.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after) {
          // Nothing to do
        }

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {
          if (s.length() != 0) {
            dialog.setActionButton(DialogAction.POSITIVE, mainActivityWeakReference.get().getString(R
                .string.confirm));
          } else {
            dialog.setActionButton(DialogAction.POSITIVE, mainActivityWeakReference.get().getString(R
                .string
                .use_current_location));
          }
        }

        @Override
        public void afterTextChanged (Editable s) {
          // Nothing to do
        }
      });
      dialog.show();
    }

    private boolean checkWeakReferences () {
      return mainActivityWeakReference.get() != null && !mainActivityWeakReference.get().isFinishing()
          && detailFragmentWeakReference.get() != null && noteTmpWeakReference.get() != null;
    }
  }

  /**
   * Manages clicks on attachment dialog
   */
  @SuppressLint("InlinedApi")
  private class AttachmentOnClickListener implements OnClickListener {

    @Override
    public void onClick (View v) {

      switch (v.getId()) {
        // Photo from camera
        case R.id.camera:
          takePhoto();
          break;
        case R.id.recording:
          if (!isRecording) {
            startRecording(v);
          } else {
            stopRecording();
            Attachment attachment = new Attachment(Uri.fromFile(new File(recordName)), MIME_TYPE_AUDIO);
            attachment.setLength(audioRecordingTime);
            addAttachment(attachment);
            mAttachmentAdapter.notifyDataSetChanged();
            mGridView.autoresize();
          }
          break;
        case R.id.video:
          takeVideo();
          break;
        case R.id.files:
          if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
              PackageManager.PERMISSION_GRANTED) {
            startGetContentAction();
          } else {
            askReadExternalStoragePermission();
          }
          break;
        case R.id.sketch:
          takeSketch(null);
          break;
        case R.id.location:
          displayLocationDialog();
          break;
        case R.id.timestamp:
          addTimestamp();
          break;
        case R.id.pushbullet:
          MessagingExtension.mirrorMessage(mainActivity, getString(R.string.app_name),
              getString(R.string.pushbullet),
              getNoteContent(), BitmapFactory.decodeResource(getResources(),
                  R.drawable.ic_stat_literal_icon),
              null, 0);
          break;
        default:
          LogDelegate.e("Wrong element choosen: " + v.getId());
      }
      if (!isRecording) {
        attachmentDialog.dismiss();
      }
    }
  }
}




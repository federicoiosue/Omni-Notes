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
package it.feio.android.omninotes;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
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
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.neopixl.pixlui.components.edittext.EditText;
import com.neopixl.pixlui.components.textview.TextView;
import com.pushbullet.android.extension.MessagingExtension;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.checklistview.ChecklistManager;
import it.feio.android.checklistview.exceptions.ViewNotSupportedException;
import it.feio.android.checklistview.interfaces.CheckListChangedListener;
import it.feio.android.checklistview.models.CheckListViewItem;
import it.feio.android.omninotes.async.AttachmentTask;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.async.notes.SaveNoteTask;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.*;
import it.feio.android.omninotes.models.adapters.AttachmentAdapter;
import it.feio.android.omninotes.models.adapters.NavDrawerCategoryAdapter;
import it.feio.android.omninotes.models.adapters.PlacesAutoCompleteAdapter;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;
import it.feio.android.omninotes.models.listeners.OnNoteSaved;
import it.feio.android.omninotes.models.listeners.OnReminderPickedListener;
import it.feio.android.omninotes.models.views.ExpandableHeightGridView;
import it.feio.android.omninotes.utils.*;
import it.feio.android.omninotes.utils.Display;
import it.feio.android.omninotes.utils.date.DateHelper;
import it.feio.android.omninotes.utils.date.ReminderPickers;
import it.feio.android.pixlui.links.TextLinkClickListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;


public class DetailFragment extends BaseFragment implements
        OnReminderPickedListener, TextLinkClickListener, OnTouchListener,
        OnGlobalLayoutListener, OnAttachingFileListener, TextWatcher, CheckListChangedListener, OnNoteSaved, 
        OnGeoUtilResultListener {

    private static final int TAKE_PHOTO = 1;
    private static final int TAKE_VIDEO = 2;
    private static final int SET_PASSWORD = 3;
    private static final int SKETCH = 4;
    private static final int TAG = 5;
    private static final int DETAIL = 6;
    private static final int FILES = 7;

    @InjectView(R.id.detail_root) ViewGroup root;
    @InjectView(R.id.detail_title) EditText title;
    @InjectView(R.id.detail_content) EditText content;
    @InjectView(R.id.gridview) ExpandableHeightGridView mGridView;
    @InjectView(R.id.location) TextView locationTextView;
    @InjectView(R.id.detail_timestamps) View timestampsView;
    @InjectView(R.id.reminder_layout) LinearLayout reminder_layout;
    @InjectView(R.id.datetime) TextView datetime;
    @InjectView(R.id.detail_tile_card) View titleCardView;
    @InjectView(R.id.content_wrapper) ScrollView scrollView;
    @InjectView(R.id.creation) TextView creationTextView;
    @InjectView(R.id.last_modification) TextView lastModificationTextView;
    @InjectView(R.id.title_wrapper) View titleWrapperView;
    @InjectView(R.id.tag_marker) View tagMarkerView;
    @InjectView(R.id.detail_wrapper) ViewManager detailWrapperView;

    public OnDateSetListener onDateSetListener;
    public OnTimeSetListener onTimeSetListener;
    public boolean goBack = false;
    MediaRecorder mRecorder = null;
    View toggleChecklistView;
    private Uri attachmentUri;
    private AttachmentAdapter mAttachmentAdapter;
    private PopupWindow attachmentDialog;
    private Note note;
    private Note noteTmp;
    private Note noteOriginal;
    // Audio recording
    private String recordName;
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
    private boolean swiping;
    private int startSwipeX;
    private SharedPreferences prefs;
    private boolean onCreateOptionsMenuAlreadyCalled = false;
    private View keyboardPlaceholder;
    private boolean orientationChanged;
    private long audioRecordingTimeStart;
    private long audioRecordingTime;
    private DetailFragment mFragment;
    private Attachment sketchEdited;
    private int contentLineCounter = 1;
    private int contentCursorPosition;
    private ArrayList<Integer> mergedNotesIds = new ArrayList<>();
    private MainActivity mainActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = this;
    }


    @Override
    public void onStart() {
        // GA tracking
        OmniNotes.getGaTracker().set(Fields.SCREEN_NAME, getClass().getName());
        OmniNotes.getGaTracker().send(MapBuilder.createAppView().build());
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
        // Adding a layout observer to perform calculus when showing keyboard
        if (root != null) {
            root.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.inject(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mainActivity = (MainActivity) getActivity();

        prefs = mainActivity.prefs;

        mainActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainActivity.getToolbar().setNavigationOnClickListener(v -> navigateUp());

        // Force the navigation drawer to stay closed
        mainActivity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
            Attachment mAttachment = new Attachment(mainActivity.sketchUri, Constants.MIME_TYPE_SKETCH);
            noteTmp.getAttachmentsList().add(mAttachment);
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
    public void onSaveInstanceState(Bundle outState) {

        // Must be restored to re-fill title EditText
        restoreLayouts();

        noteTmp.setTitle(getNoteTitle());
        noteTmp.setContent(getNoteContent());
        outState.putParcelable("noteTmp", noteTmp);
        outState.putParcelable("note", note);
        outState.putParcelable("noteOriginal", noteOriginal);
        outState.putParcelable("attachmentUri", attachmentUri);
        outState.putBoolean("orientationChanged", orientationChanged);
        super.onSaveInstanceState(outState);
    }


    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public void onPause() {
        super.onPause();

        // Checks "goBack" value to avoid performing a double saving
//        if (!goBack) {
//            saveNote(this);
//        }

        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        // Unregistering layout observer
        if (root != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
                root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        }

        // Closes keyboard on exit
        if (toggleChecklistView != null) {
            KeyboardUtils.hideKeyboard(toggleChecklistView);
            content.clearFocus();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation != newConfig.orientation) {
            orientationChanged = true;
        }
    }


    private void init() {

        // Handling of Intent actions
        handleIntents();

        if (noteOriginal == null) {
            noteOriginal = getArguments().getParcelable(Constants.INTENT_NOTE);
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
    private void checkNoteLock(Note note) {
        // If note is locked security password will be requested
        if (note.isLocked()
                && prefs.getString(Constants.PREF_PASSWORD, null) != null
                && !prefs.getBoolean("settings_password_access", false)) {
            BaseActivity.requestPassword(mainActivity, passwordConfirmed -> {
				if (passwordConfirmed) {
					noteTmp.setPasswordChecked(true);
					init();
				} else {
					goBack = true;
					goHome();
				}
			});
        } else {
            noteTmp.setPasswordChecked(true);
            init();
        }
    }


    private void handleIntents() {
        Intent i = mainActivity.getIntent();

        if (Constants.ACTION_MERGE.equals(i.getAction())) {
            noteOriginal = new Note();
            note = new Note(noteOriginal);
            noteTmp = getArguments().getParcelable(Constants.INTENT_NOTE);
            if (i.getIntegerArrayListExtra("merged_notes") != null) {
                mergedNotesIds = i.getIntegerArrayListExtra("merged_notes");
            }
            i.setAction(null);
        }

        // Action called from home shortcut
        if (Constants.ACTION_SHORTCUT.equals(i.getAction())
                || Constants.ACTION_NOTIFICATION_CLICK.equals(i.getAction())) {
            afterSavedReturnsToList = false;
            noteOriginal = DbHelper.getInstance().getNote(i.getIntExtra(Constants.INTENT_KEY, 0));
            // Checks if the note pointed from the shortcut has been deleted
            if (noteOriginal == null) {
                mainActivity.showToast(getText(R.string.shortcut_note_deleted), Toast.LENGTH_LONG);
                mainActivity.finish();
            }
            note = new Note(noteOriginal);
            noteTmp = new Note(noteOriginal);
            i.setAction(null);
        }

        // Check if is launched from a widget
        if (Constants.ACTION_WIDGET.equals(i.getAction())
                || Constants.ACTION_TAKE_PHOTO.equals(i.getAction())) {

            afterSavedReturnsToList = false;

            //  with tags to set tag
            if (i.hasExtra(Constants.INTENT_WIDGET)) {
                String widgetId = i.getExtras().get(Constants.INTENT_WIDGET).toString();
                if (widgetId != null) {
                    String sqlCondition = prefs.getString(Constants.PREF_WIDGET_PREFIX + widgetId, "");
                    String categoryId = TextHelper.checkIntentCategory(sqlCondition);
                    if (categoryId != null) {
                        Category category;
                        try {
                            category = DbHelper.getInstance().getCategory(Integer.parseInt(categoryId));
                            noteTmp = new Note();
                            noteTmp.setCategory(category);
                        } catch (NumberFormatException e) {
                            Log.e(Constants.TAG, "Category with not-numeric value!", e);
                        }
                    }
                }
            }

            // Sub-action is to take a photo
            if (Constants.ACTION_TAKE_PHOTO.equals(i.getAction())) {
                takePhoto();
            }

            i.setAction(null);
        }


        /**
         * Handles third party apps requests of sharing
         */
        if ((Intent.ACTION_SEND.equals(i.getAction())
                || Intent.ACTION_SEND_MULTIPLE.equals(i.getAction())
                || Constants.INTENT_GOOGLE_NOW.equals(i.getAction()))
                && i.getType() != null) {

            afterSavedReturnsToList = false;

            if (noteTmp == null) noteTmp = new Note();

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

            // Single attachment data
            Uri uri = i.getParcelableExtra(Intent.EXTRA_STREAM);
            // Due to the fact that Google Now passes intent as text but with
            // audio recording attached the case must be handled in specific way
            if (uri != null && !Constants.INTENT_GOOGLE_NOW.equals(i.getAction())) {
                String name = FileHelper.getNameFromUri(mainActivity, uri);
                AttachmentTask task = new AttachmentTask(this, uri, name, this);
                task.execute();
            }

            // Multiple attachment data
            ArrayList<Uri> uris = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uris != null) {
                for (Uri uriSingle : uris) {
                    String name = FileHelper.getNameFromUri(mainActivity, uriSingle);
                    AttachmentTask task = new AttachmentTask(this, uriSingle, name, this);
                    task.execute();
                }
            }

            i.setAction(null);
        }

    }


    @SuppressLint("NewApi")
    private void initViews() {

        // Sets onTouchListener to the whole activity to swipe notes
        root.setOnTouchListener(this);

        // Overrides font sizes with the one selected from user
        Fonts.overrideTextSize(mainActivity, prefs, root);

        // Color of tag marker if note is tagged a function is active in preferences
        setTagMarkerColor(noteTmp.getCategory());

        // Sets links clickable in title and content Views
        title = initTitle();
        requestFocus(title);

        content = initContent();

        // Automatic location insertion
        if (prefs.getBoolean(Constants.PREF_AUTO_LOCATION, false) && noteTmp.get_id() == 0) {
            noteTmp.setLatitude(mainActivity.currentLatitude);
            noteTmp.setLongitude(mainActivity.currentLongitude);
        }
        if (isNoteLocationValid()) {
            if (!TextUtils.isEmpty(noteTmp.getAddress())) {
                locationTextView.setVisibility(View.VISIBLE);
                locationTextView.setText(noteTmp.getAddress());
            } else {
                GeocodeHelper.getAddressFromCoordinates(mainActivity, noteTmp.getLatitude(), noteTmp.getLongitude(),
                        mFragment);
            }
        } 

        locationTextView.setOnClickListener(v -> {
            String uriString = "geo:" + noteTmp.getLatitude() + ',' + noteTmp.getLongitude()
                    + "?q=" + noteTmp.getLatitude() + ',' + noteTmp.getLongitude();
            Intent locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            if (!IntentChecker.isAvailable(mainActivity, locationIntent, null)) {
                uriString = "http://maps.google.com/maps?q=" + noteTmp.getLatitude() + ',' + noteTmp.getLongitude();
                locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            }
            startActivity(locationIntent);
        });
        locationTextView.setOnLongClickListener(v -> {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(mainActivity);
            builder.content(R.string.remove_location);
            builder.positiveText(R.string.ok);
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog materialDialog) {
                    noteTmp.setLatitude("");
                    noteTmp.setLongitude("");
                    fade(locationTextView, false);
                }
            });
            MaterialDialog dialog = builder.build();
            dialog.show();
            return true;
        });


        // Some fields can be filled by third party application and are always shown
        mAttachmentAdapter = new AttachmentAdapter(mainActivity, noteTmp.getAttachmentsList(), mGridView);
        mAttachmentAdapter.setOnErrorListener(this);

        // Initialzation of gridview for images
        mGridView.setAdapter(mAttachmentAdapter);
        mGridView.autoresize();

        // Click events for images in gridview (zooms image)
        mGridView.setOnItemClickListener((parent, v, position, id) -> {
            Attachment attachment = (Attachment) parent.getAdapter().getItem(position);
            Uri uri = attachment.getUri();
            Intent attachmentIntent;
            if (Constants.MIME_TYPE_FILES.equals(attachment.getMime_type())) {

                attachmentIntent = new Intent(Intent.ACTION_VIEW);
                attachmentIntent.setDataAndType(uri, StorageHelper.getMimeType(mainActivity,
                        attachment.getUri()));
                attachmentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent
                        .FLAG_GRANT_WRITE_URI_PERMISSION);
                if (IntentChecker.isAvailable(mainActivity.getApplicationContext(), attachmentIntent, null)) {
                    startActivity(attachmentIntent);
                } else {
                    mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.WARN);
                }

                // Media files will be opened in internal gallery
            } else if (Constants.MIME_TYPE_IMAGE.equals(attachment.getMime_type())
                    || Constants.MIME_TYPE_SKETCH.equals(attachment.getMime_type())
                    || Constants.MIME_TYPE_VIDEO.equals(attachment.getMime_type())) {
                // Title
                noteTmp.setTitle(getNoteTitle());
                noteTmp.setContent(getNoteContent());
                String title1 = TextHelper.parseTitleAndContent(mainActivity,
                        noteTmp)[0].toString();
                // Images
                int clickedImage = 0;
                ArrayList<Attachment> images = new ArrayList<>();
                for (Attachment mAttachment : noteTmp.getAttachmentsList()) {
                    if (Constants.MIME_TYPE_IMAGE.equals(mAttachment.getMime_type())
                            || Constants.MIME_TYPE_SKETCH.equals(mAttachment.getMime_type())
                            || Constants.MIME_TYPE_VIDEO.equals(mAttachment.getMime_type())) {
                        images.add(mAttachment);
                        if (mAttachment.equals(attachment)) {
                            clickedImage = images.size() - 1;
                        }
                    }
                }
                // Intent
                attachmentIntent = new Intent(mainActivity, GalleryActivity.class);
                attachmentIntent.putExtra(Constants.GALLERY_TITLE, title1);
                attachmentIntent.putParcelableArrayListExtra(Constants.GALLERY_IMAGES, images);
                attachmentIntent.putExtra(Constants.GALLERY_CLICKED_IMAGE, clickedImage);
                startActivity(attachmentIntent);

            } else if (Constants.MIME_TYPE_AUDIO.equals(attachment.getMime_type())) {
                playback(v, attachment.getUri());
            }

        });

        // Long click events for images in gridview (removes image)
        mGridView.setOnItemLongClickListener((parent, v, position, id) -> {

            // To avoid deleting audio attachment during playback
            if (mPlayer != null) return false;

            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(mainActivity)
                    .positiveText(R.string.delete);

            // If is an image user could want to sketch it!
            if (Constants.MIME_TYPE_SKETCH.equals(mAttachmentAdapter.getItem(position).getMime_type())) {
                dialogBuilder
                        .content(R.string.delete_selected_image)
                        .negativeText(R.string.edit)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog materialDialog) {
                                noteTmp.getAttachmentsList().remove(position);
                                mAttachmentAdapter.notifyDataSetChanged();
                                mGridView.autoresize();
                            }


                            @Override
                            public void onNegative(MaterialDialog materialDialog) {
                                sketchEdited = mAttachmentAdapter.getItem(position);
                                takeSketch(sketchEdited);
                            }
                        });
            } else {
                dialogBuilder
                        .content(R.string.delete_selected_image)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog materialDialog) {
                                noteTmp.getAttachmentsList().remove(position);
                                mAttachmentAdapter.notifyDataSetChanged();
                                mGridView.autoresize();
                            }
                        });
            }
            dialogBuilder.build().show();
            return true;
        });


        // Preparation for reminder icon
        reminder_layout.setOnClickListener(v -> {
			int pickerType = prefs.getBoolean("settings_simple_calendar", false) ? ReminderPickers.TYPE_AOSP :
					ReminderPickers.TYPE_GOOGLE;
			ReminderPickers reminderPicker = new ReminderPickers(mainActivity, mFragment, pickerType);
			Long presetDateTime = noteTmp.getAlarm() != null ? Long.parseLong(noteTmp.getAlarm()) : null;
			reminderPicker.pick(presetDateTime, noteTmp.getRecurrenceRule());
			onDateSetListener = reminderPicker;
			onTimeSetListener = reminderPicker;
		});


        reminder_layout.setOnLongClickListener(v -> {
            MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
                    .content(R.string.remove_reminder)
                    .positiveText(R.string.ok)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog materialDialog) {
                            noteTmp.setAlarm(null);
                            datetime.setText("");
                        }
                    }).build();
            dialog.show();
            return true;
        });

        // Reminder
        datetime.setText(initReminder(noteTmp));

        // Timestamps view
        // Bottom padding set for translucent navbar in Kitkat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int navBarHeight = Display.getNavigationBarHeightKitkat(mainActivity);
            int negativePadding = navBarHeight >= 27*3 ? - 27 : 0;
            int timestampsViewPaddingBottom = navBarHeight > 0 ? navBarHeight + negativePadding : timestampsView.getPaddingBottom();
            timestampsView.setPadding(timestampsView.getPaddingStart(), timestampsView.getPaddingTop(),
                    timestampsView.getPaddingEnd(), timestampsViewPaddingBottom);
        }

        // Footer dates of creation...
        String creation = noteTmp.getCreationShort(mainActivity);
        creationTextView.append(creation.length() > 0 ? getString(R.string.creation) + " " + creation : "");
        if (creationTextView.getText().length() == 0)
            creationTextView.setVisibility(View.GONE);

        // ... and last modification
        String lastModification = noteTmp.getLastModificationShort(mainActivity);
        lastModificationTextView.append(lastModification.length() > 0 ? getString(R.string.last_update) + " " + lastModification : "");
        if (lastModificationTextView.getText().length() == 0)
            lastModificationTextView.setVisibility(View.GONE);
    }


    private EditText initTitle() {
        title.setText(noteTmp.getTitle());
        title.gatherLinksForText();
        title.setOnTextLinkClickListener(this);
        // To avoid dropping here the  dragged checklist items
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            title.setOnDragListener((v, event) -> {
//					((View)event.getLocalState()).setVisibility(View.VISIBLE);
				return true;
			});
        }
        //When editor action is pressed focus is moved to last character in content field
        title.setOnEditorActionListener((v, actionId, event) -> {
			content.requestFocus();
			content.setSelection(content.getText().length());
			return false;
		});
        return title;
    }


    private EditText initContent() {
        content.setText(noteTmp.getContent());
        content.gatherLinksForText();
        content.setOnTextLinkClickListener(this);
        // Avoids focused line goes under the keyboard
        content.addTextChangedListener(this);

        // Restore checklist
        toggleChecklistView = content;
        if (noteTmp.isChecklist()) {
            noteTmp.setChecklist(false);
            AlphaManager.setAlpha(toggleChecklistView, 0);
            toggleChecklist2();
        }

        return content;
    }


    /**
     * Force focus and shows soft keyboard
     */
    private void requestFocus(final EditText view) {
        if (note.get_id() == 0 && !noteTmp.isChanged(note)) {
            KeyboardUtils.showKeyboard(view);
        }
    }


    /**
     * Colors tag marker in note's title and content elements
     */
    private void setTagMarkerColor(Category tag) {

        String colorsPref = prefs.getString("settings_colors_app", Constants.PREF_COLORS_APP_DEFAULT);

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
                    view.setBackgroundColor(Integer.parseInt(tag.getColor()));
                }
            } else {
                for (View view : target) {
                    view.setBackgroundColor(Color.parseColor("#00000000"));
                }
            }
        }
    }


    @SuppressLint("NewApi")
    private void setAddress() {
        if (!ConnectionManager.internetAvailable(mainActivity)) {
            noteTmp.setLatitude(mainActivity.currentLatitude);
            noteTmp.setLongitude(mainActivity.currentLongitude);
            onAddressResolved("");
            return;
        }
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_location, null);
        final AutoCompleteTextView autoCompView = (AutoCompleteTextView) v.findViewById(R.id.auto_complete_location);
        autoCompView.setHint(getString(R.string.search_location));
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(mainActivity, R.layout.simple_text_layout));
        final MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
                .customView(autoCompView, false)
                .positiveText(R.string.use_current_location)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        if (TextUtils.isEmpty(autoCompView.getText().toString())) {
                            double lat = mainActivity.currentLatitude;
                            double lon = mainActivity.currentLongitude;
                            noteTmp.setLatitude(lat);
                            noteTmp.setLongitude(lon);
                            GeocodeHelper.getAddressFromCoordinates(mainActivity, noteTmp.getLatitude(),
                                    noteTmp.getLongitude(), mFragment);
                        } else {
                            GeocodeHelper.getCoordinatesFromAddress(mainActivity, autoCompView.getText().toString(),
                                    mFragment);
                        }
                    }
                })
                .build();
        autoCompView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    dialog.setActionButton(DialogAction.POSITIVE, getString(R.string.confirm));
                } else {
                    dialog.setActionButton(DialogAction.POSITIVE, getString(R.string.use_current_location));
                }
            }


            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        dialog.show();
    }


    @Override
    public void onAddressResolved(String address) {
        if (TextUtils.isEmpty(address)) {
            if (!isNoteLocationValid()) {
                mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
                return;
            }
            address = noteTmp.getLatitude() + ", " + noteTmp.getLongitude();
        }
        if (GeocodeHelper.notCoordinates(address)) {
            noteTmp.setAddress(address);
        }
        locationTextView.setVisibility(View.VISIBLE);
        locationTextView.setText(address);
        fade(locationTextView, true);
    }


    @Override
    public void onCoordinatesResolved(double[] coords) {
        if (coords != null) {
            noteTmp.setLatitude(coords[0]);
            noteTmp.setLongitude(coords[1]);
            GeocodeHelper.getAddressFromCoordinates(mainActivity, coords[0], coords[1], new OnGeoUtilResultListener() {
                @Override
                public void onAddressResolved(String address) {
                    if (GeocodeHelper.notCoordinates(address)) {
                        noteTmp.setAddress(address);
                    }
                    locationTextView.setVisibility(View.VISIBLE);
                    locationTextView.setText(address);
                    fade(locationTextView, true);
                }


                @Override
                public void onCoordinatesResolved(double[] coords) {
                }
            });
        } else {
            mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // Show instructions on first launch
//        final String instructionName = Constants.PREF_TOUR_PREFIX + "detail";
//        if (AppTourHelper.isStepTurn(mainActivity, instructionName)
//                && !onCreateOptionsMenuAlreadyCalled) {
//            onCreateOptionsMenuAlreadyCalled = true;
//            ArrayList<Integer[]> list = new ArrayList<Integer[]>();
//            list.add(new Integer[]{R.id.menu_attachment, R.string.tour_detailactivity_attachment_title, 
// R.string.tour_detailactivity_attachment_detail, ShowcaseView.ITEM_ACTION_ITEM});
//            list.add(new Integer[]{R.id.menu_category, R.string.tour_detailactivity_action_title, 
// R.string.tour_detailactivity_action_detail, ShowcaseView.ITEM_ACTION_ITEM});
//            list.add(new Integer[]{R.id.datetime, R.string.tour_detailactivity_reminder_title, 
// R.string.tour_detailactivity_reminder_detail, null});
//            list.add(new Integer[]{R.id.detail_title, R.string.tour_detailactivity_links_title, 
// R.string.tour_detailactivity_links_detail, null});
//            list.add(new Integer[]{null, R.string.tour_detailactivity_swipe_title, 
// R.string.tour_detailactivity_swipe_detail, null, -10, Display.getUsableSize(mainActivity).y / 3, 80, 
// Display.getUsableSize(mainActivity).y / 3});
//            list.add(new Integer[]{0, R.string.tour_detailactivity_save_title, 
// R.string.tour_detailactivity_save_detail, ShowcaseView.ITEM_ACTION_HOME});
//            ((MainActivity) mainActivity).showCaseView(list, new OnShowcaseAcknowledged() {
//                @Override
//                public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
//                    prefs.edit().putBoolean(instructionName, true).commit();
//                    discard();
//                }
//            });
//        }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        // Closes search view if left open in List fragment
        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        if (searchMenuItem != null) {
            MenuItemCompat.collapseActionView(searchMenuItem);
        }

        boolean newNote = noteTmp.get_id() == 0;

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
            menu.findItem(R.id.menu_add_shortcut).setVisible(!newNote);
            menu.findItem(R.id.menu_archive).setVisible(!newNote && !noteTmp.isArchived());
            menu.findItem(R.id.menu_unarchive).setVisible(!newNote && noteTmp.isArchived());
            menu.findItem(R.id.menu_trash).setVisible(!newNote);
        }
    }


    @SuppressLint("NewApi")
    public boolean goHome() {
        stopPlaying();

        // The activity has managed a shared intent from third party app and
        // performs a normal onBackPressed instead of returning back to ListActivity
        if (!afterSavedReturnsToList) {
            if (!TextUtils.isEmpty(exitMessage)) {
                mainActivity.showToast(exitMessage, Toast.LENGTH_SHORT);
            }
            mainActivity.finish();
            return true;
        } else {
            if (!TextUtils.isEmpty(exitMessage) && exitCroutonStyle != null) {
                mainActivity.showMessage(exitMessage, exitCroutonStyle);
            }
        }

        // Otherwise the result is passed to ListActivity
        if (mainActivity != null && mainActivity.getSupportFragmentManager() != null) {
            mainActivity.getSupportFragmentManager().popBackStack();
            if (mainActivity.getSupportFragmentManager().getBackStackEntryCount() == 1) {
                mainActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
            if (mainActivity.getDrawerToggle() != null) {
                mainActivity.getDrawerToggle().setDrawerIndicatorEnabled(true);
            }
        }

        if (mainActivity.getSupportFragmentManager().getBackStackEntryCount() == 1) {
            mainActivity.animateBurger(MainActivity.BURGER);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;
            case R.id.menu_attachment:
                showPopup(mainActivity.findViewById(R.id.menu_attachment));
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
                toggleChecklist();
                break;
            case R.id.menu_checklist_off:
                toggleChecklist();
                break;
            case R.id.menu_lock:
                lockNote();
                break;
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
        }
        return super.onOptionsItemSelected(item);
    }


    private void navigateUp() {
        afterSavedReturnsToList = true;
        saveAndExit(this);
    }


    /**
     *
     */
    private void toggleChecklist() {

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

        // Inflate the popup_layout.xml
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_remove_checklist_layout,
                (ViewGroup) getView().findViewById(R.id.layout_root));

        // Retrieves options checkboxes and initialize their values
        final CheckBox keepChecked = (CheckBox) layout.findViewById(R.id.checklist_keep_checked);
        final CheckBox keepCheckmarks = (CheckBox) layout.findViewById(R.id.checklist_keep_checkmarks);
        keepChecked.setChecked(prefs.getBoolean(Constants.PREF_KEEP_CHECKED, true));
        keepCheckmarks.setChecked(prefs.getBoolean(Constants.PREF_KEEP_CHECKMARKS, true));

        new MaterialDialog.Builder(mainActivity)
                .customView(layout, false)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        prefs.edit()
                                .putBoolean(Constants.PREF_KEEP_CHECKED, keepChecked.isChecked())
                                .putBoolean(Constants.PREF_KEEP_CHECKMARKS, keepCheckmarks.isChecked())
                                .apply();
                        toggleChecklist2();
                    }
                }).build().show();
    }


    /**
     * Toggles checklist view
     */
    private void toggleChecklist2() {
        boolean keepChecked = prefs.getBoolean(Constants.PREF_KEEP_CHECKED, true);
        boolean showChecks = prefs.getBoolean(Constants.PREF_KEEP_CHECKMARKS, true);
        toggleChecklist2(keepChecked, showChecks);
    }


    @SuppressLint("NewApi")
    private void toggleChecklist2(final boolean keepChecked, final boolean showChecks) {
        // Get instance and set options to convert EditText to CheckListView
        mChecklistManager = ChecklistManager.getInstance(mainActivity);
        mChecklistManager.setMoveCheckedOnBottom(Integer.valueOf(prefs.getString("settings_checked_items_behavior",
                String.valueOf(it.feio.android.checklistview.Settings.CHECKED_HOLD))));
        mChecklistManager.setShowChecks(true);
        mChecklistManager.setNewEntryHint(getString(R.string.checklist_item_hint));

        // Links parsing options
        mChecklistManager.setOnTextLinkClickListener(mFragment);
        mChecklistManager.addTextChangedListener(mFragment);
        mChecklistManager.setCheckListChangedListener(mFragment);

        // Options for converting back to simple text
        mChecklistManager.setKeepChecked(keepChecked);
        mChecklistManager.setShowChecks(showChecks);

        // Vibration
        mChecklistManager.setDragVibrationEnabled(true);

        // Switches the views
        View newView = null;
        try {
            newView = mChecklistManager.convert(toggleChecklistView);
        } catch (ViewNotSupportedException e) {
            Log.e(Constants.TAG, "Error switching checklist view", e);
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
    private void categorizeNote() {
        // Retrieves all available categories
        final ArrayList<Category> categories = DbHelper.getInstance().getCategories();

        final MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
                .title(R.string.categorize_as)
                .adapter(new NavDrawerCategoryAdapter(mainActivity, categories))
                .positiveText(R.string.add_category)
                .negativeText(R.string.remove_category)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(mainActivity, CategoryActivity.class);
                        intent.putExtra("noHome", true);
                        startActivityForResult(intent, TAG);
                    }


                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        noteTmp.setCategory(null);
                        setTagMarkerColor(null);
                    }
                })
                .build();

        dialog.getListView().setOnItemClickListener((parent, view, position, id) -> {
			noteTmp.setCategory(categories.get(position));
			setTagMarkerColor(categories.get(position));
			dialog.dismiss();
		});

        dialog.show();
    }


    // The method that displays the popup.
    @SuppressWarnings("deprecation")
    private void showPopup(View anchor) {
        DisplayMetrics metrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Inflate the popup_layout.xml
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.attachment_dialog, null);

        // Creating the PopupWindow
        attachmentDialog = new PopupWindow(mainActivity);
        attachmentDialog.setContentView(layout);
        attachmentDialog.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        attachmentDialog.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        attachmentDialog.setFocusable(true);
        attachmentDialog.setOnDismissListener(() -> {
			if (isRecording) {
				isRecording = false;
				stopRecording();
			}
		});

        // Clear the default translucent background
        attachmentDialog.setBackgroundDrawable(new BitmapDrawable());

        // Camera
        android.widget.TextView cameraSelection = (android.widget.TextView) layout.findViewById(R.id.camera);
        cameraSelection.setOnClickListener(new AttachmentOnClickListener());
        // Audio recording
        android.widget.TextView recordingSelection = (android.widget.TextView) layout.findViewById(R.id.recording);
        recordingSelection.setOnClickListener(new AttachmentOnClickListener());
        // Video recording
        android.widget.TextView videoSelection = (android.widget.TextView) layout.findViewById(R.id.video);
        videoSelection.setOnClickListener(new AttachmentOnClickListener());
        // Files
        android.widget.TextView filesSelection = (android.widget.TextView) layout.findViewById(R.id.files);
        filesSelection.setOnClickListener(new AttachmentOnClickListener());
        // Sketch
        android.widget.TextView sketchSelection = (android.widget.TextView) layout.findViewById(R.id.sketch);
        sketchSelection.setOnClickListener(new AttachmentOnClickListener());
        // Location
        android.widget.TextView locationSelection = (android.widget.TextView) layout.findViewById(R.id.location);
        locationSelection.setOnClickListener(new AttachmentOnClickListener());
        // Desktop note with PushBullet
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            android.widget.TextView pushbulletSelection = (android.widget.TextView) layout.findViewById(R.id
                    .pushbullet);
            pushbulletSelection.setVisibility(View.VISIBLE);
            pushbulletSelection.setOnClickListener(new AttachmentOnClickListener());
        }

        try {
            attachmentDialog.showAsDropDown(anchor);
        } catch (Exception e) {
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);

        }
    }


    private void takePhoto() {
        // Checks for camera app available
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (!IntentChecker.isAvailable(mainActivity, intent, new String[]{PackageManager.FEATURE_CAMERA})) {
            mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.ALERT);

            return;
        }
        // Checks for created file validity
        File f = StorageHelper.createNewAttachmentFile(mainActivity, Constants.MIME_TYPE_IMAGE_EXT);
        if (f == null) {
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);
            return;
        }
        // Launches intent
        attachmentUri = Uri.fromFile(f);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, attachmentUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }


    private void takeVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (!IntentChecker.isAvailable(mainActivity, takeVideoIntent, new String[]{PackageManager.FEATURE_CAMERA})) {
            mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.ALERT);

            return;
        }
        // File is stored in custom ON folder to speedup the attachment
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            File f = StorageHelper.createNewAttachmentFile(mainActivity, Constants.MIME_TYPE_VIDEO_EXT);
            if (f == null) {
                mainActivity.showMessage(R.string.error, ONStyle.ALERT);

                return;
            }
            attachmentUri = Uri.fromFile(f);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, attachmentUri);
        }
        String maxVideoSizeStr = "".equals(prefs.getString("settings_max_video_size", 
                "")) ? "0" : prefs.getString("settings_max_video_size", "");
        int maxVideoSize = Integer.parseInt(maxVideoSizeStr);
        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, Long.valueOf(maxVideoSize * 1024 * 1024));
        startActivityForResult(takeVideoIntent, TAKE_VIDEO);
    }


    private void takeSketch(Attachment attachment) {

        File f = StorageHelper.createNewAttachmentFile(mainActivity, Constants.MIME_TYPE_SKETCH_EXT);
        if (f == null) {
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);
            return;
        }
        attachmentUri = Uri.fromFile(f);

        // Forces potrait orientation to this fragment only
        mainActivity.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Fragments replacing
        FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
        mainActivity.animateTransition(transaction, mainActivity.TRANSITION_HORIZONTAL);
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


    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Fetch uri from activities, store into adapter and refresh adapter
        Attachment attachment;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    attachment = new Attachment(attachmentUri, Constants.MIME_TYPE_IMAGE);
                    noteTmp.getAttachmentsList().add(attachment);
                    mAttachmentAdapter.notifyDataSetChanged();
                    mGridView.autoresize();
                    break;
                case TAKE_VIDEO:
                    // Gingerbread doesn't allow custom folder so data are retrieved from intent
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                        attachment = new Attachment(attachmentUri, Constants.MIME_TYPE_VIDEO);
                    } else {
                        attachment = new Attachment(intent.getData(), Constants.MIME_TYPE_VIDEO);
                    }
                    noteTmp.getAttachmentsList().add(attachment);
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
                    attachment = new Attachment(attachmentUri, Constants.MIME_TYPE_SKETCH);
                    noteTmp.getAttachmentsList().add(attachment);
                    mAttachmentAdapter.notifyDataSetChanged();
                    mGridView.autoresize();
                    break;
                case TAG:
                    mainActivity.showMessage(R.string.category_saved, ONStyle.CONFIRM);
                    Category tag = intent.getParcelableExtra("tag");
                    noteTmp.setCategory(tag);
                    setTagMarkerColor(tag);
                    break;
                case DETAIL:
                    mainActivity.showMessage(R.string.note_updated, ONStyle.CONFIRM);
                    break;
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void onActivityResultManageReceivedFiles(Intent intent) {
        List<Uri> uris = new ArrayList<>();
        if (Build.VERSION.SDK_INT < 16 || intent.getClipData() != null) {
            for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
                uris.add(intent.getClipData().getItemAt(i).getUri());
            }
        } else {
            uris.add(intent.getData());
        }
        for (Uri uri : uris) {
            String name = FileHelper.getNameFromUri(mainActivity, uri);
            new AttachmentTask(this, uri, name, this).execute();
        }
    }


    /**
     * Discards changes done to the note and eventually delete new attachments
     */
    @SuppressLint("NewApi")
    private void discard() {
        // Checks if some new files have been attached and must be removed
        if (!noteTmp.getAttachmentsList().equals(note.getAttachmentsList())) {
            for (Attachment newAttachment : noteTmp.getAttachmentsList()) {
                if (!note.getAttachmentsList().contains(newAttachment)) {
                    StorageHelper.delete(mainActivity, newAttachment.getUri().getPath());
                }
            }
        }

        goBack = true;

        if (!noteTmp.equals(noteOriginal)) {
            // Restore original status of the note
            if (noteOriginal.get_id() == 0) {
                mainActivity.deleteNote(noteTmp);
                goHome();
            } else {
                SaveNoteTask saveNoteTask = new SaveNoteTask(this, false);
                if (Build.VERSION.SDK_INT >= 11) {
                    saveNoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, noteOriginal);
                } else {
                    saveNoteTask.execute(noteOriginal);
                }
            }
            MainActivity.notifyAppWidgets(mainActivity);
        } else {
            goHome();
        }
    }


    @SuppressLint("NewApi")
    private void archiveNote(boolean archive) {
        // Simply go back if is a new note
        if (noteTmp.get_id() == 0) {
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
    private void trashNote(boolean trash) {
        // Simply go back if is a new note
        if (noteTmp.get_id() == 0) {
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


    private void deleteNote() {
        new MaterialDialog.Builder(mainActivity)
                .content(R.string.delete_note_confirmation)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        mainActivity.deleteNote(noteTmp);
                        Log.d(Constants.TAG, "Deleted note with id '" + noteTmp.get_id() + "'");
                        mainActivity.showMessage(R.string.note_deleted, ONStyle.ALERT);
                        MainActivity.notifyAppWidgets(mainActivity);
                        EventBus.getDefault().post(new NotesUpdatedEvent());
                        goHome();
                    }
                }).build().show();
    }


    public void saveAndExit(OnNoteSaved mOnNoteSaved) {
        exitMessage = getString(R.string.note_updated);
        exitCroutonStyle = ONStyle.CONFIRM;
        goBack = true;
        saveNote(mOnNoteSaved);
    }


    /**
     * Save new notes, modify them or archive
     */
    void saveNote(OnNoteSaved mOnNoteSaved) {

        // Changed fields
        noteTmp.setTitle(getNoteTitle());
        noteTmp.setContent(getNoteContent());

        // Check if some text or attachments of any type have been inserted or
        // is an empty note
        if (goBack && TextUtils.isEmpty(noteTmp.getTitle()) && TextUtils.isEmpty(noteTmp.getContent())
                && noteTmp.getAttachmentsList().size() == 0) {
            Log.d(Constants.TAG, "Empty note not saved");
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

        // Saving changes to the note
        SaveNoteTask saveNoteTask = new SaveNoteTask(mOnNoteSaved, lastModificationUpdatedNeeded());
        saveNoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, noteTmp);
    }


    /**
     * Checks if nothing is changed to avoid committing if possible (check)
     */
    private boolean saveNotNeeded() {
        if (noteTmp.get_id() == 0 && prefs.getBoolean(Constants.PREF_AUTO_LOCATION, false)) {
            note.setLatitude(noteTmp.getLatitude());
            note.setLongitude(noteTmp.getLongitude());
        }
        return !noteTmp.isChanged(note);
    }


    /**
     * Checks if only tag, archive or trash status have been changed
     * and then force to not update last modification date*
     */
    private boolean lastModificationUpdatedNeeded() {
        note.setCategory(noteTmp.getCategory());
        note.setArchived(noteTmp.isArchived());
        note.setTrashed(noteTmp.isTrashed());
        note.setLocked(noteTmp.isLocked());
        return noteTmp.isChanged(note);
    }


    @Override
    public void onNoteSaved(Note noteSaved) {
        MainActivity.notifyAppWidgets(OmniNotes.getAppContext());
        EventBus.getDefault().post(new NotesUpdatedEvent());
        note = new Note(noteSaved);
        deleteMergedNotes(mergedNotesIds);
        if (goBack) {
            goHome();
        }
    }

    private void deleteMergedNotes(ArrayList<Integer> mergedNotesIds) {
        for (Integer mergedNoteId : mergedNotesIds) {
            Note note = new Note();
            note.set_id(mergedNoteId);
            DbHelper.getInstance().deleteNote(note, true);
        }
    }


    private String getNoteTitle() {
        if (title != null && !TextUtils.isEmpty(title.getText())) {
            return title.getText().toString();
        } else {
            return "";
        }
    }


    private String getNoteContent() {
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
                mChecklistManager.setKeepChecked(true);
                mChecklistManager.setShowChecks(true);
                contentText = mChecklistManager.getText();
            }
        }
        return contentText;
    }


    /**
     * Updates share intent
     */
    private void shareNote() {
        Note sharedNote = new Note(noteTmp);
        sharedNote.setTitle(getNoteTitle());
        sharedNote.setContent(getNoteContent());
        mainActivity.shareNote(sharedNote);
    }


    /**
     * Notes locking with security password to avoid viewing, editing or deleting from unauthorized
     */
    private void lockNote() {
        Log.d(Constants.TAG, "Locking or unlocking note " + note.get_id());

        // If security password is not set yes will be set right now
        if (prefs.getString(Constants.PREF_PASSWORD, null) == null) {
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
        BaseActivity.requestPassword(mainActivity, passwordConfirmed -> {
			if (passwordConfirmed) {
				lockUnlock();
			}
		});
    }


    private void lockUnlock() {
        // Empty password has been set
        if (prefs.getString(Constants.PREF_PASSWORD, null) == null) {
            mainActivity.showMessage(R.string.password_not_set, ONStyle.WARN);

            return;
        }
        // Otherwise locking is performed
        if (noteTmp.isLocked()) {
            mainActivity.showMessage(R.string.save_note_to_lock_it, ONStyle.INFO);
            mainActivity.supportInvalidateOptionsMenu();
        } else {
            mainActivity.showMessage(R.string.save_note_to_lock_it, ONStyle.INFO);
            mainActivity.supportInvalidateOptionsMenu();
        }
        noteTmp.setLocked(!noteTmp.isLocked());
        noteTmp.setPasswordChecked(true);
    }


    /**
     * Used to set actual reminder state when initializing a note to be edited
     */
    private String initReminder(Note note) {
        if (noteTmp.getAlarm() == null) {
            return "";
        }
        long reminder = Long.parseLong(note.getAlarm());
        String rrule = note.getRecurrenceRule();
        if (!TextUtils.isEmpty(rrule)) {
            return DateHelper.getNoteRecurrentReminderText(reminder, rrule);
        } else {
            return DateHelper.getNoteReminderText(reminder);
        }
    }


    /**
     * Audio recordings playback
     */
    private void playback(View v, Uri uri) {
        // Some recording is playing right now
        if (mPlayer != null && mPlayer.isPlaying()) {
            // If the audio actually played is NOT the one from the click view the last one is played
            if (isPlayingView != v) {
                stopPlaying();
                isPlayingView = v;
                startPlaying(uri);
                recordingBitmap = ((BitmapDrawable) ((ImageView) v.findViewById(R.id.gridview_item_picture))
                        .getDrawable()).getBitmap();
                ((ImageView) v.findViewById(R.id.gridview_item_picture)).setImageBitmap(ThumbnailUtils
                        .extractThumbnail(BitmapFactory.decodeResource(mainActivity.getResources(),
                                R.drawable.stop), Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE));
                // Otherwise just stops playing
            } else {
                stopPlaying();
            }
            // If nothing is playing audio just plays
        } else {
            isPlayingView = v;
            startPlaying(uri);
            Drawable d = ((ImageView) v.findViewById(R.id.gridview_item_picture)).getDrawable();
            if (BitmapDrawable.class.isAssignableFrom(d.getClass())) {
                recordingBitmap = ((BitmapDrawable) d).getBitmap();
            } else {
                recordingBitmap = ((GlideBitmapDrawable)d.getCurrent()).getBitmap();
            }
            ((ImageView) v.findViewById(R.id.gridview_item_picture)).setImageBitmap(ThumbnailUtils.extractThumbnail
                    (BitmapFactory.decodeResource(mainActivity.getResources(), R.drawable.stop),
                            Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE));
        }
    }


    private void startPlaying(Uri uri) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mainActivity, uri);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(mp -> {
				mPlayer = null;
				((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture)).setImageBitmap
						(recordingBitmap);
				recordingBitmap = null;
				isPlayingView = null;
			});
        } catch (IOException e) {
            Log.e(Constants.TAG, "prepare() failed");
        }
    }


    private void stopPlaying() {
        if (mPlayer != null) {
            ((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture)).setImageBitmap(recordingBitmap);
            isPlayingView = null;
            recordingBitmap = null;
            mPlayer.release();
            mPlayer = null;
        }
    }


    private void startRecording() {
        File f = StorageHelper.createNewAttachmentFile(mainActivity, Constants.MIME_TYPE_AUDIO_EXT);
        if (f == null) {
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);

            return;
        }
        recordName = f.getAbsolutePath();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioEncodingBitRate(16);
        mRecorder.setOutputFile(recordName);

        try {
            mRecorder.prepare();
            audioRecordingTimeStart = Calendar.getInstance().getTimeInMillis();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(Constants.TAG, "prepare() failed");
        }
    }


    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            audioRecordingTime = Calendar.getInstance().getTimeInMillis() - audioRecordingTimeStart;
            mRecorder.release();
            mRecorder = null;
        }
    }


    private void fade(final View v, boolean fadeIn) {

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
                public void onAnimationStart(Animation animation) {
                    // Nothind to do
                }


                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Nothind to do
                }


                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(visibility);
                }
            });
            v.startAnimation(mAnimation);
        }
    }


    /**
     * Adding shortcut on Home screen
     */
    private void addShortcut() {
        ShortcutHelper.addShortcut(OmniNotes.getAppContext(), noteTmp);
        mainActivity.showMessage(R.string.shortcut_added, ONStyle.INFO);
    }


    /* (non-Javadoc)
     * @see com.neopixl.pixlui.links.TextLinkClickListener#onTextLinkClick(android.view.View, java.lang.String, 
     * * java.lang.String)
     *
     * Receives onClick from links in EditText and shows a dialog to open link or copy content
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onTextLinkClick(View view, final String clickedString, final String url) {
        new MaterialDialog.Builder(mainActivity)
                .content(clickedString)
                .positiveText(R.string.open)
                .negativeText(R.string.copy)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
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
                    }


                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                                mainActivity
                                        .getSystemService(Activity.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("text label",
                                clickedString);
                        clipboard.setPrimaryClip(clip);
                    }
                }).build().show();
    }


    @SuppressLint("NewApi")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                Log.v(Constants.TAG, "MotionEvent.ACTION_DOWN");
                int w;

                Point displaySize = Display.getUsableSize(mainActivity);
                w = displaySize.x;

                if (x < Constants.SWIPE_MARGIN || x > w - Constants.SWIPE_MARGIN) {
                    swiping = true;
                    startSwipeX = x;
                }

                break;

            case MotionEvent.ACTION_UP:
                Log.v(Constants.TAG, "MotionEvent.ACTION_UP");
                if (swiping)
                    swiping = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (swiping) {
                    Log.v(Constants.TAG, "MotionEvent.ACTION_MOVE at position " + x + ", " + y);
                    if (Math.abs(x - startSwipeX) > Constants.SWIPE_OFFSET) {
                        swiping = false;
                        FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
                        mainActivity.animateTransition(transaction, mainActivity.TRANSITION_VERTICAL);
                        DetailFragment mDetailFragment = new DetailFragment();
                        Bundle b = new Bundle();
                        b.putParcelable(Constants.INTENT_NOTE, new Note());
                        mDetailFragment.setArguments(b);
                        transaction.replace(R.id.fragment_container, mDetailFragment, 
                                mainActivity.FRAGMENT_DETAIL_TAG).addToBackStack(mainActivity
                                .FRAGMENT_DETAIL_TAG).commit();
                    }
                }
                break;
        }

        return true;
    }


    @Override
    public void onGlobalLayout() {
        int screenHeight = Display.getUsableSize(mainActivity).y;
        int heightDiff = screenHeight - Display.getVisibleSize(root).y;
        boolean keyboardVisible = heightDiff > 150;
        if (keyboardVisible && keyboardPlaceholder == null) {
            shrinkLayouts(heightDiff);
        } else if (!keyboardVisible && keyboardPlaceholder != null) {
            restoreLayouts();
        }
    }


    private void shrinkLayouts(int heightDiff) {
        int heightDiffLocal = heightDiff;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && !title.hasFocus()) {
            detailWrapperView.removeView(titleCardView);
            heightDiffLocal -= Display.getStatusBarHeight(mainActivity);
            if (orientationChanged) {
                orientationChanged = false;
                heightDiffLocal -= Display.getActionbarHeight(mainActivity);
            }
        }
        detailWrapperView.removeView(timestampsView);
        keyboardPlaceholder = new View(mainActivity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            root.addView(keyboardPlaceholder, LinearLayout.LayoutParams.MATCH_PARENT, heightDiffLocal);
        }
    }


    private void restoreLayouts() {
        if (root != null) {
            ViewGroup wrapper = (ViewGroup) root.findViewById(R.id.detail_wrapper);
            if (root.indexOfChild(keyboardPlaceholder) != -1) {
                root.removeView(keyboardPlaceholder);
            }
            keyboardPlaceholder = null;
            if (wrapper.indexOfChild(titleCardView) == -1) {
                wrapper.addView(titleCardView, 0);
            }
            if (wrapper.indexOfChild(timestampsView) == -1) {
                wrapper.addView(timestampsView);
            }
        }
    }


    @Override
    public void onAttachingFileErrorOccurred(Attachment mAttachment) {
        mainActivity.showMessage(R.string.error_saving_attachments, ONStyle.ALERT);
        if (noteTmp.getAttachmentsList().contains(mAttachment)) {
            noteTmp.getAttachmentsList().remove(mAttachment);
            mAttachmentAdapter.notifyDataSetChanged();
            mGridView.autoresize();
        }
    }


    @Override
    public void onAttachingFileFinished(Attachment mAttachment) {
        noteTmp.getAttachmentsList().add(mAttachment);
        mAttachmentAdapter.notifyDataSetChanged();
        mGridView.autoresize();
    }


    @Override
    public void onReminderPicked(long reminder) {
        noteTmp.setAlarm(reminder);
        if (mFragment.isAdded()) {
            datetime.setText(DateHelper.getNoteReminderText(reminder));
        }
    }

    @Override
    public void onRecurrenceReminderPicked(String recurrenceRule) {
        noteTmp.setRecurrenceRule(recurrenceRule);
        if (!TextUtils.isEmpty(recurrenceRule)) {
            Log.d(Constants.TAG, "Recurrent reminder set: " + recurrenceRule);
            datetime.setText(DateHelper.getNoteRecurrentReminderText(Long.parseLong(noteTmp
                    .getAlarm()), recurrenceRule));
        }
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        scrollContent();
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Nothing to do
    }


    @Override
    public void afterTextChanged(Editable s) {
        // Nothing to do
    }


    @Override
    public void onCheckListChanged() {
        scrollContent();
    }


    private void scrollContent() {
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
    private void addTags() {
        contentCursorPosition = getCursorIndex();

        // Retrieves all available categories
        final List<Tag> tags = TagsHelper.getAllTags(mainActivity);

        // If there is no tag a message will be shown
        if (tags.size() == 0) {
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
				}).build();
        dialog.show();
    }


    private void tagNote(List<Tag> tags, Integer[] selectedTags, Note note) {
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
        if (taggingResult.second.size() > 0) {
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


    private int getCursorIndex() {
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


    public void appendToContentViewText(String text) {
        content.setText(getNoteContent() + System.getProperty("line.separator") + text);
    }


    /**
     * Used to check currently opened note from activity to avoid openind multiple times the same one
     */
    public Note getCurrentNote() {
        return note;
    }


    private boolean isNoteLocationValid() {
        return noteTmp.getLatitude() != null
                && noteTmp.getLatitude() != 0
                && noteTmp.getLongitude() != null
                && noteTmp.getLongitude() != 0;
    }


    /**
     * Manages clicks on attachment dialog
     */
    @SuppressLint("InlinedApi")
    private class AttachmentOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Photo from camera
                case R.id.camera:
                    takePhoto();
                    attachmentDialog.dismiss();
                    break;
                case R.id.recording:
                    if (!isRecording) {
                        isRecording = true;
                        android.widget.TextView mTextView = (android.widget.TextView) v;
                        mTextView.setText(getString(R.string.stop));
                        mTextView.setTextColor(Color.parseColor("#ff0000"));
                        startRecording();
                    } else {
                        isRecording = false;
                        stopRecording();
                        Attachment attachment = new Attachment(Uri.parse(recordName), Constants.MIME_TYPE_AUDIO);
                        attachment.setLength(audioRecordingTime);
                        noteTmp.getAttachmentsList().add(attachment);
                        mAttachmentAdapter.notifyDataSetChanged();
                        mGridView.autoresize();
                        attachmentDialog.dismiss();
                    }
                    break;
                case R.id.video:
                    takeVideo();
                    attachmentDialog.dismiss();
                    break;
                case R.id.files:
                    Intent filesIntent;
                    filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    filesIntent.setType("*/*");
                    startActivityForResult(filesIntent, FILES);
                    attachmentDialog.dismiss();
                    break;
                case R.id.sketch:
                    takeSketch(null);
                    attachmentDialog.dismiss();
                    break;
                case R.id.location:
                    setAddress();
                    attachmentDialog.dismiss();
                    break;
                case R.id.pushbullet:
                    MessagingExtension.mirrorMessage(mainActivity, getString(R.string.app_name), 
                            getString(R.string.pushbullet),
                            getNoteContent(), BitmapFactory.decodeResource(getResources(), 
                                    R.drawable.ic_stat_notification_icon),
                            null, 0);
                    attachmentDialog.dismiss();
                    break;
            }
        }
    }
}




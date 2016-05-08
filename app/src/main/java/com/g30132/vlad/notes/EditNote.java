package com.g30132.vlad.notes;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.orm.SugarContext;

import org.wordpress.android.editor.EditorFragmentAbstract;
import org.wordpress.android.editor.EditorMediaUploadListener;
import org.wordpress.android.editor.ImageSettingsDialogFragment;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.StringUtils;
import org.wordpress.android.util.helpers.MediaFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vlad on 4/23/16.
 */
public class EditNote  extends AppCompatActivity implements EditorFragmentAbstract.EditorFragmentListener {

    public static final String TITLE_PARAM = "TITLE_PARAM";
    public static final String CONTENT_PARAM = "CONTENT_PARAM";
    public static final String DRAFT_PARAM = "DRAFT_PARAM";

    public static final int ADD_MEDIA_ACTIVITY_REQUEST_CODE = 1111;
    public static final int ADD_MEDIA_MAP_SCREENSHOT_ACTIVITY_REQUEST_CODE = 2222;
    public static final int ADD_TAKE_PHOTO_ACTIVITY_REQUEST_CODE = 3333;

    public static final String MEDIA_REMOTE_ID_SAMPLE = "123";

    protected Long noteID;
    protected Note note;
    protected Toolbar toolbar;
    protected Boolean discarded = false;
    protected Intent intent;

    protected String mCurrentPhotoPath;

    private EditorFragmentAbstract mEditorFragment;
    private Map<String, String> mFailedUploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        setupUI();

        SugarContext.init(this);

        intent = getIntent();

        noteID = intent.getLongExtra("noteID", -1);
        if (noteID == -1) {
            note = new Note();
        } else {
            note = Note.findById(Note.class, noteID);
            if (note == null) {
                Toast.makeText(EditNote.this, R.string.note_not_found, Toast.LENGTH_SHORT).show();
                discarded = true;
                finish();
            } else {
                mEditorFragment.setTitle( note.title );
                mEditorFragment.setContent( note.content );
                System.out.println("IN" + note.content);
            }
        }

        mFailedUploads = new HashMap<>();
    }

    protected void setupUI() {
        toolbar = (Toolbar) findViewById(R.id.editToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    protected void saveNote() {
        if (discarded) return;
        if (!validNote()) return;

        String noteContent = mEditorFragment.getContent().toString();
        String noteTitle = mEditorFragment.getTitle().toString();
        if (noteTitle.length() == 0)
            noteTitle = getString(R.string.example_post_title_placeholder);

        System.out.println(mEditorFragment.getContent());

        note.title = noteTitle;
        note.content = noteContent;
        note.save();

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    private boolean validNote() {
        return mEditorFragment.getContent().length() != 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_delete:
                note.delete();
                discarded = true;
                finish();
                return true;
            case R.id.action_save:
                saveNote();
                finish();
                return true;
            case R.id.action_discard:
                discarded = true;
                finish();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_delete, menu);
        return true;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof EditorFragmentAbstract) {
            mEditorFragment = (EditorFragmentAbstract) fragment;
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment =  getFragmentManager()
                .findFragmentByTag(ImageSettingsDialogFragment.IMAGE_SETTINGS_DIALOG_TAG);
        if (fragment != null && fragment.isVisible()) {
            ((ImageSettingsDialogFragment) fragment).dismissFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        saveNote();
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_editor_add_media, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_PICK);

        switch (item.getItemId()) {
            case R.id.editor_add_image:
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent = Intent.createChooser(intent, getString(R.string.select_image));

                startActivityForResult(intent, ADD_MEDIA_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.editor_add_video:
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent = Intent.createChooser(intent, getString(R.string.select_video));

                startActivityForResult(intent, ADD_MEDIA_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.editor_add_map_screenshot:
                startActivityForResult(new Intent(EditNote.this, MapProvider.class), ADD_MEDIA_MAP_SCREENSHOT_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.editor_take_photo:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, ADD_TAKE_PHOTO_ACTIVITY_REQUEST_CODE);
                    }
                }

                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri mediaUri;

        if (resultCode != Activity.RESULT_OK) return;

        if( requestCode != ADD_TAKE_PHOTO_ACTIVITY_REQUEST_CODE)
            if (data == null) {
                return;
            } else {
                mediaUri = data.getData();
            }
        else {
            File f = new File(mCurrentPhotoPath);
            mediaUri = Uri.fromFile(f);
        }

        MediaFile mediaFile = new MediaFile();
        String mediaId = String.valueOf(System.currentTimeMillis());
        mediaFile.setMediaId(mediaId);
        mediaFile.setVideo(mediaUri.toString().contains("video"));

        switch (requestCode) {
            case ADD_MEDIA_ACTIVITY_REQUEST_CODE:
                mEditorFragment.appendMediaFile(mediaFile, mediaUri.toString(), null);

                if (mEditorFragment instanceof EditorMediaUploadListener) {
                    simulateFileUpload(mediaId, getPath(mediaUri));//mediaUri.toString());
                }
                break;
            case ADD_MEDIA_MAP_SCREENSHOT_ACTIVITY_REQUEST_CODE:
                mEditorFragment.appendMediaFile(mediaFile, mediaUri.toString(), null);

                if (mEditorFragment instanceof EditorMediaUploadListener) {
                    simulateFileUpload(mediaId, getPath(mediaUri));//mediaUri.toString());
                }
                break;
            case ADD_TAKE_PHOTO_ACTIVITY_REQUEST_CODE:


                Toast.makeText(EditNote.this, mediaUri.toString(), Toast.LENGTH_SHORT).show();
                mEditorFragment.appendMediaFile(mediaFile, mediaUri.toString(), null);

                if (mEditorFragment instanceof EditorMediaUploadListener) {
                    simulateFileUpload(mediaId, getPath(mediaUri));//mediaUri.toString());
                }
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    @Override
    public void onSettingsClicked() {
        // TODO
    }

    @Override
    public void onAddMediaClicked() {
        // TODO
    }

    @Override
    public void onMediaRetryClicked(String mediaId) {

    }

    @Override
    public void onMediaUploadCancelClicked(String mediaId, boolean delete) {

    }

    @Override
    public void onFeaturedImageChanged(int mediaId) {

    }

    @Override
    public void onVideoPressInfoRequested(String videoId) {

    }

    @Override
    public String onAuthHeaderRequested(String url) {
        return "";
    }

    @Override
    public void onEditorFragmentInitialized() {
        // arbitrary setup
        mEditorFragment.setFeaturedImageSupported(true);
        mEditorFragment.setBlogSettingMaxImageWidth("600");
        mEditorFragment.setDebugModeEnabled(true);

        // get title and content and draft switch
        String title = getIntent().getStringExtra(TITLE_PARAM);
        if ( title == null ) title = "";
        String content = getIntent().getStringExtra(CONTENT_PARAM);
        if ( content == null ) content = "";
        boolean isLocalDraft = getIntent().getBooleanExtra(DRAFT_PARAM, true);
        mEditorFragment.setTitle(title);
        mEditorFragment.setContent(content);
        mEditorFragment.setTitlePlaceholder(getString(R.string.example_post_title_placeholder));
        mEditorFragment.setContentPlaceholder(getString(R.string.example_post_content_placeholder));
        mEditorFragment.setLocalDraft(isLocalDraft);
    }

    @Override
    public void saveMediaFile(MediaFile mediaFile) {
        // TODO
    }

    @Override
    public void onTrackableEvent(EditorFragmentAbstract.TrackableEvent event) {
        AppLog.d(AppLog.T.EDITOR, "Trackable event: " + event);
    }

    private void simulateFileUpload(final String mediaId, final String mediaUrl) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    float count = (float) 1.0;
                    while (count < 1.1) {
                        sleep(100);

                        ((EditorMediaUploadListener) mEditorFragment).onMediaUploadProgress(mediaId, count);

                        count += 0.1;
                    }

                    MediaFile mediaFile = new MediaFile();
                    mediaFile.setMediaId(MEDIA_REMOTE_ID_SAMPLE);
                    mediaFile.setFileURL(mediaUrl);

                    ((EditorMediaUploadListener) mEditorFragment).onMediaUploadSucceeded(mediaId, mediaFile);

                    if (mFailedUploads.containsKey(mediaId)) {
                        mFailedUploads.remove(mediaId);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

}

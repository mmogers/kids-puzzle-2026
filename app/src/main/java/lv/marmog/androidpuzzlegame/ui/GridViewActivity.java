package lv.marmog.androidpuzzlegame.ui;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;

import lv.marmog.androidpuzzlegame.R;
import lv.marmog.androidpuzzlegame.adapter.ImageAdapter;
import lv.marmog.androidpuzzlegame.exception.PuzzleImagesUnavailableException;

public class GridViewActivity extends AppCompatActivity {

    // complexity from complexity activity
    private int piecesIntent;
    private int columnsIntent;
    private int rowsIntent;
    private int userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreState(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        setName();

        Intent getComplexity = getIntent();
        this.piecesIntent = getComplexity.getIntExtra(Extras.PIECES_COUNT, 9);
        this.columnsIntent = getComplexity.getIntExtra(Extras.COLUMNS, 3);
        this.rowsIntent = getComplexity.getIntExtra(Extras.ROWS, 3);
        this.userId = getComplexity.getIntExtra(Extras.USER_ID, 0);
        this.username = getComplexity.getStringExtra(Extras.USERNAME);

        //Button to go to the StartActivity
        FloatingActionButton goHome = findViewById(R.id.go_home);
        goHome.setOnClickListener(v -> goHome());

        // wired before the image-loading early return below, so camera/gallery still work
        // when the built-in pictures can't be loaded
        FloatingActionButton cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(v -> onImageFromCameraClick());
        FloatingActionButton galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(v -> onImageFromGalleryClick());

        Log.w(GridViewActivity.class.getName(), "User id is " + userId);

        GridView grid = findViewById(R.id.grid);
        ImageAdapter imageAdapter;
        try {
            imageAdapter = new ImageAdapter(this);
        } catch (PuzzleImagesUnavailableException e) {
            Log.e(GridViewActivity.class.getName(), "Could not load puzzle images", e);
            Toasts.show(this, "Could not load pictures", Toast.LENGTH_SHORT);
            return;
        }

        grid.setAdapter(imageAdapter);
        grid.setOnItemClickListener((adapterView, view, i, l) -> startPuzzle(Extras.ASSET_NAME, imageAdapter.getFileName(i)));
    }

    // Every way of choosing a picture starts the same puzzle session; only the image source differs.
    // imageExtraKey is Extras.ASSET_NAME for a built-in picture, Extras.CURRENT_PHOTO_URI for camera/gallery.
    private void startPuzzle(String imageExtraKey, String imageRef) {
        Intent intent = new Intent(this, PuzzleActivity.class);
        intent.putExtra(imageExtraKey, imageRef);

        // --- put extra for complexity
        intent.putExtra(Extras.PIECES_COUNT, piecesIntent);
        intent.putExtra(Extras.COLUMNS, columnsIntent);
        intent.putExtra(Extras.ROWS, rowsIntent);

        intent.putExtra(Extras.USER_ID, userId);
        intent.putExtra(Extras.USERNAME, username);
        Log.i(GridViewActivity.class.getName(), "Sent username is " + username);

        startActivity(intent);
        finish();
    }

    //picture from camera
    private static final String STATE_CURRENT_PHOTO_URI = "currentPhotoUri";
    // must survive process death while the camera app is in front, otherwise cameraLauncher's
    // callback can neither open the taken photo nor delete the empty MediaStore row on cancel
    private Uri currentPhotoUri;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    private void saveState(Bundle outState) {
        if (currentPhotoUri != null) {
            outState.putString(STATE_CURRENT_PHOTO_URI, currentPhotoUri.toString());
        }
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        String savedUri = savedInstanceState.getString(STATE_CURRENT_PHOTO_URI);
        currentPhotoUri = savedUri != null ? Uri.parse(savedUri) : null;
    }

    private final ActivityResultLauncher<String> requestReadImagesPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (Boolean.TRUE.equals(granted)) {
                    launchGalleryPicker();
                }
            });

    private final ActivityResultLauncher<String> requestWriteStoragePermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (Boolean.TRUE.equals(granted)) {
                    launchCamera();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onCameraResult);

    private void onCameraResult(ActivityResult result) {
        if (currentPhotoUri == null) {
            return;
        }
        if (result.getResultCode() == RESULT_OK) {
            startPuzzle(Extras.CURRENT_PHOTO_URI, currentPhotoUri.toString());
        }
        else {
            // capture was cancelled: don't leave an empty JPEG_... entry in the user's gallery
            deleteUnusedPhotoEntry(currentPhotoUri);
            currentPhotoUri = null;
        }
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onGalleryResult);

    private void onGalleryResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }
        Uri uri = result.getData().getData();
        // some pickers answer RESULT_OK without a data URI (e.g. result only in ClipData)
        if (uri == null) {
            Log.w(GridViewActivity.class.getName(), "Gallery picker returned RESULT_OK without an image URI");
            Toasts.show(this, "Could not open picture", Toast.LENGTH_SHORT);
            return;
        }
        startPuzzle(Extras.CURRENT_PHOTO_URI, uri.toString());
    }

    private void onImageFromCameraClick() {
        if (new Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(getPackageManager()) == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestWriteStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        else {
            launchCamera();
        }
    }

    private void launchCamera() {
        currentPhotoUri = createImageUri();
        if (currentPhotoUri == null) {
            Toasts.show(this, "Could not create image file", Toast.LENGTH_LONG);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
        try {
            cameraLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            // camera app disappeared/was disabled after onImageFromCameraClick()'s resolveActivity() check
            Log.w(GridViewActivity.class.getName(), "No camera app to handle ACTION_IMAGE_CAPTURE", e);
            deleteUnusedPhotoEntry(currentPhotoUri);
            currentPhotoUri = null;
            Toasts.show(this, "Camera is not available", Toast.LENGTH_SHORT);
        }
    }

    // Removes the MediaStore row created by createImageUri() when no photo ended up in it.
    private void deleteUnusedPhotoEntry(Uri uri) {
        try {
            getContentResolver().delete(uri, null, null);
        } catch (SecurityException e) {
            // best-effort cleanup: an empty gallery entry is harmless, crashing is not
            Log.w(GridViewActivity.class.getName(), "Could not delete unused photo entry " + uri, e);
        }
    }

    private Uri createImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_" + timeStamp + ".jpg");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KidsPuzzle");
        }
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void onImageFromGalleryClick() {
        String readImagesPermission = readImagesPermissionForThisDevice();
        if (ContextCompat.checkSelfPermission(this, readImagesPermission) != PackageManager.PERMISSION_GRANTED) {
            requestReadImagesPermission.launch(readImagesPermission);
        }
        else {
            launchGalleryPicker();
        }
    }

    private static String readImagesPermissionForThisDevice() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    private void launchGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    //Method to go to the StartActivity
    public void goHome() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    private void setName() {
        Intent intent = getIntent();
        String nameString = intent.getStringExtra(Extras.USERNAME);
        Log.i(GridViewActivity.class.getName(), "Name for textview is " + nameString); //rivate static final String TAG
        TextView name = findViewById(R.id.username_gridview);
        name.setText(nameString);
    }
}



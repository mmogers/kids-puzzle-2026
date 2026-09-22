package lv.marmog.androidpuzzlegame.ui;

import static java.lang.Math.abs;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lv.marmog.androidpuzzlegame.R;
import lv.marmog.androidpuzzlegame.adapter.AssetImageLoader;
import lv.marmog.androidpuzzlegame.adapter.ImageAdapter;

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

        Log.w(GridViewActivity.class.getName(), "User id is " + userId);

        AssetImageLoader imageLoader = new AssetImageLoader(getAssets());
        try {
            final String[] files = imageLoader.listImageNames();

            GridView grid = findViewById(R.id.grid);
            grid.setAdapter(new ImageAdapter(this));
            grid.setOnItemClickListener((adapterView, view, i, l) -> {
                Intent intent = new Intent(getApplicationContext(), PuzzleActivity.class);
                intent.putExtra(Extras.ASSET_NAME, files[i % files.length]);

                // --- put extra for complexity
                intent.putExtra(Extras.PIECES_COUNT, piecesIntent);
                intent.putExtra(Extras.COLUMNS, columnsIntent);
                intent.putExtra(Extras.ROWS, rowsIntent);

                intent.putExtra(Extras.USER_ID, userId);
                intent.putExtra(Extras.USERNAME, username);
                Log.i(GridViewActivity.class.getName(), "Sent username is " + username);

                startActivity(intent);
                finish();
            });
        } catch (IOException e) {
            Toasts.show(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT);
        }
    }

    //picture from camera-------------------------------------------------------------------
    private Uri currentPhotoUri;

    private final ActivityResultLauncher<String> requestReadImagesPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchGalleryPicker();
                }
            });

    private final ActivityResultLauncher<String> requestWriteStoragePermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && currentPhotoUri != null) {
                    Intent intent = new Intent(this, PuzzleActivity.class);
                    intent.putExtra(Extras.PIECES_COUNT, piecesIntent);
                    intent.putExtra(Extras.COLUMNS, columnsIntent);
                    intent.putExtra(Extras.ROWS, rowsIntent);
                    intent.putExtra(Extras.USER_ID, userId);
                    intent.putExtra(Extras.USERNAME, username);
                    intent.putExtra(Extras.CURRENT_PHOTO_URI, currentPhotoUri.toString());
                    startActivity(intent);
                    finish();
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    Intent intent = new Intent(this, PuzzleActivity.class);
                    intent.putExtra(Extras.PIECES_COUNT, piecesIntent);
                    intent.putExtra(Extras.COLUMNS, columnsIntent);
                    intent.putExtra(Extras.ROWS, rowsIntent);
                    intent.putExtra(Extras.USER_ID, userId);
                    intent.putExtra(Extras.USERNAME, username);
                    intent.putExtra(Extras.CURRENT_PHOTO_URI, uri.toString());
                    startActivity(intent);
                    finish();
                }
            });

    public void onImageFromCameraClick(View view) {
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
        cameraLauncher.launch(intent);
    }

    private Uri createImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "JPEG_" + timeStamp + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KidsPuzzle");
        }
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void onImageFromGalleryClick(View view) {
        String readImagesPermission = readImagesPermissionForThisDevice();
        if (ContextCompat.checkSelfPermission(this, readImagesPermission) != PackageManager.PERMISSION_GRANTED) {
            requestReadImagesPermission.launch(readImagesPermission);
        }
        else {
            launchGalleryPicker();
        }
    }

    // Android 13 (API 33) replaced READ_EXTERNAL_STORAGE with granular media permissions;
    // apps targeting 33+ must request READ_MEDIA_IMAGES instead for gallery access to work.
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



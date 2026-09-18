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

    //Button to go to the StartActivity
     private FloatingActionButton goHome;

     //picture from camera and gallery --------------------------------------------------------------

    //-------------------------------------------------------------------picture from camera

    // complexity from complexity activity
    int piecesIntent;
    int columnsIntent;
    int rowsIntent;
    int userId;
    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        setName();

        // --- get complexity
        Intent getComplexity = getIntent();

        piecesIntent = getComplexity.getIntExtra(Extras.PIECES_COUNT, 56);
        columnsIntent = getComplexity.getIntExtra(Extras.COLUMNS, 8);
        rowsIntent = getComplexity.getIntExtra(Extras.ROWS, 7);

        userId = getComplexity.getIntExtra(Extras.USER_ID, 0);
        username = getComplexity.getStringExtra(Extras.USERNAME);

        //Button to go to the StartActivity
        goHome = findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });

        Log.w(GridViewActivity.class.getName(), "User id is " + userId);

        // --- /

        AssetImageLoader imageLoader = new AssetImageLoader(getAssets());
        try {
            final String[] files = imageLoader.listImageNames();

            GridView grid = findViewById(R.id.grid);
            grid.setAdapter(new ImageAdapter(this));
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                    Intent intent = new Intent(getApplicationContext(), PuzzleActivity.class);
                    intent.putExtra(Extras.ASSET_NAME, files[i % files.length]);

                    // --- put extra for complexity
                    intent.putExtra(Extras.PIECES_COUNT, piecesIntent);
                    intent.putExtra(Extras.COLUMNS, columnsIntent);
                    intent.putExtra(Extras.ROWS, rowsIntent);

                    intent.putExtra(Extras.USER_ID, userId);
                    intent.putExtra(Extras.USERNAME, username);
                    Log.i(GridViewActivity.class.getName(), "Sent username is " + username);

                    // --- extra for complexity

                    startActivity(intent);
                    finish();


                }
            });
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT);
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

    //clickListner
    public void onImageFromCameraClick(View view) {
        if (new Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(getPackageManager()) == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestWriteStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        currentPhotoUri = createImageUri();
        if (currentPhotoUri == null) {
            Toast.makeText(this, "Could not create image file", Toast.LENGTH_LONG).show();
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
    //-------------------------------------------------------------------picture from camera

    public void onImageFromGalleryClick(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestReadImagesPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            launchGalleryPicker();
        }
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

    public void setName() {
        Intent intent = getIntent();
        String nameString = intent.getStringExtra(Extras.USERNAME);
        Log.i(GridViewActivity.class.getName(), "Name for textview is " + nameString);
        TextView name = (TextView)findViewById(R.id.username_gridview);
        name.setText(nameString);
    }


}



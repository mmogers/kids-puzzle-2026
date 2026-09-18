package lv.marmog.androidpuzzlegame.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.abs;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import lv.marmog.androidpuzzlegame.R;
import lv.marmog.androidpuzzlegame.puzzle.PuzzlePiece;
import lv.marmog.androidpuzzlegame.puzzle.SoundEffects;
import lv.marmog.androidpuzzlegame.puzzle.TouchListener;


public class PuzzleActivity extends AppCompatActivity {
    ArrayList<PuzzlePiece> pieces;

    //floating button  for going to StartActivity
    private FloatingActionButton goHome;

    //picture from camera and gallery-------------------------------------------------------------------
    String mCurrentPhotoUri;
    //-------------------------------------------------------------------picture from camera
    //timer---------------------------------------
    TextView countTimer;
    //------------------------------------timer

    //popup-----------------------------------------------------
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView newTimeIsUpText;
    private Button newTimeIsUpNext;
    //-----------------------------------------------------popup

//  --- array for pieces, cols, rows, id - probably not needed
//    int[] arrayPiecesColsRowsId = new int[4];

    // --- separate variables for pieces, cols, rows, user id - will be initialized with
    // received results from intent extras
    int piecesNumber;
    int cols;
    int rows;
    int userId;
    String username;
    // ---


    //timer----------------------------------------------------------------
    int secondsRemaining = 300;//how many seconds left in timer
    int time;
    CountDownTimer timer = new CountDownTimer(300000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) { //every time the clock ticks
            secondsRemaining--;
            time = 299 - secondsRemaining;
            countTimer.setText(Integer.toString(time) + " secs"); //textView- xml

        }

        @Override
        public void onFinish() {

            createNewContentDialog(); //creates popup window-------------popup-----------
            timer.cancel();
        }
    };
    //---------------------------------------------------------timer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        setName();

        //Button to go to the StartActivity
        goHome = findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });

        //timer-------------------------------------------------------
        //counter initializing
        countTimer = findViewById(R.id.count_timer);
        countTimer.setText("OSec");
        timer.start();//starting timer
        //-------------------------------------------------timer


        final RelativeLayout layout = findViewById(R.id.layout);
        final ImageView imageView = findViewById(R.id.imageView);
        // ---- timer for getting it's height and positioning the pieces
        final TextView timer = findViewById(R.id.count_timer);
        // -------timer

        Intent intent = getIntent();
        final String assetName = intent.getStringExtra(Extras.ASSET_NAME);
        //picture from camera and gallery ------------------------------------------------
        mCurrentPhotoUri = intent.getStringExtra(Extras.CURRENT_PHOTO_URI);
        //-------------------------------------------------picture from camera

        // run image related code after the view was laid out
        // to have all dimensions calculated
        imageView.post(new Runnable() {
            @Override
            public void run() {
                if (assetName != null) {
                    setPicFromAsset(assetName, imageView);
                } else if (mCurrentPhotoUri != null) {
                    setPicFromUri(Uri.parse(mCurrentPhotoUri), imageView);
                }
                pieces = splitImage();
                TouchListener touchListener = new TouchListener(
                        PuzzleActivity.this.getApplicationContext(), PuzzleActivity.this::checkGameOver);

                // shuffle pieces order
                Collections.shuffle(pieces);

                // --- SWITCH FOR COMPLEXITY LEVELS ---

//positioning and size of pieces--------------------------------------------------------------------
                int border = 28; //boarder size
                double ratio = (double) ((layout.getBottom() - imageView.getBottom()) - 2 * border) / imageView.getHeight();
                if (ratio > 1) {
                    ratio = 1;
                }

                switch (pieces.size()) {
                    case 4:
                        layoutPieces(layout, imageView, touchListener, 2, 2, border, ratio);
                        break;

                    case 9:
                        layoutPieces(layout, imageView, touchListener, 3, 3, border, ratio);
                        break;

                    case 12:
                        layoutPieces(layout, imageView, touchListener, 3, 4, border, ratio);
                        break;

                }
            }
        });
    }
//--------------------------------------------------------------------positioning and size of pieces

    private void layoutPieces(RelativeLayout layout, ImageView imageView, TouchListener touchListener,
                               int rows, int columns, int border, double ratio) {
        int marginLeft;
        for (int i = 0; i < rows; i++) {
            marginLeft = border;
            for (int j = 0; j < columns; j++) {
                PuzzlePiece piece = pieces.get((i * columns) + j);
                piece.setOnTouchListener(touchListener);
                layout.addView(piece);

                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) piece.getLayoutParams();

                int percentageHeight = (int) (piece.pieceHeight * 3 / 4 * ratio);
                int percentageWidth = (int) (piece.pieceWidth * 3 / 4 * ratio);
                piece.restingHeight = percentageHeight;
                piece.restingWidth = percentageWidth;
                lParams.height = percentageHeight;
                lParams.width = percentageWidth;
                lParams.leftMargin = marginLeft;
                lParams.topMargin = imageView.getBottom() + i * (int) ((layout.getHeight() - imageView.getBottom() - border) / rows) + border;
                lParams.bottomMargin = border;
                piece.setLayoutParams(lParams);

                marginLeft += (int) (imageView.getWidth() / columns) + (int) border / (columns - 1);
            }
        }
    }


    // --- methods to get number of pieces, columns, rows and id from intent extras
    private int getPiecesNumber() {
        Intent getComplexityFromGridView = getIntent();
        piecesNumber = getComplexityFromGridView.getIntExtra(Extras.PIECES_COUNT, 0);
        return piecesNumber;
    }

    private int getCols() {
        Intent getComplexityFromGridView = getIntent();
        cols = getComplexityFromGridView.getIntExtra(Extras.COLUMNS, 0);
        return cols;
    }

    private int getRows() {
        Intent getComplexityFromGridView = getIntent();
        rows = getComplexityFromGridView.getIntExtra(Extras.ROWS, 0);
        return rows;
    }

    private int getUserId() {
        Intent getComplexityFromGridView = getIntent();
        userId = getComplexityFromGridView.getIntExtra(Extras.USER_ID, 0);
        return userId;
    }

    private String getUsername() {
        Intent getComplexityFromGridView = getIntent();
        username = getComplexityFromGridView.getStringExtra(Extras.USERNAME);
        return username;
    }


    // --- methods to get complexity and id
    public void checkGameOver() {
        if (isGameOver()) {
            timer.cancel(); //stops the timer
            // --- sound on finish
            SoundEffects.play(this, R.raw.cheer);
            // --- /sound

            //we want to do it after 3 seconds
            //3 sec waiting timer---------------------------------------------------------------
            CountDownTimer pauseTimer = new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {

                    piecesNumber = getPiecesNumber();
                    userId = getUserId();
                    username = getUsername();
                    Log.w(PuzzleActivity.class.getName(), "Received level is " + piecesNumber);
                    Log.w(PuzzleActivity.class.getName(), "Received id is " + userId);

                    Intent countIntent = new Intent(getApplicationContext(), ScoreActivity.class);
                    countIntent.putExtra(Extras.TIME, time);//want to transfer final textview with seconds
                    Log.i(PuzzleActivity.class.getName(),"Timer result sent from puzzle activity is " + time);
                    countIntent.putExtra(Extras.USER_ID, userId);
                    countIntent.putExtra(Extras.PIECES_COUNT, piecesNumber);
                    countIntent.putExtra(Extras.USERNAME, username);
                    Log.i(PuzzleActivity.class.getName(), "Sent level is " + piecesNumber);
                    Log.i(PuzzleActivity.class.getName(), "Sent id is " + userId);
                    Log.i(PuzzleActivity.class.getName(), "Sent username is " + username);
                    startActivity(countIntent);//transfers to ScoreActivity
                    finish();

                }
            };
            pauseTimer.start();
            //---------------------------------------------------------------3 sec waiting timer
        }
    }

    private boolean isGameOver() {
        for (PuzzlePiece piece : pieces) {
            if (piece.canMove) {
                return false;
            }
        }

        return true;
    }

    private void setPicFromAsset(String assetName, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        AssetManager am = getAssets();
        try {
            InputStream is = am.open("img/" + assetName);
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            is.reset();

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<PuzzlePiece> splitImage() {

        // --- using separate methods
        piecesNumber = getPiecesNumber();
        cols = getCols();
        rows = getRows();
        // ---

        ImageView imageView = findViewById(R.id.imageView);

        ArrayList<PuzzlePiece> pieces = new ArrayList<>(piecesNumber);

        // Get the scaled bitmap of the source image
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        int[] dimensions = getBitmapPositionInsideImageView(imageView);
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft);
        int croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true);//calls createBitmap(Bitmap source, int 0, int 0, int width, int height), matrix- m, filter , The result will be the same as bitmap buth with sizes scaledBitmapWidth, scaledBitmapHeight
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, abs(scaledBitmapLeft), abs(scaledBitmapTop), croppedImageWidth, croppedImageHeight);//creates bitmap from existing bitmap- scaledbitmap, x, y, width, height

        // Calculate the with and height of the pieces
        int pieceWidth = croppedImageWidth / cols;
        int pieceHeight = croppedImageHeight / rows;

        // Create each bitmap piece and add it to the resulting array
        int yCoord = 0;
        for (int row = 0; row < rows; row++) {
            int xCoord = 0;
            for (int col = 0; col < cols; col++) {
                // calculate offset for each piece
                int offsetX = 0;
                int offsetY = 0;
                if (col > 0) {
                    offsetX = pieceWidth / 3;
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3;
                }

                // apply the offset to each piece
                Bitmap pieceBitmap = Bitmap.createBitmap(croppedBitmap, xCoord - offsetX, yCoord - offsetY, pieceWidth + offsetX, pieceHeight + offsetY);
                PuzzlePiece piece = new PuzzlePiece(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);
                piece.xCoord = xCoord - offsetX + imageView.getLeft();
                piece.yCoord = yCoord - offsetY + imageView.getTop();
                piece.pieceWidth = pieceWidth + offsetX;
                piece.pieceHeight = pieceHeight + offsetY;

                // this bitmap will hold our final puzzle piece image
                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                // draw path
                int bumpSize = pieceHeight / 4;
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (row == 0) {
                    // top side piece
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                } else {
                    // top bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3.0f, offsetY);
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6.0f, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6.0f * 5, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3.0f * 2, offsetY);
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                }

                if (col == cols - 1) {
                    // right side piece
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                } else {
                    // right bump
                    path.lineTo(pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3.0f);
                    path.cubicTo(pieceBitmap.getWidth() - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6.0f, pieceBitmap.getWidth() - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6.0f * 5, pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3.0f * 2);
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                }

                if (row == rows - 1) {
                    // bottom side piece
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                } else {
                    // bottom bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3.0f * 2, pieceBitmap.getHeight());
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6.0f * 5, pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6.0f, pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3.0f, pieceBitmap.getHeight());
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                }

                if (col == 0) {
                    // left side piece
                    path.close();
                } else {
                    // left bump
                    path.lineTo(offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3.0f * 2);
                    path.cubicTo(offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6.0f * 5, offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6.0f, offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3.0f);
                    path.close();
                }

                // mask the piece
                Paint paint = new Paint();
                paint.setColor(0XFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(pieceBitmap, 0, 0, paint);

                // draw a white border
                Paint border = new Paint();
                border.setColor(0X80FFFFFF);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path, border);

                // draw a black border
                border = new Paint();
                border.setColor(0X80000000);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path, border);

                // set the resulting bitmap to the piece
                piece.setImageBitmap(puzzlePiece);

                pieces.add(piece);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }

        return pieces;
    }

    private int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (imgViewH - actH) / 2;
        int left = (imgViewW - actW) / 2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }

    //popup------------------------
    public void createNewContentDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View timeIsUpPopupView = getLayoutInflater().inflate(R.layout.activity_time_is_up, null);
        newTimeIsUpText = timeIsUpPopupView.findViewById(R.id.timeIsUpText);
        newTimeIsUpNext = timeIsUpPopupView.findViewById(R.id.timeIsUpNext);

        dialogBuilder.setView(timeIsUpPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        //Button to go to the HomePage
        goHome = findViewById(R.id.goHome);
        goHome.setOnClickListener(v -> goHome());

        newTimeIsUpNext.setOnClickListener(v -> {
            //define next button
            int userIdToSend = getUserId();
            String usernameToSend = getUsername();
            Intent intent = new Intent(getApplicationContext(), ComplexityActivity.class);
            intent.putExtra(Extras.USER_ID, userIdToSend);
            intent.putExtra(Extras.USERNAME, usernameToSend);
            Log.i(PuzzleActivity.class.getName(), "User id " + userIdToSend + " from time is up popup was sent to complexity");
            startActivity(intent);
            finish();
        });
    }
    //------------------------popup

    //picture from camera and gallery---------------------------------------------------------------------------
    private void setPicFromUri(Uri uri, ImageView imageView) {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        try {
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            try (InputStream boundsStream = getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(boundsStream, new Rect(-1, -1, -1, -1), bmOptions);
            }

            // Determine how much to scale down the image
            int scaleFactor = Math.max(1, Math.min(bmOptions.outWidth / targetW, bmOptions.outHeight / targetH));

            // Decode the image into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap bitmap;
            try (InputStream dataStream = getContentResolver().openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(dataStream, new Rect(-1, -1, -1, -1), bmOptions);
            }

            Bitmap rotatedBitmap = bitmap; //just for rotating

            // rotate bitmap if needed
            try (InputStream exifStream = getContentResolver().openInputStream(uri)) {
                ExifInterface ei = new ExifInterface(exifStream);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;
                }
            }

            imageView.setImageBitmap(rotatedBitmap);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //rotates bitmap to angle degrees
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    //Method to go to the StartActivity
    public void goHome() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    // show username on screen
    public void setName() {
        TextView name = findViewById(R.id.username_puzzle);
        name.setText(getUsername());
    }

}
package lv.marmog.androidpuzzlegame.adapter;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Everything about listing/decoding/scaling puzzle source images that live in assets/img/.
public class AssetImageLoader {
    private static final String IMG_ASSETS_DIR = "img";
    // fixed pool size is an arbitrary small cap so scrolling the grid fast can't spawn unbounded decode threads
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    private final AssetManager assetManager;

    public AssetImageLoader(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public String[] listImageNames() throws IOException {
        return assetManager.list(IMG_ASSETS_DIR);
    }

    // run image related code after the view was laid out
    public void loadInto(ImageView imageView, String fileName) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        imageView.post(() -> EXECUTOR.execute(() -> {
            Bitmap bitmap = decode(imageView, fileName);
            mainHandler.post(() -> imageView.setImageBitmap(bitmap));
        }));
    }

    private Bitmap decode(ImageView imageView, String fileName) {

        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        if (targetW == 0 || targetH == 0) {
            return null;
        }

        try {
            InputStream is = assetManager.open(assetPath(fileName));
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = calculateScaleFactor(photoW, photoH, targetW, targetH);

            is.reset();
            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            return BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static int calculateScaleFactor(int photoWidth, int photoHeight, int targetWidth, int targetHeight) {
        return Math.min(photoWidth / targetWidth, photoHeight / targetHeight);
    }

    private static String assetPath(String fileName) {
        return IMG_ASSETS_DIR + "/" + fileName;
    }
}

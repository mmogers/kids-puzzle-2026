package lv.marmog.androidpuzzlegame.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

 class AssetImageLoaderTest {

    @Test
     void calculateScaleFactor_downscalesToWholeFactor() {
        // photo is exactly twice the target size in both dimensions -> factor 2
        assertEquals(2, AssetImageLoader.calculateScaleFactor(200, 200, 100, 100));
    }

    @Test
     void calculateScaleFactor_usesTheLessAggressiveDimension() {
        // width alone would need a 4x downscale, height only 2x -> the smaller factor wins,
        // so the image isn't shrunk more than the tighter dimension requires
        assertEquals(2, AssetImageLoader.calculateScaleFactor(400, 200, 100, 100));
    }

    @Test
     void calculateScaleFactor_returnsZeroWhenPhotoSmallerThanTarget() {
        // documents an existing quirk: integer division rounds down to 0 when the source
        // photo is smaller than the target view; BitmapFactory treats inSampleSize 0 as 1
        assertEquals(0, AssetImageLoader.calculateScaleFactor(50, 50, 100, 100));
    }
}

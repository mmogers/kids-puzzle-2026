package lv.marmog.androidpuzzlegame.adapter;

import android.content.Context;
import android.content.res.AssetManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import lv.marmog.androidpuzzlegame.exception.PuzzleImagesUnavailableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

 class ImageAdapterTest {

    private ImageAdapter adapter;

    @BeforeEach
     void setUp() throws Exception {
        Context context = mock(Context.class);
        AssetManager assetManager = mock(AssetManager.class);
        when(context.getAssets()).thenReturn(assetManager);
        when(assetManager.list("img")).thenReturn(new String[]{"a.jpg", "b.jpg", "c.jpg"});

        adapter = new ImageAdapter(context);
    }

    @Test
     void getCount_returnsNumberOfAssetFiles() {
        assertEquals(3, adapter.getCount());
    }

    @Test
     void getItem_alwaysReturnsNull() {
        assertNull(adapter.getItem(0));
        assertNull(adapter.getItem(2));
    }

    @Test
     void getItemId_alwaysReturnsZero() {
        assertEquals(0, adapter.getItemId(0));
        assertEquals(0, adapter.getItemId(2));
    }

     @Test
     void getFileName_returnsFileAtPosition() {
         assertEquals("a.jpg", adapter.getFileName(0));
         assertEquals("c.jpg", adapter.getFileName(2));
     }

     @Test
     void constructor_throwsPuzzleImagesUnavailable_whenAssetsCannotBeListed() throws Exception {
         Context context = mock(Context.class);
         AssetManager assetManager = mock(AssetManager.class);
         when(context.getAssets()).thenReturn(assetManager);
         IOException cause = new IOException("boom");
         when(assetManager.list("img")).thenThrow(cause);

         PuzzleImagesUnavailableException e =
                 assertThrows(PuzzleImagesUnavailableException.class, () -> new ImageAdapter(context));
         assertSame(cause, e.getCause());
     }
}

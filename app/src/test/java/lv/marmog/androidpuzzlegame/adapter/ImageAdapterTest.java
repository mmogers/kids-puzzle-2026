package lv.marmog.androidpuzzlegame.adapter;

import android.content.Context;
import android.content.res.AssetManager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImageAdapterTest {

    private ImageAdapter adapter;

    @Before
    public void setUp() throws Exception {
        Context context = mock(Context.class);
        AssetManager assetManager = mock(AssetManager.class);
        when(context.getAssets()).thenReturn(assetManager);
        when(assetManager.list("img")).thenReturn(new String[]{"a.jpg", "b.jpg", "c.jpg"});

        adapter = new ImageAdapter(context);
    }

    @Test
    public void getCount_returnsNumberOfAssetFiles() {
        assertEquals(3, adapter.getCount());
    }

    @Test
    public void getItem_alwaysReturnsNull() {
        assertNull(adapter.getItem(0));
        assertNull(adapter.getItem(2));
    }

    @Test
    public void getItemId_alwaysReturnsZero() {
        assertEquals(0, adapter.getItemId(0));
        assertEquals(0, adapter.getItemId(2));
    }
}

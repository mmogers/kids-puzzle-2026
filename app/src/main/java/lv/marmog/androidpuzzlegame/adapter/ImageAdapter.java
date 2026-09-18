package lv.marmog.androidpuzzlegame.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.IOException;

import lv.marmog.androidpuzzlegame.R;

// Feeds the puzzle-source-image grid on the "choose a picture" screen.
public class ImageAdapter extends BaseAdapter {
    private final Context mContext;
    private final AssetImageLoader imageLoader;
    private final String[] files;

    public ImageAdapter(Context context) {
        this.mContext = context;
        this.imageLoader = new AssetImageLoader(context.getAssets());
        try {
            this.files = imageLoader.listImageNames();
        } catch (IOException e) {
            // fail fast here instead of leaving `files` null and crashing later with a
            // confusing NPE from getCount()/getView() far away from the real cause
            throw new RuntimeException("Could not list puzzle images in assets/img", e);
        }
    }

    @Override
    public int getCount() {
        return files.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_element, null);
        }

        ImageView imageView = convertView.findViewById(R.id.gridImageview);
        // clear any bitmap left over from a recycled cell before the new one loads
        imageView.setImageBitmap(null);
        imageLoader.loadInto(imageView, files[position]);

        return convertView; //image for every file
    }
}

package lv.marmog.androidpuzzlegame.ui;

import android.content.Context;
import android.widget.Toast;

// Shows a short user-facing message. Centralizes Toast.makeText(...).show()
public final class Toasts {

    private Toasts() {
    }

    public static void show(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }
}

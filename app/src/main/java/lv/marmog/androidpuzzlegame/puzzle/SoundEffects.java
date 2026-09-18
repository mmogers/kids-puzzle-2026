package lv.marmog.androidpuzzlegame.puzzle;

import android.content.Context;
import android.media.MediaPlayer;

// Plays a one-shot sound effect and releases the MediaPlayer once it finishes.
// Shared by TouchListener (pick-up/snap sounds) and PuzzleActivity (game-complete sound)
// so that create/start/release isn't duplicated in both places.
public class SoundEffects {

    private SoundEffects() {
    }

    public static void play(Context context, int soundResId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundResId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }
}

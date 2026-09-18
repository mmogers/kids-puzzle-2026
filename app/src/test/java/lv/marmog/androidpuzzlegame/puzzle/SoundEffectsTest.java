package lv.marmog.androidpuzzlegame.puzzle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.media.MediaPlayer;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

// MediaPlayer.create(...) is a static SDK factory method, so it's mocked via Mockito's
// mockStatic (available because this project already relies on Mockito 5's inline mock maker).
public class SoundEffectsTest {

    @Test
    public void play_startsTheCreatedMediaPlayer() {
        Context context = mock(Context.class);
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);

        try (MockedStatic<MediaPlayer> mediaPlayerStatic = mockStatic(MediaPlayer.class)) {
            mediaPlayerStatic.when(() -> MediaPlayer.create(context, 42)).thenReturn(mediaPlayer);

            SoundEffects.play(context, 42);

            verify(mediaPlayer).start();
        }
    }

    @Test
    public void play_releasesTheMediaPlayer_whenPlaybackCompletes() {
        Context context = mock(Context.class);
        MediaPlayer mediaPlayer = mock(MediaPlayer.class);

        try (MockedStatic<MediaPlayer> mediaPlayerStatic = mockStatic(MediaPlayer.class)) {
            mediaPlayerStatic.when(() -> MediaPlayer.create(context, 42)).thenReturn(mediaPlayer);

            SoundEffects.play(context, 42);

            ArgumentCaptor<MediaPlayer.OnCompletionListener> listenerCaptor =
                    ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener.class);
            verify(mediaPlayer).setOnCompletionListener(listenerCaptor.capture());

            // simulate playback finishing, the way Android itself would call the listener
            listenerCaptor.getValue().onCompletion(mediaPlayer);

            verify(mediaPlayer).release();
        }
    }
}

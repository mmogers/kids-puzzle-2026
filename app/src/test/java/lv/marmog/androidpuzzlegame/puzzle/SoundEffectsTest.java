package lv.marmog.androidpuzzlegame.puzzle;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.media.MediaPlayer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

// MediaPlayer.create(...) is a static SDK factory method, so it's mocked via Mockito's
// mockStatic (available because this project already relies on Mockito 5's inline mock maker).
@ExtendWith(MockitoExtension.class)
 class SoundEffectsTest {

    @Mock
    private Context context;
    @Mock
    private MediaPlayer mediaPlayer;

    @Test
     void play_startsTheCreatedMediaPlayer() {
        try (MockedStatic<MediaPlayer> mediaPlayerStatic = mockStatic(MediaPlayer.class)) {
            mediaPlayerStatic.when(() -> MediaPlayer.create(context, 42)).thenReturn(mediaPlayer);

            SoundEffects.play(context, 42);

            verify(mediaPlayer).start();
        }
    }

    @Test
     void play_releasesTheMediaPlayer_whenPlaybackCompletes() {
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

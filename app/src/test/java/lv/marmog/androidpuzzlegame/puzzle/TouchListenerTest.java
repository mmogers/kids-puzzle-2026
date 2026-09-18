package lv.marmog.androidpuzzlegame.puzzle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.junit.Test;
import org.mockito.MockedStatic;

// PuzzlePiece/MotionEvent/ViewGroup are mocked rather than constructed for real - Android View
// constructors need a real windowing environment this JVM-only test doesn't have. Wherever the
// listener plays a sound, MediaPlayer.create(...) is mocked statically too (same technique as
// SoundEffectsTest), otherwise it returns null under the unit-test stub jar and NPEs on start().
public class TouchListenerTest {

    private final Context context = mock(Context.class);
    private final OnPieceSnappedListener listener = mock(OnPieceSnappedListener.class);
    private final TouchListener touchListener = new TouchListener(context, listener);

    private static RelativeLayout.LayoutParams layoutParams(int left, int top, int width, int height) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.leftMargin = left;
        params.topMargin = top;
        params.width = width;
        params.height = height;
        return params;
    }

    private static MotionEvent motionEvent(int action, float rawX, float rawY) {
        MotionEvent event = mock(MotionEvent.class);
        when(event.getAction()).thenReturn(action);
        when(event.getRawX()).thenReturn(rawX);
        when(event.getRawY()).thenReturn(rawY);
        return event;
    }

    @Test
    public void onTouch_ignoresEvent_whenPieceCannotMove() {
        PuzzlePiece piece = mock(PuzzlePiece.class);
        piece.canMove = false;

        boolean handled = touchListener.onTouch(piece, motionEvent(MotionEvent.ACTION_DOWN, 0, 0));

        assertTrue(handled);
        verify(piece, never()).getLayoutParams();
    }

    @Test
    public void actionDown_enlargesPieceAndBringsItToFront() {
        try (MockedStatic<MediaPlayer> mediaPlayerStatic = mockStatic(MediaPlayer.class)) {
            mediaPlayerStatic.when(() -> MediaPlayer.create(any(), anyInt())).thenReturn(mock(MediaPlayer.class));

            PuzzlePiece piece = mock(PuzzlePiece.class);
            piece.canMove = true;
            piece.pieceWidth = 100;
            piece.pieceHeight = 80;
            RelativeLayout.LayoutParams lParams = layoutParams(10, 20, 50, 40);
            when(piece.getLayoutParams()).thenReturn(lParams);

            touchListener.onTouch(piece, motionEvent(MotionEvent.ACTION_DOWN, 15, 25));

            assertEquals(100, lParams.width);
            assertEquals(80, lParams.height);
            verify(piece).bringToFront();
        }
    }

    @Test
    public void actionMove_followsFingerUsingOffsetFromPickUpPoint() {
        try (MockedStatic<MediaPlayer> mediaPlayerStatic = mockStatic(MediaPlayer.class)) {
            mediaPlayerStatic.when(() -> MediaPlayer.create(any(), anyInt())).thenReturn(mock(MediaPlayer.class));

            PuzzlePiece piece = mock(PuzzlePiece.class);
            piece.canMove = true;
            RelativeLayout.LayoutParams lParams = layoutParams(10, 20, 50, 40);
            when(piece.getLayoutParams()).thenReturn(lParams);

            // picked up at (100, 100) while the piece's top-left corner was at (10, 20)
            touchListener.onTouch(piece, motionEvent(MotionEvent.ACTION_DOWN, 100, 100));
            // finger moves to (150, 130)
            touchListener.onTouch(piece, motionEvent(MotionEvent.ACTION_MOVE, 150, 130));

            assertEquals(60, lParams.leftMargin); // 150 - (100 - 10)
            assertEquals(50, lParams.topMargin);  // 130 - (100 - 20)
            verify(piece, times(1)).setLayoutParams(lParams);
        }
    }

    @Test
    public void actionUp_snapsPieceIntoPlace_whenCloseEnoughToTarget() {
        try (MockedStatic<MediaPlayer> mediaPlayerStatic = mockStatic(MediaPlayer.class)) {
            mediaPlayerStatic.when(() -> MediaPlayer.create(any(), anyInt())).thenReturn(mock(MediaPlayer.class));

            PuzzlePiece piece = mock(PuzzlePiece.class);
            piece.canMove = true;
            piece.xCoord = 200;
            piece.yCoord = 300;
            RelativeLayout.LayoutParams lParams = layoutParams(198, 302, 50, 40);
            when(piece.getLayoutParams()).thenReturn(lParams);
            when(piece.getWidth()).thenReturn(90);
            when(piece.getHeight()).thenReturn(60);
            ViewGroup parent = mock(ViewGroup.class);
            when(piece.getParent()).thenReturn(parent);

            touchListener.onTouch(piece, motionEvent(MotionEvent.ACTION_UP, 0, 0));

            assertEquals(200, lParams.leftMargin);
            assertEquals(300, lParams.topMargin);
            assertFalse(piece.canMove);
            verify(parent).removeView(piece);
            verify(parent).addView(piece, 0);
            verify(listener).onPieceSnapped();
        }
    }

    @Test
    public void actionUp_shrinksPieceBackAndDoesNotSnap_whenTooFarFromTarget() {
        PuzzlePiece piece = mock(PuzzlePiece.class);
        piece.canMove = true;
        piece.xCoord = 200;
        piece.yCoord = 300;
        piece.restingWidth = 30;
        piece.restingHeight = 25;
        RelativeLayout.LayoutParams lParams = layoutParams(0, 0, 90, 60);
        when(piece.getLayoutParams()).thenReturn(lParams);
        when(piece.getWidth()).thenReturn(90);
        when(piece.getHeight()).thenReturn(60);

        touchListener.onTouch(piece, motionEvent(MotionEvent.ACTION_UP, 0, 0));

        assertEquals(30, lParams.width);
        assertEquals(25, lParams.height);
        verify(piece).setLayoutParams(lParams);
        verify(listener, never()).onPieceSnapped();
    }
}

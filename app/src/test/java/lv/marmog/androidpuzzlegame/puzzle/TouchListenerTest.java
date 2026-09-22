package lv.marmog.androidpuzzlegame.puzzle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

// PuzzlePiece/MotionEvent/ViewGroup are mocked rather than constructed for real - Android View
// constructors need a real windowing environment this JVM-only test doesn't have.
@ExtendWith(MockitoExtension.class)
 class TouchListenerTest {

    @Mock
    private Context context;
    @Mock
    private OnPieceSnappedListener listener;
    @Mock
    private PuzzlePiece piece;
    @Mock
    private ViewGroup parent;

    @InjectMocks
    private TouchListener touchListener;

    private static RelativeLayout.LayoutParams layoutParams(int left, int top, int width, int height) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.leftMargin = left;
        params.topMargin = top;
        params.width = width;
        params.height = height;
        return params;
    }

    // lenient(): onTouch() returns before reading these when piece.canMove is false
    private static MotionEvent motionEvent(int action, float rawX, float rawY) {
        MotionEvent event = mock(MotionEvent.class);
        lenient().when(event.getAction()).thenReturn(action);
        lenient().when(event.getRawX()).thenReturn(rawX);
        lenient().when(event.getRawY()).thenReturn(rawY);
        return event;
    }

    @Test
     void onTouch_ignoresEvent_whenPieceCannotMove() {
        piece.canMove = false;

        boolean handled = touchListener.onTouch(piece, motionEvent(MotionEvent.ACTION_DOWN, 0, 0));

        assertTrue(handled);
        verify(piece, never()).getLayoutParams();
    }

    @Test
     void actionDown_enlargesPieceAndBringsItToFront() {
        try (MockedStatic<MediaPlayer> mediaPlayerStatic = mockStatic(MediaPlayer.class)) {
            mediaPlayerStatic.when(() -> MediaPlayer.create(any(), anyInt())).thenReturn(mock(MediaPlayer.class));

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
     void actionMove_followsFingerUsingOffsetFromPickUpPoint() {
        try (MockedStatic<MediaPlayer> mediaPlayerStatic = mockStatic(MediaPlayer.class)) {
            mediaPlayerStatic.when(() -> MediaPlayer.create(any(), anyInt())).thenReturn(mock(MediaPlayer.class));

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
     void actionUp_snapsPieceIntoPlace_whenCloseEnoughToTarget() {
        try (MockedStatic<MediaPlayer> mediaPlayerStatic = mockStatic(MediaPlayer.class)) {
            mediaPlayerStatic.when(() -> MediaPlayer.create(any(), anyInt())).thenReturn(mock(MediaPlayer.class));

            piece.canMove = true;
            piece.xCoord = 200;
            piece.yCoord = 300;
            RelativeLayout.LayoutParams lParams = layoutParams(198, 302, 50, 40);
            when(piece.getLayoutParams()).thenReturn(lParams);
            when(piece.getWidth()).thenReturn(90);
            when(piece.getHeight()).thenReturn(60);
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
     void actionUp_shrinksPieceBackAndDoesNotSnap_whenTooFarFromTarget() {
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

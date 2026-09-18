package lv.marmog.androidpuzzlegame.puzzle;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.abs;

import lv.marmog.androidpuzzlegame.R;

public class TouchListener implements View.OnTouchListener {

    private float xDelta;
    private float yDelta;
    private final Context context;
    private final OnPieceSnappedListener listener;

    public TouchListener(Context context, OnPieceSnappedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        PuzzlePiece piece = (PuzzlePiece) view;
        if (!piece.canMove) {
            return true;
        }

        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

        float x = motionEvent.getRawX();
        float y = motionEvent.getRawY();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            handlePickUp(piece, lParams, x, y);
            break;
        case MotionEvent.ACTION_MOVE:
            handleDrag(view, lParams, x, y);
            break;
        case MotionEvent.ACTION_UP:
            handleRelease(piece, lParams);
            break;
        }
        return true;
    }

    // pieces become larger while being dragged, so they're easier to grab and see
    private void enlargeToFullSize(PuzzlePiece piece, RelativeLayout.LayoutParams lParams) {
        lParams.height = piece.pieceHeight;
        lParams.width = piece.pieceWidth;
    }

    // undo enlargeToFullSize() when a piece is dropped somewhere that isn't its correct spot
    private void shrinkToRestingSize(PuzzlePiece piece, RelativeLayout.LayoutParams lParams) {
        lParams.height = piece.restingHeight;
        lParams.width = piece.restingWidth;
        piece.setLayoutParams(lParams);
    }

    private void handlePickUp(PuzzlePiece piece, RelativeLayout.LayoutParams lParams, float x, float y) {
        enlargeToFullSize(piece, lParams);
        xDelta = x - lParams.leftMargin;
        yDelta = y - lParams.topMargin;
        piece.bringToFront();
        SoundEffects.play(context, R.raw.salt_shake);
    }

    private void handleDrag(View view, RelativeLayout.LayoutParams lParams, float x, float y) {
        lParams.leftMargin = (int) (x - xDelta);
        lParams.topMargin = (int) (y - yDelta);
        view.setLayoutParams(lParams);
    }

    private void handleRelease(PuzzlePiece piece, RelativeLayout.LayoutParams lParams) {
        if (!isCloseEnoughToCorrectPosition(piece, lParams)) {
            shrinkToRestingSize(piece, lParams);
            return;
        }

        lParams.leftMargin = piece.xCoord;
        lParams.topMargin = piece.yCoord;

        blinkOnSnap(piece);
        SoundEffects.play(context, R.raw.lighter_flick3);

        piece.setLayoutParams(lParams);
        piece.canMove = false;
        sendViewToBack(piece);
        listener.onPieceSnapped();
    }

    private boolean isCloseEnoughToCorrectPosition(PuzzlePiece piece, RelativeLayout.LayoutParams lParams) {
        double tolerance = sqrt(pow(piece.getWidth(), 2) + pow(piece.getHeight(), 2)) / 10;
        int xDiff = abs(piece.xCoord - lParams.leftMargin);
        int yDiff = abs(piece.yCoord - lParams.topMargin);
        return xDiff <= tolerance && yDiff <= tolerance;
    }

    // brief white flash so a piece snapping into place is visually obvious
    private void blinkOnSnap(PuzzlePiece piece) {
        new CountDownTimer(300, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                piece.setColorFilter(0X80FFFFFF); //can change filter or make blinking faster
            }

            @Override
            public void onFinish() {
                piece.clearColorFilter();
            }
        }.start();
    }

    private void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

}

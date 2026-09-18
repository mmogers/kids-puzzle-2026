package lv.marmog.androidpuzzlegame.puzzle;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;

public class PuzzlePiece extends AppCompatImageView {
    public int xCoord;
    public int yCoord;
    public int pieceWidth;
    public int pieceHeight;
    // "resting" size (scattered, not being dragged) - PuzzleActivity.layoutPieces() fills
    // these in; TouchListener uses them to shrink a piece back down after a missed drop
    public int restingWidth;
    public int restingHeight;
    public boolean canMove = true;

    public PuzzlePiece(Context context) {
        super(context);
    }

}
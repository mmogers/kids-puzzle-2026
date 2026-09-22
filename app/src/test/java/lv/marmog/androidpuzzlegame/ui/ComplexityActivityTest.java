package lv.marmog.androidpuzzlegame.ui;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import org.junit.Test;
import org.mockito.Answers;
import org.mockito.MockedConstruction;

import java.lang.reflect.Method;

import lv.marmog.androidpuzzlegame.R;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComplexityActivityTest {

    private ComplexityActivity newActivity(int userId, String username) {
        Intent receivedIntent = mock(Intent.class);
        when(receivedIntent.getIntExtra(Extras.USER_ID, 0)).thenReturn(userId);
        when(receivedIntent.getStringExtra(Extras.USERNAME)).thenReturn(username);

        // mock(..., CALLS_REAL_METHODS) creates the instance without running the real
        // AppCompatActivity/ComponentActivity constructor chain, which needs a real main
        // Looper and would NPE outside a real Android runtime
        ComplexityActivity activity = mock(ComplexityActivity.class, Answers.CALLS_REAL_METHODS);
        doReturn(receivedIntent).when(activity).getIntent();
        doNothing().when(activity).startActivity(any(Intent.class));
        doNothing().when(activity).finish();
        return activity;
    }

    // onCreate() itself can't be driven directly here: it calls super.onCreate(), which
    // needs AppCompatActivity/ComponentActivity internals only set up by the real
    // constructor (skipped on purpose - see newActivity()) - that requires a real Android
    // runtime or Robolectric. setName() doesn't touch super.onCreate() though, so it can
    // be exercised directly via reflection.
    @Test
    public void setName_displaysTheUsernameFromTheIntent() throws Exception {
        ComplexityActivity activity = newActivity(1, "bob");
        TextView usernameView = mock(TextView.class);
        doReturn(usernameView).when(activity).findViewById(R.id.username_complexity);

        Method setName = ComplexityActivity.class.getDeclaredMethod("setName");
        setName.setAccessible(true);
        setName.invoke(activity);

        verify(usernameView).setText("bob");
    }

    @Test
    public void selectPieces_fourPieces_startsGridViewActivityWithTwoByTwoGrid() {
        assertSelectPiecesStartsGridView(4, 2, 2);
    }

    @Test
    public void selectPieces_ninePieces_startsGridViewActivityWithThreeByThreeGrid() {
        assertSelectPiecesStartsGridView(9, 3, 3);
    }

    @Test
    public void selectPieces_twelvePieces_startsGridViewActivityWithFourByThreeGrid() {
        assertSelectPiecesStartsGridView(12, 4, 3);
    }

    private void assertSelectPiecesStartsGridView(int piecesCount, int expectedColumns, int expectedRows) {
        ComplexityActivity activity = newActivity(7, "alice");
        View view = mock(View.class);
        when(view.getTag()).thenReturn(String.valueOf(piecesCount));

        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class)) {
            activity.selectPieces(view);

            assertEquals(1, mockedIntent.constructed().size());
            Intent sentIntent = mockedIntent.constructed().get(0);

            verify(sentIntent).putExtra(Extras.USER_ID, 7);
            verify(sentIntent).putExtra(Extras.USERNAME, "alice");
            verify(sentIntent).putExtra(Extras.PIECES_COUNT, piecesCount);
            verify(sentIntent).putExtra(Extras.COLUMNS, expectedColumns);
            verify(sentIntent).putExtra(Extras.ROWS, expectedRows);
            verify(activity).startActivity(sentIntent);
            verify(activity).finish();
        }
    }

    @Test
    public void goHome_startsStartActivityAndFinishes() {
        ComplexityActivity activity = newActivity(1, "bob");

        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class)) {
            activity.goHome();

            assertEquals(1, mockedIntent.constructed().size());
            verify(activity).startActivity(mockedIntent.constructed().get(0));
            verify(activity).finish();
        }
    }
}

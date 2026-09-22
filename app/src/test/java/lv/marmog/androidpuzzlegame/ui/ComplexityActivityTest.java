package lv.marmog.androidpuzzlegame.ui;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;

import lv.marmog.androidpuzzlegame.R;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// LENIENT: not every test uses all of setUp()'s stubs (e.g. goHome() never calls getIntent())
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
 class ComplexityActivityTest {

    // CALLS_REAL_METHODS skips the real Activity constructor (needs a main Looper we don't have)
    // while still running real method bodies
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ComplexityActivity activity;
    @Mock
    private Intent receivedIntent;

    @BeforeEach
     void setUp() {
        doReturn(receivedIntent).when(activity).getIntent();
        doNothing().when(activity).startActivity(any(Intent.class));
        doNothing().when(activity).finish();
    }

    private void configureIntent(int userId, String username) {
        when(receivedIntent.getIntExtra(Extras.USER_ID, 0)).thenReturn(userId);
        when(receivedIntent.getStringExtra(Extras.USERNAME)).thenReturn(username);
    }

    // onCreate() can't be called directly (super.onCreate() needs a real constructor); setName()
    // doesn't touch it, so it's exercised via reflection instead
    @Test
     void setName_displaysTheUsernameFromTheIntent() throws Exception {
        configureIntent(1, "bob");
        TextView usernameView = mock(TextView.class);
        doReturn(usernameView).when(activity).findViewById(R.id.username_complexity);

        Method setName = ComplexityActivity.class.getDeclaredMethod("setName");
        setName.setAccessible(true);
        setName.invoke(activity);

        verify(usernameView).setText("bob");
    }

    @Test
     void selectPieces_fourPieces_startsGridViewActivityWithTwoByTwoGrid() {
        assertSelectPiecesStartsGridView(4, 2, 2);
    }

    @Test
     void selectPieces_ninePieces_startsGridViewActivityWithThreeByThreeGrid() {
        assertSelectPiecesStartsGridView(9, 3, 3);
    }

    @Test
     void selectPieces_twelvePieces_startsGridViewActivityWithFourByThreeGrid() {
        assertSelectPiecesStartsGridView(12, 4, 3);
    }

    private void assertSelectPiecesStartsGridView(int piecesCount, int expectedColumns, int expectedRows) {
        configureIntent(7, "alice");
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
     void goHome_startsStartActivityAndFinishes() {
        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class)) {
            activity.goHome();

            assertEquals(1, mockedIntent.constructed().size());
            verify(activity).startActivity(mockedIntent.constructed().get(0));
            verify(activity).finish();
        }
    }
}

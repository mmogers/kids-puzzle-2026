package lv.marmog.androidpuzzlegame.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

import lv.marmog.androidpuzzlegame.R;

public class ComplexityActivity extends AppCompatActivity {

    // activity_complexity.xml plus a new entry here
    private static final Map<Integer, GridSize> COMPLEXITY_OPTIONS = new HashMap<>();

    static {
        COMPLEXITY_OPTIONS.put(4, new GridSize(2, 2));
        COMPLEXITY_OPTIONS.put(9, new GridSize(3, 3));
        COMPLEXITY_OPTIONS.put(12, new GridSize(4, 3));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_complexity);
        setName();

        //Button to go to the StartActivity
        FloatingActionButton goHome = findViewById(R.id.go_home);
        goHome.setOnClickListener(v -> goHome());

    }

    //Method that selects pieces for puzzle when is pressed one of the buttons
    public void selectPieces(View view) {
        Intent complexityIntent = new Intent(this, GridViewActivity.class);

        // sending id to gridView activity
        int userId = getUserId();
        String username = getUsername();
        complexityIntent.putExtra(Extras.USER_ID, userId);
        complexityIntent.putExtra(Extras.USERNAME, username);

        // sending number of pieces, columns and rows from buttons to gridView activity
        int piecesCount = Integer.parseInt((String) view.getTag());
        GridSize gridSize = COMPLEXITY_OPTIONS.get(piecesCount);
        complexityIntent.putExtra(Extras.PIECES_COUNT, piecesCount);
        complexityIntent.putExtra(Extras.COLUMNS, gridSize.columns);
        complexityIntent.putExtra(Extras.ROWS, gridSize.rows);

        startActivity(complexityIntent);
        finish();
    }

    private int getUserId() {
        return getIntent().getIntExtra(Extras.USER_ID, 0);
    }

    private String getUsername() {
        return getIntent().getStringExtra(Extras.USERNAME);
    }

    //Method to go to the StartActivity
    public void goHome() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    private void setName() {
        TextView name = findViewById(R.id.username_complexity);
        name.setText(getUsername());
    }

    private record GridSize(int columns, int rows) {
    }
}
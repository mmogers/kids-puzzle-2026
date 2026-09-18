package lv.marmog.androidpuzzlegame.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import lv.marmog.androidpuzzlegame.R;

public class ComplexityActivity extends AppCompatActivity {

    //Variables that is extras
    int userId;
    String username;
    //Button to go to the StartActivity
    private FloatingActionButton goHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complexity);
        setName();

        //Button to go to the StartActivity
        goHome = findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });

    }
//Method that selects pieces for puzzle when is pressed one of the buttons
    public void selectPieces(View view) {

        userId = getUserId();
        username = getUsername();

        Intent complexityIntent = new Intent(this, GridViewActivity.class);

        // sending id to gridView activity
        complexityIntent.putExtra(Extras.USER_ID, userId);
        complexityIntent.putExtra(Extras.USERNAME, username);


        // sending number of pieces, columns and rows from buttons to gridView activity
        if (view == findViewById(R.id.choose4)) {
            complexityIntent.putExtra(Extras.PIECES_COUNT, 4);
            complexityIntent.putExtra(Extras.COLUMNS, 2);
            complexityIntent.putExtra(Extras.ROWS, 2);
        } else if (view == findViewById(R.id.choose9)) {
            complexityIntent.putExtra(Extras.PIECES_COUNT, 9);
            complexityIntent.putExtra(Extras.COLUMNS, 3);
            complexityIntent.putExtra(Extras.ROWS, 3);
        } else if (view == findViewById(R.id.choose12)) {
            complexityIntent.putExtra(Extras.PIECES_COUNT, 12);
            complexityIntent.putExtra(Extras.COLUMNS, 4);
            complexityIntent.putExtra(Extras.ROWS, 3);
        }

        startActivity(complexityIntent);
        finish();

    }

    // --- method to get user id
    private int getUserId() {
        userId = getIntent().getIntExtra(Extras.USER_ID, 0);
        return userId;
    }

    // method to get username
    private String getUsername() {
        username = getIntent().getStringExtra(Extras.USERNAME);
        return username;
    }

    //Method to go to the StartActivity
    public void goHome() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    public void setName() {
        TextView name = (TextView)findViewById(R.id.username_complexity);
        name.setText(getUsername());
    }
}
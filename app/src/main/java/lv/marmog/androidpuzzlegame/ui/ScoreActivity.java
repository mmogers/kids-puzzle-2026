package lv.marmog.androidpuzzlegame.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import lv.marmog.androidpuzzlegame.R;
import lv.marmog.androidpuzzlegame.database.TimerDAO;


public class ScoreActivity extends AppCompatActivity {

    private TextView yourTime;
    private TextView bestTime;
    private TextView kidName;
    private int userId, level, time;
    private String username;
    //Button to go to the StartActivity
    private FloatingActionButton goHome;

    private TimerDAO timerDAO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // database
        timerDAO = new TimerDAO(this);

        // show timer result from puzzleActivity
        yourTime = (TextView) findViewById(R.id.your_time);
        String timeString = String.valueOf(getTime());
        yourTime.setText(timeString + " seconds");

        kidName = (TextView) findViewById(R.id.kid_name);
        username = getUsername();
        Log.i(ScoreActivity.class.getName(), "Username for textview is " + username);
        kidName.setText(username + "!");

        // id, level, timer for db
        userId = getUserId();
        level = getLevel();
        time = getTime();

        // insert in db before reading back the best result, so this game's time counts too
        insertResult();

        //show best time
        bestTime = (TextView) findViewById(R.id.best_time);
        bestTime.setText(showBestResult() + " seconds");

        //Button to go in StartActivity
        goHome = findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });
    }

    public boolean insertResult() {
        return timerDAO.insertResult(userId, level, time);
    }

    public String showBestResult() {
        return String.valueOf(timerDAO.getBestResult(getUserId(), getLevel()));
    }

    public void startNewGame(View view) {
        Intent intent = new Intent(this, ComplexityActivity.class);// redirect from this page to MainActivity page- list of images
        userId = getUserId();
        username = getUsername();
        intent.putExtra(Extras.USER_ID, userId);
        intent.putExtra(Extras.USERNAME, username);
        Log.i(ScoreActivity.class.getName(), "User id " + userId + " was sent to complexity activity");
        startActivity(intent);
        finish();
    }

    // --- methods to get userId, level and time from intent extras
    public int getUserId() {
        Intent getUserIntent = getIntent();
        userId = getUserIntent.getIntExtra(Extras.USER_ID, 0);
        Log.i(ScoreActivity.class.getName(), "Id is " + userId);
        return userId;
    }

    public String getUsername() {
        Intent getUserIntent = getIntent();
        username = getUserIntent.getStringExtra(Extras.USERNAME);
        Log.i(ScoreActivity.class.getName(), "Username is " + username);
        return username;
    }

    public int getLevel() {
        Intent getLevelIntent = getIntent();
        level = getLevelIntent.getIntExtra(Extras.PIECES_COUNT, 0);
        Log.i(ScoreActivity.class.getName(), "Level is " + level);
        return level;
    }

    public int getTime() {
        Intent getTimeIntent = getIntent();
        int time = getTimeIntent.getIntExtra(Extras.TIME, 0);
        return time;
    }
    // --- /methods to get userId, level and time

    //Method to go to the StartActivity
    public void goHome() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}

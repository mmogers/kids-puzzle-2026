package lv.marmog.androidpuzzlegame.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lv.marmog.androidpuzzlegame.R;
import lv.marmog.androidpuzzlegame.model.User;
import lv.marmog.androidpuzzlegame.database.UserDAO;

public class CreateUsernameActivity extends AppCompatActivity {

    private static final String TAG = CreateUsernameActivity.class.getName();

    //references to buttons and other controls on the layout
    private int idToDelete;
    private EditText usernameInput;
    private EditText repeatUsername;
    private ListView usernamesListView;
    private List<User> usernames;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_username);

        this.userDAO = new UserDAO(this);

        //value for variable that is find by id that is created in layout
        this.usernameInput = findViewById(R.id.enter_username);
        this.repeatUsername = findViewById(R.id.repeat_username);
        Button saveNewUsername = findViewById(R.id.save_username);
        Button deleteUsername = findViewById(R.id.delete_username);

        // Button to go to the StartActivity
        FloatingActionButton goHome = findViewById(R.id.go_home);
        goHome.setOnClickListener(v -> goHome());

        //creates usernamesList in current layout
        this.usernamesListView = findViewById(R.id.view_usernames_listview);
        this.usernames = new ArrayList<>(0);

        populateUsernamesList();
        //onClickListener for button to save new username in database
        saveNewUsername.setOnClickListener(v -> onSaveUsernameClicked());

        this.usernamesListView.setOnItemClickListener((parent, view, position, id) -> {
            this.usernamesListView.setSelection(position);
            view.setSelected(true);
            //get user id of selected user
            int usernameId = this.usernames.get(position).getUsernameId();
            Log.i(TAG, "Selected username id is " + usernameId);
            setIdToDelete(usernameId);
            Log.i(TAG, "idToDelete is set to  " + usernameId);
        });

        deleteUsername.setOnClickListener(v -> {
            Log.i(TAG, "Username id to be deleted is: " + getIdToDelete());
            User user = new User();
            user.setUsernameId(getIdToDelete());
            this.userDAO.deleteResults(user);
            this.userDAO.deleteUser(user);
            Toasts.show(this, "User has been deleted", Toast.LENGTH_LONG);
            populateUsernamesList();
        });
    }

    private void onSaveUsernameClicked() {
        //variables that have to convert the field of the layout into string
        String username = usernameInput.getText().toString();
        String reUser = repeatUsername.getText().toString();

        //check if field is filled and show message if not
        if (username.isEmpty() || reUser.isEmpty()) {
            Toasts.show(this, "Please enter all the fields", Toast.LENGTH_SHORT);
            return;
        }

        //check username and repeatUsername fields if they are the same
        if (!username.equals(reUser)) {
            Toasts.show(this, "Username not matching", Toast.LENGTH_LONG);
            return;
        }

        //check if the username already exists
        if (userDAO.checkUsername(username)) {
            Toasts.show(this, "User already exists", Toast.LENGTH_LONG);
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        if (!userDAO.createUser(newUser)) {
            Toasts.show(this, "Registration failed", Toast.LENGTH_LONG);
            return;
        }

        Toasts.show(this, "New username is created", Toast.LENGTH_LONG);
        populateUsernamesList();
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
        finish();
    }

    private void populateUsernamesList() {
        this.usernames = userDAO.getAllUsers();

        List<String> userStrings = usernames.stream()
                .map(User::toString)
                .collect(Collectors.toList());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.listview_element, userStrings);
        this.usernamesListView.setAdapter(arrayAdapter);
    }

    private void setIdToDelete(int id) {
        this.idToDelete = id;
    }

    private int getIdToDelete() {
        return this.idToDelete;
    }

    //Method to go to the StartActivity
    private void goHome() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}
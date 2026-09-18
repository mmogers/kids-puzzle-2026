package lv.marmog.androidpuzzlegame.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import lv.marmog.androidpuzzlegame.R;
import lv.marmog.androidpuzzlegame.model.User;
import lv.marmog.androidpuzzlegame.database.UserDAO;

public class CreateUsernameActivity extends AppCompatActivity {

//references to buttons and other controls on the layout
    private int usernameId;
    private  int idToDelete;
    private EditText user;
    private EditText repeatUsername;
    private Button saveNewUsername, deleteUsername;
    private FloatingActionButton goHome;
    private ListView usernamesListView;
    private List<User> usernames;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_username);

        userDAO = new UserDAO(this);
        //value for variable that is find by id that is created in layout
        user = (EditText) findViewById(R.id.enter_username);
        repeatUsername = (EditText) findViewById(R.id.repeat_username);
        saveNewUsername = (Button) findViewById(R.id.save_username);
        deleteUsername = (Button) findViewById(R.id.delete_username);

        // Button to go to the StartActivity
        goHome = findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });


        //creates usernamesList in current layout
        usernamesListView = (ListView) findViewById(R.id.view_usernames_listview);
        usernames = new ArrayList<User>(0);
        populateUsernamesList();
        //onClickListener for button to save new username in database
        saveNewUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //variables that have to convert the field of the layout into string
                String username = user.getText().toString();
                String reUser = repeatUsername.getText().toString();
                User u = new User();
                u.setUsername(username);
           //check if field are filled and show message if not
                if(username.equals("") || reUser.equals("")) {
                    Toast.makeText(CreateUsernameActivity.this, "Please enter all the field", Toast.LENGTH_SHORT).show();
                }

                //check username and repeatUsername fields if they are same
                else{
                    if(username.equals(reUser)){
                        //First one if the method is taken from databaseHelper.
                       // Boolean checkUser = db.checkUsername(username);
                        Boolean checkUser = userDAO.checkUsername(username);
                        //check if the username already exists, if no, than insert new username to database
                        if(checkUser == false) {
                          Boolean insert = userDAO.createUser(u);
                            if (insert == true) {
                                Toast.makeText(CreateUsernameActivity.this, "New username is created", Toast.LENGTH_LONG).show();
                                populateUsernamesList();
                                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(CreateUsernameActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                            }
                        }
                            else{
                                Toast.makeText(CreateUsernameActivity.this, "User already exists", Toast.LENGTH_LONG).show();
                            }
                    }
                    else{
                        Toast.makeText(CreateUsernameActivity.this, "Username not matching", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

//      usernamesListView.setOnItemLongClickListener(listViewListener);
        usernamesListView.setOnItemClickListener(listViewListener);

        deleteUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //delete only user by usernameId from TABLE_USERS
                Log.i(CreateUsernameActivity.class.getName(), "Username id to be deleted is: " + getIdToDelete());
                User user = new User();
                user.setUsernameId(getIdToDelete());
                userDAO.deleteResults(user);
                userDAO.deleteUser(user);
                Toast.makeText(CreateUsernameActivity.this,"User has been deleted", Toast.LENGTH_LONG).show();
                populateUsernamesList();
            }
        });
    }

    private void populateUsernamesList(){
        //Create a List of Strings
        List<String> userStrings = new ArrayList<String>(0);
        usernames = userDAO.getAllUsers();

        for(int i = 0; i<usernames.size(); i++){
            userStrings.add(usernames.get(i).toString());

        }
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userStrings);
        usernamesListView.setAdapter(arrayAdapter);
    }

//    private AdapterView.OnItemLongClickListener listViewListener = new AdapterView.OnItemLongClickListener() {
//        @Override
//        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//            usernamesListView.getSelectedItem();
//            usernamesListView.getItemAtPosition(position);
//            usernamesListView.setSelection(position);
//            usernameId = usernames.get(position).getUsernameId();
//            view.setSelected(true);
//            Log.i(CreateUsernameActivity.class.getName(), "Username id to delete is: " + usernameId);
//            return false;
//        }
//    };

    private AdapterView.OnItemClickListener listViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            usernamesListView.setSelection(position);
            view.setSelected(true);
            //get user id of selected user
            usernameId = usernames.get(position).getUsernameId();
            Log.i(CreateUsernameActivity.class.getName(), "Selected username id is " + usernameId);
            setIdToDelete(usernameId);
            Log.i(CreateUsernameActivity.class.getName(), "idToDelete is set to  " + usernameId);
        }
    };

    private int setIdToDelete(int id) {
        idToDelete = id;
        return idToDelete;
    }

    public int getIdToDelete() {
        return idToDelete;
    }

//    protected void goToComplexityActivity(int id){
//        Intent complexityActivity = new Intent(this, ComplexityActivity.class);
//        startActivity(complexityActivity);
//    }
    //Method to go to the StartActivity
    public void goHome() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}
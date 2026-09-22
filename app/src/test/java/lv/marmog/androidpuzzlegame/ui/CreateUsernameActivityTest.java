package lv.marmog.androidpuzzlegame.ui;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import lv.marmog.androidpuzzlegame.database.UserDAO;
import lv.marmog.androidpuzzlegame.model.User;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CreateUsernameActivity wires all of its behaviour (save/delete/list) from lambdas
 * registered in onCreate(). onCreate() itself calls super.onCreate(), which needs
 * internals (e.g. ComponentActivity's SavedStateRegistryController) that only exist
 * once the real Activity constructor has run - something we can't do here without a
 * real Android runtime (or Robolectric). So instead of going through onCreate(), these
 * tests build the activity with mock(..., CALLS_REAL_METHODS) - which skips the
 * constructor entirely - inject its collaborators directly via reflection, and invoke
 * the (private) business-logic methods directly.
 */
public class CreateUsernameActivityTest {

    private CreateUsernameActivity activity;
    private EditText usernameInput;
    private EditText repeatUsername;
    private ListView usernamesListView;
    private UserDAO userDAO;

    private void setUp() throws Exception {
        activity = mock(CreateUsernameActivity.class, Answers.CALLS_REAL_METHODS);
        usernameInput = mockEditText();
        repeatUsername = mockEditText();
        usernamesListView = mock(ListView.class);
        userDAO = mock(UserDAO.class);

        setField("usernameInput", usernameInput);
        setField("repeatUsername", repeatUsername);
        setField("usernamesListView", usernamesListView);
        setField("userDAO", userDAO);

        doNothing().when(activity).startActivity(any(Intent.class));
        doNothing().when(activity).finish();
        doReturn(mock(Context.class)).when(activity).getApplicationContext();
    }

    private void setField(String name, Object value) throws Exception {
        Field field = CreateUsernameActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(activity, value);
    }

    private void invokePrivateMethod(String name) throws Exception {
        Method method = CreateUsernameActivity.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(activity);
    }

    private static EditText mockEditText() {
        EditText editText = mock(EditText.class);
        setText(editText, "");
        return editText;
    }

    private static void setText(EditText editText, String text) {
        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn(text);
        when(editText.getText()).thenReturn(editable);
    }

    private static User newUser(int id, String username) {
        User user = new User();
        user.setUsernameId(id);
        user.setUsername(username);
        return user;
    }

    private static MockedStatic<Toast> mockToast() {
        MockedStatic<Toast> toastMock = mockStatic(Toast.class);
        Toast toast = mock(Toast.class);
        toastMock.when(() -> Toast.makeText(any(), anyString(), anyInt())).thenReturn(toast);
        return toastMock;
    }

    @Test
    public void populateUsernamesList_loadsUsersFromDatabaseIntoTheListView() throws Exception {
        setUp();
        when(userDAO.getAllUsers()).thenReturn(Arrays.asList(newUser(1, "alice"), newUser(2, "bob")));

        try (MockedConstruction<ArrayAdapter> mockedAdapter = mockConstruction(ArrayAdapter.class)) {
            invokePrivateMethod("populateUsernamesList");

            verify(userDAO).getAllUsers();
            verify(usernamesListView).setAdapter(mockedAdapter.constructed().get(0));
        }
    }

    @Test
    public void onSaveUsernameClicked_showsError_whenFieldsAreEmpty() throws Exception {
        setUp();

        try (MockedStatic<Toast> toastMock = mockToast()) {
            invokePrivateMethod("onSaveUsernameClicked");

            verify(userDAO, never()).createUser(any(User.class));
            toastMock.verify(() -> Toast.makeText(any(), eq("Please enter all the fields"), eq(Toast.LENGTH_SHORT)));
            verify(activity, never()).startActivity(any(Intent.class));
        }
    }

    @Test
    public void onSaveUsernameClicked_showsError_whenUsernamesDoNotMatch() throws Exception {
        setUp();
        setText(usernameInput, "alice");
        setText(repeatUsername, "alicia");

        try (MockedStatic<Toast> toastMock = mockToast()) {
            invokePrivateMethod("onSaveUsernameClicked");

            verify(userDAO, never()).createUser(any(User.class));
            toastMock.verify(() -> Toast.makeText(any(), eq("Username not matching"), eq(Toast.LENGTH_LONG)));
        }
    }

    @Test
    public void onSaveUsernameClicked_showsError_whenUsernameAlreadyExists() throws Exception {
        setUp();
        setText(usernameInput, "alice");
        setText(repeatUsername, "alice");
        when(userDAO.checkUsername("alice")).thenReturn(true);

        try (MockedStatic<Toast> toastMock = mockToast()) {
            invokePrivateMethod("onSaveUsernameClicked");

            verify(userDAO, never()).createUser(any(User.class));
            toastMock.verify(() -> Toast.makeText(any(), eq("User already exists"), eq(Toast.LENGTH_LONG)));
        }
    }

    @Test
    public void onSaveUsernameClicked_showsError_whenCreateUserFails() throws Exception {
        setUp();
        setText(usernameInput, "alice");
        setText(repeatUsername, "alice");
        when(userDAO.checkUsername("alice")).thenReturn(false);
        when(userDAO.createUser(any(User.class))).thenReturn(false);

        try (MockedStatic<Toast> toastMock = mockToast()) {
            invokePrivateMethod("onSaveUsernameClicked");

            toastMock.verify(() -> Toast.makeText(any(), eq("Registration failed"), eq(Toast.LENGTH_LONG)));
            verify(activity, never()).startActivity(any(Intent.class));
        }
    }

    @Test
    public void onSaveUsernameClicked_createsUserAndReturnsHome_whenValid() throws Exception {
        setUp();
        setText(usernameInput, "newkid");
        setText(repeatUsername, "newkid");
        when(userDAO.checkUsername("newkid")).thenReturn(false);
        when(userDAO.createUser(any(User.class))).thenReturn(true);
        when(userDAO.getAllUsers()).thenReturn(new ArrayList<>());

        try (MockedConstruction<ArrayAdapter> mockedAdapter = mockConstruction(ArrayAdapter.class);
             MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class);
             MockedStatic<Toast> toastMock = mockToast()) {

            invokePrivateMethod("onSaveUsernameClicked");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDAO).createUser(userCaptor.capture());
            assertEquals("newkid", userCaptor.getValue().getUsername());
            verify(usernamesListView).setAdapter(mockedAdapter.constructed().get(0));
            assertEquals(1, mockedIntent.constructed().size());
            verify(activity).startActivity(mockedIntent.constructed().get(0));
            verify(activity).finish();
        }
    }

    @Test
    public void goHome_startsStartActivityAndFinishes() throws Exception {
        setUp();

        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class)) {
            invokePrivateMethod("goHome");

            assertEquals(1, mockedIntent.constructed().size());
            verify(activity).startActivity(mockedIntent.constructed().get(0));
            verify(activity).finish();
        }
    }
}

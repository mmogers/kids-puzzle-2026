package lv.marmog.androidpuzzlegame.ui;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import lv.marmog.androidpuzzlegame.database.UserDAO;
import lv.marmog.androidpuzzlegame.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

// onCreate() can't be called directly (super.onCreate() needs a real constructor), so these
// tests inject the activity's private fields via reflection and invoke its business-logic
// methods directly instead.
// LENIENT: not every test uses all of setUp()'s stubs (only the "valid save" test navigates)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
 class CreateUsernameActivityTest {

    // CALLS_REAL_METHODS skips the real Activity constructor (needs a main Looper we don't have)
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private CreateUsernameActivity activity;
    @Mock
    private EditText usernameInput;
    @Mock
    private EditText repeatUsername;
    @Mock
    private ListView usernamesListView;
    @Mock
    private UserDAO userDAO;
    @Mock
    private Toast toast;

    @BeforeEach
     void setUp() throws Exception {
        setText(usernameInput, "");
        setText(repeatUsername, "");

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

    private MockedStatic<Toast> mockToast() {
        MockedStatic<Toast> toastMock = mockStatic(Toast.class);
        toastMock.when(() -> Toast.makeText(any(), anyString(), anyInt())).thenReturn(toast);
        return toastMock;
    }

    @Test
     void populateUsernamesList_loadsUsersFromDatabaseIntoTheListView() throws Exception {
        when(userDAO.getAllUsers()).thenReturn(Arrays.asList(newUser(1, "alice"), newUser(2, "bob")));

        try (MockedConstruction<ArrayAdapter> mockedAdapter = mockConstruction(ArrayAdapter.class)) {
            invokePrivateMethod("populateUsernamesList");

            verify(userDAO).getAllUsers();
            verify(usernamesListView).setAdapter(mockedAdapter.constructed().get(0));
        }
    }

    @Test
     void onSaveUsernameClicked_showsError_whenFieldsAreEmpty() throws Exception {
        try (MockedStatic<Toast> toastMock = mockToast()) {
            invokePrivateMethod("onSaveUsernameClicked");

            verify(userDAO, never()).createUser(any(User.class));
            toastMock.verify(() -> Toast.makeText(any(), eq("Please enter all the fields"), eq(Toast.LENGTH_SHORT)));
            verify(activity, never()).startActivity(any(Intent.class));
        }
    }

    @Test
     void onSaveUsernameClicked_showsError_whenUsernamesDoNotMatch() throws Exception {
        setText(usernameInput, "alice");
        setText(repeatUsername, "alicia");

        try (MockedStatic<Toast> toastMock = mockToast()) {
            invokePrivateMethod("onSaveUsernameClicked");

            verify(userDAO, never()).createUser(any(User.class));
            toastMock.verify(() -> Toast.makeText(any(), eq("Username not matching"), eq(Toast.LENGTH_LONG)));
        }
    }

    @Test
     void onSaveUsernameClicked_showsError_whenUsernameAlreadyExists() throws Exception {
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
     void onSaveUsernameClicked_showsError_whenCreateUserFails() throws Exception {
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
     void onSaveUsernameClicked_createsUserAndReturnsHome_whenValid() throws Exception {
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
     void goHome_startsStartActivityAndFinishes() throws Exception {
        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class)) {
            invokePrivateMethod("goHome");

            assertEquals(1, mockedIntent.constructed().size());
            verify(activity).startActivity(mockedIntent.constructed().get(0));
            verify(activity).finish();
        }
    }
}

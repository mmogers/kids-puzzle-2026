package lv.marmog.androidpuzzlegame.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import lv.marmog.androidpuzzlegame.R;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// onCreate() can't be called directly (super.onCreate() needs a real constructor), so business
// logic is exercised directly / via reflection instead, following ComplexityActivityTest and
// CreateUsernameActivityTest. The ActivityResultLauncher fields (cameraLauncher, galleryLauncher,
// requestWriteStoragePermission, requestReadImagesPermission) are normally built by field
// initializers that only run inside the real constructor, which Objenesis skips for this mock -
// they start out null and are injected via reflection in setUp(), the same way onCreate()-only
// fields are injected in CreateUsernameActivityTest.
// The test JVM's mockable android.jar reports Build.VERSION.SDK_INT as 0 (returnDefaultValues),
// so every SDK-gated branch below exercises its pre-Q / pre-Tiramisu path; the modern-API branches
// aren't reachable this way (same class of environment gap already accepted in CLAUDE.md).
// LENIENT: not every test uses all of setUp()'s stubs.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GridViewActivityTest {

    // CALLS_REAL_METHODS skips the real Activity constructor (needs a main Looper we don't have)
    // while still running real method bodies
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private GridViewActivity activity;
    @Mock
    private Intent receivedIntent;
    @Mock
    private ActivityResultLauncher<Intent> cameraLauncher;
    @Mock
    private ActivityResultLauncher<Intent> galleryLauncher;
    @Mock
    private ActivityResultLauncher<String> requestWriteStoragePermission;
    @Mock
    private ActivityResultLauncher<String> requestReadImagesPermission;

    @BeforeEach
    void setUp() throws Exception {
        doReturn(receivedIntent).when(activity).getIntent();
        doNothing().when(activity).startActivity(any(Intent.class));
        doNothing().when(activity).finish();

        setField("cameraLauncher", cameraLauncher);
        setField("galleryLauncher", galleryLauncher);
        setField("requestWriteStoragePermission", requestWriteStoragePermission);
        setField("requestReadImagesPermission", requestReadImagesPermission);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = GridViewActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(activity, value);
    }

    private Object invokePrivate(String name) throws Exception {
        Method method = GridViewActivity.class.getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(activity);
    }

    private void invokePrivateWithBundle(String name, Bundle bundle) throws Exception {
        Method method = GridViewActivity.class.getDeclaredMethod(name, Bundle.class);
        method.setAccessible(true);
        method.invoke(activity, bundle);
    }

    private void invokeStartPuzzle(String imageExtraKey, String imageRef) throws Exception {
        Method method = GridViewActivity.class.getDeclaredMethod("startPuzzle", String.class, String.class);
        method.setAccessible(true);
        method.invoke(activity, imageExtraKey, imageRef);
    }

    private void invokeOnCameraResult(ActivityResult result) throws Exception {
        Method method = GridViewActivity.class.getDeclaredMethod("onCameraResult", ActivityResult.class);
        method.setAccessible(true);
        method.invoke(activity, result);
    }

    private void invokeOnGalleryResult(ActivityResult result) throws Exception {
        Method method = GridViewActivity.class.getDeclaredMethod("onGalleryResult", ActivityResult.class);
        method.setAccessible(true);
        method.invoke(activity, result);
    }

    private Object getField(String name) throws Exception {
        Field field = GridViewActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(activity);
    }

    private MockedConstruction<Intent> mockResolvableCameraIntent() {
        return mockConstruction(Intent.class, (mockIntent, context) ->
                when(mockIntent.resolveActivity(any())).thenReturn(mock(ComponentName.class)));
    }

    private MockedStatic<ContextCompat> mockPermission(String permission, int result) {
        MockedStatic<ContextCompat> contextCompat = mockStatic(ContextCompat.class);
        contextCompat.when(() -> ContextCompat.checkSelfPermission(activity, permission)).thenReturn(result);
        return contextCompat;
    }

    // ---- setName() ----

    @Test
    void setName_displaysUsernameFromIntent() throws Exception {
        when(receivedIntent.getStringExtra(Extras.USERNAME)).thenReturn("bob");
        TextView usernameView = mock(TextView.class);
        doReturn(usernameView).when(activity).findViewById(R.id.username_gridview);

        invokePrivate("setName");

        verify(usernameView).setText("bob");
    }

    // ---- saveState() / restoreState() (currentPhotoUri across process death) ----
    // onSaveInstanceState()/onCreate() themselves can't be called (their super calls need
    // ComponentActivity internals), so the private helpers they delegate to are tested instead.

    @Test
    void saveState_storesCurrentPhotoUri_whenSet() throws Exception {
        Uri uri = mock(Uri.class);
        when(uri.toString()).thenReturn("content://media/external/images/media/42");
        setField("currentPhotoUri", uri);
        Bundle outState = mock(Bundle.class);

        invokePrivateWithBundle("saveState", outState);

        verify(outState).putString("currentPhotoUri", "content://media/external/images/media/42");
    }

    @Test
    void saveState_storesNothing_whenNoPhotoInProgress() throws Exception {
        Bundle outState = mock(Bundle.class);

        invokePrivateWithBundle("saveState", outState);

        verifyNoInteractions(outState);
    }

    @Test
    void restoreState_restoresCurrentPhotoUri_fromSavedState() throws Exception {
        Bundle savedState = mock(Bundle.class);
        when(savedState.getString("currentPhotoUri")).thenReturn("content://media/external/images/media/42");
        Uri parsedUri = mock(Uri.class);

        try (MockedStatic<Uri> uriMock = mockStatic(Uri.class)) {
            uriMock.when(() -> Uri.parse("content://media/external/images/media/42")).thenReturn(parsedUri);

            invokePrivateWithBundle("restoreState", savedState);
        }

        assertEquals(parsedUri, getField("currentPhotoUri"));
    }

    @Test
    void restoreState_leavesUriNull_whenSavedStateHasNoPhoto() throws Exception {
        invokePrivateWithBundle("restoreState", mock(Bundle.class));

        assertNull(getField("currentPhotoUri"));
    }

    @Test
    void restoreState_doesNothing_onFirstLaunch() throws Exception {
        Uri uri = mock(Uri.class);
        setField("currentPhotoUri", uri);

        invokePrivateWithBundle("restoreState", null);

        assertEquals(uri, getField("currentPhotoUri"));
    }

    // ---- startPuzzle() ----

    @Test
    void startPuzzle_passesImageAndSessionExtras_andFinishes() throws Exception {
        setField("piecesIntent", 12);
        setField("columnsIntent", 3);
        setField("rowsIntent", 4);
        setField("userId", 7);
        setField("username", "bob");

        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class)) {
            invokeStartPuzzle(Extras.ASSET_NAME, "cat.jpg");

            Intent sent = mockedIntent.constructed().get(0);
            verify(sent).putExtra(Extras.ASSET_NAME, "cat.jpg");
            verify(sent).putExtra(Extras.PIECES_COUNT, 12);
            verify(sent).putExtra(Extras.COLUMNS, 3);
            verify(sent).putExtra(Extras.ROWS, 4);
            verify(sent).putExtra(Extras.USER_ID, 7);
            verify(sent).putExtra(Extras.USERNAME, "bob");
            verify(activity).startActivity(sent);
            verify(activity).finish();
        }
    }

    // ---- onCameraResult() ----

    @Test
    void onCameraResult_startsPuzzleWithPhotoUri_andKeepsEntry_whenPhotoTaken() throws Exception {
        Uri photoUri = mock(Uri.class);
        when(photoUri.toString()).thenReturn("content://media/external/images/media/42");
        setField("currentPhotoUri", photoUri);
        ContentResolver resolver = mock(ContentResolver.class);
        doReturn(resolver).when(activity).getContentResolver();

        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class)) {
            invokeOnCameraResult(new ActivityResult(Activity.RESULT_OK, null));

            Intent sent = mockedIntent.constructed().get(0);
            verify(sent).putExtra(Extras.CURRENT_PHOTO_URI, "content://media/external/images/media/42");
            verify(activity).startActivity(sent);
            verify(activity).finish();
            verifyNoInteractions(resolver);
        }
    }

    @Test
    void onCameraResult_deletesEmptyEntryAndResetsUri_whenCancelled() throws Exception {
        Uri photoUri = mock(Uri.class);
        setField("currentPhotoUri", photoUri);
        ContentResolver resolver = mock(ContentResolver.class);
        doReturn(resolver).when(activity).getContentResolver();

        invokeOnCameraResult(new ActivityResult(Activity.RESULT_CANCELED, null));

        verify(resolver).delete(photoUri, null, null);
        assertNull(getField("currentPhotoUri"));
        verify(activity, never()).startActivity(any(Intent.class));
    }

    @Test
    void onCameraResult_doesNotCrash_whenCleanupDeleteIsNotPermitted() throws Exception {
        Uri photoUri = mock(Uri.class);
        setField("currentPhotoUri", photoUri);
        ContentResolver resolver = mock(ContentResolver.class);
        doReturn(resolver).when(activity).getContentResolver();
        when(resolver.delete(photoUri, null, null)).thenThrow(new SecurityException("no write access"));

        invokeOnCameraResult(new ActivityResult(Activity.RESULT_CANCELED, null));

        assertNull(getField("currentPhotoUri"));
    }

    @Test
    void onCameraResult_doesNothing_whenNoPhotoUriIsKnown() throws Exception {
        ContentResolver resolver = mock(ContentResolver.class);
        doReturn(resolver).when(activity).getContentResolver();

        invokeOnCameraResult(new ActivityResult(Activity.RESULT_OK, null));

        verify(activity, never()).startActivity(any(Intent.class));
        verifyNoInteractions(resolver);
    }

    // ---- onGalleryResult() ----

    @Test
    void onGalleryResult_startsPuzzleWithPickedUri() throws Exception {
        Uri pickedUri = mock(Uri.class);
        when(pickedUri.toString()).thenReturn("content://picked/1");
        Intent data = mock(Intent.class);
        when(data.getData()).thenReturn(pickedUri);

        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class)) {
            invokeOnGalleryResult(new ActivityResult(Activity.RESULT_OK, data));

            Intent sent = mockedIntent.constructed().get(0);
            verify(sent).putExtra(Extras.CURRENT_PHOTO_URI, "content://picked/1");
            verify(activity).startActivity(sent);
            verify(activity).finish();
        }
    }

    @Test
    void onGalleryResult_showsToastAndStays_whenPickerReturnsNoUri() throws Exception {
        Intent data = mock(Intent.class);
        when(data.getData()).thenReturn(null);

        try (MockedStatic<Toast> toastMock = mockStatic(Toast.class)) {
            toastMock.when(() -> Toast.makeText(any(), anyString(), eq(Toast.LENGTH_SHORT))).thenReturn(mock(Toast.class));

            invokeOnGalleryResult(new ActivityResult(Activity.RESULT_OK, data));

            toastMock.verify(() -> Toast.makeText(eq(activity), eq("Could not open picture"), eq(Toast.LENGTH_SHORT)));
            verify(activity, never()).startActivity(any(Intent.class));
            verify(activity, never()).finish();
        }
    }

    @Test
    void onGalleryResult_doesNothing_whenCancelled() throws Exception {
        invokeOnGalleryResult(new ActivityResult(Activity.RESULT_CANCELED, null));

        verify(activity, never()).startActivity(any(Intent.class));
        verify(activity, never()).finish();
    }

    @Test
    void onGalleryResult_doesNothing_whenResultHasNoData() throws Exception {
        invokeOnGalleryResult(new ActivityResult(Activity.RESULT_OK, null));

        verify(activity, never()).startActivity(any(Intent.class));
        verify(activity, never()).finish();
    }

    // ---- goHome() ----

    @Test
    void goHome_startsStartActivityAndFinishes() {
        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class)) {
            activity.goHome();

            assertEquals(1, mockedIntent.constructed().size());
            verify(activity).startActivity(mockedIntent.constructed().get(0));
            verify(activity).finish();
        }
    }

    // ---- readImagesPermissionForThisDevice() ----

    @Test
    void readImagesPermissionForThisDevice_returnsReadExternalStorage_belowTiramisu() throws Exception {
        Method method = GridViewActivity.class.getDeclaredMethod("readImagesPermissionForThisDevice");
        method.setAccessible(true);

        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE, method.invoke(null));
    }

    // ---- createImageUri() ----

    @Test
    void createImageUri_writesDisplayNameAndMimeType_andReturnsInsertedUri() throws Exception {
        Uri expectedUri = mock(Uri.class);
        ContentResolver resolver = mock(ContentResolver.class);
        doReturn(resolver).when(activity).getContentResolver();
        when(resolver.insert(eq(MediaStore.Images.Media.EXTERNAL_CONTENT_URI), any(ContentValues.class)))
                .thenReturn(expectedUri);

        try (MockedConstruction<ContentValues> mockedValues = mockConstruction(ContentValues.class)) {
            Method method = GridViewActivity.class.getDeclaredMethod("createImageUri");
            method.setAccessible(true);
            Object result = method.invoke(activity);

            assertEquals(expectedUri, result);
            ContentValues values = mockedValues.constructed().get(0);
            verify(values).put(eq(MediaStore.MediaColumns.DISPLAY_NAME), anyString());
            verify(values).put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            verify(values, never()).put(eq(MediaStore.MediaColumns.RELATIVE_PATH), anyString());
        }
    }

    // ---- onImageFromCameraClick() / launchCamera() ----

    @Test
    void onImageFromCameraClick_doesNothing_whenNoCameraAppIsAvailable() throws Exception {
        // real, unmocked Intent#resolveActivity() returns null under returnDefaultValues
        invokePrivate("onImageFromCameraClick");

        verifyNoInteractions(requestWriteStoragePermission, cameraLauncher);
    }

    @Test
    void onImageFromCameraClick_requestsWriteStoragePermission_whenCameraAvailableButPermissionMissing() throws Exception {
        try (MockedConstruction<Intent> mockedIntent = mockResolvableCameraIntent();
             MockedStatic<ContextCompat> contextCompat =
                     mockPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_DENIED)) {

            invokePrivate("onImageFromCameraClick");

            verify(requestWriteStoragePermission).launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            verifyNoInteractions(cameraLauncher);
        }
    }

    @Test
    void onImageFromCameraClick_launchesCamera_whenCameraAvailableAndPermissionGranted() throws Exception {
        ContentResolver resolver = mock(ContentResolver.class);
        doReturn(resolver).when(activity).getContentResolver();
        when(resolver.insert(any(), any(ContentValues.class))).thenReturn(mock(Uri.class));

        try (MockedConstruction<Intent> mockedIntent = mockResolvableCameraIntent();
             MockedStatic<ContextCompat> contextCompat =
                     mockPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED)) {

            invokePrivate("onImageFromCameraClick");

            verify(cameraLauncher).launch(any(Intent.class));
        }
    }

    @Test
    void launchCamera_showsToastAndDoesNotLaunch_whenImageUriCannotBeCreated() throws Exception {
        ContentResolver resolver = mock(ContentResolver.class);
        doReturn(resolver).when(activity).getContentResolver();
        when(resolver.insert(any(), any(ContentValues.class))).thenReturn(null);

        try (MockedStatic<Toast> toastMock = mockStatic(Toast.class)) {
            Toast toast = mock(Toast.class);
            toastMock.when(() -> Toast.makeText(any(), anyString(), eq(Toast.LENGTH_LONG))).thenReturn(toast);

            invokePrivate("launchCamera");

            toastMock.verify(() -> Toast.makeText(eq(activity), eq("Could not create image file"), eq(Toast.LENGTH_LONG)));
            verifyNoInteractions(cameraLauncher);
        }
    }

    @Test
    void launchCamera_deletesEntryAndShowsToast_whenNoCameraAppHandlesLaunch() throws Exception {
        Uri photoUri = mock(Uri.class);
        ContentResolver resolver = mock(ContentResolver.class);
        doReturn(resolver).when(activity).getContentResolver();
        when(resolver.insert(any(), any(ContentValues.class))).thenReturn(photoUri);
        doThrow(new ActivityNotFoundException()).when(cameraLauncher).launch(any(Intent.class));

        try (MockedStatic<Toast> toastMock = mockStatic(Toast.class)) {
            toastMock.when(() -> Toast.makeText(any(), anyString(), eq(Toast.LENGTH_SHORT))).thenReturn(mock(Toast.class));

            invokePrivate("launchCamera");

            verify(resolver).delete(photoUri, null, null);
            assertNull(getField("currentPhotoUri"));
            toastMock.verify(() -> Toast.makeText(eq(activity), eq("Camera is not available"), eq(Toast.LENGTH_SHORT)));
        }
    }

    @Test
    void launchCamera_stillShowsToast_whenCleanupDeleteIsNotPermitted() throws Exception {
        Uri photoUri = mock(Uri.class);
        ContentResolver resolver = mock(ContentResolver.class);
        doReturn(resolver).when(activity).getContentResolver();
        when(resolver.insert(any(), any(ContentValues.class))).thenReturn(photoUri);
        when(resolver.delete(photoUri, null, null)).thenThrow(new SecurityException("no write access"));
        doThrow(new ActivityNotFoundException()).when(cameraLauncher).launch(any(Intent.class));

        try (MockedStatic<Toast> toastMock = mockStatic(Toast.class)) {
            toastMock.when(() -> Toast.makeText(any(), anyString(), eq(Toast.LENGTH_SHORT))).thenReturn(mock(Toast.class));

            invokePrivate("launchCamera");

            assertNull(getField("currentPhotoUri"));
            toastMock.verify(() -> Toast.makeText(eq(activity), eq("Camera is not available"), eq(Toast.LENGTH_SHORT)));
        }
    }

    // ---- onImageFromGalleryClick() ----

    @Test
    void onImageFromGalleryClick_requestsPermission_whenNotGranted() throws Exception {
        try (MockedStatic<ContextCompat> contextCompat =
                     mockPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_DENIED)) {

            invokePrivate("onImageFromGalleryClick");

            verify(requestReadImagesPermission).launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            verifyNoInteractions(galleryLauncher);
        }
    }

    @Test
    void onImageFromGalleryClick_launchesGalleryPicker_whenPermissionGranted() throws Exception {
        try (MockedConstruction<Intent> mockedIntent = mockConstruction(Intent.class);
             MockedStatic<ContextCompat> contextCompat =
                     mockPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED)) {

            invokePrivate("onImageFromGalleryClick");

            verify(galleryLauncher).launch(any(Intent.class));
        }
    }
}

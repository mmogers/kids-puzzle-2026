package lv.marmog.androidpuzzlegame.exception;

// Thrown when the built-in puzzle pictures can't be loaded, so the UI can react to that
// without knowing where the pictures come from. Unchecked: a missing/unreadable assets/img
// means a broken build, not a recoverable runtime condition callers should be forced to handle.
public class PuzzleImagesUnavailableException extends RuntimeException {
    public PuzzleImagesUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

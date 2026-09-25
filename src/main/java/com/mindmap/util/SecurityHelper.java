package com.mindmap.util;

import com.mindmap.model.Note;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class SecurityHelper {
    public static boolean verifyPin(Note note) {
        if (note == null || !note.isPrivate()) {
            return true;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Private Note");
        dialog.setHeaderText("This note is private.");
        dialog.setContentText("Please enter PIN:");
        
        // Custom styling for password mask would be ideal, but standard TextInputDialog is fine for this minimal prompt.
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String entered = result.get().trim();
            if (entered.equals(note.getPin())) {
                return true;
            } else {
                UiUtils.showError("Access Denied", "Incorrect PIN.");
                return false;
            }
        }
        return false;
    }
}

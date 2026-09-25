package com.mindmap.util;

import com.mindmap.model.Note;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.geometry.Insets;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecurityHelper {
    
    public static boolean verifyPin(Note note) {
        if (note == null || !note.isPrivate()) {
            return true;
        }
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("🔒 Private Note");
        dialog.setHeaderText("This note is protected.");
        
        ButtonType unlockButtonType = new ButtonType("Unlock", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(unlockButtonType, ButtonType.CANCEL);
        
        PasswordField pwdField = new PasswordField();
        pwdField.setPromptText("PIN");
        
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.getChildren().addAll(new Label("Please enter the PIN to view this note:"), pwdField);
        
        dialog.getDialogPane().setContent(vbox);
        
        Node unlockButton = dialog.getDialogPane().lookupButton(unlockButtonType);
        unlockButton.setDisable(true);
        
        pwdField.textProperty().addListener((observable, oldValue, newValue) -> {
            unlockButton.setDisable(newValue.trim().isEmpty());
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == unlockButtonType) {
                return pwdField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String entered = result.get().trim();
            if (verifyHash(entered, note.getPin())) {
                return true;
            } else {
                UiUtils.showError("Access Denied", "Incorrect PIN.");
                return false;
            }
        }
        return false;
    }
    
    public static String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    
    private static boolean verifyHash(String rawPin, String storedHash) {
        if (storedHash == null) return false;
        // Check if it's already hashed (Base64 is typically 44 chars for SHA-256)
        // If it's a legacy unhashed PIN (e.g. "1234"), we should support it during transition
        if (storedHash.length() < 30 && storedHash.equals(rawPin)) {
            return true;
        }
        return hashPin(rawPin).equals(storedHash);
    }
}

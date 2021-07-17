package dev.kaua.squash.Notifications;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.jetbrains.annotations.NotNull;

import dev.kaua.squash.Firebase.ConfFirebase;

public class MyFirebaseIdService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);
        FirebaseUser firebaseUser = ConfFirebase.getFirebaseUser();
        if(firebaseUser != null){
            updateToken(s);
        }
    }

    private void updateToken(String s) {
        FirebaseUser firebaseUser = ConfFirebase.getFirebaseUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(s);
        reference.child(firebaseUser.getUid()).setValue(token);
    }
}

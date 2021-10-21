package dev.kaua.squash.Notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Tools.Methods;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class SenderHelper {
    private static final String TAG = "NotificationHelper";
    private static final FirebaseUser fUser = myFirebaseHelper.getFirebaseUser();
    private static final APIService apiService = Client.getClient(Methods.FCM_URL).create(APIService.class);


    public static void sendFollow(String receiver, String username){
        Query query = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.TOKENS_REFERENCE)
                .orderByKey().equalTo(receiver);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    if(token != null){
                        //  Create notification info
                        Data data = new Data(fUser.getUid(), String.valueOf(Data.TYPE_FOLLOW), username, receiver);

                        Sender sender = new Sender(data, token.getToken());

                        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                                if(response.code() == 200){
                                    assert response.body() != null;
                                    if(response.body().success != 1)
                                        Log.w(TAG, "Send Message Notification -> Failed");
                                }
                            }
                            @Override
                            public void onFailure(@NotNull Call<MyResponse> call, @NotNull Throwable t) {}
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }
}

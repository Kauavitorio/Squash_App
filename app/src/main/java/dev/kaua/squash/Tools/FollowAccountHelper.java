package dev.kaua.squash.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.AsyncUser_Follow;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.MainFragment;
import dev.kaua.squash.Notifications.SenderHelper;
import dev.kaua.squash.Security.EncryptHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressWarnings("unchecked")
public class FollowAccountHelper extends ContextWrapper {
    DtoAccount account = new DtoAccount();
    static final Retrofit retrofitUser = Methods.GetRetrofitBuilder();

    public FollowAccountHelper(Context base) {
        super(base);
    }

    public void DoFollow(long account_another_user, String username, Activity activity){
        account = new DtoAccount();
        account.setAccount_id_cry(EncryptHelper.encrypt(String.valueOf(MyPrefs.getUserInformation(getApplicationContext()).getAccount_id())));
        account.setAccount_id_following(EncryptHelper.encrypt(String.valueOf(account_another_user)));
        AccountServices services = retrofitUser.create(AccountServices.class);
        Call<DtoAccount> call = services.follow_a_user(account);
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                if(response.code() == 201){
                    Methods.LoadFollowersAndFollowing(getApplicationContext(), 1);
                    MainFragment.RefreshRecycler();
                    AsyncUser_Follow asyncUser_follow = new AsyncUser_Follow(activity);
                    asyncUser_follow.execute();

                    final DtoAccount account_chat = new DtoAccount();
                    myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot fullSnapshot) {
                            for(DataSnapshot snapshot: fullSnapshot.getChildren()){
                                DtoAccount account = snapshot.getValue(DtoAccount.class);
                                if(account != null){
                                    if(account.getUsername().equals(username)){
                                        if(account_chat.getAccount_id_cry() == null && getApplicationContext() != null){
                                            SenderHelper.sendFollow(account.getId(),
                                                    MyPrefs.getUserInformation(getApplicationContext()).getUsername());
                                        }
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                Warnings.showWeHaveAProblem(getApplicationContext(), ErrorHelper.PROFILE_FOLLOW_AN_USER);
            }
        });
    }

    public void DoUnFollow(long account_another_user, Activity activity){
        account = new DtoAccount();
        account.setAccount_id_cry(EncryptHelper.encrypt(String.valueOf(MyPrefs.getUserInformation(getApplicationContext()).getAccount_id())));
        account.setAccount_id_following(EncryptHelper.encrypt(String.valueOf(account_another_user)));
        AccountServices services = retrofitUser.create(AccountServices.class);
        Call<DtoAccount> call = services.un_follow_a_user(account);
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                if(response.code() == 201){
                    Methods.LoadFollowersAndFollowing(activity, 1);
                    AsyncUser_Follow asyncUser_follow = new AsyncUser_Follow(activity);
                    asyncUser_follow.execute();
                }
            }
            @Override
            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                Warnings.showWeHaveAProblem(activity, ErrorHelper.PROFILE_UNFOLLOW_AN_USER);
            }
        });
    }

}

package dev.kaua.squash.Activities.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.Chat.UserChatHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;

public class ProfileDetailsChatActivity extends AppCompatActivity {
    TextView txt_username, txt_name;
    ImageView img_verification;
    CircleImageView img_profile;
    LinearLayout userContainer;
    SwitchCompat mute_message_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details_chat);
        Ids();

        //  Open user profile
        userContainer.setOnClickListener(v -> {
            finish();
            MessageActivity.getInstance().OpenUserProfile();
        });
    }

    void Ids(){
        getWindow().setStatusBarColor(getColor(R.color.background_menu_sheet));

        txt_username = findViewById(R.id.txt_profile_details_user_username);
        txt_name = findViewById(R.id.profile_details_user_name);
        img_verification = findViewById(R.id.profile_details_user_verification_ic_message);
        img_profile = findViewById(R.id.profile_details_user_img_ac);
        userContainer = findViewById(R.id.base_user_profile_details_ac);
        mute_message_switch = findViewById(R.id.mute_message_switch);
        findViewById(R.id.btn_close_profile_details_chat_ac).setOnClickListener(v -> finish());

        loadProfileInfo();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    void loadProfileInfo(){
        final DtoAccount userDto = UserChatHelper.userDto;
        if(userDto == null) finish();
        else{

            //  Load Profile image
            final String imgUrl = userDto.getImageURL();
            if(imgUrl == null || imgUrl.equals(DtoAccount.DEFAULT)) img_profile.setImageResource(R.drawable.pumpkin_default_image);
            else Glide.with(this).load(EncryptHelper.decrypt(imgUrl)).into(img_profile);

            //  User Level
            if(userDto.getVerification_level() != null){
                final int verified = Methods.parseUserLevel(EncryptHelper.decrypt(userDto.getVerification_level()));
                if(verified > DtoAccount.NORMAL_ACCOUNT){
                    img_verification.setImageDrawable(getDrawable(Methods.loadUserImageLevel(verified)));
                    img_verification.setVisibility(View.VISIBLE);
                }else img_verification.setVisibility(View.GONE);
            }else img_verification.setVisibility(View.GONE);

            //  Name and username
            txt_name.setText(userDto.getName_user());
            txt_username.setText(userDto.getUsername());

            final ArrayList<String> muteList = MyPrefs.getMutedUsers(this);
            if(muteList.size() <= 0) mute_message_switch.setChecked(false);
            else
                mute_message_switch.setChecked(muteList.contains(UserChatHelper.userId));


            mute_message_switch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE)
                    .child(myFirebaseHelper.getFirebaseUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(UserChatHelper.userId != null){
                        final DataSnapshot selected = snapshot.child(myFirebaseHelper.CHAT_PREF_REFERENCE).child(UserChatHelper.userId);
                        if(!isChecked){
                            if(selected.exists()) selected.getRef().removeValue();
                            if(muteList.contains(UserChatHelper.userId)){
                                muteList.remove(UserChatHelper.userId);
                                MyPrefs.setMutedUsers(ProfileDetailsChatActivity.this, muteList);
                            }
                        }else {
                            if(!selected.exists()) {
                                if(!muteList.contains(UserChatHelper.userId)){
                                    muteList.add(UserChatHelper.userId);
                                    MyPrefs.setMutedUsers(ProfileDetailsChatActivity.this, muteList);
                                }
                                selected.getRef().setValue(true);
                            }
                        }
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            }));
        }
    }
}
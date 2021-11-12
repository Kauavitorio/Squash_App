package dev.kaua.squash.Tools.Chat;

import android.content.Intent;

import dev.kaua.squash.Activities.Chat.MessageActivity;
import dev.kaua.squash.Activities.Chat.ProfileDetailsChatActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ToastHelper;

public class UserChatHelper extends MessageActivity {
    public static DtoAccount userDto = new DtoAccount();

    public static void openProfileDetail(){
        if(userId == null || userId.length() <= 3)
            ToastHelper.toast(getInstance(), getInstance().getString(R.string.no), ToastHelper.SHORT_DURATION);
        else{
            userDto = user_im_chat;
            final Intent iDetails = new Intent(getInstance(), ProfileDetailsChatActivity.class);
            getInstance().startActivity(iDetails);
        }
    }

}

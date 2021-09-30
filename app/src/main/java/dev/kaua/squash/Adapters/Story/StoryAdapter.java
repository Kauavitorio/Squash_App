package dev.kaua.squash.Adapters.Story;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.Story.AddStoryActivity;
import dev.kaua.squash.Activitys.Story.StoryActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Stories.DtoStory;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.MyPrefs;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private static final int BASE_POS = 0;
    private static final String TAG = "STORY_ADAPTER";
    private static final int START_POS = 1;
    private final Context mContext;
    private final DtoAccount mAccount;
    private final List<DtoStory> mStory;

    public StoryAdapter(Context mContext, List<DtoStory> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
        this.mAccount = MyPrefs.getUserInformation(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == BASE_POS){
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false));
        }else{
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final DtoStory story = mStory.get(position);
        userInfo(holder, story.getUserId(), position);

        if(position != BASE_POS)
            if(holder.story_username != null) holder.story_username.setText(story.getUserName());

        if(holder.getAdapterPosition() != BASE_POS){
            if(story.isSeen()){
                holder.story_photo.setVisibility(View.GONE);
                holder.story_photo_seen.setVisibility(View.VISIBLE);
            }else{
                holder.story_photo.setVisibility(View.VISIBLE);
                holder.story_photo_seen.setVisibility(View.GONE);
            }
        }

        if(holder.getAdapterPosition() == BASE_POS){
            myStory(holder.addStory_text, holder.story_plus, false);
        }

        holder.itemView.setOnClickListener(v -> {
            if(holder.getAdapterPosition() == BASE_POS){
                myStory(holder.addStory_text, holder.story_plus, true);
            }else{
                Intent i = new Intent(mContext, StoryActivity.class);
                i.putExtra(StoryActivity.USER_ID_TAG, story.getUserId());
                i.putExtra(StoryActivity.USERNAME_TAG, holder.story_username.getText());
                i.putExtra(StoryActivity.USER_PHOTO_TAG, story.getUserPhoto());
                i.putExtra(StoryActivity.UPLOAD_TIME_TAG, story.getUploadTime());
                mContext.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView story_photo, story_plus, story_photo_seen;
        public TextView story_username,  addStory_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            story_photo = itemView.findViewById(R.id.story_photo);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_plus = itemView.findViewById(R.id.story_plus);
            story_username = itemView.findViewById(R.id.story_username);
            addStory_text = itemView.findViewById(R.id.addStory_text);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(position == BASE_POS) return BASE_POS;
        return START_POS;
    }

    private void userInfo(ViewHolder viewHolder, String userId, int pos){
        if(!userId.equals(String.valueOf(mAccount.getAccount_id()))){
            myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE)
                    .orderByChild(DtoAccount.ACCOUNT_ID_CRY).equalTo(EncryptHelper.encrypt(userId)).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot appleSnapshot: snapshot.getChildren()) {
                        final DtoAccount account = appleSnapshot.getValue(DtoAccount.class);
                        if(account != null){
                            Glide.with(mContext).load(account.getImageURL()).into(viewHolder.story_photo);
                            if(pos != BASE_POS){
                                mStory.get(pos).setUserPhoto(account.getImageURL());
                                Glide.with(mContext).load(account.getImageURL()).into(viewHolder.story_photo_seen);
                                viewHolder.story_username.setText(account.getUsername());
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {}
            });
        }else{
            Glide.with(mContext).load(mAccount.getProfile_image()).into(viewHolder.story_photo);
        }
    }

    private void myStory(TextView textView, CircleImageView imageView, boolean click){
        myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.STORY_REFERENCE)
                .child(String.valueOf(MyPrefs.getUserInformation(mContext).getAccount_id())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                DtoStory story = new DtoStory();
                long timeCurrent = System.currentTimeMillis();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    story = dataSnapshot.getValue(DtoStory.class);
                    if(story != null){
                        if(timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()){
                            count++;
                        }
                    }
                }

                if(click){
                    if(count > 0){
                        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                        DtoStory finalStory = story;
                        alert.setPositiveButton("View Story", (dialog, which) -> {
                            Intent i = new Intent(mContext, StoryActivity.class);
                            i.putExtra(StoryActivity.USER_ID_TAG, String.valueOf(MyPrefs.getUserInformation(mContext).getAccount_id()));
                            i.putExtra(StoryActivity.USERNAME_TAG, MyPrefs.getUserInformation(mContext).getUsername());
                            i.putExtra(StoryActivity.USER_PHOTO_TAG, MyPrefs.getUserInformation(mContext).getProfile_image());
                            if(finalStory.getUploadTime() != null)
                                i.putExtra(StoryActivity.UPLOAD_TIME_TAG, finalStory.getUploadTime());
                            else
                                i.putExtra(StoryActivity.UPLOAD_TIME_TAG, "");
                            mContext.startActivity(i);
                            dialog.dismiss();
                        })
                        .setNeutralButton(mContext.getString(R.string.add_story), (dialog, which) -> {
                            mContext.startActivity(new Intent(mContext, AddStoryActivity.class));
                            dialog.dismiss();
                        }).show();
                    }else
                        mContext.startActivity(new Intent(mContext, AddStoryActivity.class));
                }else{
                    if(count > 0){
                        textView.setText(mContext.getString(R.string.your_story));
                        imageView.setVisibility(View.GONE);
                    }else{
                        textView.setText(mContext.getString(R.string.add_story));
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}

package dev.kaua.squash.Adapters.Notification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Notifications.Data;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.Methods;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Activity mContext;
    private List<Data> mNotification;

    public NotificationAdapter(Activity mContext, List<Data> mNotification){
        this.mContext = mContext;
        this.mNotification = mNotification;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_notification_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull @NotNull NotificationAdapter.ViewHolder holder, int position) {
        final Data data = mNotification.get(position);
        if(data != null && data.getType() != null && data.getUser() != null){
            try{
                final int type = Integer.parseInt(data.getType());
                if(type != Data.NO_TYPE){
                    if(type == Data.TYPE_FOLLOW)
                        holder.title.setText(mContext.getString(R.string.new_follow));
                    else if(type == Data.TYPE_COMMENT)
                        holder.title.setText(mContext.getString(R.string.new_comment));
                    else if(type == Data.TYPE_MESSAGE)
                        holder.title.setText(mContext.getString(R.string.new_message));

                    SetBody(holder, data, type);
                    holder.date_time.setText(Methods.loadLastSeen(mContext, data.getDate_time()));

                    final DatabaseReference db = myFirebaseHelper.getFirebaseDatabase()
                            .getReference().child(myFirebaseHelper.USERS_REFERENCE).child(data.getUser());
                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!mContext.isFinishing() && !mContext.isDestroyed()){
                                DtoAccount account = snapshot.getValue(DtoAccount.class);
                                if(account != null && account.getId() != null){
                                    Glide.with(mContext).load(account.getImageURL()).into(holder.ic_notification);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

                }else holder.itemView.setVisibility(View.GONE);
            }catch (Exception ex){
                Log.d("NOTIFICATION_ADAPTER", ex.toString());
                holder.itemView.setVisibility(View.GONE);
            }
        }else holder.itemView.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void SetBody(@NonNull ViewHolder holder, Data data, int type) {
        if(type == Data.TYPE_FOLLOW) holder.body.setText(data.getBody() + " " + mContext.getString(R.string.started_following_you));
        else{
            if(data.getBody().length() > 60) holder.body.setText(data.getBody().substring(0, 60) + "...");
            else holder.body.setText(data.getBody());
        }
    }

    @Override
    public int getItemCount() { return mNotification.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView title, body, date_time;
        private final CircleImageView ic_notification;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.txt_title_notification);
            body = itemView.findViewById(R.id.txt_body_notification);
            date_time = itemView.findViewById(R.id.txt_date_time_notification);
            ic_notification = itemView.findViewById(R.id.ic_notification_adapter);
        }
    }

}

package dev.kaua.squash.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.AsyncLikes_Posts_Comment;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Data.Post.PostServices;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.LocalDataBase.DaoPosts;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.Warnings;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

@SuppressLint({"UseCompatLoadingForDrawables", "StaticFieldLeak"})
public class Comments_Adapters extends RecyclerView.Adapter<Comments_Adapters.MyHolderComments> {
    ArrayList<DtoPost> list;
    static Context mContext;
    static DaoPosts daoPosts;
    static DtoAccount account;
    private static BottomSheetDialog bottomSheetDialog;
    FirebaseStorage firebaseStorage;

    final Retrofit retrofit = Methods.GetRetrofitBuilder();

    public Comments_Adapters(ArrayList<DtoPost> ArrayList, Context mContext) {
        this.list = ArrayList;
        Comments_Adapters.mContext = mContext;
        daoPosts = new DaoPosts(mContext);
        account = new DtoAccount();
    }

    @NonNull
    @Override
    public MyHolderComments onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comments, parent, false);
        return new MyHolderComments(listItem);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull MyHolderComments holder, int position) {
        if(Integer.parseInt(Objects.requireNonNull(list.get(position).getVerification_level())) != 0){
            holder.ic_account_badge_comment.setVisibility(View.VISIBLE);
            if (Integer.parseInt(Objects.requireNonNull(list.get(position).getVerification_level())) == 1)
                holder.ic_account_badge_comment.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_account));
            else
                holder.ic_account_badge_comment.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_employee_account));

        }else holder.ic_account_badge_comment.setVisibility(View.GONE);
        Glide.with(mContext).load(list.get(position).getProfile_image()).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.icon_user_profile_comment);
        holder.txt_name_user_comment.setText(list.get(position).getName_user());
        holder.txt_username_comment.setText( "| @" + list.get(position).getUsername());
        holder.txt_comment_content.setText(list.get(position).getComment());
        holder.txt_post_reply_to.setText( mContext.getString(R.string.reply_to) + " @" + list.get(position).getReply_to());

        //  Apply all url on Texts Views
        Linkify.addLinks(holder.txt_comment_content, Linkify.ALL);

        //  URL CLICK'S listener
        holder.txt_comment_content.setMovementMethod(BetterLinkMovementMethod.newInstance().setOnLinkClickListener((textView, url) -> {
            if (Patterns.WEB_URL.matcher(url).matches()) {
                //An web url is detected
                Methods.browseTo(mContext, url);
                return true;
            }

            return false;
        }));

        Check_Like(holder, position);

        holder.txt_likes_comment.setText(Methods.NumberTrick(Long.parseLong(list.get(position).getLikes())));

        holder.icon_user_profile_comment.setOnClickListener(v -> {
            ((Activity)mContext).finish();
            Bundle bundle = new Bundle();
            bundle.putString("account_id", list.get(position).getAccount_id());
            bundle.putInt("control", 0);
            MainActivity.getInstance().GetBundleProfile(bundle);
            MainActivity.getInstance().CallProfile();
            ProfileFragment.getInstance().LoadAnotherUser();
        });

        EnableActions(holder, position);

        holder.btn_like_comment.setOnClickListener(v -> Like_Un_Like_A_Post(holder, position, list.get(position).getComment_id()));
    }

    private void EnableActions(MyHolderComments holder, int position) {
        holder.btn_actions.setVisibility(View.GONE);
        /*SharedPreferences sp_First = mContext.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        account.setAccount_id_cry(EncryptHelper.decrypt(sp_First.getString("pref_account_id", null)));
        if(account.getAccount_id_cry() != null){
            DtoAccount user = MyPrefs.getUserInformation(mContext);
            if(Long.parseLong(list.get(position).getAccount_id()) == Long.parseLong(user.getAccount_id() + "")){
                holder.btn_actions.setVisibility(View.VISIBLE);
                holder.btn_actions.setOnClickListener(v -> {
                    DtoPost dtoPost = new DtoPost();
                    dtoPost.setAccount_id(EncryptHelper.encrypt(user.getAccount_id() + ""));
                    dtoPost.setPost_id(EncryptHelper.encrypt(list.get(position).getPost_id()));
                    bottomSheetDialog = new BottomSheetDialog(mContext, R.style.BottomSheetTheme);
                    bottomSheetDialog.setCancelable(true);
                    //  Creating View for SheetMenu
                    View sheetView = LayoutInflater.from(mContext).inflate(R.layout.adapter_sheet_menu_post_action,
                            ((Activity)mContext).findViewById(R.id.sheet_menu_post_action));

                    sheetView.findViewById(R.id.btn_delete_post).setOnClickListener(v1 -> {
                        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                        alert.setTitle(mContext.getString(R.string.delete_post));
                        alert.setMessage(mContext.getString(R.string.delete_post_message));
                        alert.setNeutralButton(mContext.getString(R.string.no), null);
                        alert.setPositiveButton(mContext.getString(R.string.yes), (dialog, which) -> {
                            PostServices services = retrofit.create(PostServices.class);
                            Call<DtoPost> call = services.delete_post(dtoPost);

                            LoadingDialog loadingDialog = new LoadingDialog((Activity) mContext);
                            loadingDialog.startLoading();
                            call.enqueue(new Callback<DtoPost>() {
                                @Override
                                public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                                    loadingDialog.dismissDialog();
                                    if(response.code() == 200){
                                        DtoPost img_list = daoPosts.get_post_img(Long.parseLong(list.get(position).getPost_id()));
                                        if(img_list.getPost_images() != null && img_list.getPost_images().size() > 0){
                                            for (int i = 0; i < img_list.getPost_images().size(); i++){
                                                if(img_list.getPost_images().get(i) != null){
                                                    firebaseStorage = FirebaseStorage.getInstance();
                                                    StorageReference photoRef = firebaseStorage.getReferenceFromUrl(Objects.requireNonNull(EncryptHelper.decrypt(img_list.getPost_images().get(i))));
                                                    photoRef.delete().addOnSuccessListener(aVoid -> {
                                                        // File deleted successfully
                                                        Log.d("POSTS_ADAPTER", "onSuccess: deleted file");
                                                    }).addOnFailureListener(exception -> {
                                                        // Uh-oh, an error occurred!
                                                        Log.d("POSTS_ADAPTER", "onFailure: did not delete file");
                                                    });
                                                }
                                            }
                                        }
                                        list.remove(position);
                                        notifyDataSetChanged();
                                        MainFragment.RefreshRecycler();
                                    }else
                                        Warnings.showWeHaveAProblem(mContext);
                                }

                                @Override
                                public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                                    loadingDialog.dismissDialog();
                                    Warnings.showWeHaveAProblem(mContext);
                                }
                            });
                        });
                        bottomSheetDialog.dismiss();
                        alert.show();
                    });

                    sheetView.findViewById(R.id.btn_cancel_actions).setOnClickListener(v1 -> bottomSheetDialog.dismiss());

                    bottomSheetDialog.setContentView(sheetView);
                    bottomSheetDialog.show();
                });
            }else holder.btn_actions.setVisibility(View.GONE);
        }else holder.btn_actions.setVisibility(View.GONE);*/
    }

    private void Check_Like(@NotNull Comments_Adapters.MyHolderComments holder, int position) {
        SharedPreferences sp_First = mContext.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        account.setAccount_id_cry(EncryptHelper.decrypt(sp_First.getString("pref_account_id", null)));
        if(account.getAccount_id_cry() != null){
            DtoAccount user = MyPrefs.getUserInformation(mContext);
            boolean result_like = daoPosts.get_A_Like_comment(Long.parseLong(list.get(position).getComment_id()), Long.parseLong(user.getAccount_id() + ""));
            if(result_like) holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.red_heart));
            else holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.ic_heart));
        }else holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.ic_heart));
    }

    private void Like_Un_Like_A_Post(@NotNull Comments_Adapters.MyHolderComments holder, long position, String comment_id) {
        SharedPreferences sp_First = mContext.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        account.setAccount_id_cry(EncryptHelper.decrypt(sp_First.getString("pref_account_id", null)));
        if(account.getAccount_id_cry() != null){
            //  Get User info
            DtoAccount user = MyPrefs.getUserInformation(mContext);

            boolean result_like = daoPosts.get_A_Like_comment(Long.parseLong(comment_id), Long.parseLong(user.getAccount_id() + ""));
            long like_now = Long.parseLong(list.get((int) position).getLikes());
            if(result_like) {
                holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.ic_heart));
                like_now = like_now - 1;
                daoPosts.delete_like_comment(Long.parseLong(comment_id), Long.parseLong(user.getAccount_id() + ""));
            }else{
                holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.red_heart));
                like_now = like_now + 1;
                daoPosts.Register_A_Like_Comment(Long.parseLong(comment_id), Long.parseLong(user.getAccount_id() + ""));
            }

            if(like_now >= 0){
                list.get((int) position).setLikes(like_now + "");
                holder.txt_likes_comment.setText(Methods.NumberTrick(like_now));

                //  Do Like or Un Like
                DtoPost dtoPost = new DtoPost();
                dtoPost.setComment_id(EncryptHelper.encrypt(comment_id));
                dtoPost.setPost_id(EncryptHelper.encrypt(list.get((int) position).getPost_id()));
                dtoPost.setAccount_id_cry(EncryptHelper.encrypt(user.getAccount_id() + ""));
                PostServices services = retrofit.create(PostServices.class);
                Call<DtoPost> call = services.like_Un_Like_A_Post_Comment(dtoPost);
                call.enqueue(new Callback<DtoPost>() {
                    @Override
                    public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                        AsyncLikes_Posts_Comment async = new AsyncLikes_Posts_Comment((Activity) mContext , Long.parseLong(user.getAccount_id() + ""));
                        //noinspection unchecked
                        async.execute();
                    }
                    @Override
                    public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) { Warnings.showWeHaveAProblem(mContext, ErrorHelper.COMMENT_LIKE_ACTION); }
                });
            }
        }else Warnings.NeedLoginWithShortCutAlert((Activity) mContext, 0);
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class MyHolderComments extends RecyclerView.ViewHolder{
        CircleImageView icon_user_profile_comment;
        TextView txt_name_user_comment, txt_username_comment, txt_comment_content;
        TextView txt_likes_comment, txt_post_reply_to;
        ImageView ic_account_badge_comment, img_heart_like, btn_actions;
        LinearLayout btn_like_comment;

        public MyHolderComments(@NonNull View itemView) {
            super(itemView);
            icon_user_profile_comment = itemView.findViewById(R.id.icon_user_profile_comment);
            txt_name_user_comment = itemView.findViewById(R.id.txt_name_user_comment);
            btn_actions = itemView.findViewById(R.id.btn_actions);
            img_heart_like = itemView.findViewById(R.id.img_heart_like_post);
            txt_username_comment = itemView.findViewById(R.id.txt_username_comment);
            txt_comment_content = itemView.findViewById(R.id.txt_comment_content);
            btn_like_comment = itemView.findViewById(R.id.btn_like_comment);
            ic_account_badge_comment = itemView.findViewById(R.id.ic_account_badge_comment);
            txt_likes_comment = itemView.findViewById(R.id.txt_likes_comment);
            txt_post_reply_to = itemView.findViewById(R.id.txt_post_reply_to);
        }
    }
}

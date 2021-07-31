package dev.kaua.squash.Tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.WebActivity;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.AsyncLikes_Posts;
import dev.kaua.squash.Data.Post.AsyncLikes_Posts_Comment;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public abstract class Methods extends MainActivity {

    //  Base API URL
    public static final String BASE_URL = "https://squash-social.herokuapp.com/";
    public static final String FCM_URL = "https://fcm.googleapis.com/";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&.;])[A-Za-z\\d@$!%*#?&.;]{8,}$";
    private static FirebaseUser firebaseUser;
    private static DatabaseReference reference;

    //  Method to validate phone number.
    public static boolean isValidPhoneNumber(@NotNull String phone) {
        if (!phone.trim().equals("") && phone.length() > 10) return Patterns.PHONE.matcher(phone).matches();
        return false;
    }

    //  Method to remove Spaces from String.
    public static String RemoveSpace(String str){
        str = str.replaceAll("^ +| +$|( )+", "$1");
        return str;
    }

    //  Method to generate Random Characters.
    //  The only parameter is the number of characters it will return.
    private static final Random rand = new Random();
    private static final char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$!@#$!@#$".toCharArray();
    private static final char[] lettersWithoutSpecials = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    @NonNull
    public static String RandomCharacters (int CharactersAmount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CharactersAmount; i++) {
            int ch = rand.nextInt (letters.length);
            sb.append (letters [ch]);
        }
        return sb.toString();
    }

    @NonNull
    public static String RandomCharactersWithoutSpecials (int CharactersAmount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CharactersAmount; i++) {
            int ch = rand.nextInt (lettersWithoutSpecials.length);
            sb.append (lettersWithoutSpecials [ch]);
        }
        return sb.toString();
    }

    //  Method to return Default Retrofit Builder
    @NonNull
    @Contract(" -> new")
    public static Retrofit GetRetrofitBuilder(){
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, (float) pixels, (float) pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static void LoadFollowersAndFollowing(Context context, int base){
        SharedPreferences sp_First = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
        if(base == 0){
            AsyncLikes_Posts async = new AsyncLikes_Posts((Activity) context , Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(sp_First.getString("pref_account_id", null)))));
            //noinspection unchecked
            async.execute();
            AsyncLikes_Posts_Comment posts_comment = new AsyncLikes_Posts_Comment((Activity) context , Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(sp_First.getString("pref_account_id", null)))));
            //noinspection unchecked
            posts_comment.execute();
        }

        final Retrofit retrofitUser = GetRetrofitBuilder();
        SharedPreferences sp = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        DtoAccount account = new DtoAccount();
        account.setAccount_id_cry(sp.getString("pref_account_id", null));
        AccountServices services = retrofitUser.create(AccountServices.class);
        Call<DtoAccount> call = services.get_followers_following(account);
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                if(response.code() == 200){
                    DtoAccount info = new DtoAccount();
                    info.setAccount_id(Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(sp.getString("pref_account_id", null)))));
                    assert response.body() != null;
                    info.setFollowers(response.body().getFollowers());
                    info.setFollowing(response.body().getFollowing());
                    DaoAccount daoAccount = new DaoAccount(context);
                    long lines = daoAccount.Register_Followers_Following(info);
                    if(lines > 0) Log.d("LocalDataBase", "Followers and Following Update");
                    else Log.d("LocalDataBase", "Followers and Following is NOT Update");
                }
            }
            @Override
            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {}
        });
    }

    //  Method "NumberTrick" is for change number
    //  For example 1000 to 1K and 10000 to 10K


    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String NumberTrick(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return NumberTrick(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + NumberTrick(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        //noinspection ConstantConditions
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        //noinspection IntegerDivisionInFloatingPointContext
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    //  Method to know if user is has internet connection
    public static boolean isOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null &&
                manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    protected static void makeLinkClickable(Context context, SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                browseTo(context, span.getURL());
                // Do something with span.getURL() to handle the link click...
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    public static void browseTo(Context context, String url){
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
        Intent i = new Intent(context, WebActivity.class);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    //  Method to set new user status for chat system
    public static void status_chat(String status, Context context){
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_date = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
        String formattedDate = df_date.format(c.getTime());
        firebaseUser = ConfFirebase.getFirebaseUser();
        //noinspection ConstantConditions
        if(firebaseUser.getUid() != null){
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            if(status.equals("offline"))
                hashMap.put("last_seen", formattedDate);
            hashMap.put("status_chat", status);
            hashMap.put("verification_level", EncryptHelper.encrypt(MyPrefs.getUserInformation(context).getVerification_level()));

            reference.updateChildren(hashMap);
        }
    }

    //  Method to update typing status for chat system
    public static void typingTo_chat_Status(String typing){
        firebaseUser = ConfFirebase.getFirebaseUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        reference.updateChildren(hashMap);
    }

    //  Method to load last seen
    public static String loadLastSeen(Context context, String get_date_time){
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
        String formattedDate = df_time.format(c.getTime());
        try {
            String[] splitDate = formattedDate.split("/");
            String[] splitTime = formattedDate.split(" ");
            String[] splitDateGet = get_date_time.split("/");
            int day = Integer.parseInt(splitDate[0]) - Integer.parseInt(splitDateGet[0]);
            String year = splitDate[2].substring(0, 4);
            String yearGET = splitDateGet[2].substring(0, 4);
            if(splitDate[0].equals(splitDateGet[0]) && splitDate[1].equals(splitDateGet[1]) && year.equals(yearGET)){
                String time_GET = splitDateGet[2].substring(4, 10);
                myTimeHelper now = myTimeHelper.now();
                myTimeHelper now_GET = myTimeHelper.parse(time_GET.replace(" ", ""));
                return context.getString(R.string.today) + " "  + showTimeAgo(now_GET, now + "", context);
            }
            else if(day == 1 && splitDate[1].equals(splitDateGet[1]) && year.equals(yearGET))
                return context.getString(R.string.yesterday) + " " + splitTime[1];
            else return get_date_time;
        }catch (Exception ex){
            Log.d("LastSeen", ex.toString());
            return get_date_time;
        }
    }

    public static String loadLastSeenUser(Context context, String get_date_time){
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
        String formattedDate = df_time.format(c.getTime());
        try {
            String[] splitDate = formattedDate.split("/");
            String[] splitTime = formattedDate.split(" ");
            String[] splitDateGet = get_date_time.split("/");
            int day = Integer.parseInt(splitDate[0]) - Integer.parseInt(splitDateGet[0]);
            String year = splitDate[2].substring(0, 4);
            String yearGET = splitDateGet[2].substring(0, 4);
            if(splitDate[0].equals(splitDateGet[0]) && splitDate[1].equals(splitDateGet[1]) && year.equals(yearGET)){
                String time_GET = splitDateGet[2].substring(4, 10);
                myTimeHelper now = myTimeHelper.now();
                myTimeHelper now_GET = myTimeHelper.parse(time_GET.replace(" ", ""));
                return context.getString(R.string.today) + " "  + showTimeAgo(now_GET, now + "", context);
            }
            else if(day == 1 && splitDate[1].equals(splitDateGet[1]) && year.equals(yearGET))
                return context.getString(R.string.yesterday) + " " + splitTime[1];
            else return get_date_time;
        }catch (Exception ex){
            Log.d("LastSeen", ex.toString());
            return get_date_time;
        }
    }

    public static String shuffle(String s) {
        List<String> letters = Arrays.asList(s.split(""));
        Collections.shuffle(letters);
        StringBuilder t = new StringBuilder(s.length());
        for (String k : letters) {
            t.append(k);
        }
        return t.toString();
    }

    private static String showTimeAgo(myTimeHelper now, String goal, Context context) {
        myTimeHelper desired = myTimeHelper.parse(goal);
        myTimeHelper lack = desired.difference(now);
        String result =  lack + "";
        String[] result_split = result.split(":");
        if(result_split[0].equals("00")) result = result_split[1] + " " + context.getString(R.string.minutes_ago);
        else if(Integer.parseInt(result_split[0]) > 1) result = result_split[0] + " " + context.getString(R.string.hours_ago);
        else result = result_split[0].replace("0", "") + " " + context.getString(R.string.hour_ago);
        return result;
    }

    private static SharedPreferences mPrefs;
    @SuppressLint("MutatingSharedPrefs")
    public static void PinAUser_Chat(Context context, String userId){
        ToastHelper.toast((Activity)context, context.getString(R.string.under_development), 0);
        /*mPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        //Retrieve the values
        Set<String> set = mPrefs.getStringSet("pinned_users_chat", null);
        SharedPreferences.Editor editor = mPrefs.edit();

        if (set == null)
            set = new HashSet<>();

        if(!set.contains(userId))
            set.add(userId);

        //Set the values
        Set<String> set_list = new HashSet<>(set);
        editor.putStringSet("pinned_users_chat", set_list);
        editor.apply();*/
    }

    public static Set<String> GetPinnedChat(Context context){
        mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        //Retrieve the values
        return mPrefs.getStringSet("pinned_users_chat", null);
    }

    // This method can be used in the future
    /*public boolean isValidPhone(String phone) {
        if (!phone.matches("^[+]?[0-9]{10,13}$"))
            return false;
        else
            return android.util.Patterns.PHONE.matcher(phone).matches();
    }*/
}

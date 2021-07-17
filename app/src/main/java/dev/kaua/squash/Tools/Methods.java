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
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public abstract class Methods extends MainActivity {

    //  Base API URL
    private static final String BASE_URL = "https://dev-river-api.herokuapp.com/";
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
    public static String RandomCharacters (int CharactersAmount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CharactersAmount; i++) {
            int ch = rand.nextInt (letters.length);
            sb.append (letters [ch]);
        }
        return sb.toString();
    }

    //  Method to return Default Retrofit Builder
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

    public static void LoadFollowersAndFollowing(Context context){
        final Retrofit retrofitUser = GetRetrofitBuilder();
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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

    public static String NumberTrick(int number) {
        String numberString = "";
        if (Math.abs(number / 1000000) > 1)
            numberString = (number / 1000000) + "m";
        else if (Math.abs(number / 1000) > 1)
            numberString = (number / 1000) + "k";
        else
            numberString = number + "";
        return numberString;
    }

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

    public static void setTextViewHTML(Context context, TextView text, String html)
    {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(context, strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void browseTo(Context context, String url){
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }


    public static void status_chat(String status){
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("dd MMMM yyyy HH:mm a");
        String formattedDate = df_time.format(c.getTime());
        firebaseUser = ConfFirebase.getFirebaseUser();
        //noinspection ConstantConditions
        if(firebaseUser.getUid() != null){
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            if(status.equals("offline"))
                hashMap.put("last_seen", formattedDate);
            hashMap.put("status_chat", status);

            reference.updateChildren(hashMap);
        }
    }

    public static void typingTo_chat_Status(String typing){
        firebaseUser = ConfFirebase.getFirebaseUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        reference.updateChildren(hashMap);
    }

    public static String loadLastSeen(Context context, String get_date_time){
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("dd MMMM yyyy HH:mm a");
        String formattedDate = df_time.format(c.getTime());
        String[] splitDate = formattedDate.split(" ");
        String[] splitDateGet = get_date_time.split(" ");
        int day = Integer.parseInt(splitDate[0]) - Integer.parseInt(splitDateGet[0]);

        if(splitDate[0].equals(splitDateGet[0]) && splitDate[1].equals(splitDateGet[1]) && splitDate[2].equals(splitDateGet[2]))
            return context.getString(R.string.today) + " " + splitDateGet[3];
        else if(day == 1 && splitDate[1].equals(splitDateGet[1]) && splitDate[2].equals(splitDateGet[2]))
            return context.getString(R.string.yesterday) + " "  + splitDateGet[3];
        else return get_date_time;
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
        mPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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

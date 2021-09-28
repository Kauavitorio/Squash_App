package dev.kaua.squash.Tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.WebActivity;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.AsyncLikes_Posts;
import dev.kaua.squash.Data.Post.AsyncLikes_Posts_Comment;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import okhttp3.OkHttpClient;
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
    private static final String TAG = "METHODS_LOG";
    public static final String SQUASH_ORIGINAL_USERNAME = "squash";
    public static final String REWARDED_AD_ID = "ca-app-pub-5161149668539506/2830793531";
    public static final String INTERSTICIAL_AD_ID = "ca-app-pub-5161149668539506/3847296073";
    public static final long VERIFY_AD_GOAL = 20000;
    public static final String PAYPAL_DONATE = "https://www.paypal.com/donate?hosted_button_id=PRKZAKGHHKA7S";
    public static final String GOOGLE_PLAY_APP_LINK = "https://play.google.com/store/apps/details?id=dev.kaua.squash";
    public static final String GOOGLE_PLAY_APP_LINK_SHORT = "https://squashc.com/url/app";
    public static final String BASE_URL_HTTPS = "https://squash-social.herokuapp.com/";
    public static final String BASE_URL_HTTP = "http://squash-social.herokuapp.com/";
    public static final String FCM_URL = "https://fcm.googleapis.com/";
    public static final String DOMAIN_SHORT_DYNAMIC_LINKS = "https://squashc.com/url/";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&.;])[A-Za-z\\d@$!%*#?&.;]{8,}$";
    private static FirebaseUser firebaseUser;
    private static DatabaseReference reference;
    private static long account_id_hold;

    //  Method to validate phone number.
    public static boolean isValidPhoneNumber(@NotNull String phone) {
        if (!phone.trim().equals("") && phone.length() > 10) return Patterns.PHONE.matcher(phone).matches();
        return false;
    }

    public static void HoldId(long id){ account_id_hold = id;}
    public static long getIdHold(){ return account_id_hold;}

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
    public static String RandomCharactersWithoutSpecials (final int CharactersAmount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CharactersAmount; i++) {
            int ch = rand.nextInt (lettersWithoutSpecials.length);
            sb.append (lettersWithoutSpecials [ch]);
        }
        return sb.toString();
    }

    //  Method to return Default Retrofit Builder
    static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS).build();

    @NonNull
    @Contract(" -> new")
    public static Retrofit GetRetrofitBuilder(){
        return new Retrofit.Builder()
                .baseUrl(BASE_URL_HTTPS)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static void SharePost(Activity mContext, DtoPost postInfo, FirebaseAnalytics mFirebaseAnalytics){

        Intent myIntent = new Intent(Intent.ACTION_SEND);
        myIntent.setType("text/plain");
        final String search_from;
        if(getUserLevel(mContext) == DtoAccount.ACCOUNT_IS_ADM) search_from = "STAFF";
        else search_from = Methods.RandomCharactersWithoutSpecials(3);
        String body = Methods.BASE_URL_HTTPS + "share/" + postInfo.getUsername().replace(" ", "")
                + "/post/" +  postInfo.getPost_id()
                + "?s=" + search_from;
        body = body.replace(" ", "");
        myIntent.putExtra(Intent.EXTRA_TEXT, body);


        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext, R.style.BottomSheetTheme);
        //  Creating View for SheetMenu
        View sheetView = LayoutInflater.from(mContext).inflate(R.layout.adapter_sheet_post_options_share,
                mContext.findViewById(R.id.main_container_sheet_options_share));
        TextView close = sheetView.findViewById(R.id.hide_share_options_post);

        sheetView.findViewById(R.id.card_hold_to_close_post_share_option).setElevation(0);


        final String SHARE_URL = body;
        //  Copy Link Click
        sheetView.findViewById(R.id.copy_link_option_share).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("URL_" + SHARE_URL, SHARE_URL);
            clipboard.setPrimaryClip(clip);
            ToastHelper.toast(mContext, mContext.getString(R.string.url_copied), ToastHelper.SHORT_DURATION);
            bottomSheetDialog.dismiss();
        });

        //  Share Click
        sheetView.findViewById(R.id.share_option_share).setOnClickListener(v -> {
            mContext.startActivity(Intent.createChooser(myIntent, mContext.getString(R.string.share_using)));

            //  Creating analytic for share action
            Bundle bundle_Analytics = new Bundle();
            bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_ID, myFirebaseHelper.getFirebaseUser().getUid() + "_" + postInfo.getPost_id());
            bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_NAME, postInfo.getPost_id());
            bundle_Analytics.putString(FirebaseAnalytics.Param.CONTENT_TYPE, postInfo.getUsername());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle_Analytics);
            bottomSheetDialog.dismiss();
        });

        //  Report Click
        sheetView.findViewById(R.id.report_option_share).setOnClickListener(v -> ToastHelper.toast(mContext, mContext.getString(R.string.under_development), ToastHelper.SHORT_DURATION));

        close.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
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

    public static void LoadFollowersAndFollowing(@NonNull Context context, final int base){
        if(MyPrefs.getUserInformation(context).getAccount_id() != DtoAccount.ACCOUNT_DISABLE){
            if(base == 0){
                AsyncLikes_Posts async = new AsyncLikes_Posts((Activity) context , MyPrefs.getUserInformation(context).getAccount_id(), AsyncLikes_Posts.NOT_NOTIFY);
                //noinspection unchecked
                async.execute();
                AsyncLikes_Posts_Comment posts_comment = new AsyncLikes_Posts_Comment((Activity) context , MyPrefs.getUserInformation(context).getAccount_id());
                //noinspection unchecked
                posts_comment.execute();
            }

            if(base != 999){
                final Retrofit retrofitUser = GetRetrofitBuilder();
                SharedPreferences sp = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                final DtoAccount account = new DtoAccount();
                account.setAccount_id_cry(sp.getString("pref_account_id", null));
                AccountServices services = retrofitUser.create(AccountServices.class);
                Call<DtoAccount> call = services.get_followers_following(account);
                call.enqueue(new Callback<DtoAccount>() {
                    @Override
                    public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                        if(response.code() == 200 && response.body() != null){
                            final DtoAccount info = new DtoAccount();
                            final long id = MyPrefs.getUserInformation(context).getAccount_id();
                            if(id > DtoAccount.ACCOUNT_DISABLE){
                                info.setAccount_id(id);
                                info.setFollowers(response.body().getFollowers());
                                info.setFollowing(response.body().getFollowing());
                                DaoAccount daoAccount = new DaoAccount(context);
                                long lines = daoAccount.Register_Followers_Following(info);
                                if(lines > 0) Log.d("LocalDataBase", "Followers and Following Update");
                                else Log.d("LocalDataBase", "Followers and Following is NOT Update");
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {}
                });
            }
        }
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

    public static String NumberTrick(final long value) {
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

    public static void browseTo(Context context, @NonNull String url){
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
        Intent i = new Intent(context, WebActivity.class);
        i.setData(Uri.parse(url));
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.move_to_right_back, R.anim.move_to_right_go);
        ActivityCompat.startActivity(context, i, activityOptionsCompat.toBundle());
    }

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    //  Method to set new user status for chat system
    public static void status_chat(String status, Context context){
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_date = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
        String formattedDate = df_date.format(c.getTime());
        firebaseUser = myFirebaseHelper.getFirebaseUser();
        //noinspection ConstantConditions
        if(firebaseUser != null && firebaseUser.getUid() != null){
            reference = null;
            reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE).child(firebaseUser.getUid());
            final HashMap<String, Object> hashMap = new HashMap<>();
            if(status.equals(OFFLINE))
                hashMap.put("last_seen", formattedDate);
            hashMap.put("status_chat", status);
            hashMap.put("verification_level", EncryptHelper.encrypt(String.valueOf(getUserLevel(context))));

            reference.updateChildren(hashMap);
        }
    }

    public static String getMonth(int month) {
        try {
            return new DateFormatSymbols().getMonths()[month-1];
        }catch (Exception ex){
            return String.valueOf(month);
        }
    }

    public static long getUserLevel(Context context){
       try {
           return Long.parseLong(MyPrefs.getUserInformation(context).getVerification_level());
       }catch (Exception ex){
           return DtoAccount.NORMAL_ACCOUNT;
       }
    }

    static final int min = 35;
    static final int max = 100;
    public static int getRandomAmount(){
        return (int) Math.floor(Math.random()*(max-min+1)+min);
    }

    public static final String NO_ONE = "noOne";
    //  Method to update typing status for chat system
    public static void typingTo_chat_Status(String typing){
        firebaseUser = null;
        reference = null;
        firebaseUser = myFirebaseHelper.getFirebaseUser();
        reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE).child(firebaseUser.getUid());
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
                if(showTimeAgo(now_GET, now + "", context).contains("00")) return context.getString(R.string.just_now);
                else return showTimeAgo(now_GET, now + "", context);
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

    public static String shuffle(@NonNull String s) {
        List<String> letters = Arrays.asList(s.split(""));
        Collections.shuffle(letters);
        StringBuilder t = new StringBuilder(s.length());
        for (String k : letters) {
            t.append(k);
        }
        return t.toString();
    }

    public static Bitmap mergeBitmaps(final Bitmap logo, final Bitmap qrcode) {
        final Bitmap combined = Bitmap.createBitmap(qrcode.getWidth(), qrcode.getHeight(), qrcode.getConfig());
        final Canvas canvas = new Canvas(combined);
        final int canvasWidth = canvas.getWidth();
        final int canvasHeight = canvas.getHeight();
        canvas.drawBitmap(qrcode, new Matrix(), null);

        Bitmap resizeLogo = Bitmap.createScaledBitmap(logo, canvasWidth / 5, canvasHeight / 5, true);
        final int centreX = (canvasWidth - resizeLogo.getWidth()) /2;
        final int centreY = (canvasHeight - resizeLogo.getHeight()) / 2;
        canvas.drawBitmap(resizeLogo, centreX, centreY, null);
        return combined;
    }

    private static String showTimeAgo(myTimeHelper now, String goal, Context context) {
        String result = "";
        try {
            myTimeHelper desired = myTimeHelper.parse(goal);
            myTimeHelper lack = desired.difference(now);
            result = String.valueOf(lack);
            String[] result_split = result.split(":");
            if(result_split[0].equals("00")) result = result_split[1] + context.getString(R.string.m_ago);
            else if(Integer.parseInt(result_split[0]) > 1) result = result_split[0] + context.getString(R.string.h_ago);
            else result = result_split[0].replace("0", "") + context.getString(R.string.h_ago);
        }catch (Exception ex){
            Log.d(TAG, ex.toString());
        }
        return result;
    }

    public static final int VIBRATE_SHORT = 200;
    public static final int VIBRATE_LONG = 500;
    private static Vibrator vibrator;
    public static void vibrate(Context context, int VIBRATE_TIME) {
        try {
            if(vibrator == null) vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_TIME);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public static File SaveImage(Context mContext, Bitmap finalBitmap, @NonNull String image_id, String timeReceive) {
        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/Squash Images");
        //noinspection ResultOfMethodCallIgnored
        myDir.mkdirs();

        String fName = "IMG-"+ timeReceive + image_id.substring(0, 11) + ".jpg";
        File file = new File (myDir, fName.toUpperCase());
        if (file.exists()) {
            Log.d("ExternalStorage", "Image already exists");
        }else{
            fName = "IMG-"+ timeReceive + "_" + image_id.substring(0, 11) + ".jpg";
            file = new File (myDir, fName.toUpperCase());
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("ExternalStorage", e.toString());
            }
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(mContext, new String[]{file.toString()}, null,
                    (path, uri) -> {
                        Log.d("ExternalStorage", "Time -> " + timeReceive);
                        Log.d("ExternalStorage", "Scanned " + path + ":");
                        Log.d("ExternalStorage", "uri -> " + uri);
                    });
        }
        return file;
    }

    public static Uri bitmapToUriConverter(Context mContext, Bitmap mBitmap) {
        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 200, 200,
                    true);
            File file = new File(mContext.getFilesDir(), "Image"
                    + new Random().nextInt() + ".jpeg");
            FileOutputStream out = mContext.openFileOutput(file.getName(),
                    Context.MODE_WORLD_READABLE);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //get absolute path
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);

        } catch (Exception e) {
            Log.e("Your Error Message", e.getMessage());
        }
        return uri;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public static void Profile_From_USERNAME(Activity mContext, String text) {
        if(ConnectionHelper.isOnline(mContext)){
            DtoAccount account = new DtoAccount();
            account.setUsername(text.replace("@", ""));
            AccountServices services = GetRetrofitBuilder().create(AccountServices.class);
            Call<DtoPost> call = services.search_with_username(account);
            LoadingDialog loadingDialog = new LoadingDialog(mContext);
            loadingDialog.startLoading();
            call.enqueue(new Callback<DtoPost>() {
                @Override
                public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                    loadingDialog.dismissDialog();
                    if(response.code() == 200){
                        if(response.body() != null){
                            Bundle bundle = new Bundle();
                            bundle.putString("account_id", response.body().getAccount_id());
                            bundle.putInt("control", 0);
                            MainActivity.getInstance().GetBundleProfile(bundle);
                            MainActivity.getInstance().CallProfile();
                            ProfileFragment.getInstance().LoadAnotherUser();
                        }
                    }else ToastHelper.toast(mContext, mContext.getString(R.string.user_not_found), ToastHelper.SHORT_DURATION);
                }
                @Override
                public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                    loadingDialog.dismissDialog();
                    Warnings.showWeHaveAProblem(mContext, ErrorHelper.PROFILE_MENTION_CLICK);
                }
            });
        }else ToastHelper.toast(mContext, mContext.getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
    }

    // This method can be used in the future
    /*public boolean isValidPhone(String phone) {
        if (!phone.matches("^[+]?[0-9]{10,13}$"))
            return false;
        else
            return android.util.Patterns.PHONE.matcher(phone).matches();
    }*/
}

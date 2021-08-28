package dev.kaua.squash.Tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class ConnectionHelper {
    private static final String DEBUG_TAG = "NetworkStatus";
    Context context;

    public ConnectionHelper(Context base_context){
        this.context = base_context;

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                isWifiConn |= networkInfo.isConnected();
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                isMobileConn |= networkInfo.isConnected();
        }
        Log.d(DEBUG_TAG, "Wifi connected: " + isWifiConn);
        Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);
    }
     public ConnectionHelper(){}

    //  Method to know if user is has internet connection
    public static boolean isOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null &&
                manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public Long getUidRxBytes(int uid) {
        BufferedReader reader;
        long rxBytes = 0L;
        try {
            reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid
                    + "/tcp_rcv"));
            rxBytes = Long.parseLong(reader.readLine());
            reader.close();
        }
        catch (FileNotFoundException e) {
            rxBytes = TrafficStats.getUidRxBytes(uid);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(DEBUG_TAG, "RX in bytes: " + rxBytes);
        return rxBytes;
    }

    public Long getUidTxBytes(int uid) {
        BufferedReader reader;
        long txBytes = 0L;
        try {
            reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid
                    + "/tcp_snd"));
            txBytes = Long.parseLong(reader.readLine());
            reader.close();
        }
        catch (FileNotFoundException e) {
            txBytes = TrafficStats.getUidTxBytes(uid);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(DEBUG_TAG, "TX in bytes: " + txBytes);
        return txBytes;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) return bytes + " B";
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static String getIp(Context context){
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
}
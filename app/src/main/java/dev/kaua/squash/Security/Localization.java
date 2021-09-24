package dev.kaua.squash.Security;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.Locale;

public class Localization {
    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                Locale loc = new Locale(Locale.getDefault().getLanguage(), simCountry.toLowerCase(Locale.US));
                return loc.getDisplayCountry();
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    Locale loc = new Locale(Locale.getDefault().getLanguage(), networkCountry.toLowerCase(Locale.US));
                    return loc.getDisplayCountry();
                }
            }
        }
        catch (Exception ignored) { }
        return null;
    }
}
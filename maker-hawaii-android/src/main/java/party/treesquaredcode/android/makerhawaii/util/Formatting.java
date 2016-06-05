package party.treesquaredcode.android.makerhawaii.util;

/**
 * Created by rht on 6/4/16.
 */
public class Formatting {
    public static String distanceString(float meters) {
        if (meters < 1000f) {
            return String.format("%.2fm", meters);
        }
        return String.format("%.2fkm", meters / 1000f);
    }
}

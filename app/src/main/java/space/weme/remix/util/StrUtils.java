package space.weme.remix.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import space.weme.remix.APP;
import space.weme.remix.R;

/**
 * Created by Liujilong on 16/1/22.
 * liujilong.me@gmail.com
 */
public final class StrUtils {

    private StrUtils() {
    }

    public static final String PHONE_PATTERN =
            "^1[34578]\\d{9}$";


    /**
     * ####################### URLs ###############################
     **/
    public static final String BASE_URL = "http://218.244.147.240:8080/";
//    public static final String BASE_URL = "http://samuellcn.6655.la:18973/weme/";
//    public static final String BASE_URL = "http://192.168.199.182:8080/weme/";

    private static final String BASE_URL_NGINX = "http://218.244.147.240/";

    public static final String GET_VISIT_INFO = BASE_URL + "visitinfo";

    public static final String VISIT_USER = BASE_URL + "visit";

    public static final String GET_TIME_LINE_URL = BASE_URL + "getusertimeline";

    public static final String GET_USER_IMAGES_URL = BASE_URL + "getuserimages";

    public static final String GET_PERSIONAL_IMAGE_URL = BASE_URL + "getpersonalimages";

    public static final String GET_FOLLOWERS_URL = BASE_URL + "followview";

    public static final String SEARCH_USER_URL = BASE_URL + "searchuser";

    public static final String GET_USER_MESSAGE_LIST = BASE_URL + "getSendUserList";

    public static final String GET_SYSTEM_NOTIFICATION = BASE_URL + "systemnotification";

    public static final String READ_MESSAGE = BASE_URL + "readmessage";

    public static final String GET_MESSAGE_DETAIL = BASE_URL + "getMessageDetailList";

    public static final String SEND_MESSAGE = BASE_URL + "sendmessage";

    public static final String EDIT_CARD_SETTING = BASE_URL + "editprofile/editcardsetting";

    public static final String CHECK_UPDATE_URL = BASE_URL + "checkapkversion";

    public static final String LIKE_USER_CARD = BASE_URL + "likeusercard";

    public static final String GET_LIKE_COUNT = BASE_URL + "getlikeusernumber";

    public static final String READ_COMMUNITY_NOTIFICATION = BASE_URL + "readcommunitynotification";

    public static final String GET_TAGS_BY_ID = BASE_URL + "gettagsbyid";

    public static final String SET_TAGS = BASE_URL + "settags";

    public static final String GET_AVATAR = BASE_URL_NGINX + "avatar/";

    public static final String UPLOAD_AVATAR_URL = BASE_URL_NGINX + "uploadavatar";

    public static final String GET_BACKGROUND = BASE_URL_NGINX + "background/";

    public static String thumForID(String id) {
        return GET_AVATAR + id + "_thumbnail.jpg";
    }

    public static String cardForID(String id) {
        return GET_AVATAR + id + "-1_card.jpg";
    }

    public static String avatarForID(String id) {
        return GET_AVATAR + id;
    }

    public static String backgroundForID(String id) {
        return GET_BACKGROUND + id;
    }


    /**
     * ################## SharedPreferences #######################
     **/

    public static final String SP_USER = "StrUtils_sp_user";
    public static final String SP_USER_TOKEN = SP_USER + "_token";
    public static final String SP_USER_ID = SP_USER + "_id";
    public static final String SP_USER_GENDER = SP_USER + "_gender";
    public static final String SP_USER_CAN_FOUND = SP_USER + "_can_found";


    public static final MediaType MEDIA_TYPE_IMG = MediaType.parse("image/*");

    public static String timeTransfer(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EE, d LLLL yyyy HH:mm:ss zzzz", Locale.US);
        try {
            Date date = sdf.parse(timestamp);
            long seconds = date.getTime();
            long gmtDif = TimeZone.getDefault().getRawOffset();
            long now = System.currentTimeMillis();
            long dif = (now - seconds + gmtDif) / 1000;
            return timeTransfer(dif);
        } catch (ParseException e) {
            return "";
        }
    }

    public static String timeTransfer(long dif) {
        if (dif < 3600) {
            return (dif / 60) + APP.context().getString(R.string.minute_ago);
        } else if (dif < 24 * 3600) {
            return (dif / 3600) + APP.context().getString(R.string.hour_ago);
        } else if (dif < 7 * 24 * 3600) {
            return (dif / 3600 / 24) + APP.context().getString(R.string.day_ago);
        } else if (dif < 30 * 24 * 3600) {
            return (dif / 3600 / 24 / 7) + APP.context().getString(R.string.week_ago);
        } else if (dif < 365 * 30 * 24 * 3600) {
            return (dif / 3600 / 24 / 30) + APP.context().getString(R.string.month_ago);
        } else {
            return (dif / 3600 / 24 / 365) + APP.context().getString(R.string.year_ago);
        }
    }

    public static String distanceTransfer(double distance) {
        return String.format("%.1f", distance / 1000) + APP.context().getString(R.string.thousand_meters);

    }


    public static String token() {
        SharedPreferences sp = APP.context().getSharedPreferences(SP_USER, Context.MODE_PRIVATE);
        return sp.getString(SP_USER_TOKEN, "");
    }

    public static String id() {
        SharedPreferences sp = APP.context().getSharedPreferences(SP_USER, Context.MODE_PRIVATE);
        return sp.getString(SP_USER_ID, "");
    }

    public static String md5(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] inputBytes = input.getBytes();
            byte[] outputBytes = messageDigest.digest(inputBytes);
            return bytesToHex(outputBytes);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in setId(int)
     * This value will not collide with id values generated at build time by aapt for R.id.
     *
     * @return a generated id value
     */
    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }


    public static final String cropFilePath = Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg";
}

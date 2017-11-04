package com.alexeyturkin.justrss.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Turkin A. on 02.11.2017.
 */

public class AppUtilities {

    public static final String TAG = AppUtilities.class.getSimpleName();

    private AppUtilities() {
    }

    public static final String COMPLETE_FEED_URL = "https://newsapi.org/v1/articles?source=bbc-news&sortBy=top&apiKey=6f80087bb0ff4f3184424dbe9a22499c";

    public static final String API_KEY = "6f80087bb0ff4f3184424dbe9a22499c";

    public static final String BASE_URL = "https://newsapi.org/v1/";

    public static final String BBC_ARTICLES_SOURCE = "articles?source=bbc-news";

    public static final String SORT_OPTION = "top";

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (cm != null) {
            netInfo = cm.getActiveNetworkInfo();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}

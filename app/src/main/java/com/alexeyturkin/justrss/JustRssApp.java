package com.alexeyturkin.justrss;

import android.app.Application;

import com.alexeyturkin.justrss.utils.AppUtilities;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmObject;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Turkin A. on 02.11.2017.
 */

public class JustRssApp extends Application {

    public static final String TAG = JustRssApp.class.getSimpleName();

    static Retrofit sRetrofit = null;

    static JustRssApp sInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        Realm.init(this);

        buildRetrofit();
    }

    private static void buildRetrofit() {
        Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                if (AppUtilities.isOnline(sInstance)) {
                    int maxAge = 60; // read from cache for 1 minute
                    return originalResponse.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .build();
                } else {
                    int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                    return originalResponse.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .build();
                }
            }
        };

        File httpCacheDirectory = new File(sInstance.getExternalCacheDir(), "responses");
        int cacheSize = 50 * 1024 * 1024; // 50 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .cache(cache)
                .build();

        sRetrofit = new Retrofit.Builder()
                .baseUrl(AppUtilities.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static Retrofit getRetrofitInstance() {
        if (sRetrofit == null) {
            buildRetrofit();
        }

        return sRetrofit;
    }
}

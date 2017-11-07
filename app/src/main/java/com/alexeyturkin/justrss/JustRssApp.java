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

    @Override
    public void onCreate() {
        super.onCreate();

        buildRetrofit();
    }

    private static void buildRetrofit() {
        sRetrofit = new Retrofit.Builder()
                .baseUrl(AppUtilities.BASE_URL)
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

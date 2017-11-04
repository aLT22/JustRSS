package com.alexeyturkin.justrss.rest.api.service;

import com.alexeyturkin.justrss.rest.model.FeedResponse;
import com.alexeyturkin.justrss.utils.AppUtilities;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Turkin A. on 02.11.2017.
 */

public interface NewYorkTimesService {

    @GET(AppUtilities.BBC_ARTICLES_SOURCE)
    Observable<FeedResponse> getObservableFeed(@Query("sortBy") String sortBy,
                                               @Query("apiKey") String apiKey);

    @GET(AppUtilities.BBC_ARTICLES_SOURCE)
    Call<FeedResponse> getCallFeed(@Query("sortBy") String sortBy,
                                   @Query("apiKey") String apiKey);

}

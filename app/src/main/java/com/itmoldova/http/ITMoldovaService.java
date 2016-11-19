package com.itmoldova.http;

import com.itmoldova.model.Rss;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Author vgrec, on 09.07.16.
 */
public interface ITMoldovaService {
    @GET("/feed")
    Observable<Rss> getDefaultRssFeed(@Query("paged") int page);

    @GET("/category/{category_name}/feed")
    Observable<Rss> getRssFeedByCategory(@Path("category_name") String category, @Query("paged") int page);
}
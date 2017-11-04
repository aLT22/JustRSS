package com.alexeyturkin.justrss.rest.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by Turkin A. on 03.11.2017.
 */

@Dao
public interface ArticleDao {

    @Insert
    void insertArticles(Article... articles);

    @Update
    void updateArticle(Article article);

    @Insert
    void insertArticles(List<Article> articles);

    @Delete
    void deleteArticle(Article... articles);

    @Query("SELECT * FROM article")
    Flowable<List<Article>> getFlowableArticles();

    @Query("SELECT * FROM article")
    List<Article> getListArticles();

    /*@Query("SELECT * FROM article")
    Observable<List<Article>> getObservableArticles();*/

}

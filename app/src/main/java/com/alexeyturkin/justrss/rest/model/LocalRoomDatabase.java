package com.alexeyturkin.justrss.rest.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Turkin A. on 03.11.2017.
 */

@Database(entities = {Article.class}, version = 1)
public abstract class LocalRoomDatabase extends RoomDatabase {

    public abstract ArticleDao getArticleDao();

}

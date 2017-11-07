package com.alexeyturkin.justrss.local.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.alexeyturkin.justrss.rest.model.Article;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Turkin A. on 04.11.17.
 */

public class LocalArticle extends RealmObject implements Parcelable {

    @PrimaryKey
    private String id;

    private String author;

    private String title;

    private String description;

    private String url;

    private String urlToImage;

    private String publishedAt;

    private byte[] compressedImage = null;

    public LocalArticle() {
    }

    public LocalArticle(Parcel parcel) {
        this.author = parcel.readString();
        this.title = parcel.readString();
        this.description = parcel.readString();
        this.url = parcel.readString();
        this.urlToImage = parcel.readString();
        this.publishedAt = parcel.readString();
        this.compressedImage = parcel.createByteArray();
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public byte[] getCompressedImage() {
        return compressedImage;
    }

    public void setCompressedImage(byte[] compressedImage) {
        this.compressedImage = compressedImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeString(this.description);
        dest.writeString(this.title);
        dest.writeString(this.publishedAt);
        dest.writeString(this.url);
        dest.writeString(this.urlToImage);
        dest.writeByteArray(this.compressedImage);
    }

    @Ignore
    public static final Parcelable.Creator<LocalArticle> CREATOR = new Parcelable.Creator<LocalArticle>() {
        public LocalArticle createFromParcel(Parcel in) {
            return new LocalArticle(in);
        }

        public LocalArticle[] newArray(int size) {
            return new LocalArticle[size];
        }
    };
}

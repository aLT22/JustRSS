package com.alexeyturkin.justrss.adapter;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexeyturkin.justrss.R;
import com.alexeyturkin.justrss.rest.model.Article;
import com.alexeyturkin.justrss.rest.model.ArticleDao;
import com.alexeyturkin.justrss.rest.model.LocalRoomDatabase;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Turkin A. on 02.11.2017.
 */

public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder> {

    public static final String TAG = FeedRecyclerAdapter.class.getSimpleName();


    private Context mContext;
    private List<Article> mList;
    private OnItemClickDelegate mDelegate;

    private LocalRoomDatabase mDatabase = null;
    private ArticleDao mDao = null;

    public FeedRecyclerAdapter(Context context, List<Article> list, OnItemClickDelegate onItemClickDelegate) {
        this.mContext = context;
        this.mList = list;
        this.mDelegate = onItemClickDelegate;
        this.mDatabase = Room.databaseBuilder(mContext, LocalRoomDatabase.class, "article-database").build();
        this.mDao = this.mDatabase.getArticleDao();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(R.layout.feed_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Article item = mList.get(position);

        holder.mTitle.setText(item.getTitle());
        holder.mAuthor.setText(item.getAuthor());
        holder.mDescription.setText(item.getDescription());
        Glide.with(mContext)
                .load(item.getUrlToImage())
                .into(holder.mImage);

        holder.mFeedItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDelegate.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void updateWholeFeed(List<Article> newArticles) {
        mList = new ArrayList<>();
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                //mDao.deleteArticle();
            }
        }).start();*/
        mList.addAll(newArticles);
        this.notifyDataSetChanged();

        /*for (final Article article :
                mList) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //mDao.insertArticles(article);
                    Bitmap bitmap = null;
                    try {
                        bitmap = Glide.with(mContext)
                                .asBitmap()
                                .load(article.getUrlToImage())
                                .submit()
                                .get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] bitmapdata = stream.toByteArray();
                    article.setCompressedImage(bitmapdata);

                    mDao.updateArticle(article);
                }
            }).start();

            *//*Observable.just("Just for Background thread")
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            mDao.insertArticles(article);
                            Bitmap bitmap = Glide.with(mContext)
                                    .asBitmap()
                                    .load(article.getUrlToImage())
                                    .submit()
                                    .get();
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] bitmapdata = stream.toByteArray();
                            article.setCompressedImage(bitmapdata);

                            mDao.updateArticle(article);
                        }
                    });*//*
        }*/

        /*Flowable.fromIterable(newArticles)
                .subscribeOn(Schedulers.newThread())
                .toObservable()
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<Article>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(final Article article) {
                        try {
                            Observable
                                    .just(Glide.with(mContext)
                                            .asBitmap()
                                            .load(article.getUrlToImage())
                                            .submit()
                                            .get())
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(Schedulers.newThread())
                                    .subscribe(new Consumer<Bitmap>() {
                                        @Override
                                        public void accept(Bitmap bitmap) throws Exception {
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                            byte[] bitmapdata = stream.toByteArray();
                                            article.setCompressedImage(bitmapdata);
                                        }
                                    });
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }

                        mDao.insertArticles(article);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete: ");

                        mDao.getFlowableArticles()
                                .subscribeOn(Schedulers.newThread())
                                .toObservable()
                                .flatMap(new Function<List<Article>, ObservableSource<?>>() {
                                    @Override
                                    public ObservableSource<?> apply(List<Article> articles) throws Exception {
                                        return Observable.fromIterable(articles);
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Object>() {
                                    @Override
                                    public void accept(Object o) throws Exception {
                                        if (o instanceof Article) {
                                            Log.e(TAG, "accept: " + ((Article) o).getCompressedImage().length);
                                        } else {
                                            Log.e(TAG, "accept: " + o.toString());
                                        }
                                    }
                                });
                    }
                });*/
    }

    public void updateFeedOneByOne(Article newArticle) {
        mList.add(newArticle);
        this.notifyDataSetChanged();
    }

    public interface OnItemClickDelegate {
        void onItemClick(Article item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_item)
        CardView mFeedItem;

        @BindView(R.id.tv_title)
        TextView mTitle;

        @BindView(R.id.tv_author)
        TextView mAuthor;

        @BindView(R.id.tv_description)
        TextView mDescription;

        @BindView(R.id.iv_feed_image)
        ImageView mImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
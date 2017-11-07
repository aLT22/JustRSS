package com.alexeyturkin.justrss.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexeyturkin.justrss.R;
import com.alexeyturkin.justrss.local.model.LocalArticle;
import com.alexeyturkin.justrss.rest.model.Article;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

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

    private List<LocalArticle> mLocalArticles;

    public FeedRecyclerAdapter(Context context, List<Article> list, OnItemClickDelegate onItemClickDelegate) {
        this.mContext = context;
        this.mList = list;
        this.mDelegate = onItemClickDelegate;
    }

    public void setLocalArticles(List<LocalArticle> localArticles) {
        this.mLocalArticles = localArticles;
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
        mList.addAll(newArticles);
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
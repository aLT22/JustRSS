package com.alexeyturkin.justrss.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Turkin A. on 04.11.17.
 */

public class LocalFeedRecyclerAdapter extends RecyclerView.Adapter<LocalFeedRecyclerAdapter.ViewHolder> {

    private static final String TAG = LocalFeedRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<LocalArticle> mList;
    private OnItemClickDelegate mDelegate;

    public LocalFeedRecyclerAdapter(Context context, List<LocalArticle> list, OnItemClickDelegate onItemClickDelegate) {
        this.mContext = context;
        this.mList = list;
        this.mDelegate = onItemClickDelegate;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.feed_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LocalArticle item = mList.get(position);

        holder.mAuthor.setText(item.getAuthor());
        holder.mDescription.setText(item.getDescription());
        holder.mTitle.setText(item.getTitle());

        Bitmap bmp = null;
        if (item.getCompressedImage() != null) {
            bmp = BitmapFactory.decodeByteArray(item.getCompressedImage(), 0, item.getCompressedImage().length);
        }

        if (bmp != null) {
            holder.mImage.setImageBitmap(bmp);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDelegate.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void updateWholeFeed(List<LocalArticle> newArticles) {
        mList = new ArrayList<>();
        mList.addAll(newArticles);
        this.notifyDataSetChanged();
    }

    public interface OnItemClickDelegate {
        void onItemClick(LocalArticle item);
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
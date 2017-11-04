package com.alexeyturkin.justrss.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexeyturkin.justrss.R;
import com.alexeyturkin.justrss.rest.model.Article;
import com.alexeyturkin.justrss.ui.BaseFragment;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ArticleDetailsFragment extends BaseFragment {

    public static final String TAG = ArticleDetailsFragment.class.getSimpleName();

    @BindView(R.id.tv_title)
    TextView mTitle;

    @BindView(R.id.tv_author)
    TextView mAuthor;

    @BindView(R.id.tv_description)
    TextView mDescription;

    @BindView(R.id.tv_link)
    TextView mLink;

    @BindView(R.id.iv_feed_image)
    ImageView mFeedImage;

    Unbinder mUnbinder = null;

    public static ArticleDetailsFragment newInstance() {
        ArticleDetailsFragment fragment = new ArticleDetailsFragment();

        return fragment;
    }

    public static ArticleDetailsFragment newInstance(Bundle bundle) {
        ArticleDetailsFragment fragment = new ArticleDetailsFragment();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_article_details, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Article outerArticle = getArguments().getParcelable(Article.class.getSimpleName());

            mTitle.setText(outerArticle.getTitle());
            mAuthor.setText(outerArticle.getAuthor());
            mDescription.setText(outerArticle.getDescription());
            mLink.setText(outerArticle.getUrl());
            Glide.with(getActivity())
                    .load(outerArticle.getUrlToImage())
                    .into(mFeedImage);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_article_details;
    }

}

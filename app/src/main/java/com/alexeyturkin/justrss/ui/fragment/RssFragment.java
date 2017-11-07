package com.alexeyturkin.justrss.ui.fragment;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.alexeyturkin.justrss.JustRssApp;
import com.alexeyturkin.justrss.R;
import com.alexeyturkin.justrss.adapter.FeedRecyclerAdapter;
import com.alexeyturkin.justrss.adapter.LocalFeedRecyclerAdapter;
import com.alexeyturkin.justrss.local.model.LocalArticle;
import com.alexeyturkin.justrss.rest.api.service.NewYorkTimesService;
import com.alexeyturkin.justrss.rest.model.Article;
import com.alexeyturkin.justrss.rest.model.FeedResponse;
import com.alexeyturkin.justrss.ui.BaseFragment;
import com.alexeyturkin.justrss.utils.AppUtilities;
import com.bumptech.glide.Glide;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.BitmapCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RssFragment extends BaseFragment implements FeedRecyclerAdapter.OnItemClickDelegate,
        LocalFeedRecyclerAdapter.OnItemClickDelegate {

    public static final String TAG = RssFragment.class.getSimpleName();

    @BindView(R.id.srl_container)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.rv_feed)
    RecyclerView mFeedList;

    @BindView(R.id.fl_details_container)
    FrameLayout mDetailsContainer;

    Unbinder mUnbinder = null;

    NewYorkTimesService mRetrofitService = null;

    CompositeDisposable mDisposable = null;

    FeedRecyclerAdapter mAdapter = null;
    LocalFeedRecyclerAdapter mLocalFeedAdapter = null;

    List<Article> mArticles = new ArrayList<>();
    List<LocalArticle> mLocalArticles = new ArrayList<>();

    boolean loadFromInternet = false, loadFromRealm = false, isLoadedArticles = false, isLoadedToRealm = false;

    Realm mRealm = null;

    public static RssFragment newInstance() {
        RssFragment fragment = new RssFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Realm.init(getContext());
        mRealm = Realm.getDefaultInstance();

        isLoadedArticles = false;

        mRetrofitService = JustRssApp.getRetrofitInstance().create(NewYorkTimesService.class);
        mDisposable = new CompositeDisposable();

        loadArticles();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rss, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        mRealm = Realm.getDefaultInstance();

        mAdapter = new FeedRecyclerAdapter(getActivity(), mArticles, this);
        mLocalFeedAdapter = new LocalFeedRecyclerAdapter(getActivity(), mLocalArticles, this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (AppUtilities.isOnline(getContext()) &&
                isLoadedArticles) {
            mFeedList.setAdapter(mAdapter);
        } else {
            mFeedList.setAdapter(mLocalFeedAdapter);
        }

        mFeedList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshArticles();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mUnbinder.unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mDisposable.clear();
        mRealm.close();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_rss;
    }

    private void loadArticles() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        if (AppUtilities.isOnline(getContext()) && AppUtilities.Connectivity.isConnectedFast(getContext())) {
            mArticles.clear();
            isLoadedArticles = false;
            loadFromInternet();
        } else {
            loadFromRealm();
        }

    }

    private void loadFromRealm() {
        mLocalArticles.clear();
        loadFromInternet = false;
        loadFromRealm = true;
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<LocalArticle> articleRealmQuery = realm.where(LocalArticle.class);

                mLocalArticles.addAll(realm.copyFromRealm(articleRealmQuery.findAll()));
            }
        });

        if (mLocalArticles.size() == 0) {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        } else {
            if (mFeedList != null && mLocalFeedAdapter != null) {

                mFeedList.setAdapter(mLocalFeedAdapter);
                mLocalFeedAdapter.updateWholeFeed(mLocalArticles);
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }

    private void loadFromInternet() {
        loadFromInternet = true;
        loadFromRealm = true;
        if (!isLoadedArticles) {
            mRetrofitService
                    .getObservableFeed(AppUtilities.SORT_OPTION, AppUtilities.API_KEY)
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Function<FeedResponse, ObservableSource<?>>() {
                        @Override
                        public ObservableSource<?> apply(FeedResponse feedResponse) throws Exception {
                            return Observable.fromIterable(feedResponse.getArticles());
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Object>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Object o) {
                            mArticles.add((Article) o);
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (mSwipeRefreshLayout != null) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                            Toast.makeText(getActivity(), "Error connecting to server!", Toast.LENGTH_SHORT).show();

                            loadFromRealm();
                        }

                        @Override
                        public void onComplete() {
                            isLoadedArticles = true;
                            if (mAdapter != null) {
                                mAdapter.updateWholeFeed(mArticles);
                            }
                            if (mFeedList != null) {
                                mFeedList.setAdapter(mAdapter);
                            }
                            saveLoadedDataToRealm();
                        }
                    });
        }
    }

    private void saveLoadedDataToRealm() {
        isLoadedToRealm = false;
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(LocalArticle.class);
                for (Article article :
                        mArticles) {
                    final LocalArticle localArticle = realm.createObject(LocalArticle.class, UUID.randomUUID().toString());

                    localArticle.setAuthor(article.getAuthor());
                    localArticle.setTitle(article.getTitle());
                    localArticle.setDescription(article.getDescription());
                    localArticle.setUrl(article.getUrl());
                    localArticle.setUrlToImage(article.getUrlToImage());
                    localArticle.setPublishedAt(article.getPublishedAt());

                    try {
                        URL url = new URL(article.getUrlToImage());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        myBitmap.compress(Bitmap.CompressFormat.WEBP, 10, stream);

                        byte[] byteArray = stream.toByteArray();

                        localArticle.setCompressedImage(byteArray);
                    } catch (IOException e) {
                        Log.e(TAG, "execute: " + e.getMessage());
                    }
                }


                isLoadedToRealm = true;
            }
        });

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void refreshArticles() {
        if (AppUtilities.isOnline(getContext())) {
            if (isLoadedToRealm) {
                loadArticles();
            } else {
                if (!isLoadedArticles) {
                    loadArticles();
                } else {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        } else {
            Toast.makeText(getContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
            loadFromRealm();
        }
    }

    @Override
    public void onItemClick(Article item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Article.class.getSimpleName(), item);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showArticleDetailsLand(bundle);
        } else {
            showArticleDetails(bundle);
        }
    }

    private void showArticleDetails(Bundle bundle) {
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_fragment_container, ArticleDetailsFragment.newInstance(bundle), ArticleDetailsFragment.TAG)
                .addToBackStack(ArticleDetailsFragment.TAG)
                .commit();
    }

    private void showArticleDetailsLand(Bundle bundle) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_details_container, ArticleDetailsFragment.newInstance(bundle), ArticleDetailsFragment.TAG)
                .commit();
    }

    @Override
    public void onItemClick(LocalArticle item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(LocalArticle.class.getSimpleName(), item);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showArticleDetailsLand(bundle);
        } else {
            showArticleDetails(bundle);
        }
    }
}

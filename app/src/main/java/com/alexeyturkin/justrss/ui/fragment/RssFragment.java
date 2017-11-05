package com.alexeyturkin.justrss.ui.fragment;


import android.content.res.Configuration;
import android.graphics.Bitmap;
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

import java.io.ByteArrayOutputStream;
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

    boolean isLoadedArticles = false;

    Realm mRealm = null;

    public static RssFragment newInstance() {
        RssFragment fragment = new RssFragment();

        return fragment;
    }

    public static RssFragment newInstance(Bundle bundle) {
        RssFragment fragment = new RssFragment();

        fragment.setArguments(bundle);

        return fragment;
    }

    //Send requests to server etc
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    //Initialize non-view fields here
    //Dagger injects here
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mRealm = Realm.getDefaultInstance();

        isLoadedArticles = false;

        mRetrofitService = JustRssApp.getRetrofitInstance().create(NewYorkTimesService.class);
        mDisposable = new CompositeDisposable();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rss, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        mAdapter = new FeedRecyclerAdapter(getActivity(), mArticles, this);
        mLocalFeedAdapter = new LocalFeedRecyclerAdapter(getActivity(), mLocalArticles, this);

        loadArticles();

        return rootView;
    }

    //Filling views
    //Set up adapters etc
    //Attach adapter to views etc
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshArticles();
                if (!AppUtilities.isOnline(getActivity())) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_SHORT).show();
                }
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
            mFeedList.setAdapter(mAdapter);
            mFeedList.setLayoutManager(new LinearLayoutManager(getActivity()));
            loadFromInternet();
        } else {
            mFeedList.setAdapter(mLocalFeedAdapter);
            mFeedList.setLayoutManager(new LinearLayoutManager(getActivity()));
            loadFromRealm();
        }

    }

    private void loadFromRealm() {
        mLocalArticles.clear();
        mRealm = Realm.getDefaultInstance();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<LocalArticle> articleRealmQuery = realm.where(LocalArticle.class);

                mLocalArticles.addAll(realm.copyFromRealm(articleRealmQuery.findAll()));
            }
        });

        if (mLocalArticles.size() == 0) {
            Toast.makeText(getContext(), "Local cache is empty!", Toast.LENGTH_LONG).show();
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            if (mFeedList != null && mLocalFeedAdapter != null) {

                mLocalFeedAdapter.updateWholeFeed(mLocalArticles);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void loadFromInternet() {
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
                            if (mSwipeRefreshLayout != null) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                            if (mAdapter != null) {
                                mAdapter.updateWholeFeed(mArticles);
                            }

                            saveLoadedDataToRealm();
                        }
                    });
        }
    }

    private void saveLoadedDataToRealm() {
        mRealm.beginTransaction();
        mRealm.deleteAll();
        mRealm.commitTransaction();
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Article article :
                        mArticles) {
                    LocalArticle localArticle = realm.createObject(LocalArticle.class, UUID.randomUUID().toString());

                    localArticle.setAuthor(article.getAuthor());
                    localArticle.setTitle(article.getTitle());
                    localArticle.setDescription(article.getDescription());
                    localArticle.setUrl(article.getUrl());
                    localArticle.setUrlToImage(article.getUrlToImage());
                    localArticle.setPublishedAt(article.getPublishedAt());

                    try {
                        Bitmap bitmap = Glide
                                .with(getActivity())
                                .asBitmap()
                                .load(article.getUrlToImage())
                                .into(-1, -1)
                                .get();

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        localArticle.setCompressedImage(byteArray);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void refreshArticles() {
        loadArticles();
    }

    @Override
    public void onItemClick(Article item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Article.class.getSimpleName(), item);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_details_container, ArticleDetailsFragment.newInstance(bundle), ArticleDetailsFragment.TAG)
                    .commit();
        } else {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_fragment_container, ArticleDetailsFragment.newInstance(bundle), ArticleDetailsFragment.TAG)
                    .addToBackStack(ArticleDetailsFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void onItemClick(LocalArticle item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(LocalArticle.class.getSimpleName(), item);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_details_container, ArticleDetailsFragment.newInstance(bundle), ArticleDetailsFragment.TAG)
                    .commit();
        } else {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_fragment_container, ArticleDetailsFragment.newInstance(bundle), ArticleDetailsFragment.TAG)
                    .addToBackStack(ArticleDetailsFragment.TAG)
                    .commit();
        }
    }
}

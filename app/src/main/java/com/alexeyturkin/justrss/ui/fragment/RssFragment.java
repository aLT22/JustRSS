package com.alexeyturkin.justrss.ui.fragment;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.alexeyturkin.justrss.JustRssApp;
import com.alexeyturkin.justrss.R;
import com.alexeyturkin.justrss.adapter.FeedRecyclerAdapter;
import com.alexeyturkin.justrss.rest.api.service.NewYorkTimesService;
import com.alexeyturkin.justrss.rest.model.Article;
import com.alexeyturkin.justrss.rest.model.FeedResponse;
import com.alexeyturkin.justrss.ui.BaseFragment;
import com.alexeyturkin.justrss.utils.AppUtilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RssFragment extends BaseFragment implements FeedRecyclerAdapter.OnItemClickDelegate {

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
    List<Article> mArticles = new ArrayList<>();

    boolean isLoadedArticles = false;

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
                if (AppUtilities.isOnline(getActivity())) {
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAdapter = new FeedRecyclerAdapter(getActivity(), mArticles, this);

        mFeedList.setAdapter(mAdapter);
        mFeedList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter.updateWholeFeed(mArticles);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDisposable.clear();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_rss;
    }

    private void loadArticles() {
        loadFromInternet();
    }

    private void loadFromInternet() {
        if (!isLoadedArticles) {

            mRetrofitService
                    .getCallFeed(AppUtilities.SORT_OPTION, AppUtilities.API_KEY)
                    .enqueue(new Callback<FeedResponse>() {
                        @Override
                        public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                            for (Article article :
                                    response.body().getArticles()) {
                                mArticles.add(article);
                            }

                            isLoadedArticles = true;
                            if (mSwipeRefreshLayout != null) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                            if (mAdapter != null) {
                                mAdapter.updateWholeFeed(mArticles);
                            }
                        }

                        @Override
                        public void onFailure(Call<FeedResponse> call, Throwable t) {

                        }
                    });

            /*mRetrofitService
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
                        }
                    });*/
        }
    }

    private void refreshArticles() {
        mSwipeRefreshLayout.setRefreshing(true);
        mArticles.clear();
        isLoadedArticles = false;
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
}

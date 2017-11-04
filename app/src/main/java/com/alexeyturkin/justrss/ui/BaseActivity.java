package com.alexeyturkin.justrss.ui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.alexeyturkin.justrss.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Turkin A. on 02.11.2017.
 */

public abstract class BaseActivity extends AppCompatActivity {

    public static final String TAG = BaseActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fl_fragment_container)
    FrameLayout mWrapper;

    Unbinder mUnbinder = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mUnbinder = ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        getLayoutInflater().inflate(getLayoutRes(), mWrapper);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUnbinder.unbind();
    }

    @LayoutRes
    protected abstract int getLayoutRes();
}

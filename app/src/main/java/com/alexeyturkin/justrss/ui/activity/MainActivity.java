package com.alexeyturkin.justrss.ui.activity;

import android.os.Bundle;

import com.alexeyturkin.justrss.R;
import com.alexeyturkin.justrss.ui.BaseActivity;
import com.alexeyturkin.justrss.ui.fragment.RssFragment;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fl_fragment_container, RssFragment.newInstance(), RssFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }
}

package com.itmoldova.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.itmoldova.Extra;
import com.itmoldova.R;
import com.itmoldova.model.Item;
import com.itmoldova.util.ActivityUtils;

import java.util.List;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            Item item = getIntent().getParcelableExtra(Extra.INSTANCE.getITEM());
            List<Item> items = getIntent().getParcelableArrayListExtra(Extra.INSTANCE.getITEMS());
            DetailFragment detailFragment = DetailFragment.newInstance(items, item);
            ActivityUtils.addFragmentToActivity(getFragmentManager(), detailFragment, android.R.id.content);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        DetailFragment detailFragment = (DetailFragment) getFragmentManager().findFragmentById(android.R.id.content);
        if (detailFragment != null) {
            Item item = intent.getParcelableExtra(Extra.INSTANCE.getITEM());
            List<Item> items = intent.getParcelableArrayListExtra(Extra.INSTANCE.getITEMS());
            detailFragment.loadArticle(items, item);
        }
    }
}

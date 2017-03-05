package com.itmoldova.detail;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.itmoldova.Extra;
import com.itmoldova.R;
import com.itmoldova.model.Item;
import com.itmoldova.parser.DetailViewCreator;
import com.itmoldova.photoview.PhotoViewActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Shows details of an article.
 * <p>
 * Author vgrec, on 09.07.16.
 */
public class DetailFragment extends Fragment implements DetailContract.View, View.OnClickListener {

    private DetailContract.Presenter presenter;
    private Item item;
    private List<Item> items;

    @BindView(R.id.content)
    ViewGroup contentGroup;

    @BindView(R.id.title)
    TextView titleView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.image_header)
    ImageView imageHeaderView;


    public static DetailFragment newInstance(List<Item> items, Item item) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.ITEM, item);
        args.putParcelableArrayList(Extra.ITEMS, new ArrayList<>(items));
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getArguments().getParcelable(Extra.ITEM);
        items = getArguments().getParcelableArrayList(Extra.ITEMS);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupToolbar();

        presenter = new DetailPresenter(this, new DetailViewCreator(getActivity()));
        loadArticle(items, item);
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.articles_list, menu);
    }

    @Override
    public void showArticleDetail(List<View> views) {
        contentGroup.removeAllViews();
        for (View view : views) {
            if (view instanceof ImageView) {
                view.setOnClickListener(this);
            }
            contentGroup.addView(view);
        }
    }

    @Override
    public void showRelatedArticles(List<Item> items) {
        Toast.makeText(getActivity(), "Show related", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showTitle(String title) {
        titleView.setText(title);
    }

    @Override
    public void showHeaderImage(String url) {
        imageHeaderView.setVisibility(View.VISIBLE);
        imageHeaderView.setTag(url);
        imageHeaderView.setOnClickListener(this);
        Picasso.with(getActivity()).load(url).into(imageHeaderView);
    }

    @Override
    public void hideHeaderImage() {
        imageHeaderView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        List<String> urls = presenter.extractPhotoUrlsFromArticle();
        Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
        intent.putStringArrayListExtra(Extra.PHOTO_URLS, (ArrayList<String>) urls);
        intent.putExtra(Extra.CLICKED_URL, (String) v.getTag());
        startActivity(intent);
    }

    public void loadArticle(List<Item> items, Item item) {
        if (presenter != null) {
            presenter.loadArticleDetail(item);
            presenter.loadRelatedArticles(items, item);
        }
    }
}

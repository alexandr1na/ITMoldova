package com.itmoldova.detail;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itmoldova.Extra;
import com.itmoldova.R;
import com.itmoldova.comments.NewCommentActivity;
import com.itmoldova.db.AppDatabase;
import com.itmoldova.model.Item;
import com.itmoldova.parser.DetailViewCreator;
import com.itmoldova.photoview.PhotoViewActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Shows details of an article.
 * <p>
 * Author vgrec, on 09.07.16.
 */
public class DetailFragment extends Fragment implements DetailContract.View, View.OnClickListener {

    @BindView(R.id.content)
    ViewGroup contentGroup;
    @BindView(R.id.related)
    ViewGroup relatedGroup;
    @BindView(R.id.title)
    TextView titleView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.image_header)
    ImageView imageHeaderView;
    private DetailContract.Presenter presenter;
    private Item item;
    private List<Item> items;
    private DetailViewCreator detailViewCreator;

    public static DetailFragment newInstance(List<Item> items, Item item) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.INSTANCE.getITEM(), item);
        args.putParcelableArrayList(Extra.INSTANCE.getITEMS(), new ArrayList<>(items));
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getArguments().getParcelable(Extra.INSTANCE.getITEM());
        items = getArguments().getParcelableArrayList(Extra.INSTANCE.getITEMS());
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

        detailViewCreator = new DetailViewCreator(getActivity());
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
    public void showRelatedArticles(List<Item> relatedItems) {
        View relatedArticlesView = detailViewCreator.createRelatedViews(relatedItems,
                relatedArticle -> {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra(Extra.INSTANCE.getITEM(), relatedArticle);
                    intent.putParcelableArrayListExtra(Extra.INSTANCE.getITEMS(), new ArrayList<>(items));
                    getActivity().finish();
                    startActivity(intent);
                });
        relatedGroup.addView(relatedArticlesView);
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
        intent.putStringArrayListExtra(Extra.INSTANCE.getPHOTO_URLS(), (ArrayList<String>) urls);
        intent.putExtra(Extra.INSTANCE.getCLICKED_URL(), (String) v.getTag());
        startActivity(intent);
    }

    @OnClick(R.id.fab)
    public void openNewCommentActivity() {
        startActivity(new Intent(getActivity(), NewCommentActivity.class));
    }

    @OnClick(R.id.view_in_browser)
    public void openInBrowser() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getActivity(), Uri.parse(item.getLink()));
    }

    private void bookMarkArticle() {
        // test code to bookmark an article.
        AppDatabase db = AppDatabase.getDatabase(getActivity());
        if (db.itemDao().getItemById(item.getGuid()) == null) {
            db.itemDao().insertItem(item);
        } else {
            db.itemDao().deleteItem(item);
        }

        // show the list of all items:
        List<Item> items = db.itemDao().loadAllItems();
        for (Item item : items) {
            System.out.println(item.getTitle());
        }
    }

    public void loadArticle(List<Item> items, Item item) {
        if (presenter != null) {
            presenter.loadArticleDetail(item);
            presenter.loadRelatedArticles(items, item);
        }
    }
}

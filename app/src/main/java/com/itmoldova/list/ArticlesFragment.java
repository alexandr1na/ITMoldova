package com.itmoldova.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.itmoldova.R;
import com.itmoldova.adapter.ArticlesAdapter;
import com.itmoldova.detail.DetailActivity;
import com.itmoldova.http.RssFeedLoader;
import com.itmoldova.model.Item;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Author vgrec, on 09.07.16.
 */
public class ArticlesFragment extends Fragment implements ArticlesContract.View {

    private ArticlesContract.Presenter presenter;
    private ArticlesAdapter adapter;
    private List<Item> items = new ArrayList<>();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    public static Fragment newInstance(String item) {
        return new ArticlesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_articles_list, container, false);
        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ArticlesAdapter(items, this::openArticleDetail);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> presenter.loadArticles());

        return view;
    }

    private void openArticleDetail(Item item) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(DetailActivity.ITEM, item);
        startActivity(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter = new ArticlesPresenter(new RssFeedLoader(getActivity().getApplicationContext()), this);
        presenter.loadArticles();
    }

    @Override
    public void showArticles(List<Item> items) {
        int fromPosition = this.items.size();
        int toPosition = items.size();
        this.items.addAll(items);
        adapter.notifyItemRangeInserted(fromPosition, toPosition);
    }

    @Override
    public void setLoadingIndicator(boolean loading) {
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(loading));
    }

    @Override
    public void showError() {
        Toast.makeText(getActivity(), "Error", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showNoInternetConnection() {
        Toast.makeText(getActivity(), "No Internet", Toast.LENGTH_LONG).show();
    }

    @Override
    public void setPresenter(ArticlesContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.articles_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                presenter.loadArticles();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

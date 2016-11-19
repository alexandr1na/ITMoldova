package com.itmoldova.list;

import com.itmoldova.http.ITMoldovaService;
import com.itmoldova.http.NetworkConnectionManager;
import com.itmoldova.model.Category;
import com.itmoldova.model.Rss;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ArticlesPresenter implements ArticlesContract.Presenter {

    private ArticlesContract.View view;
    private ITMoldovaService apiService;
    private Subscription subscription;
    private NetworkConnectionManager connectionManager;

    public ArticlesPresenter(ITMoldovaService apiService, ArticlesContract.View view, NetworkConnectionManager connectionManager) {
        this.view = view;
        this.apiService = apiService;
        this.connectionManager = connectionManager;
    }

    @Override
    public void loadArticles(Category category, int page) {
        loadRssFeed(category, page, false);
    }

    @Override
    public void refreshArticles(Category category) {
        loadRssFeed(category, 0, true);
    }

    private void loadRssFeed(Category category, int page, boolean clearDataSet) {
        if (!connectionManager.hasInternetConnection()) {
            view.showNoInternetConnection();
            return;
        }
        subscription = getObservableByCategory(category, page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> view.setLoadingIndicator(true))
                .doOnTerminate(() -> view.setLoadingIndicator(false))
                .subscribe(
                        rss -> processResponse(rss, clearDataSet),
                        error -> view.showError());
    }

    private Observable<Rss> getObservableByCategory(Category category, int page) {
        if (category == Category.HOME) {
            return apiService.getDefaultRssFeed(page);
        } else {
            return apiService.getRssFeedByCategory(category.getCategoryName(), page);
        }
    }

    private void processResponse(Rss response, boolean clearDataSet) {
        if (response != null && response.getChannel() != null) {
            view.showArticles(response.getChannel().getItemList(), clearDataSet);
        } else {
            view.showError();
        }
    }

    @Override
    public void cancel() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}

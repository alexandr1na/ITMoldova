package com.itmoldova.detail

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.itmoldova.BaseActivity
import com.itmoldova.Extra
import com.itmoldova.R
import com.itmoldova.model.Article
import com.itmoldova.util.ActivityUtils

class DetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(if (IS_DARK) R.style.AppTheme_Dark_NoActionBar else R.style.AppTheme_Light_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        if (savedInstanceState == null) {
            val article = intent.getParcelableExtra<Article>(Extra.ARTICLE)
            val relatedArticles = intent.getParcelableArrayListExtra<Article>(Extra.ARTICLES)
            val detailFragment = DetailFragment.newInstance(relatedArticles, article)
            ActivityUtils.addFragmentToActivity(fragmentManager, detailFragment, android.R.id.content)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val detailFragment = fragmentManager.findFragmentById(android.R.id.content)
        detailFragment?.let {
            val article = intent.getParcelableExtra<Article>(Extra.ARTICLE)
            val topArticles = intent.getParcelableArrayListExtra<Article>(Extra.ARTICLES)
            (it as DetailFragment).loadArticle(topArticles, article)
        }
    }

    override fun onBackPressed() {
        val detailFragment = fragmentManager.findFragmentById(android.R.id.content)
        detailFragment?.let {
            (it as DetailFragment).hideFab()
        }
        Handler().postDelayed({ super.onBackPressed() }, DetailFragment.FAB_CLOSE_ANIM_DURATION)
    }
}

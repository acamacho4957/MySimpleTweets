package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeTweetDialogFragment.ComposeTweetDialogListener {

    private TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer;
    MenuItem miActionProgressItem;
    private EndlessRecyclerViewScrollListener scrollListener;
    private Long lowest_max_id;
    FragmentManager fm = getSupportFragmentManager();

    private final int REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TWITTER", "on create called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        client = TwitterApp.getRestClient(this);
        // find the RecyclerView
        rvTweets = findViewById(R.id.rvTweet);
        rvTweets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // init the arraylist
        tweets = new ArrayList<>();
        // construct the adapter from this datasouce
        tweetAdapter = new TweetAdapter(tweets, fm, client);
        // RecyclerView setup (layout manager, use adapter)
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        // set the adapter
        rvTweets.setAdapter(tweetAdapter);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeline(0);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i("TWITTER", "on prepare called");
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Extract the action-view from the menu item
        ProgressBar v =  (ProgressBar) miActionProgressItem.getActionView();
        // Return to finish
        populateTimeline(0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("TWITTER", "on create options called");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_timeline, menu);
        MenuItem composeItem = menu.findItem(R.id.miCompose);
        composeItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showComposeTweetDialog();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void populateTimeline(Integer page) {
        showProgressBar();

        Long max_id = null;
        if (page <= 0) {
            tweetAdapter.clear();
        } else {
            max_id = lowest_max_id;
        }

        client.getUserTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    Tweet tweet = null;
                    try {
                        tweet = Tweet.fromJSON(response.getJSONObject(i));
                        if (lowest_max_id == null || (tweet.uid > 1 && tweet.uid < lowest_max_id)) {
                            lowest_max_id = tweet.uid;
                        }
                        tweets.add(tweet);
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                swipeContainer.setRefreshing(false);
                hideProgressBar();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

        }, max_id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract tweet from result extras
            Tweet tweet = (Tweet) data.getParcelableExtra("tweet");
            // Insert tweet to top of timeline
            tweets.add(0, tweet);
            tweetAdapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
            // Toast to notify tweet was sent
            Toast.makeText(this, "Tweet sent", Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_CANCELED && requestCode == REQUEST_CODE) {
            // Toast to notify tweet was NOT sent
            Toast.makeText(this, "Tweet cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    public void fetchTimelineAsync(int page) {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // CLEAR OUT old items before appending in the new ones
                tweetAdapter.clear();

                for (int i = 0; i < response.length(); i++) {
                    Tweet tweet = null;
                    try {
                        tweet = Tweet.fromJSON(response.getJSONObject(i));
                        if (lowest_max_id == null || (tweet.uid > 1 && tweet.uid < lowest_max_id)) {
                            lowest_max_id = tweet.uid;
                        }
                        tweets.add(tweet);
                        tweetAdapter.notifyItemInserted(tweets.size()-1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            public void onFailure(Throwable e) {
                Log.d("DEBUG", "Fetch timeline error: " + e.toString());
            }
        });
    }

    public void showProgressBar() {
        // Show progress item
        if (miActionProgressItem != null){
            miActionProgressItem.setVisible(true); }
    }

    public void hideProgressBar() {
        // Hide progress item
        if (miActionProgressItem != null){
            miActionProgressItem.setVisible(false); }
    }

    // Append the next page of data into the adapter
    public void loadNextDataFromApi(int offset) {
        populateTimeline(offset);
    }

    public void showComposeTweetDialog() {
        ComposeTweetDialogFragment composeDialog = new ComposeTweetDialogFragment();
        composeDialog.show(fm, "compose_fragment");
    }


    @Override
    public void onAcceptCompose(Tweet tweet) {
        //from onActivityResult
        tweets.add(0, tweet);
        tweetAdapter.notifyItemInserted(0);
        rvTweets.scrollToPosition(0);
        // Toast to notify tweet was sent
        Toast.makeText(this, "Tweet sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeclineCompose() {
        Toast.makeText(this, "Tweet cancelled", Toast.LENGTH_SHORT).show();
    }
}

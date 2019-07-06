package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {
    private List<Tweet> mTweets;
    private FragmentManager fm;
    private TwitterClient client;
    Context context;

    // pass in the Tweets array in the constructor
    public TweetAdapter (List<Tweet> tweets, FragmentManager fm, TwitterClient client) {
        mTweets = tweets;
        this.fm = fm;
        this.client = client;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return  viewHolder;
    }

    // bind the values based on the position of the element
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Tweet tweet = mTweets.get(position);

        holder.tvUsername.setText(tweet.user.name);
        holder.tvScreenName.setText("@" + tweet.user.screenName);
        holder.tvBody.setText(tweet.body);
        holder.tvTimestamp.setText(getRelativeTimeAgo(tweet.createdAt));

        if (tweet.retweetCount > 0) {
            holder.tvRetweetCount.setText(tweet.retweetCount.toString());
        } else {
            holder.tvRetweetCount.setText(null);
        }

        if (tweet.likeCount != null && tweet.likeCount > 0) {
            holder.tvLikeCount.setText(tweet.likeCount.toString());
        } else {
            holder.tvLikeCount.setText(null);
        }

        if (tweet.didLike != null && tweet.didLike) {
            holder.btLike.setBackground(context.getResources().getDrawable(R.drawable.ic_vector_heart));
            holder.tvLikeCount.setTextColor(context.getResources().getColor(R.color.twitter_blue));
        } else {
            holder.btLike.setBackground(context.getResources().getDrawable(R.drawable.ic_vector_heart_stroke));
            holder.tvLikeCount.setTextColor(context.getResources().getColor(R.color.black));
        }

        if (tweet.didRetweet != null && tweet.didRetweet) {
            holder.btRetweet.setBackground(context.getResources().getDrawable(R.drawable.ic_vector_retweet));
            holder.tvRetweetCount.setTextColor(context.getResources().getColor(R.color.twitter_blue));
        } else {
            holder.btRetweet.setBackground(context.getResources().getDrawable(R.drawable.ic_vector_retweet_stroke));
            holder.tvRetweetCount.setTextColor(context.getResources().getColor(R.color.black));
        }

        if (tweet.imageUrl != null) {
            holder.ivPostedImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(tweet.imageUrl)
                    .bitmapTransform(new RoundedCornersTransformation(context, 25, 0))
                    .into(holder.ivPostedImage);
        } else {
            holder.ivPostedImage.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(tweet.user.profileImageUrl)
                .bitmapTransform(new RoundedCornersTransformation(context, 25, 0))
                .into(holder.ivProfileImage);

        holder.btReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Twitter", "replied");
                showComposeTweetDialog(tweet, context);
            }
        });

//        holder.btRetweet.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("Twitter", "retweeted");
//                client.postRetweet(new JsonHttpResponseHandler() , tweet.uid);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage;
        public TextView tvUsername;
        public TextView tvBody;
        public TextView tvTimestamp;
        public TextView tvScreenName;
        public ImageView ivPostedImage;
        public TextView tvRetweetCount;
        public TextView tvLikeCount;
        public Button btRetweet;
        public Button btReply;
        public Button btLike;

        public ViewHolder (View itemView) {
            super(itemView);

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUsername = itemView.findViewById(R.id.tvUserName);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            btReply = itemView.findViewById(R.id.btReply);
            ivPostedImage = itemView.findViewById(R.id.ivPostedImage);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            btRetweet = itemView.findViewById(R.id.btRetweet);
            btLike = itemView.findViewById(R.id.btLike);
        }
    }

    // adapted from https://gist.github.com/nesquena/f786232f5ef72f6e10a7
    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    private String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String abrevDate = relativeDate;

        Pattern pattern = Pattern.compile("[0-9]*\\s[a-z]");
        Matcher matcher = pattern.matcher(abrevDate);
        if (matcher.find()) {
            abrevDate =  matcher.group(0).replaceAll("\\s+","");
        }

        return abrevDate;
    }

    // Clean all elements of the recycler
    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

    private void showComposeTweetDialog(Tweet tweet, Context context) {
        ComposeTweetDialogFragment composeDialog = new ComposeTweetDialogFragment();
        composeDialog.setContext(context);
        composeDialog.setTweet(tweet);
        composeDialog.show(fm, "fragment_alert");
    }
}

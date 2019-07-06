package com.codepath.apps.restclienttemplate.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Tweet implements Parcelable {

    public String body;
    public long uid; // database ID for the tweet
    public String createdAt;
    public User user;
    public String imageUrl;
    public Integer likeCount;
    public Integer retweetCount;
    public Boolean didLike;
    public Boolean didRetweet;

    // empty constructor for static method
    private Tweet() { }

    protected Tweet(Parcel in) {
        body = in.readString();
        uid = in.readLong();
        createdAt = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        @Override
        public Tweet createFromParcel(Parcel in) {
            return new Tweet(in);
        }

        @Override
        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };

    // deserialize the JSON
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        // extract the values from JSON
        tweet.body = jsonObject.getString("text");
        tweet.uid = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.likeCount = jsonObject.getInt("favorite_count");
        tweet.didLike = jsonObject.getBoolean("favorited");
        tweet.didRetweet = jsonObject.getBoolean("retweeted");
        try {
            JSONObject media = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0);
            tweet.imageUrl = media.getString("media_url_https");
        } catch (JSONException e) {}
        return tweet;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(body);
        dest.writeLong(uid);
        dest.writeString(createdAt);
        dest.writeParcelable(user, flags);
    }
}

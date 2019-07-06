package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ComposeTweetDialogFragment extends DialogFragment {
    Context context;

    private EditText etCompose;
    private TwitterClient client;
    private Button btAccept;
    private Button btClose;
    private TextView tvCounter;
    private TextView tvReplyStatus;

    private final int MAX_COUNT = 280;
    private Tweet tweet;

    public ComposeTweetDialogFragment() { }

    public interface ComposeTweetDialogListener {
        void onAcceptCompose(Tweet tweet);
        void onDeclineCompose();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_tweet, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        client = TwitterApp.getRestClient(getContext());

        etCompose = view.findViewById(R.id.etCompose);
        btAccept = view.findViewById(R.id.btAccept);
        btClose = view.findViewById(R.id.btClose);
        tvCounter = view.findViewById(R.id.tvCounter);
        tvReplyStatus = view.findViewById(R.id.tvReplyStatus);

        if (tweet != null) {
            etCompose.setText("@" + tweet.user.screenName);
            tvReplyStatus.setText("in reply to " + tweet.user.name);
            view.findViewById(R.id.ivDown).setVisibility(View.VISIBLE);
        }


        tvCounter.setText(String.valueOf(280 - etCompose.length()));

        btAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etCompose.getText().toString();
                client.sendTweet(message, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            Tweet tweet = Tweet.fromJSON(response);
                            ComposeTweetDialogListener listener = (ComposeTweetDialogListener) getActivity();
                            listener.onAcceptCompose(tweet);
                            // Close the dialog and return back to the parent activity
                            dismiss();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComposeTweetDialogListener listener = (ComposeTweetDialogListener) getActivity();
                listener.onDeclineCompose();
                dismiss();
            }
        });

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int characterCount = MAX_COUNT - s.length();
                tvCounter.setText(String.valueOf(characterCount));
                if (characterCount < 0) {
                    tvCounter.setTextColor(getResources().getColor(R.color.medium_red));
                    btAccept.setClickable(false);
                } else {
                    tvCounter.setTextColor(getResources().getColor(R.color.medium_gray));
                    btAccept.setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}

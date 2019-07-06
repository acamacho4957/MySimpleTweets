package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity  {
    private TwitterClient client;
    @BindView(R.id.etCompose) EditText etCompose;
    @BindView(R.id.btAccept) Button btAccept;
    @BindView(R.id.btDecline) Button btDecline;
    @BindView(R.id.tvCounter) TextView tvCounter;
    private final int MAX_COUNT = 280;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        client = TwitterApp.getRestClient(this);

        ButterKnife.bind(this);

        boolean isReply = getIntent().getBooleanExtra("isReply", false);

        if (isReply) {
            String replyName = getIntent().getStringExtra("replyName");
            etCompose.setText(replyName);
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
                            // Prepare data intent
                            Intent data = new Intent();
                            // Pass relevant data back as a result
                            data.putExtra("tweet", tweet);
                            // Activity finished ok, return the data
                            setResult(RESULT_OK, data); // set result code and bundle data for response
                            finish(); // closes the activity, pass data to parent
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        btDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                setResult(RESULT_CANCELED, data); // set result code to cancelled
                finish();
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
}

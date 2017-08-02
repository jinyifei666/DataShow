package com.yrish.datashow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private EditText accountEdit;
    private EditText passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);

    }

    public void login(View view){
        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("username",accountEdit.getText().toString().trim());
        params.put("password",passwordEdit.getText().toString().trim());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        client.post("http://hopes.yrish.com/login",params,new JsonHttpResponseHandler(){
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String state=null;
                try {
                   state=response.getString("state");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (state.equals("0")){
                    startActivity(new Intent(MainActivity.this,DataShowActivity.class));
                }
            }
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.w("JsonHttpRH", responseString);
            }
        });
    }
}

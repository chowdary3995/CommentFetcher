package com.krishna.commentfetcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Krishna on 2/16/2016.
 */
public class MainActivity extends Activity {

    private TextView text;
    private ListView listView;
    private ArrayList<String> authorList;
    private ArrayList<String> commentList;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //local change two
        //local change three
        //remote changes
        setContentView(R.layout.activity_main);

        authorList = new ArrayList<String>();
        commentList = new ArrayList<String>();

        text = (TextView) findViewById(R.id.appName);
        listView = (ListView) findViewById(R.id.listView);


        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2,android.R.id.text1, authorList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                text1.setTypeface(null, Typeface.BOLD);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(authorList.get(position));//persons.get(position).getname()
                text2.setText(commentList.get(position));
                return view;
            }
        };
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isNetworkAvailable()) {
            fetchComments();
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setMessage("Do you want to connect to Internet?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            text.setText("Need internet to proceed");
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

    public boolean isNetworkAvailable() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else
            return false;
    }

    public void fetchComments(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://itunes.apple.com/rss/customerreviews/id=529479190/json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                         parseJSON(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                text.setText("Sorry, Server error occurred! Try again Later.");
            }
        });


// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    public void parseJSON(JSONObject jsonObject){
        try {

            JSONArray array = jsonObject.getJSONObject("feed").getJSONArray("entry");
            text.setText(array.getJSONObject(0).getJSONObject("im:name").getString("label"));
            for(int i=0; i<array.length();i++){
                JSONObject obj = array.getJSONObject(i);
                try {
                    authorList.add(obj.getJSONObject("author").getJSONObject("name").getString("label"));
                }catch(Exception e){
                    continue;
                }
                try {
                    commentList.add(obj.getJSONObject("content").getString("label"));
                }catch(Exception e){
                    commentList.add("");
                }

            }
            System.out.println("parsing..");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();

    }
}

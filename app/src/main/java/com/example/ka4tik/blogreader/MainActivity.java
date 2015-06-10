package com.example.ka4tik.blogreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ListActivity {


    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MainActivity.class.getSimpleName();
    protected JSONObject mBlogData;
    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        if(isNetworkAvailable()) {
            GetBlogPostTask getBlogPostTask = new GetBlogPostTask();
            getBlogPostTask.execute();
        }
        else {
            Toast.makeText(this,"Network is unavailable", Toast.LENGTH_LONG).show();
        }

    }

    private boolean isNetworkAvailable() {
        boolean isAvailable = false;
        ConnectivityManager manager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo !=null && networkInfo.isConnected())
            isAvailable = true;
        return isAvailable;

    }
    private void updateList() {
        mProgressBar.setVisibility(View.INVISIBLE);

        if(mBlogData == null){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Opps! Sorry");
            builder.setMessage("There was a error getting data from network");
            builder.setPositiveButton(android.R.string.ok,null);
            AlertDialog dialog = builder.create();
            dialog.show();

            TextView emptyTextView = (TextView) getListView().getEmptyView();
            emptyTextView.setText("No items to display");
        }
        else
        {
            try{
                  JSONArray jsonPosts =mBlogData.getJSONArray("posts");
                    ArrayList<HashMap<String,String> > blogPosts = new ArrayList<HashMap<String, String>>();

                    for(int i=0;i<jsonPosts.length();i++){
                        JSONObject post = jsonPosts.getJSONObject(i);
                        String title = post.getString("title");
                        title  = Html.fromHtml(title).toString();
                        String author = post.getString("author");
                        title  = Html.fromHtml(title).toString();

                        HashMap<String ,String> blogPost = new HashMap<String,String>();
                        blogPost.put("title",title);
                        blogPost.put("author",author);
                        blogPosts.add(blogPost);
                    }

                String keys[] = { "title" ,"author"};
                int []ids = {android.R.id.text1,android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this, blogPosts,android.R.layout.simple_list_item_2,keys,ids);
                setListAdapter(adapter);

            }
            catch (JSONException e){
                Log.e(TAG,"Exception caught!",e);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetBlogPostTask extends AsyncTask<Object,Void,JSONObject> {


        @Override
        protected JSONObject doInBackground(Object... params) {
            int responseCode = -1;
            JSONObject jsonResponse = null;
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);

            try {
                HttpResponse response = client.execute(httpget);
                StatusLine statusLine = response.getStatusLine();
                responseCode = statusLine.getStatusCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while((line = reader.readLine()) != null){
                        builder.append(line);
                    }

                    jsonResponse = new JSONObject(builder.toString());
                }
                else {
                    Log.i(TAG, String.format("Unsuccessful HTTP response code: %d", responseCode));
                }
            }
            catch (JSONException e) {
                Log.e(TAG,"Exception caught!",e);
            }
            catch (Exception e) {
                Log.e(TAG, "Exception caught!", e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result){
            mBlogData = result;
            updateList();

        }

    }
}

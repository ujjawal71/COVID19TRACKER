package com.ujjawalproject.covid19trackerall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class StatesecondActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    Intent sharingIntent;
    // URL to get contacts JSON
    private static String url = "https://api.covid19india.org/data.json";

    ArrayList<HashMap<String, String>> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stateactivity_second);


        contactList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        new GetContacts().execute();
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ujjumenu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                new AlertDialog.Builder(this)
                        .setTitle("COVID-19")
                        .setCancelable(true)
                        .setMessage("About COVID19"+"\n\n"+"The new coronavirus is a respiratory virus which" +
                                " spreads primarily through droplets generated when an infected person coughs or sneezes, " +
                                "or through droplets of saliva or discharge from" +
                                " the nose. To protect yourself, clean your hands frequently with an alcohol-based hand " +
                                "rub or wash them with soap and water." +
                                "\n\n" +
                                "Software Developer: Ujjawal Kumar" +
                                "\n" +
                                "\n" +"Website\n\n"+
                                "www.csecoder.com")
                        .setPositiveButton("Close", null)
                        .setIcon(R.drawable.ic_info)
                        .show();
                return true;
            case R.id.action_share:
                sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Install (COVID-19 App) " +
                        "https://www.csecoder.com/corona/covid19.apk" +
                        "\n\n" +
                        "Source Code Available on GitHub\n" +
                        "https://www.github.com/ujjawal71";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "COVID-19 App");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share COVID-19 Android App"));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
















    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(StatesecondActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            StateHttpHandler sh = new StateHttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("statewise");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String state = c.getString("state");
                        String confirmed = c.getString("confirmed");
                        String recovered = c.getString("recovered");
                        String active = c.getString("active");
                        String deaths = c.getString("deaths");

                        // Phone node is JSON Object
                       /* JSONObject phone = c.getJSONObject("phone");
                        String mobile = phone.getString("mobile");
                        String home = phone.getString("home");
                        String office = phone.getString("office");*/

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("state", state);
                        contact.put("confirmed", confirmed);
                        contact.put("recovered", recovered);
                        contact.put("active",active);
                        contact.put("deaths",deaths);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())

                pDialog.dismiss();

            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    StatesecondActivity.this, contactList,
                    R.layout.statesingle_item, new String[]{"state", "active",
                    "confirmed","deaths","recovered"}, new int[]{R.id.main_state,
                    R.id.main_active, R.id.main_confirmed,R.id.main_death,R.id.main_recovered  });

            lv.setAdapter(adapter);
        }

    }
}
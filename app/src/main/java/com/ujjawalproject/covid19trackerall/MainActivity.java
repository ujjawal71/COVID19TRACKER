package com.ujjawalproject.covid19trackerall;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;




public class MainActivity extends AppCompatActivity {


    TextView textViewCases, textViewRecovered, textViewDeaths, textViewDate, textViewDeathsTitle,
            textViewRecoveredTitle, textViewActive, textViewActiveTitle, textViewNewDeaths,
            textViewNewCases, textViewNewDeathsTitle, textViewNewCasesTitle ;
    EditText textSearchBox;
    Handler handler;
    String url = "https://www.worldometers.info/coronavirus/";
    String tmpCountry, tmpCases, tmpRecovered, tmpDeaths, tmpPercentage, germanResults, tmpNewCases, tmpNewDeaths;
    org.jsoup.nodes.Document doc;
    Document germanDoc;
    org.jsoup.nodes.Element countriesTable;
    org.jsoup.nodes.Element row;
    Element germanTable;
    Elements countriesRows;
    Elements cols;
    Elements germanRows;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Calendar myCalender;
    SimpleDateFormat myFormat;
    double tmpNumber;
    DecimalFormat generalDecimalFormat;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    ListView listViewCountries;
    ujjurecyclerview listCountriesAdapter;
    ArrayList<allcountry> allCountriesResults, FilteredArrList;
    Intent sharingIntent;
    int colNumCountry, colNumCases, colNumRecovered, colNumDeaths, colNumActive, colNumNewCases, colNumNewDeaths;
    SwipeRefreshLayout mySwipeRefreshLayout;
    InputMethodManager inputMethodManager;
    Iterator<org.jsoup.nodes.Element> rowIterator;
    ProgressBar countryProgressBar;

    //state data
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    button=(Button)findViewById(R.id.button2);
    button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            statesecondActivitymethod();
        }
    });




        //country data



        textViewActiveTitle = (TextView)findViewById(R.id.textViewActiveTitle);
        textViewActive = (TextView)findViewById(R.id.textViewActive);

        textViewCases = (TextView)findViewById(R.id.textViewCases);
        textViewRecovered = (TextView)findViewById(R.id.textViewRecovered);
        textViewDeaths = (TextView)findViewById(R.id.textViewDeaths);
        textViewDate = (TextView)findViewById(R.id.textViewDate);
        textViewRecoveredTitle = (TextView)findViewById(R.id.textViewRecoveredTitle);
        textViewDeathsTitle = (TextView)findViewById(R.id.textViewDeathsTitle);
        textViewNewDeaths = (TextView)findViewById(R.id.textViewNewDeaths);
        textViewNewCases = (TextView)findViewById(R.id.textViewNewCases);
        textViewNewCasesTitle = (TextView)findViewById(R.id.textViewNewCasesTitle);
        textViewNewDeathsTitle = (TextView)findViewById(R.id.textViewNewDeathsTitle);
        listViewCountries = (ListView)findViewById(R.id.listViewCountries);
        textSearchBox = (EditText)findViewById(R.id.textSearchBox);
        countryProgressBar = (ProgressBar) findViewById(R.id.countryProgressBar);
        colNumCountry = 0; colNumCases = 1; colNumRecovered = 0; colNumDeaths = 0; colNumNewCases = 0; colNumNewDeaths = 0;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        myFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aaa", Locale.US);
        myCalender = Calendar.getInstance();
        handler = new Handler() ;
        generalDecimalFormat = new DecimalFormat("0.00", symbols);
        allCountriesResults = new ArrayList<allcountry>();

        // Implement Swipe to Refresh
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.coronaMainSwipeRefresh);
        mySwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshData();
                    }
                }
        );

        // fix interference between scrolling in listView & parent SwipeRefreshLayout
        listViewCountries.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        if (!listIsAtTop()) mySwipeRefreshLayout.setEnabled(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mySwipeRefreshLayout.setEnabled(true);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
            private boolean listIsAtTop()   {
                if(listViewCountries.getChildCount() == 0) return true;
                return listViewCountries.getChildAt(0).getTop() == 0;
            }
        });

        // fetch previously saved data in SharedPreferences, if any
        if(preferences.getString("textViewCases", null) != null ){
            textViewCases.setText(preferences.getString("textViewCases", null));
            textViewRecovered.setText(preferences.getString("textViewRecovered", null));
            textViewDeaths.setText(preferences.getString("textViewDeaths", null));
            textViewDate.setText(preferences.getString("textViewDate", null));
            textViewActive.setText(preferences.getString("textViewActive", null));
        }

        // Add Text Change Listener to textSearchBox to filter by Country
        textSearchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence searchSequence, int start, int before, int count) {
                FilteredArrList = new ArrayList<allcountry>();
                if (searchSequence == null || searchSequence.length() == 0) {
                    // back to original
                    setListViewCountries(allCountriesResults);
                } else {
                    searchSequence = searchSequence.toString().toLowerCase();
                    for (int i = 0; i < allCountriesResults.size(); i++) {
                        String data = allCountriesResults.get(i).countryName;
                        if (data.toLowerCase().startsWith(searchSequence.toString())) {
                            FilteredArrList.add(new allcountry(
                                    allCountriesResults.get(i).countryName,
                                    allCountriesResults.get(i).cases,
                                    allCountriesResults.get(i).newCases,
                                    allCountriesResults.get(i).recovered,
                                    allCountriesResults.get(i).deaths,
                                    allCountriesResults.get(i).newDeaths));
                        }
                    }
                    // set the Filtered result to return
                    setListViewCountries(FilteredArrList);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Hide keyboard after hitting done button
        textSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do something, e.g. set your TextView here via .setText()
                    inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    textSearchBox.clearFocus();
                    return true;
                }
                return false;
            }
        });

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String filtered = "";
                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (!Character.isWhitespace(character)) {
                        filtered += character;
                    }
                }

                return filtered;
            }

        };

        textSearchBox.setFilters(new InputFilter[] { filter });
        textSearchBox.clearFocus();
        // Call refreshData once the app is opened only one time, then user can request updates
        refreshData();
    }




    public void statesecondActivitymethod()
    {

        Intent intent =new Intent(this , StatesecondActivity.class);
        startActivity(intent);
    }





    @Override
    protected void onResume() {
        super.onResume();
        textSearchBox.clearFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ujjumenu, menu);
        return true;
    }

    void setListViewCountries(ArrayList<allcountry> allCountriesResults) {
        Collections.sort(allCountriesResults);
        listCountriesAdapter = new ujjurecyclerview(this, allCountriesResults);
        listViewCountries.setAdapter(listCountriesAdapter);
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

    void calculate_percentages () {

        tmpNumber = Double.parseDouble(textViewRecovered.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100;
        textViewRecoveredTitle.setText("Cured " + generalDecimalFormat.format(tmpNumber) + "%");



        tmpNumber = Double.parseDouble(textViewDeaths.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100 ;
        textViewDeathsTitle.setText("Death " + generalDecimalFormat.format(tmpNumber) + "%");

        tmpNumber = Double.parseDouble(textViewActive.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100 ;
        textViewActiveTitle.setText("All Active   " + generalDecimalFormat.format(tmpNumber) + "%");


    }

    void refreshData() {
        mySwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    doc = null; // Fetches the HTML document
                    doc = Jsoup.connect(url).timeout(10000).get();
                    // table id main_table_countries
                    countriesTable = doc.select("table").get(0);
                    countriesRows = countriesTable.select("tr");

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // get countries
                            rowIterator = countriesRows.iterator();
                            allCountriesResults = new ArrayList<allcountry>();

                            // read table header and find correct column number for each category
                            row = rowIterator.next();
                            cols = row.select("th");
                            //Log.e("COLS: ", cols.text());
                            if (cols.get(0).text().contains("Country")) {
                                for(int i=1; i < cols.size(); i++){
                                    if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Cases"))
                                    {colNumCases = i; Log.e("Cases: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Recovered"))
                                    {colNumRecovered = i; Log.e("Recovered: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Deaths"))
                                    {colNumDeaths = i; Log.e("Deaths: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("Active") && cols.get(i).text().contains("Cases"))
                                    {colNumActive = i; Log.e("Active: ", cols.get(i).text());}

                                    else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Cases"))
                                    {colNumNewCases = i; Log.e("NewCases: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Deaths"))
                                    {colNumNewDeaths = i; Log.e("NewDeaths: ", cols.get(i).text());}
                                }
                            }

                            while (rowIterator.hasNext()) {
                                row = rowIterator.next();
                                cols = row.select("td");

                                if (cols.get(0).text().contains("World")) {
                                    textViewCases.setText(cols.get(colNumCases).text());
                                    textViewRecovered.setText(cols.get(colNumRecovered).text());
                                    textViewDeaths.setText(cols.get(colNumDeaths).text());

                                    if (cols.get(colNumActive).hasText()) {textViewActive.setText(cols.get(colNumActive).text());}
                                    else {textViewActive.setText("0");}
                                    if (cols.get(colNumNewCases).hasText()) {textViewNewCases.setText(cols.get(colNumNewCases).text());}
                                    else {textViewNewCases.setText("0");}
                                    if (cols.get(colNumNewDeaths).hasText()) {textViewNewDeaths.setText(cols.get(colNumNewDeaths).text());}
                                    else {textViewNewDeaths.setText("0");}
                                    continue;
                                } else if (
                                        cols.get(0).text().contains("Total") ||
                                                cols.get(0).text().contains("Europe") ||
                                                cols.get(0).text().contains("North America") ||
                                                cols.get(0).text().contains("Asia") ||
                                                cols.get(0).text().contains("South America") ||
                                                cols.get(0).text().contains("Africa") ||
                                                cols.get(0).text().contains("Oceania")
                                ) {
                                    continue;
                                }

                                if (cols.get(colNumCountry).hasText()) {tmpCountry = cols.get(0).text();}
                                else {tmpCountry = "NA";}

                                if (cols.get(colNumCases).hasText()) {tmpCases = cols.get(colNumCases).text();}
                                else {tmpCases = "0";}

                                if (cols.get(colNumRecovered).hasText()){
                                    if(!cols.get(colNumRecovered).text().contains("N/A")) {
                                        tmpRecovered = cols.get(colNumRecovered).text();
                                        tmpPercentage = (generalDecimalFormat.format(Double.parseDouble(tmpRecovered.replaceAll(",", ""))
                                                / Double.parseDouble(tmpCases.replaceAll(",", ""))
                                                * 100)) + "%";

                                        tmpRecovered = tmpRecovered + " \n " + tmpPercentage;

                                    }
                                    else {tmpRecovered = "NA";}
                                }
                                else {tmpRecovered = "0";}

                                if(cols.get(colNumDeaths).hasText()) {
                                    if(!cols.get(colNumDeaths).text().contains("N/A")) {
                                        tmpDeaths = cols.get(colNumDeaths).text();
                                        tmpPercentage = (generalDecimalFormat.format(Double.parseDouble(tmpDeaths.replaceAll(",", ""))
                                                / Double.parseDouble(tmpCases.replaceAll(",", ""))
                                                * 100)) + "%";
                                        tmpDeaths = tmpDeaths + " \n " + tmpPercentage;
                                    }
                                    else {tmpDeaths = "NA";}
                                }
                                else {tmpDeaths = "0";}

                                if (cols.get(colNumNewCases).hasText()) {tmpNewCases = cols.get(colNumNewCases).text();}
                                else {tmpNewCases = "0";}

                                if (cols.get(colNumNewDeaths).hasText()) {tmpNewDeaths = cols.get(colNumNewDeaths).text();}
                                else {tmpNewDeaths = "0";}

                                allCountriesResults.add(new allcountry(tmpCountry, tmpCases, tmpNewCases, tmpRecovered, tmpDeaths, tmpNewDeaths));
                            }

                            setListViewCountries(allCountriesResults);
                            textSearchBox.setText(null);
                            textSearchBox.clearFocus();

                            // save results
                            editor.putString("textViewCases", textViewCases.getText().toString());
                            editor.putString("textViewRecovered", textViewRecovered.getText().toString());
                            editor.putString("textViewActive", textViewActive.getText().toString());
                            editor.putString("textViewDeaths", textViewDeaths.getText().toString());
                            editor.putString("textViewDate", textViewDate.getText().toString());
                            editor.apply();

                            calculate_percentages();

                            myCalender = Calendar.getInstance();
                            textViewDate.setText("" + myFormat.format(myCalender.getTime()));
                        }
                    });
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Network Not Available!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                finally {
                    doc = null;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mySwipeRefreshLayout.setRefreshing(false);
                    }});
            }
        }).start();
    }
}


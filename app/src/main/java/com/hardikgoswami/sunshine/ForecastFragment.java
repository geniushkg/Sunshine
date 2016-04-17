package com.hardikgoswami.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by geniushkg on 4/16/2016.
 */
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> mForeCastAdapter;
    private ArrayList<String> mForeCastData;
    public ForecastFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] foreCastArray = {"Today - Sunny 88/63 ", "Tommorow - Cloudy - 77/54",
                "Wednesday - Clear - 77/88", "Thursday- Sunny -66/44", "Friday- clear - 45/78",
                "Saturday - Clear - 45/54"};
        ArrayList<String> foreCastData = new ArrayList<>(Arrays.asList(foreCastArray));
        mForeCastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, foreCastData);
        ListView listViewForeCast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listViewForeCast.setAdapter(mForeCastAdapter);
        return rootView;
    }

    void processFinish(String output){
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.forecastfragment,menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                Toast.makeText(getContext(), "menu selected refresh ", Toast.LENGTH_SHORT).show();
                Log.d("TAG","refresh log");
                FetchWeatherTask weatherTask = new FetchWeatherTask();
                weatherTask.execute("94046");
                return true;
            default:
                Toast.makeText(getContext(),"default executed",Toast.LENGTH_SHORT).show();
                return true;
        }
    }
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private String[] foreCastArray = null;

        public AsyncResponse delegate = null;

        @Override
        protected void onPostExecute(String[] result) {
            delegate.processFinish(result);
        }

        @Override
        protected String[] doInBackground(String... params) {

            Log.d("TAG","fetch weather executed");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            String postCode = params[0];
            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("q",postCode)
                        .appendQueryParameter("mode","json")
                        .appendQueryParameter("units","metric")
                        .appendQueryParameter("cnt","7")
                        .appendQueryParameter("APPID","8e7d7fff8df57f165ec4c588739c656e");
                String urlBuilt = builder.build().toString();
            //     String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q="+postCode+"&mode=json&units=metric&cnt=7";
            //    String apiKey = "&APPID=8e7d7fff8df57f165ec4c588739c656e";// + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                URL url = new URL(urlBuilt);
                Log.d("TAG","url is : "+url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    Log.d("TAG","input stream null");
                    return null;
                }
                Log.d("TAG","input stream is :"+inputStream);
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("TAG","line is :"+line);
                    Log.d("TAG","reader content is:"+reader);
                }
                Log.d("TAG","buffer content is :"+buffer);
                if (buffer.length() == 0) {
                    Log.d("TAG","Null buffer");
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.d("TAG","forecast json is : "+forecastJsonStr);
                foreCastArray = getWeatherDataFromJson(forecastJsonStr,7);
                for (String s : foreCastArray){
                    Log.d("TAG","data is : "+s);
                }

            } catch (IOException e) {
                Log.e("TAG", "Error "+e.getMessage());
                Log.d("TAG","exception occured : "+e.getMessage());
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                    Log.d("TAG","url connection disconnected");

                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("TAG", "Error closing stream", e);
                    }
                }
                mForeCastData = new ArrayList<>(Arrays.asList(foreCastArray));
                return foreCastArray;
            }
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }


    }

    public interface AsyncResponse {
        void processFinish(String[] output);
    }

}

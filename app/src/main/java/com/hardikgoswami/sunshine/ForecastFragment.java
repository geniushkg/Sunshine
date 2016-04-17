package com.hardikgoswami.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by geniushkg on 4/16/2016.
 */
public class ForecastFragment extends Fragment {
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
        ArrayAdapter<String> foreCastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, foreCastData);
        ListView listViewForeCast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listViewForeCast.setAdapter(foreCastAdapter);
        return rootView;
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
    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        private String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        //94043
        @Override
        protected Void doInBackground(String... params) {

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
                return null;
            }
        }


    }
}

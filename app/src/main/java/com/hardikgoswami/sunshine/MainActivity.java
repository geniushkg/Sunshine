package com.hardikgoswami.sunshine;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static String TAG ="SUNSHINE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new PlaceHolderFragment())
                    .commit();
        }
    }
    public static class PlaceHolderFragment extends Fragment {
        public PlaceHolderFragment() {
        }
        @Nullable

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container ,false);
            String[] foreCastArray = {"Today - Sunny 88/63 ", "Tommorow - Cloudy - 77/54",
                    "Wednesday - Clear - 77/88", "Thursday- Sunny -66/44", "Friday- clear - 45/78",
                    "Saturday - Clear - 45/54"};
            ArrayList<String> foreCastData = new ArrayList<>(Arrays.asList(foreCastArray));
            ArrayAdapter<String> foreCastAdapter = new ArrayAdapter<>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,foreCastData);
            ListView listViewForeCast = (ListView)rootView.findViewById(R.id.listview_forecast);
            listViewForeCast.setAdapter(foreCastAdapter);

            // declaring outside try/catch so that can be closed in finally
            HttpURLConnection httpURLConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            try{
                // construct new url
                URL url = new URL("http://api.openweathermap.org");
            }catch (MalformedURLException urlException){
                Log.d(TAG,"Malformed URL : "+urlException);
            }


          return rootView;
        }
    }
}

package com.example.android.sunshine.app;

/**
 * Created by arbalan on 7/16/16.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private static final int DAYS = 7;
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> forecastList = new ArrayList<>();
        mForecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.view_list_item_forecast, R.id.list_item_forecast_textview, forecastList);

        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(mForecastAdapter);
        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent activityIntent = new Intent(getActivity(), DetailActivity.class);
                activityIntent.putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(position));
                startActivity(activityIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forecast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
           updateWeatherData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeatherData();
    }

    private void updateWeatherData(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        new WeatherRequestTask(location, DAYS).execute();
    }

    private class WeatherRequestTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = WeatherRequestTask.class.getSimpleName();
        private String zipCode;
        private int numDays;
        private final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
        private final String QUERY_PARAM = "q";
        private final String FORMAT_PARAM = "mode";
        private final String UNITS_PARAM = "units";
        private final String DAYS_PARAM = "cnt";
        private final String APPID_PARAM = "appid";

        public WeatherRequestTask(String code, int days) {
            zipCode = code;
            numDays = days;
        }

        @Override
        protected String[] doInBackground(String... params) {
            return getJsonDataFromWeatherApI(zipCode, numDays);
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                List<String> weekForecast = new ArrayList<>(Arrays.asList(result));
                mForecastAdapter.clear();
                for (String forecast : weekForecast) {
                    mForecastAdapter.add(forecast);
                }
                mForecastAdapter.notifyDataSetChanged();
            }
        }

        private String[] getJsonDataFromWeatherApI(String zipCode, int numDays) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                Uri.Builder builder = new Uri.Builder().encodedPath(BASE_URL)
                        .appendQueryParameter(QUERY_PARAM, zipCode)
                        .appendQueryParameter(FORMAT_PARAM, "json")
                        .appendQueryParameter(UNITS_PARAM, "metric")
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY);

                Uri uri = builder.build();
                URL url = new URL(uri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.i(LOG_TAG, "Forecast output : json : " + forecastJsonStr);

                try {
                    return WeatherDataParser.getWeatherDataFromJson(forecastJsonStr, numDays);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}

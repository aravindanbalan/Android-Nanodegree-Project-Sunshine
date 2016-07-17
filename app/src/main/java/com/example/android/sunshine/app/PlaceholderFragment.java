package com.example.android.sunshine.app;

/**
 * Created by arbalan on 7/16/16.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> forecastList = new ArrayList<>();
        forecastList.add("Today - Sunny - 88 / 63");
        forecastList.add("Tomorrow - Foggy - 70 / 46");
        forecastList.add("Weds - Cloudy - 72 / 63");
        forecastList.add("Thurs - Rainy - 64 / 51");
        forecastList.add("Fri - Foggy - 70 / 46");
        forecastList.add("Sat - Sunny - 76 / 68");

        return rootView;
    }
}

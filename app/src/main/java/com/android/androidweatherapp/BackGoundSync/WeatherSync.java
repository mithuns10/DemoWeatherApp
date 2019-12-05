package com.android.androidweatherapp.BackGoundSync;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class WeatherSync extends JobService {
    private LocationManager locationManager;
    private static final int FINE_LOCATION_REQUEST_CODE = 10;
    private Location location;
    private String provider;

    @Override
    public boolean onStartJob(JobParameters params) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        doInBackGround(params);
        return true;
    }

    private void doInBackGround(JobParameters parameters) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String strJsonResp = getWeatherDetails("http://api.openweathermap.org/data/2.5/weather?q=" + getLocationDetails() + "&appid=5ad7218f2e11df834b0eaf3a33a39d2a");
                    sendBroadcast(new Intent("JOB_SERVICE_SUCCESS").putExtra("JSON_RESPONSE", strJsonResp));
                }
            }).start();
        } catch (Exception e) {
        }
    }

    private String getWeatherDetails(String strURL) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(strURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            return buffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getLocationDetails() {
        String strResult = "";
        try {
            if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
                return " ";
            }
            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);
            location = getLastKnownLocation();
            if (location != null) {
                System.out.println("Provider " + provider + " has been selected.");
                strResult = onLocationChanged(location);
            }
        } catch (Exception e) {

        }
        return strResult;
    }


    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public String onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String strCityName = "";
        try {
            Geocoder geoCoder = new Geocoder(getApplication(), Locale.getDefault());
            List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
            strCityName = address.get(0).getLocality();
        } catch (IOException e) {
        } catch (NullPointerException e) {
        }
        return strCityName;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}

package com.android.androidweatherapp.BackGoundSync;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.androidweatherapp.Activities.WeatherActivity;
import com.android.androidweatherapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class BackGroundSycAndFetch extends AsyncTask<String, Void, String> {

    private static final int FINE_LOCATION_REQUEST_CODE = 10;
    private LocationManager locationManager;
    private WeatherActivity weatherActivity;
    private Location location;
    private String provider;
    private ProgressDialog mProgressSpinner;
    private boolean isWeatherDetailsSync;

    public BackGroundSycAndFetch(WeatherActivity weatherActivity, LocationManager locationManager, boolean isWeatherDetailsSync) {
        this.locationManager = locationManager;
        this.weatherActivity = weatherActivity;
        this.isWeatherDetailsSync = isWeatherDetailsSync;
        mProgressSpinner = new ProgressDialog(weatherActivity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            mProgressSpinner.setCancelable(false);
            mProgressSpinner.setMessage("Fetching the location please wait!...");
            mProgressSpinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressSpinner.setIndeterminate(true);
            Drawable customDrawableSpinner = weatherActivity.getResources().getDrawable(R.drawable.progressbar_spinner);
            mProgressSpinner.setIndeterminateDrawable(customDrawableSpinner);
            mProgressSpinner.show();
        } catch (Exception e) {

        }

    }

    @Override
    protected String doInBackground(String... strings) {

        String strResult = "";
        try {
            if (isWeatherDetailsSync) {
                strResult = getWeatherDetails(strings[0]);
            } else {
                if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
                    return "";
                } else {
                    ActivityCompat.requestPermissions(weatherActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
                }
                Criteria criteria = new Criteria();
                provider = locationManager.getBestProvider(criteria, false);
                location = getLastKnownLocation();


                if (location != null) {
                    System.out.println("Provider " + provider + " has been selected.");
                    strResult = onLocationChanged(location);
                }
            }
        } catch (Exception e) {
            Log.v("Exception", e.toString());
        }
        return strResult;
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

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) weatherActivity.getApplicationContext().getSystemService(LOCATION_SERVICE);
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
            Geocoder geoCoder = new Geocoder(weatherActivity, Locale.getDefault());
            List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
            strCityName = address.get(0).getLocality();

        } catch (IOException e) {
        } catch (NullPointerException e) {
        }
        return strCityName;
    }


    @Override
    protected void onPostExecute(String strResult) {
        super.onPostExecute(strResult);
        if (mProgressSpinner.isShowing()) {
            mProgressSpinner.dismiss();
        }
    }
}

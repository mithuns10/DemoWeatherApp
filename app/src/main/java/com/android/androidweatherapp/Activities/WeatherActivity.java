package com.android.androidweatherapp.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.androidweatherapp.BackGoundSync.WeatherSync;
import com.android.androidweatherapp.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {

    TextView addressTxt;
    TextView updated_atTxt;
    TextView statusTxt;
    TextView tempTxt;
    TextView temp_minTxt;
    TextView temp_maxTxt;
    TextView sunriseTxt;
    TextView sunsetTxt;
    TextView windTxt;
    TextView pressureTxt;
    TextView humidityTxt;
    private Button btnWeatherDetails;
    private String strServerResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            initialize();
            btnWeatherDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ComponentName componentName = new ComponentName(WeatherActivity.this, WeatherSync.class);
                    JobInfo jobInfo = new JobInfo.Builder(123, componentName).setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED).setPersisted(true).setPeriodic(2 * 60 * 60 * 1000).build();
                    JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                    scheduler.schedule(jobInfo);
                }
            });
        } catch (Exception e) {
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("JOB_SERVICE_SUCCESS")) {
                strServerResponse = intent.getExtras().getString("JSON_RESPONSE");
                UpdateUI(strServerResponse);
            }
        }
    };

    private void initialize() {
        try {
            registerReceiver(receiver, new IntentFilter("JOB_SERVICE_SUCCESS"));
            addressTxt = findViewById(R.id.address);
            updated_atTxt = findViewById(R.id.updated_at);
            statusTxt = findViewById(R.id.status);
            tempTxt = findViewById(R.id.temp);
            temp_minTxt = findViewById(R.id.temp_min);
            temp_maxTxt = findViewById(R.id.temp_max);
            sunriseTxt = findViewById(R.id.sunrise);
            sunsetTxt = findViewById(R.id.sunset);
            windTxt = findViewById(R.id.wind);
            pressureTxt = findViewById(R.id.pressure);
            humidityTxt = findViewById(R.id.humidity);
            btnWeatherDetails = findViewById(R.id.btnWeatherDetails);
            isPermissionGranted(WeatherActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 0);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("THE_RESPONSE", strServerResponse);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String strResponse = savedInstanceState.getString("THE_RESPONSE");
        if (!strResponse.isEmpty()) {
            UpdateUI(strResponse);
        }
    }

    private void UpdateUI(String strResult) {
        try {
            JSONObject jsonObj = new JSONObject(strResult);
            JSONObject main = jsonObj.getJSONObject("main");
            JSONObject sys = jsonObj.getJSONObject("sys");
            JSONObject wind = jsonObj.getJSONObject("wind");
            JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);
            Long updatedAt = jsonObj.getLong("dt");
            String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
            String temp = main.getString("temp") + "°C";
            String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
            String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
            String pressure = main.getString("pressure");
            String humidity = main.getString("humidity");

            Long sunrise = sys.getLong("sunrise");
            Long sunset = sys.getLong("sunset");
            String windSpeed = wind.getString("speed");
            String weatherDescription = weather.getString("description");

            String address = jsonObj.getString("name") + ", " + sys.getString("country");


            addressTxt.setText(address);
            updated_atTxt.setText(updatedAtText);
            statusTxt.setText(weatherDescription.toUpperCase());
            tempTxt.setText(temp);
            temp_minTxt.setText(tempMin);
            temp_maxTxt.setText(tempMax);
            sunriseTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
            sunsetTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
            windTxt.setText(windSpeed);
            pressureTxt.setText(pressure);
            humidityTxt.setText(humidity);

            findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            findViewById(R.id.errorText).setVisibility(View.VISIBLE);
        }
    }

    public static boolean isPermissionGranted(Activity activity, String[] permissionType, int requestCode) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                ArrayList<String> list = new ArrayList<>();
                for (int i = 0; i < permissionType.length; i++) {
                    if (activity.checkCallingOrSelfPermission(permissionType[i]) != PackageManager.PERMISSION_GRANTED) {
                        list.add(permissionType[i]);
                    }
                }
                if (list.size() > 0) {
                    String[] PermissionRequest = list.toArray(new String[list.size()]);
                    ActivityCompat.requestPermissions(activity, PermissionRequest, requestCode);
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e("isPermissionGranted", "" + e);
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

}

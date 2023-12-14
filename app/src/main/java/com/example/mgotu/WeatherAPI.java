package com.example.mgotu;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;


public class WeatherAPI {
    private static final String appid = "e53301e27efa0b66d05045d91b2742d3";
    private static final String urlweather = "https://api.openweathermap.org/data/2.5/weather";
    private final Application application;

    public WeatherAPI(Application application) {
        this.application = application;
    }

    public void getWeatherDetails() {
        String city = "Королёв";
        String country = "RU";
        String tempUrl = urlweather + "?q=" + city + "," + country + "&appid=" + appid + "&units=metric";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, tempUrl, null, response -> {
            try {
                JSONObject jsonObjectMain = response.getJSONObject("main");
                double temp = jsonObjectMain.getDouble("temp");
                double feelsLike = jsonObjectMain.getDouble("feels_like");
                DecimalFormat df = new DecimalFormat("#");
                Toast.makeText(application.getApplicationContext(), "Температура " + city + ": " + df.format(temp) + "°C\n Ощущается как : " + df.format(feelsLike) + "°C", Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("getWeatherDetails", error.toString().trim()));

        RequestQueue requestQueue = Volley.newRequestQueue(application.getApplicationContext());
        requestQueue.add(jsonObjectRequest);
    }
}
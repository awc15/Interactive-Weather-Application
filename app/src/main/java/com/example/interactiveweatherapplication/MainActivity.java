package com.example.interactiveweatherapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.StrictModeUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    public static final String OPEN_WEATHER_MAP_URL = "api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}";
    public static final String OPEN_WEATHER_MAP_API = "b30d8b3bbeddee2d9cf7c263bf30bba5";
    public String IMAGE_ICON_URL = "http://openweathermap.org/img/w/";
    TextView cityField, detailsField, currentTemperatureField, humidityField, pressureField, updateField;
    ImageView weatherIcon;
    Typeface weatherFont;
    private EditText enterCity;
    static String latitude;
    static String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allFindViewById();
        /*weatherFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);*/
      /*  locationEnabled();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        requestPermissions();*/

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); //To check wifi is on/off

        if (!mWifi.isConnected() && !isMobileDataEnabled()) {
            showNoInternetDialog();
        }

        FusedLocationProviderClient mFusedLocationProvider;
        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        mFusedLocationProvider.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(lat, lng, 3);
                        String cityName = addresses.get(0).getLocality();
                        getCurrentWeather(cityName);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

               /* String[] jsonData = getJSONResponse();

                cityField.setText(jsonData[0]);
                detailsField.setText(jsonData[1]);
                currentTemperatureField.setText(jsonData[2]);
                humidityField.setText(jsonData[3]);
                pressureField.setText(jsonData[4]);
                updateField.setText(jsonData[5]);
                weatherIcon.setText(jsonData[6]);*/
            }
        });


    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Kindly Turn on your internet connection and press OK to use this application")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //this will always start your activity as a new task
                        startActivity(intent);
                    }
                })
                .setIcon(R.drawable.ic_wifi_off)
                .setCancelable(false)
                .show();

    }

    private boolean isMobileDataEnabled() {
        boolean mobileDataEnabled = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);

            mobileDataEnabled = (Boolean) method.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mobileDataEnabled;
    }

    /*  public String[] getJSONResponse() {
          String[] jsonData = new String[7];
          JSONObject jsonWeather = null;
          try {
              jsonWeather = getWeatherJSON(latitude, longitude);

          } catch (Exception e) {
              Log.d("ERROR", "cannot process JSON results", e);
          }

          try {
              if (jsonWeather != null) {
                  JSONObject details = jsonWeather.getJSONArray("weather").getJSONObject(0);
                  JSONObject main = jsonWeather.getJSONObject("main");
                  DateFormat dateFormat = DateFormat.getDateInstance();

                  String city = jsonWeather.getString("name") + ", " + jsonWeather.getJSONObject("sys").getString("country");
                  String description = details.getString("description").toLowerCase(Locale.US);
                  String temperature = String.format("%.0f", main.getDouble("temp")) + "°";
                  String humidity = "Humidity Percentage : "+main.getString("humidity") + "%";
                  String pressure = "Air Pressure : "+main.getString("pressure") + "hPa";
                  String updateOn = dateFormat.format(new Date(jsonWeather.getLong("dt") * 1000));
                  String iconText = setWeatherIcon(details.getInt("id"), jsonWeather.getJSONObject("sys").getLong("sunrise") * 1000,
                          jsonWeather.getJSONObject("sys").getLong("sunset") * 1000);

                  jsonData[0] = city;
                  jsonData[1] = description;
                  jsonData[2] = temperature;
                  jsonData[3] = humidity;
                  jsonData[4] = pressure;
                  jsonData[5] = updateOn;
                  jsonData[6] = iconText;
              }
          } catch (Exception e) {

          }

          return jsonData;
      }
  */
    public static JSONObject getWeatherJSON(String latitude, String longitude) {
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_URL, latitude, longitude));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("x-API-key", OPEN_WEATHER_MAP_API);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer json = new StringBuffer(1024);
            String tmp = "";
            while ((tmp = reader.readLine()) != null) {
                json.append(tmp).append("\n");
            }
            reader.close();
            JSONObject data = new JSONObject(json.toString());
            if (data.getInt("cod") != 200) {
                return null;
            }
            return data;

        } catch (Exception e) {
            return null;
        }
    }

    private void allFindViewById() {
        enterCity = findViewById(R.id.enterCityEditText);
        cityField = findViewById(R.id.city_field);
        detailsField = findViewById(R.id.details_field);
        currentTemperatureField = findViewById(R.id.current_temperature_field);
        humidityField = findViewById(R.id.humidity_field);
        pressureField = findViewById(R.id.pressure_field);
        weatherIcon = findViewById(R.id.weather_icon);
        updateField = findViewById(R.id.updated_field);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);

    }

    private void locationEnabled() {
        LocationManager lm = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Enable GPS")
                    .setMessage("Kindly Enable GPS")
                    .setCancelable(false)
                    .setPositiveButton("Settings", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    finish();
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();

        }
    }

    public static String setWeatherIcon(int actualId, Integer sunrise, Integer sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = "\uF00D";
            } else {
                icon = "\uF02E";

            }
        } else {
            switch (id) {
                case 2:
                    icon = "\uF01E";
                    break;
                case 3:
                    icon = "\uf01c;";
                    break;
                case 7:
                    icon = "\uf014;";
                    break;
                case 8:
                    icon = "\uf013;";
                    break;
                case 6:
                    icon = "\uf01b;";
                    break;
                case 5:
                    icon = "\uf019;";
                    break;
            }
        }
        return icon;
    }

    public void getCurrentWeather(String cityName){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherApi myApi = retrofit.create(WeatherApi.class);
        final Call<GetWeatherData> getWeatherData = myApi.getWeather(cityName, OPEN_WEATHER_MAP_API);
        getWeatherData.enqueue(new Callback<GetWeatherData>() {
            @Override
            public void onResponse(Call<GetWeatherData> call, Response<GetWeatherData> response) {
                if (response.code() == 404) {
                    Toast.makeText(getApplicationContext(), "Enter Valid", Toast.LENGTH_SHORT).show();
                } else if (!(response.isSuccessful())) {
                    Toast.makeText(getApplicationContext(), "Response : " + response, Toast.LENGTH_SHORT).show();
                } else {
                    GetWeatherData weatherData = response.body();
                    List<Weather> weather = weatherData.getWeather();
                    Main main = weatherData.getMain();
                    setAllWeatherData(main, weather);


                }
            }

            @Override
            public void onFailure(Call<GetWeatherData> call, Throwable t) {

            }
        });

    }

    public void getWeather(View view) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherApi myApi = retrofit.create(WeatherApi.class);
        final Call<GetWeatherData> getWeatherData = myApi.getWeather(enterCity.getText().toString().trim(), OPEN_WEATHER_MAP_API);
        getWeatherData.enqueue(new Callback<GetWeatherData>() {
            @Override
            public void onResponse(Call<GetWeatherData> call, Response<GetWeatherData> response) {
                if (response.code() == 404) {
                    Toast.makeText(getApplicationContext(), "Enter Valid", Toast.LENGTH_SHORT).show();
                } else if (!(response.isSuccessful())) {
                    Toast.makeText(getApplicationContext(), "Response : " + response, Toast.LENGTH_SHORT).show();
                } else {
                    GetWeatherData weatherData = response.body();
                    List<Weather> weather = weatherData.getWeather();
                    Main main = weatherData.getMain();
                    setAllWeatherData(main, weather);


                }
            }

            @Override
            public void onFailure(Call<GetWeatherData> call, Throwable t) {

            }
        });
    }


    private void setAllWeatherData(Main main, List<Weather> weather) {


        String desc = weather.get(0).getDescription();
        detailsField.setText(desc);

        String icon = weather.get(0).getIcon();
        IMAGE_ICON_URL = IMAGE_ICON_URL + icon + ".png";

        Picasso.with(getApplicationContext()).load(IMAGE_ICON_URL).into(weatherIcon);
        //weatherIcon.setText(icon);

        Double temp = main.getTemp();
        Integer temperature = (int) (temp - 273.15);
        currentTemperatureField.setText(String.valueOf(temperature) + "°");

        cityField.setText(enterCity.getText().toString().trim());

        Integer pressure = main.getPressure();
        pressureField.setText("Air Pressure : " + String.valueOf(pressure) + " hPa");

        Integer humidity = main.getHumidity();
        humidityField.setText("Humidity Percentage : " + String.valueOf(humidity) + "%");
    }
}
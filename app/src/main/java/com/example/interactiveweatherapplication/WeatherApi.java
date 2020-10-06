package com.example.interactiveweatherapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    @GET("weather")
    Call<GetWeatherData> getWeather(@Query("q") String cityName,
                                    @Query("appid") String apiKey);
}

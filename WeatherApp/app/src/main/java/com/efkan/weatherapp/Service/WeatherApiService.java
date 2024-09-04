package com.efkan.weatherapp.Service;

import com.efkan.weatherapp.Model.WeatherResponse;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface WeatherApiService {
    @GET
    Observable<WeatherResponse> getWeatherInfo(@Url String url);
}

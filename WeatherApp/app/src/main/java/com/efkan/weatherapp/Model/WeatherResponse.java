package com.efkan.weatherapp.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("current")
    public CurrentWeather current;

    @SerializedName("forecast")
    public Forecast forecast;

    public static class CurrentWeather {
        @SerializedName("temp_c")
        public String tempC;

        @SerializedName("is_day")
        public int isDay;

        @SerializedName("condition")
        public Condition condition;
    }

    public static class Condition {
        @SerializedName("text")
        public String text;

        @SerializedName("icon")
        public String icon;
    }

    public static class Forecast {
        @SerializedName("forecastday")
        public List<ForecastDay> forecastDayList;
    }

    public static class ForecastDay {
        @SerializedName("date")
        public String date;

        @SerializedName("day")
        public Day day;

        @SerializedName("hour")
        public List<Hour> hourList;
    }

    public static class Day {
        @SerializedName("maxtemp_c")
        public String maxTempC;

        @SerializedName("mintemp_c")
        public String minTempC;

        @SerializedName("condition")
        public Condition condition;
    }

    public static class Hour {
        @SerializedName("time")
        public String time;

        @SerializedName("temp_c")
        public String tempC;

        @SerializedName("condition")
        public Condition condition;

        @SerializedName("wind_kph")
        public String windKph;
    }
}

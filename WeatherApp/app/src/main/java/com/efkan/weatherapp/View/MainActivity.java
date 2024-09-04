package com.efkan.weatherapp.View;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.efkan.weatherapp.Adapter.WeatherRvAdapter;
import com.efkan.weatherapp.Model.WeatherResponse;
import com.efkan.weatherapp.R;
import com.efkan.weatherapp.Service.WeatherApiService;
import com.efkan.weatherapp.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private static final int PERMISSION_CODE = 1;
    private String cityName = "";
    private ArrayList<WeatherResponse.Hour> weatherHourList;
    private WeatherRvAdapter weatherRvAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    // Retrofit and service instance
    private Retrofit retrofit;
    private WeatherApiService weatherApiService;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.weatherapi.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        weatherApiService = retrofit.create(WeatherApiService.class);

        weatherHourList = new ArrayList<>();
        weatherRvAdapter = new WeatherRvAdapter(this, weatherHourList);
        binding.idRvWeather.setAdapter(weatherRvAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_CODE);
        } else {
            getLocation();
        }

        binding.IVSearch.setOnClickListener(v -> {
            String cityName = binding.idEdtCity.getText().toString();
            if (cityName.equals("")) {
                Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
            } else {
                binding.idTVCityName.setText(cityName);
                String city = convertTurkishCharacters(cityName); // Türkçe karakterleri İngilizce karakterlere dönüştür
                cityName = city;
                getWeatherInfo(cityName);
            }
            //binding.idEdtCity.setText("");
        });
        binding.IVLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocationUpdates();
                Toast.makeText(MainActivity.this,"Location is updating..",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Log.d("MainActivity", "Son Bilinen Konum: Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                cityName = getCityName(location.getLatitude(), location.getLongitude());
                if (cityName.equals("Not found")) {
                    Log.d("MainActivity", "Son bilinen konumdan şehir bulunamadı, güncel konum güncellemeleri başlatılıyor...");
                    requestLocationUpdates();
                } else {
                    getWeatherInfo(cityName);
                }
            } else {
                Log.d("MainActivity", "Son bilinen konum boş, güncel konum güncellemeleri başlatılıyor...");
                requestLocationUpdates();
            }
        }).addOnFailureListener(e -> {
            Log.e("MainActivity", "Son bilinen konumu almakta hata oluştu: " + e.getMessage());
            requestLocationUpdates();
        });
    }

    private void requestLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 saniyede bir güncelle
        locationRequest.setFastestInterval(5000); // 5 saniyede bir en hızlı güncelleme

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d("MainActivity", "Konum güncellenmedi.");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d("MainActivity", "Güncellenmiş Konum: Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                        cityName = getCityName(location.getLatitude(), location.getLongitude());
                        if (!cityName.equals("Not found")) {
                            getWeatherInfo(cityName);
                            fusedLocationClient.removeLocationUpdates(locationCallback); // İlk konum alındığında dinleyici durdur
                            break;
                        }
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }


    private String getCityName(double latitude, double longitude) {
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address address = addresses.get(0);

                // Öncelikle ilçeyi al
                cityName = address.getSubAdminArea();


                if (cityName == null) {
                    // İlçe adı boşsa şehri al
                    cityName = address.getLocality(); // Şehir adı
                    if (cityName == null || cityName.isEmpty()) {
                        cityName = address.getAdminArea(); // Yönetim bölgesi adı (Eyalet, İlçe vb.)
                    }
                }

                // Türkçe karakterleri İngilizce karakterlere çevir
                if (cityName != null) {
                    cityName = convertTurkishCharacters(cityName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity", "Geocoder hata: " + e.getMessage());
        }
        Log.d("MainActivity", "Bulunan şehir: " + cityName);
        return cityName;
    }

    private String convertTurkishCharacters(String input) {
        String[][] trChars = {
                {"Ç", "C"}, {"Ş", "S"}, {"Ğ", "G"}, {"Ü", "U"}, {"İ", "I"}, {"Ö", "O"},
                {"ç", "c"}, {"ş", "s"}, {"ğ", "g"}, {"ü", "u"}, {"ı", "i"}, {"ö", "o"}
        };

        for (String[] trChar : trChars) {
            input = input.replace(trChar[0], trChar[1]);
        }

        return input;
    }




    private void getWeatherInfo(String cityName) {
        binding.loading.setVisibility(View.VISIBLE);
        binding.idRLHome.setVisibility(View.GONE);

        String apiKey = "7aa4e01db8c24371b64195003242107";

        // Şehir adını API çağrısı için İngilizce karakterlere çevir
        String cityNameForApi = convertTurkishCharacters(cityName);

        // Dinamik URL oluştur
        String url = "https://api.weatherapi.com/v1/forecast.json?key=" + apiKey + "&q=" + cityNameForApi + "&days=1&aqi=no&alerts=no";

        Observable<WeatherResponse> observable = weatherApiService.getWeatherInfo(url);

        compositeDisposable.add(
                observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                weatherResponse -> {
                                    binding.loading.setVisibility(View.GONE);
                                    binding.idRLHome.setVisibility(View.VISIBLE);
                                    weatherHourList.clear();

                                    String temperature = weatherResponse.current.tempC;
                                    binding.idTVTemperature.setText(temperature + "°C");

                                    // Şehir adını tekrar Türkçe karakterlere çevir
                                    String cityNameInTurkish = revertToTurkishCharacters(cityNameForApi);
                                    binding.idTVCityName.setText(cityNameInTurkish);

                                    String condition = weatherResponse.current.condition.text;
                                    String conditionIcon = weatherResponse.current.condition.icon;
                                    Picasso.get().load("https:".concat(conditionIcon)).into(binding.idIVIcon);
                                    binding.idTVCondition.setText(condition);

                                    int isDay = weatherResponse.current.isDay;
                                    if (isDay == 1) {
                                        binding.idIVBack.setImageResource(R.drawable.gunduz);
                                    } else {
                                        binding.idIVBack.setImageResource(R.drawable.gece);
                                    }

                                    for (WeatherResponse.Hour hour : weatherResponse.forecast.forecastDayList.get(0).hourList) {
                                        weatherHourList.add(hour);
                                    }
                                    weatherRvAdapter.notifyDataSetChanged();
                                },
                                throwable -> {
                                    Toast.makeText(MainActivity.this, "Failed to get weather data", Toast.LENGTH_SHORT).show();
                                    binding.loading.setVisibility(View.GONE);
                                    binding.idRLHome.setVisibility(View.VISIBLE);
                                }
                        )
        );
    }

    // İngilizce karakterleri tekrar Türkçe karakterlere çeviren metod
    private String revertToTurkishCharacters(String input) {
        String[][] enToTrChars = {{"C", "Ç"}, {"S", "Ş"}, {"G", "Ğ"}, {"U", "Ü"}, {"I", "İ"}, {"O", "Ö"},
                {"c", "ç"}, {"s", "ş"}, {"g", "ğ"}, {"u", "ü"}, {"i", "ı"}, {"o", "ö"}};

        for (String[] enToTrChar : enToTrChars) {
            input = input.replace(enToTrChar[0], enToTrChar[1]);
        }
        return input;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                getLocation();
            } else {
                //Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}


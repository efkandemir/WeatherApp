    package com.efkan.weatherapp.Adapter;

    import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.efkan.weatherapp.Model.WeatherResponse;
    import com.efkan.weatherapp.R;
    import com.squareup.picasso.Picasso;

    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;

    public class WeatherRvAdapter extends RecyclerView.Adapter<WeatherRvAdapter.WeatherHolder> {
        private Context context;
        private ArrayList<WeatherResponse.Hour> weatherHourList;

        public WeatherRvAdapter(Context context, ArrayList<WeatherResponse.Hour> weatherHourList) {
            this.context = context;
            this.weatherHourList = weatherHourList;
        }

        @NonNull
        @Override
        public WeatherHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false);
            return new WeatherHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WeatherHolder holder, int position) {
            WeatherResponse.Hour hour = weatherHourList.get(position);
            holder.temperatureTV.setText(hour.tempC + "°C");
            Picasso.get().load("https:".concat(hour.condition.icon)).into(holder.conditionIV);
            holder.windTV.setText(hour.windKph + " Km/h");

            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
            try {
                Date t = input.parse(hour.time);
                holder.timeTV.setText(output.format(t));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return weatherHourList.size();
        }

        public class WeatherHolder extends RecyclerView.ViewHolder {
            private TextView windTV, temperatureTV, timeTV;
            private ImageView conditionIV;

            public WeatherHolder(@NonNull View itemView) {
                super(itemView);
                windTV = itemView.findViewById(R.id.idTVWindSpeed);
                temperatureTV = itemView.findViewById(R.id.idTVTemperature);
                timeTV = itemView.findViewById(R.id.idTVTime);
                conditionIV = itemView.findViewById(R.id.idIVCondition);
            }
        }
    }

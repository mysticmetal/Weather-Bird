package com.developer.apnatural.sunshine;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by APnaturals on 5/9/2016.
 */
public class WeatherAdapter extends ArrayAdapter<WeatherDetail> {
    public WeatherAdapter(Activity context, List<WeatherDetail> objects) {
        super(context, 0, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            WeatherDetail det=getItem(position);

       String url="http://openweathermap.org/img/w/"+det.icon+".png";
       if(convertView==null) {
           convertView = LayoutInflater.from(getContext()).inflate(R.layout.weather_row_layout, parent,false);
       }
        ImageView wImage=(ImageView) convertView.findViewById(R.id.mImg);
        Picasso.with(getContext()).load(url.trim()).into(wImage);
        TextView wday=(TextView) convertView.findViewById(R.id.Date);
        if(position==0)
        wday.setText("Today");
        else  if(position==1)
            wday.setText("Tomorrow");
        else
        wday.setText(det.day);
        TextView desc=(TextView) convertView.findViewById(R.id.desc);
        desc.setText(det.desc);
        TextView max=(TextView) convertView.findViewById(R.id.maxTemp);
        TextView min=(TextView) convertView.findViewById(R.id.minTemp);
       max.setText(det.maxTemp);
        min.setText(det.minTemp);
        return convertView;
    }
}

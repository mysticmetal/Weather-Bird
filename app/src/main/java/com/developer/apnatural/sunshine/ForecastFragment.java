package com.developer.apnatural.sunshine;

/**
 * Created by APnaturals on 2/28/2016.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    public String locpin;
    public WeatherDetail[] wdet;
    WeatherAdapter mForecastAdapter;
    public ListView listView;
    public String metrics;
    public View rootView;
    public String locDisp;
    public String ccode;
    public String language;
    public ForecastFragment() {
    }



    public void onCreate(Bundle savedinstancestate) {
        super.onCreate(savedinstancestate);
       if(savedinstancestate!=null)
       {
         language=savedinstancestate.getString("Language");
           metrics=savedinstancestate.getString("Metric");
           locpin=savedinstancestate.getString("LocationToSearch");
       }
        else
       {
           locpin="Pauri";
                   metrics="metric";
           language="en";
       }
        setHasOptionsMenu(true);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("Language",language);
        outState.putString("Metric",metrics);
        outState.putString("LocationToSearch",locpin);
        super.onSaveInstanceState(outState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchWeatherTask datin = new FetchWeatherTask();
            datin.execute();
            return true;
        }

        if (id == R.id.action_cityID) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_location, null);
            builder.setTitle("Location").setView(view).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Dialog foo = (Dialog) dialog;
                    EditText edt = (EditText) foo.findViewById(R.id.location);
                    String temp=edt.getText().toString();
                    if(temp!=null || temp.length()>0) {
                        locpin = edt.getText().toString();
                        FetchWeatherTask fetchit = new FetchWeatherTask();
                        fetchit.execute();
                    }
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        }

        if(id==R.id.action_langsupp)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

final String[] languager={"English","Russian","Italian","French","German","Spanish","Chinese Traditional"};
           builder.setTitle("Language").setItems(R.array.languages,new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   String c=languager[which];
                   if(c=="English")
                       language="en";
                   if(c=="Russian")
                       language="ru";
                   if(c=="Italian")
                       language="it";
                   if(c=="French")
                       language="fr";
                   if(c=="German")
                       language="de";
                   if(c=="Spanish")
                       language="es";
                   if(c=="Chinese Traditional")
                       language="zh_tw";

                   FetchWeatherTask datain=new FetchWeatherTask();
                   datain.execute();
               }
           }).create().show();

        }
        if(id== R.id.action_units)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

            LayoutInflater inflater=getActivity().getLayoutInflater();
            View view=inflater.inflate(R.layout.dialog_unit,null);

            builder.setTitle("Select Unit").setView(view).setPositiveButton("Done!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   FetchWeatherTask datain=new FetchWeatherTask();
                    datain.execute();
                }
            });
            builder.create().show();
            RadioGroup rg=(RadioGroup) view.findViewById(R.id.radiogrp);
            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.celsius)
                    {
                        metrics="metric";
                    }
                    else metrics="imperial";
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         rootView = inflater.inflate(R.layout.fragment_main, container, false);
     
        FetchWeatherTask getw=new FetchWeatherTask();
        getw.execute();
        listView=(ListView) rootView.findViewById(R.id.listview_forecast);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<Void, Void, Void>
    {

        private final String LOG_TAG=FetchWeatherTask.class.getSimpleName();

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private void getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "description";

            final String OWM_ICON="icon";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
             wdet=new WeatherDetail[weatherArray.length()];
            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.
                JSONObject cdet=forecastJson.getJSONObject("city");
            locDisp=cdet.getString("name");
            ccode=cdet.getString("country");
            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String high;
                String low;
                wdet[i]=new WeatherDetail();
                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);
                wdet[i].setDay(day);
                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                String icon=weatherObject.getString(OWM_ICON);
                wdet[i].setDesc(description);
                wdet[i].setIcon(icon);
                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = Math.round(temperatureObject.getDouble(OWM_MAX))+"";
                low = Math.round(temperatureObject.getDouble(OWM_MIN))+"";
                wdet[i].setMaxTemp(high);
                wdet[i].setMinTemp(low);
            }

        }


        final String your_key_here="a1ce2deccc132a31816394a3b965e031";

        protected Void doInBackground(Void... params)
     {



         HttpURLConnection urlConnection=null;
         BufferedReader reader=null;
         String format="json";
         int numDays=7;

         String forecastJsonStr=null;

         try{
             final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily?";
             final String QUERY_PARAM="q";
             final String FORMAT_PARAM="mode";
             final String UNITS_PARAM="units";
             final String DAYS_PARAM="cnt";

             Uri builtUri=Uri.parse(FORECAST_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM,locpin).appendQueryParameter("lang",language).appendQueryParameter(FORMAT_PARAM,format).appendQueryParameter(UNITS_PARAM,metrics).appendQueryParameter(DAYS_PARAM,numDays+"").appendQueryParameter("APPID",
                     your_key_here).build();

             URL url=new URL(builtUri.toString());
              urlConnection=(HttpURLConnection)url.openConnection();
             urlConnection.setRequestMethod("GET");
             urlConnection.connect();

             InputStream inputStream=urlConnection.getInputStream();
             StringBuffer buffer=new StringBuffer();
             if(inputStream==null)
             {
                 return null;
             }
             reader=new BufferedReader(new InputStreamReader(inputStream));
             String line=null;
             while((line=reader.readLine())!=null)
             {
                 buffer.append(line+"\n");
             }
             if(buffer.length()==0)return null;
             forecastJsonStr=buffer.toString();
         } catch (MalformedURLException e) {
             Toast.makeText(getActivity(),"URL issue have occured",Toast.LENGTH_SHORT).show();
             return null;
         } catch (IOException e) {
             Toast.makeText(getActivity(),"Stream Check!!",Toast.LENGTH_SHORT).show();
             return null;
         }
         finally {
             if(urlConnection!=null) urlConnection.disconnect();
             if(reader!=null)
             {
                 try{
                     reader.close();
                 }
                 catch (final IOException e)
                 {
                     Toast.makeText(getActivity(),"Someone left the stream opened",Toast.LENGTH_SHORT).show();
                 }
             }
         }
         String res[]=null;
         try {
             if(forecastJsonStr!=null || forecastJsonStr.length()>0)
          getWeatherDataFromJson(forecastJsonStr,numDays);
             else
             {
                 Toast.makeText(getActivity(),"Enter valid Location",Toast.LENGTH_SHORT).show();
             }

         } catch (JSONException e) {
         Toast.makeText(getActivity(),"Try entering a valid location",Toast.LENGTH_SHORT).show();

         }
         return null;
     }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            TextView tv=(TextView) rootView.findViewById(R.id.Current_Location);
            tv.setText("Weather of "+locDisp+","+ccode);
            List<WeatherDetail> weekForecast=new ArrayList<>(Arrays.asList(wdet));
            if(weekForecast==null || weekForecast.size()==0) {
                Toast.makeText(getActivity(), "Location is not valid!", Toast.LENGTH_SHORT).show();
            }
            else {
                mForecastAdapter = new WeatherAdapter(getActivity(), weekForecast);
                mForecastAdapter.notifyDataSetChanged();
                listView.setAdapter(mForecastAdapter);
            }

        }


    }


}

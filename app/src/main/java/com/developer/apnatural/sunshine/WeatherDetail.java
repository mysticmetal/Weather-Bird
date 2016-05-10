package com.developer.apnatural.sunshine;

/**
 * Created by APnaturals on 5/9/2016.
 */
public class WeatherDetail {

public String maxTemp;
public String minTemp;
public String Loc;
public String pressure;
public String humidity;
public String day;

    public String desc;
    public String icon;


    public void setDesc(String a)
    {
        desc=a;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setMaxTemp(String a)
    {
    maxTemp=a;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setMinTemp(String a)
    {
        minTemp=a;
    }

    public void setLoc(String a)
    {
        Loc=a;
    }
    public void setPressure(String a)
    {
     pressure=a;
    }

    public void setHumidity(String a)
    {
        humidity=a;
    }

}

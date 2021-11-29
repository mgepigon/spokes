package com.example.spokes;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Vector;

//Trip Class --> should be the one put into extra
public class Trip implements Parcelable {
    private double Distance;
    private double avgSpeed;
    private List<Location> Route;
    private String Time;

    public Trip(double distance, double speed, String time, List<Location> route){
        this.Distance = distance;
        this.avgSpeed = speed;
        this.Time = time;
        this.Route = new Vector<Location>(route);
    }

    protected Trip(Parcel in){
        Distance = in.readDouble();
        avgSpeed = in.readDouble();
        Time = in.readString();
        Route = new Vector<Location>();
        in.readList(Route, Location.class.getClassLoader());
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(Distance);
        dest.writeDouble(avgSpeed);
        dest.writeString(Time);
        dest.writeList(Route);
    }

    public double getDistance(){
        return this.Distance;
    }
    public double getAvgSpeed(){
        return this.avgSpeed;
    }
    public double getRouteSize(){
        return this.Route.size();
    }
    public String getTime(){
        return this.Time;
    }

    public List<Location> getRoute() {
        return this.Route;
    }
}

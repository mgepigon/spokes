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

    public Trip(double distance, List<Location> route, double speed){
        this.Distance = distance;
        this.Route = new Vector<Location>(route);
        this.avgSpeed = speed;
    }

    protected Trip(Parcel in){
        Distance = in.readDouble();
        avgSpeed = in.readDouble();
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

}

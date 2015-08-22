package com.example.compass;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

public class LocationUtil {
	private LocationManager locationManager;

	private String provider;
	
	public void getlocation(Context context, LocationListener listener){
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        
        //判断是否存在可用服务
        List<String> providers = locationManager.getProviders(true);
        if(providers.contains(LocationManager.GPS_PROVIDER)){
        	provider = LocationManager.GPS_PROVIDER;
        }else if(providers.contains(LocationManager.NETWORK_PROVIDER)){
        	provider = LocationManager.NETWORK_PROVIDER;
        }else{
        	Toast.makeText(context, "location service is not used", Toast.LENGTH_LONG).show();
        	return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if(location != null){
        	
        }
        
        locationManager.requestLocationUpdates(provider, 5000, 1, listener);
	}
	
}

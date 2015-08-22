package com.stream.compass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private SensorManager sensorManager;
	private SharedPreferences pres;
	private Editor editor;
	private ImageView compassImg;
	private ImageView arrowImg;
	private float lastRotateDegree;
	
	
	//位置相关
	private LocationManager locationManager;
	private String provider;
	private TextView latitude;
	private TextView langitude;
	private TextView location;
	private Location gpsLocation;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获得数据
		pres = PreferenceManager.getDefaultSharedPreferences(this);
		editor = pres.edit();
		
		//获得位置控件
		latitude = (TextView)findViewById(R.id.latitude);
		langitude = (TextView)findViewById(R.id.langitude);
		location = (TextView)findViewById(R.id.location);
	      //位置逻辑
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        compassImg = (ImageView)findViewById(R.id.compass_img);
        arrowImg = (ImageView)findViewById(R.id.arrow_img);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor aSensor = (Sensor)sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mSensor = (Sensor)sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        sensorManager.registerListener(listener, aSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, mSensor, SensorManager.SENSOR_DELAY_GAME);
        

        
       //判断是否存在可用服务
        List<String> providers = locationManager.getProviders(true);
        if(providers.contains(LocationManager.GPS_PROVIDER)){
        	provider = LocationManager.GPS_PROVIDER;
        }else if(providers.contains(LocationManager.NETWORK_PROVIDER)){
        	provider = LocationManager.NETWORK_PROVIDER;
        }else{
        	Toast.makeText(this, "location service is not used", Toast.LENGTH_SHORT).show();
        	return;
        }
        gpsLocation = locationManager.getLastKnownLocation(provider);
        if(gpsLocation!=null){
        	showLocation(gpsLocation);
        }
        locationManager.requestLocationUpdates(provider, 5000, 1, ll);
        
        
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(sensorManager!=null){
			sensorManager.unregisterListener(listener);
		}
		if(locationManager!=null){
			locationManager.removeUpdates(ll);
		}
	}

	private SensorEventListener listener = new SensorEventListener() {

		float[] aValues = new float[3];
		float[] mValues = new float[3];

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				aValues = event.values.clone();
			} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mValues = event.values.clone();
			}
			float[] R = new float[9];
			float[] values = new float[3];
			SensorManager.getRotationMatrix(R, null, aValues, mValues);
			SensorManager.getOrientation(R, values);
			int set = pres.getInt("set", 0);
			float rotateDegree = -(float) Math.toDegrees(values[0]);
				
				if(Math.abs(rotateDegree-lastRotateDegree)>1){
					RotateAnimation animation = new RotateAnimation(
							lastRotateDegree, rotateDegree, Animation.RELATIVE_TO_SELF
							, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					animation.setFillAfter(true);
					compassImg.startAnimation(animation);
					if(set==1){
						arrowImg.setAnimation(null);
					}else{
						RotateAnimation animation1 = new RotateAnimation(
								lastRotateDegree, rotateDegree, Animation.RELATIVE_TO_SELF
								, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
						animation1.setFillAfter(true);
						arrowImg.startAnimation(animation1);
					}
					
					lastRotateDegree = rotateDegree;
				}
			
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		//获得存储数据
		int set = pres.getInt("set", 0);
		//创建对话框
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请选择模式");
		builder.setSingleChoiceItems(new String[]{"指针动","仅罗盘动"}, set,
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
						case 0:
							editor.putInt("set", 0);
							editor.commit();
							dialog.dismiss();
							break;
						case 1:
							editor.putInt("set", 1);
							editor.commit();
							dialog.dismiss();
							break;
						}
					}
				});
		builder.create().show();
		return true;
	}
	
	/**
	 * 获得位置信息
	 */
	
	
	
	/**
	 * 显示当前的位置信息
	 */
	private void showLocation(Location location){
		latitude.setText(location.getLatitude()+"");
		langitude.setText(location.getLongitude()+"");
		getPostion(location);
	}
	
	/**
	 * 开启一个子线程去网络查找当前位置信息
	 */
    private void getPostion(final Location location){
    	new Thread(new Runnable(){

			@Override
			public void run() {
				StringBuilder url = new StringBuilder();
				url.append("http://api.map.baidu.com/geocoder/v2/?" +
						"ak=GCUtx4VCxRWURvEj4d2AWVHc&location=");
				url.append(location.getLatitude()+",");
				url.append(location.getLongitude()+"&output=json");
				url.append("&mcode=43:2F:A6:9D:14:D0:D8:E3:15:D6:88:3F:A1:D2:12:19;" +
							"com.stream.compass");
				HttpURLConnection connection = null;
				try {
					connection = (HttpURLConnection) new URL(url.toString()).openConnection();
					connection.setRequestMethod("GET");
					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line;
					while((line=reader.readLine())!=null){
						response.append(line);
					}
					JSONObject status = new JSONObject(response.toString());
					JSONObject result = status.getJSONObject("result");
					String add = result.getString("formatted_address");

						if(add!=null){
							Message msg = new Message();
							msg.what = 0;
							msg.obj = add;
							handler.sendMessage(msg);
						}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
    		
    	}).start();
    }
    
    private Handler handler=new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch(msg.what){
    		case 0:
    			String postion = (String)msg.obj;
    			location.setText(postion);
    			break;
    		default:
    			break;
    		}
    	}
    };

    /**
     * 位置监听器
     */
 LocationListener ll = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			showLocation(location);
		}
	};
}

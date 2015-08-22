package com.example.compass;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private SensorManager sensorManager;

	private ImageView compassImg;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        compassImg = (ImageView)findViewById(R.id.compass_img);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor aSensor = (Sensor)sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mSensor = (Sensor)sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        sensorManager.registerListener(listener, aSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, mSensor, SensorManager.SENSOR_DELAY_GAME);
        
      
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(sensorManager!=null){
			sensorManager.unregisterListener(listener);
		}
	}

	private SensorEventListener listener = new SensorEventListener() {

		float[] aValues = new float[3];
		float[] mValues = new float[3];
		private float lastRotateDegree;

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
			
			float rotateDegree = -(float) Math.toDegrees(values[0]);
			if(Math.abs(rotateDegree-lastRotateDegree)>1){
				RotateAnimation animation = new RotateAnimation(
						lastRotateDegree, rotateDegree, Animation.RELATIVE_TO_SELF
						, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setFillAfter(true);
				compassImg.startAnimation(animation);
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

}

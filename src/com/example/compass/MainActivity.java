package com.example.compass;

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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private SensorManager sensorManager;

	private SharedPreferences pres;
	private Editor editor;
	private ImageView compassImg;
	private ImageView arrowImg;
	private float lastRotateDegree;
	
	//位置相关
	private TextView latitude;
	private TextView langitude;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //获得数据
		pres = PreferenceManager.getDefaultSharedPreferences(this);
		editor = pres.edit();
		
        setContentView(R.layout.activity_main);
        compassImg = (ImageView)findViewById(R.id.compass_img);
        arrowImg = (ImageView)findViewById(R.id.arrow_img);
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

}

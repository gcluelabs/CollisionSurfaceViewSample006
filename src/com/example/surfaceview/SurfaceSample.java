package com.example.surfaceview;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceSample extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MySurfaceView mSurfaceView = new MySurfaceView(this);
		setContentView(mSurfaceView);
	}
}

class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable, SensorEventListener {
	
	/**
	 * 画面のグリッド(X軸)
	 */
	private int DX = 20;
	
	/**
	 * 画面のグリッド(Y軸)
	 */
	private int DY = 20;
	
	/**
	 * 描画するCount値
	 */
	private int count = 0;

	/**
	 * SensorManager
	 */
	private SensorManager mSensorManager;
	
	/**
	 * 画像を格納する変数
	 */
	private Bitmap myBitmap; 
	
	/**
	 * 床を格納する変数
	 */
	private Bitmap floorBitmap;
	/**
	 * 自分のx座標
	 */
	private int myX = DX*5;
	
	/**
	 * 自分の　y座標
	 */
	private int myY = DY*5;
	
	/**
	 * 床のx座標
	 */
	private int floorX = DY*5;
	
	/**
	 * 床y座標
	 */
	private int floorY = DY*20;	
	/**
	 * Thread
	 */
	private Thread mThread;
	
	/**
	 * Threadが動いているかどうかの判断
	 */
	private boolean isRunning = false;
	
	/**
	 * 速度
	 */
	private float v;
	
	/**
	 * 重力
	 */
	private float g = 9.8f;
	
	public MySurfaceView(Context context) {
		super(context);
		Log.i("SURFACE", "MySurfaceView()");

		// SensorManager
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

		// Sensorの取得とリスナーへの登録
		List< Sensor > sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
		if (sensors.size() > 0) {
			Sensor sensor = sensors.get(0);
			mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
		}
		
		// Resourceインスタンスの生成 
		Resources res = this.getContext().getResources(); 
		// 画像の読み込み(res/drawable/droid.png)  
		myBitmap = BitmapFactory.decodeResource(res, R.drawable.droid);
		
		// 画像の読み込み(res/drawable/floor.png)
		floorBitmap = BitmapFactory.decodeResource(res, R.drawable.floor);
		
		// Callbackを登録する
		getHolder().addCallback(this);

		// Threadを起動する
		isRunning = true;
		mThread = new Thread(this);
		mThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i("SURFACE", "surfaceChanged()");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("SURFACE", "surfaceDestroyed()");
		mSensorManager.unregisterListener(this);
		
		// Threadを破棄する
		isRunning = false;
		try {
			mThread.join();
		} catch (InterruptedException ex) {
		}
		mThread = null;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("SURFACE", "surfaceCreaded()");
	}

	@Override
	public void run() {
		Log.i("SURFACE", "run()");
		while (isRunning) {
			Log.i("SURFACE", "loop");

			// countに+1する
			count++;
			
			// 速度の分だけ移動する
			myY += v;
			
			// あたり判定
			if (((myY < floorY + floorBitmap.getHeight()) && (myY + myBitmap.getHeight() > floorY))
				&& ((myX + myBitmap.getWidth() > floorX) && (myX < floorX + floorBitmap.getWidth()))) {
				
				// 床にめり込まないようにする
				myY = floorY - myBitmap.getHeight();
				
				// 時間を初期化
				count = 0;
				
				// 跳ね返りの速度
				v = -v * 0.8f;
			} else {
				
				// 速度の計算
				v = v + g * count/3;
				
				// 最高速度の設定
				if (v > 50) {
					v = 50;
				}
			}
			// Canvasを取得する
			Canvas canvas = getHolder().lockCanvas();

			if (canvas != null) {
				
				// 背景を青くする
				canvas.drawColor(Color.BLUE);
				
				// 描画するための線の色を設定
				Paint mainPaint = new Paint();
				mainPaint.setStyle(Paint.Style.FILL);
				mainPaint.setARGB(255, 255, 255, 100);

				// 文字を描画
				canvas.drawText("count:" + count, 20, 20, mainPaint);
				canvas.drawText("y:" + myY, 20, 40, mainPaint);
				canvas.drawText("v:" + v, 20, 60, mainPaint);
				
				// 画像の描画
				canvas.drawBitmap(myBitmap, myX, myY, mainPaint);  
					
				// 床の描画
				canvas.drawBitmap(floorBitmap, floorX, floorY, mainPaint);
				
				// 画面に描画をする
				getHolder().unlockCanvasAndPost(canvas);
			}

			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.i("SURFACE", "onAccuracyChanged()");
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		Log.i("SURFACE", "SensorChanged()");
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
//			Log.i("SURFACE", "yaw:" + sensorEvent.values[0]);
//			Log.i("SURFACE", "picth:" + sensorEvent.values[1]);
//			Log.i("SURFACE", "roll:" + sensorEvent.values[2]);
			
			myX -= sensorEvent.values[2]/10;
			//myY -= sensorEvent.values[1]/10;
		}
	}
}
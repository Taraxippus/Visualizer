package com.taraxippus.visualizer;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.opengl.*;
import android.os.*;
import android.view.*;
import android.view.WindowManager.*;
import com.taraxippus.visualizer.gl.*;
import android.view.ScaleGestureDetector.*;

public class VisualizerService extends Service
{
	private WindowManager windowManager;
	private GLSurfaceView view;

	public void onCreate()
	{
		super.onCreate();
		view = new GLSurfaceView(this);
	
		view.setEGLContextClientVersion(2);
		view.setPreserveEGLContextOnPause(true);
		view.setEGLConfigChooser(new ConfigChooser(this));
		view.setZOrderOnTop(true);
		view.getHolder().setFormat(PixelFormat.RGBA_8888);
		
		view.setRenderer(new GLRenderer(this, view.getHolder()));
		
		windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

		final LayoutParams paramsF = new WindowManager.LayoutParams(
			300,
			300,
			LayoutParams.TYPE_PHONE,
			LayoutParams.FLAG_NOT_FOCUSABLE,
			PixelFormat.TRANSLUCENT);

		paramsF.gravity = Gravity.CENTER;
		windowManager.addView(view, paramsF);

		try
		{

			view.setOnTouchListener(new View.OnTouchListener() 
				{
					private int initialX;
					private int initialY;
					private float initialTouchX;
					private float initialTouchY;

					ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(VisualizerService.this, new ScaleGestureDetector.SimpleOnScaleGestureListener()
					{
							@Override
							public boolean onScale(ScaleGestureDetector detector) 
							{
								paramsF.width *= detector.getScaleFactor();
								paramsF.height *= detector.getScaleFactor();
							
								
								windowManager.updateViewLayout(view, paramsF);
								
								return true;
							}
					});
					
					@Override
					public boolean onTouch(View v, MotionEvent event) 
					{
						scaleGestureDetector.onTouchEvent(event);
						
						switch(event.getAction())
						{
							case MotionEvent.ACTION_DOWN:
								initialX = paramsF.x;
								initialY = paramsF.y;
								initialTouchX = event.getRawX();
								initialTouchY = event.getRawY();
								
								break;

							case MotionEvent.ACTION_UP:
								break;

							case MotionEvent.ACTION_MOVE:
								paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
								paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);

								windowManager.updateViewLayout(view, paramsF);
								break;
						}
						return false;
					}
				});
				
				
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		windowManager.removeView(view);
	}
	

	@Override
	public IBinder onBind(Intent p1)
	{
		return null;
	}
}
	

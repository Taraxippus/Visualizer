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
import android.widget.*;

public class VisualizerService extends Service
{
	private WindowManager windowManager;
	private GLSurfaceView view;
	private FloatingWidgetBorder border;

	public void onCreate()
	{
		super.onCreate();
		
		view = new GLSurfaceView(this);
	
		view.setEGLContextClientVersion(2);
		view.setPreserveEGLContextOnPause(true);
		view.setEGLConfigChooser(new ConfigChooser(this));
		view.getHolder().setFormat(PixelFormat.RGBA_8888);
		
		view.setRenderer(new GLRenderer(this, view.getHolder()));
		
		final int padding = getResources().getDimensionPixelSize(R.dimen.resize_frame_background_padding);
		
		border = new FloatingWidgetBorder(this);
		
		final LayoutParams paramsB = new WindowManager.LayoutParams(
			300 + padding * 2,
			300 + padding * 2,
			LayoutParams.TYPE_SYSTEM_ALERT,
			LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_HARDWARE_ACCELERATED | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
			PixelFormat.TRANSLUCENT);
		
		windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

		final LayoutParams paramsF = new WindowManager.LayoutParams(
			300,
			300,
			LayoutParams.TYPE_SYSTEM_ALERT,
			LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_HARDWARE_ACCELERATED,
			PixelFormat.TRANSLUCENT);
			
		paramsF.gravity = Gravity.CENTER;
		
		windowManager.addView(view, paramsF);
	
		border.setVisibility(View.INVISIBLE);
		
		try
		{
			view.setOnLongClickListener(new View.OnLongClickListener()
				{
					@Override
					public boolean onLongClick(View p1)
					{
						if (border.getVisibility() == View.INVISIBLE)
						{
							border.setVisibility(View.VISIBLE);
							windowManager.addView(border, paramsB);
						}
						return true;
					}
			});
			
			
			border.setOnTouchListener(new View.OnTouchListener() 
				{
					private int initialX, initialY, initialWidth, initialHeight;
					
					private float initialTouchX;
					private float initialTouchY;
					
					@Override
					public boolean onTouch(View v, MotionEvent event) 
					{
						
						switch(event.getAction())
						{
							case MotionEvent.ACTION_DOWN:
								initialX = paramsF.x;
								initialY = paramsF.y;
								initialWidth = paramsF.width;
								initialHeight = paramsF.height;
								initialTouchX = event.getRawX();
								initialTouchY = event.getRawY();
								
								border.setActiveHandle(event.getX(), event.getY());
								
								break;

							case MotionEvent.ACTION_UP:
								border.setActiveHandle(-1);
								break;

							case MotionEvent.ACTION_OUTSIDE:
							case MotionEvent.ACTION_CANCEL:
								border.setVisibility(View.INVISIBLE);
								windowManager.removeView(border);
								break;
								
							case MotionEvent.ACTION_MOVE:
								
								paramsF.x = initialX;
								paramsF.y = initialY;
								paramsF.width = initialWidth;
								paramsF.height = initialHeight;
								
								if (border.activeHandle == -1)
								{
									paramsF.x += (int) (event.getRawX() - initialTouchX);
									paramsF.y += (int) (event.getRawY() - initialTouchY);
									
								}
								else
								{
									if ((border.activeHandle & 0b1) != 0)
									{
										paramsF.x += (int) (event.getRawX() - initialTouchX) / 2F;
										paramsF.width -= (int) (event.getRawX() - initialTouchX);
									}
									if ((border.activeHandle & 0b10) != 0)
									{
										paramsF.x += (int) (event.getRawX() - initialTouchX) / 2F;
										paramsF.width += (int) (event.getRawX() - initialTouchX);
									}
									if ((border.activeHandle & 0b100) != 0)
									{
										paramsF.y += (int) (event.getRawY() - initialTouchY) / 2F;
										paramsF.height -= (int) (event.getRawY() - initialTouchY);
									}
									if ((border.activeHandle & 0b1000) != 0)
									{
										paramsF.y += (int) (event.getRawY() - initialTouchY) / 2F;
										paramsF.height += (int) (event.getRawY() - initialTouchY);
									}
								}
								
								paramsB.x = paramsF.x;
								paramsB.y = paramsF.y;
								paramsB.width = paramsF.width + padding * 2;
								paramsB.height = paramsF.height + padding * 2;
								
								windowManager.updateViewLayout(view, paramsF);
								windowManager.updateViewLayout(border, paramsB);
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
		
		if (view != null)
		{
			windowManager.removeView(view);

			view = null;
			
			if (border.getVisibility() == View.VISIBLE)
				windowManager.removeView(border);
				
			border = null;
		}
	}
	

	@Override
	public IBinder onBind(Intent p1)
	{
		return null;
	}
}
	

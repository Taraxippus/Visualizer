package com.taraxippus.visualizer.gl;

import android.content.*;
import android.opengl.*;
import android.preference.*;
import javax.microedition.khronos.egl.*;

public class ConfigChooser implements GLSurfaceView.EGLConfigChooser
{
	public final Context context;

	public ConfigChooser(Context context)
	{
		this.context = context;
	}

	@Override
	public javax.microedition.khronos.egl.EGLConfig chooseConfig(EGL10 p1, javax.microedition.khronos.egl.EGLDisplay p2)
	{
		int attribs[] = 
		{
			EGL10.EGL_LEVEL, 0,
			EGL10.EGL_RENDERABLE_TYPE, 4,
			EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
			EGL10.EGL_RED_SIZE, 8,
			EGL10.EGL_GREEN_SIZE, 8,
			EGL10.EGL_BLUE_SIZE, 8,
			EGL10.EGL_ALPHA_SIZE, 8,
			EGL10.EGL_DEPTH_SIZE, 16,
			EGL10.EGL_SAMPLE_BUFFERS, 1,
			EGL10.EGL_SAMPLES, PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableMSAA", true) ? 4 : 1, 
			EGL10.EGL_NONE
		};

		javax.microedition.khronos.egl.EGLConfig[] configs = new  javax.microedition.khronos.egl.EGLConfig[1];
		int[] configCounts = new int[1];
		p1.eglChooseConfig(p2, attribs, configs, 1, configCounts);

		if (configCounts[0] == 0) 
		{
			throw new RuntimeException("Couln't set up opengl es");
		} 
		else
		{
			return configs[0];
		}

	}
}

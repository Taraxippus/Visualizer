package com.taraxippus.visualizer.gl;

import android.opengl.*;
import android.view.*;
import com.taraxippus.visualizer.*;

public class OpenGLESWallpaperService extends GLWallpaperService 
{
    @Override
    public Engine onCreateEngine()
	{
        return new OpenGLES2Engine();
    }

    public class OpenGLES2Engine extends GLWallpaperService.GLEngine
	{

        @Override
        public void onCreate(SurfaceHolder surfaceHolder)
		{
            super.onCreate(surfaceHolder);
			

			setEGLContextClientVersion(2);
			setPreserveEGLContextOnPause(true);
			setEGLConfigChooser(new ConfigChooser(OpenGLESWallpaperService.this));

			setRenderer(getNewRenderer(surfaceHolder));
        }

    }

    private GLSurfaceView.Renderer getNewRenderer(SurfaceHolder holder)
	{
		return new GLRenderer(this, holder);
	}
}

package com.taraxippus.visualizer;
import android.content.*;
import android.opengl.*;
import android.service.wallpaper.*;
import android.view.*;
import com.taraxippus.visualizer.gl.*;

public abstract class GLWallpaperService extends WallpaperService
{
	public static SurfaceHolder holder;
	
	public class GLEngine extends Engine
	{
		private WallpaperGLSurfaceView glSurfaceView;
		private boolean rendererHasBeenSet;
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) 
		{
			super.onCreate(surfaceHolder);
			
			holder = surfaceHolder;
			
			glSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) 
		{
			super.onVisibilityChanged(visible);
			
			if (rendererHasBeenSet) 
			{
				if (visible) 
				{
					glSurfaceView.onResume();
				}
				else 
				{
					glSurfaceView.onPause();     
				}
			}
		}
		
		@Override
		public void onDestroy() 
		{
			super.onDestroy();
			glSurfaceView.onDestroy();
		}
		
		protected void setRenderer(GLSurfaceView.Renderer renderer) 
		{
			glSurfaceView.setRenderer(renderer);
			rendererHasBeenSet = true;
		}

		protected void setEGLContextClientVersion(int version)
		{
			glSurfaceView.setEGLContextClientVersion(version);
		}

		protected void setPreserveEGLContextOnPause(boolean preserve)
		{
			glSurfaceView.setPreserveEGLContextOnPause(preserve);
		}

		protected void setEGLConfigChooser(GLSurfaceView.EGLConfigChooser chooser)
		{
			glSurfaceView.setEGLConfigChooser(chooser);
		}
		

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
		{
			((GLRenderer)glSurfaceView.renderer).onOffsetChanged(xOffset, yOffset);
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
		}
		
		public class WallpaperGLSurfaceView extends GLSurfaceView
		{		
			WallpaperGLSurfaceView(Context context)
			{
				super(context);
			}

			@Override
			public SurfaceHolder getHolder()
			{
				return GLEngine.this == null ? holder : GLEngine.this.getSurfaceHolder();
			}

			GLSurfaceView.Renderer renderer;
			
			@Override
			public void setRenderer(GLSurfaceView.Renderer renderer)
			{
				super.setRenderer(renderer);
				
				this.renderer = renderer;
			}
			
			public void onDestroy()
			{
				if (renderer instanceof GLRenderer)
				{
					((GLRenderer) renderer).release();
					super.onDetachedFromWindow();
				}
			}
			
			public void update()
			{
				if (renderer instanceof GLRenderer)
				{
					((GLRenderer) renderer).update();
				}
			}
		}
	}
}


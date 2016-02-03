package com.taraxippus.visualizer.gl;
import android.graphics.*;
import android.net.*;
import android.opengl.*;

public class Texture
{
	final int[] texture = new int[1];
	
	public Texture()
	{
		
	}
	
	public void init(Bitmap bitmap)
	{
		if (this.initialized())
			delete();
		
		GLES20.glGenTextures(1, texture, 0);
		
		this.bind();
		
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		
		bitmap.recycle();
		
		if (!initialized())
		{
			delete();
			throw new RuntimeException("Error creating texture");
		}
	}
	
	public boolean initialized()
	{
		return texture[0] != 0;
	}
	
	public void bind()
	{
		if (!initialized())
		{
			System.err.println("Tried to bind an uninitializef texture");
		}
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
	}
	
	public void delete()
	{
		if (!initialized())
		{
			System.err.println("Tried to delete an uninitializef texture");
		}
		
		GLES20.glDeleteTextures(1, texture, 0);
		
		texture[0] = 0;
	}
}

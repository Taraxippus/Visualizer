package com.taraxippus.visualizer.gl;

import android.opengl.*;
import java.nio.*;

public class Shape
{
	final int[] vbo = new int[2];
	
	int indexCount;
	int type;
	
	boolean hasIndices = true;
	
	public Shape()
	{
		
	}
	
	public void init(int type, FloatBuffer vertices, ShortBuffer indices)
	{
		init(type, vertices, indices, 0);
	}
	
	public void init(int type, FloatBuffer vertices, int indexCount)
	{
		init(type, vertices, null, indexCount);
	}
	
	private void init(int type, FloatBuffer vertices, ShortBuffer indices, int indexCount)
	{
		if (initialized())
			delete();
		
		this.type = type;
		this.hasIndices = indices != null;
		
		if (hasIndices)
		{
			this.indexCount = indices.capacity();

			GLES20.glGenBuffers(2, vbo, 0);

			if (initialized()) 
			{
				vertices.position(0);
				indices.position(0);

				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
				GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.capacity() * 4, vertices, GLES20.GL_STATIC_DRAW);

				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo[1]);
				GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * 2, indices, GLES20.GL_STATIC_DRAW);

				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

				vertices.limit(0);
				indices.limit(0);
			}
			else 
			{
				throw new RuntimeException("Error creating VBO");
			}
		}
		else
		{
			this.indexCount = indexCount;

			GLES20.glGenBuffers(1, vbo, 0);

			if (initialized()) 
			{
				vertices.position(0);

				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
				GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.capacity() * 4, vertices, GLES20.GL_STATIC_DRAW);
				
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			
				vertices.limit(0);
			}
			else 
			{
				throw new RuntimeException("Error creating VBO");
			}
		}
	}
	
	public boolean initialized()
	{
		return vbo[0] != 0 && (!hasIndices || vbo[1] != 0);
	}
	
	int stride;
	int offset;
	
	public void draw(int... attributes)
	{
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
		
		stride = offset = 0;
		
		for (int i = 0; i < attributes.length; ++i)
		{
			stride += attributes[i] * 4;
		}
		
		for (int i = 0; i < attributes.length; ++i)
		{
			GLES20.glVertexAttribPointer(i, attributes[i], GLES20.GL_FLOAT, false, stride, offset);
			GLES20.glEnableVertexAttribArray(i);
			
			offset += attributes[i] * 4;
		}

		if (hasIndices)
		{
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo[1]);
			GLES20.glDrawElements(type, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		else
		{
			GLES20.glDrawArrays(type, 0, indexCount);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		}
	}
	
	public void delete()
	{
		if (!initialized())
		{
			System.err.println("Tried to delete uninitialized vbo");
			return;
		}
		
		GLES20.glDeleteBuffers(hasIndices ? 2 : 1, vbo, 0);
		vbo[0] = vbo[1] = 0;
	}
}

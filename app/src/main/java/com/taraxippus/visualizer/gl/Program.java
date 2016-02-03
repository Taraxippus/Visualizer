package com.taraxippus.visualizer.gl;

import android.opengl.*;

public class Program
{
	int program;
	
	public Program()
	{
		
	}
	
	public void init(String vertexShader, String fragmentShader, String... attributes)
	{
		if (initialized())
			delete();
		
		int vertex = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

		if (vertex != 0) 
		{
			GLES20.glShaderSource(vertex, vertexShader);
			GLES20.glCompileShader(vertex);

			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertex, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			if (compileStatus[0] == 0) 
			{				
				System.err.println(GLES20.glGetShaderInfoLog(vertex));
				GLES20.glDeleteShader(vertex);
				vertex = 0;
			}
		}

		if (vertex == 0)
		{
			throw new RuntimeException("Error creating vertex shader.");
		}

		int fragment = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

		if (fragment != 0) 
		{
			GLES20.glShaderSource(fragment, fragmentShader);
			GLES20.glCompileShader(fragment);

			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragment, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			if (compileStatus[0] == 0) 
			{				
				System.err.println(GLES20.glGetShaderInfoLog(fragment));
				GLES20.glDeleteShader(fragment);
				fragment = 0;
			}
		}

		if (fragment == 0)
		{
			throw new RuntimeException("Error creating fragment shader.");
		}

		program = GLES20.glCreateProgram();

		if (program != 0) 
		{
			GLES20.glAttachShader(program, vertex);			
			GLES20.glAttachShader(program, fragment);
			
			for (int i = 0; i < attributes.length; ++i)
			{
				GLES20.glBindAttribLocation(program, i, attributes[i]);
			}
			
			GLES20.glLinkProgram(program);

			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

			if (linkStatus[0] == 0) 
			{				
				System.err.println(GLES20.glGetProgramInfoLog(program));
				GLES20.glDeleteProgram(program);
				program = 0;
			}
			
			GLES20.glDeleteShader(vertex);
			GLES20.glDeleteShader(fragment);
		}

		if (program == 0)
		{
			throw new RuntimeException("Error creating program.");
		}
	}
	
	public boolean initialized()
	{
		return program != 0;
	}
	
	public void use()
	{
		if (initialized())
		{
			GLES20.glUseProgram(program);
		}
		else
		{
			System.err.println("Tried to use an uninitialized Program");
		}
	}
	
	public void delete()
	{
		if (!initialized())
		{
			System.err.println("Tried to delete an uninitialized Program");
			return;
		}
		
		GLES20.glDeleteProgram(program);
		
		program = 0;
	}
}

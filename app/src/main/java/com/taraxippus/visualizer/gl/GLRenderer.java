package com.taraxippus.visualizer.gl;


import android.content.*;
import android.graphics.*;
import android.media.audiofx.*;
import android.opengl.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.view.Display.*;
import com.taraxippus.visualizer.*;
import java.nio.*;
import javax.microedition.khronos.opengles.*;

import android.opengl.Matrix;

public class GLRenderer implements GLSurfaceView.Renderer, Visualizer.OnDataCaptureListener, SharedPreferences.OnSharedPreferenceChangeListener
{
	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String p2)
	{
		update();
	}

	long lastTime;

	@Override
	public void onWaveFormDataCapture(Visualizer p1, byte[] p2, int p3)
	{
		System.arraycopy(waveform, p2.length, waveform, 0, waveform.length - p2.length);
		System.arraycopy(p2, 0, waveform, waveform.length - p2.length, p2.length);

		lastTime = System.currentTimeMillis();
	}

	@Override
	public void onFftDataCapture(Visualizer p1, byte[] p2, int p3)
	{
		System.arraycopy(p2, 0, fft, 0, p2.length);
	}

	public final int COUNT = 64;
	public final int LINE_COUNT = 16;
	public final float BAR_WIDTH = 0.25F;
	public final float BAR_SPACE = 0.05F;

	private final String vertexShader_bars =
	"#version 100\n" +
	"uniform mat4 u_MVP;" +
	"uniform float u_Height[" + COUNT + "];" +
	"attribute vec4 a_Position;" +
	"varying vec4 v_Position;" +
	"void main() {" +
	"  v_Position = vec4(a_Position.x, a_Position.y * (u_Height[int(a_Position.w)] + 0.1), a_Position.z * (u_Height[int(a_Position.w)] + 0.1), 1.0);" +
	"  gl_Position = u_MVP * v_Position;" +
	"}";

	private final String vertexShader_bars_circle =
	"#version 100\n" +
	"uniform mat4 u_MVP;" +
	"uniform float u_Height[" + COUNT + "];" +
	"attribute vec4 a_Position;" +
	"varying vec4 v_Position;" +
	"void main() {" +
	"  v_Position = vec4(a_Position.x, a_Position.y * (u_Height[int(a_Position.w)] + 0.1), a_Position.z, 1.0);" +
	"  gl_Position = u_MVP * v_Position;" +
	"}";

	private final String fragmentShader_bars =
	"#version 100\n" +
	"precision mediump float;" +
	"uniform vec4 u_Color;" +
	"varying vec4 v_Position;" +
	"void main() {" +
	"  gl_FragColor = vec4(u_Color.rgb, v_Position.y < 0.0 ? clamp(v_Position.y * 0.25 + 0.75, 0.0, 1.0) * 0.9 : 1.0);" +
	"}";

	private final String vertexShader_bars_circle_rainbow =
	"#version 100\n" +
	"uniform mat4 u_MVP;" +
	"uniform float u_Height[" + COUNT + "];" +
	"attribute vec4 a_Position;" +
	"varying vec4 v_Position;" +
	"varying vec3 v_Color;" +

	"vec3 hsv2rgb(vec3 c)" +
	"{" +
	"  vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);" +
	"  vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);" +
	"  return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);" +
	"}" +

	"void main() {" +
	"  v_Position = vec4(a_Position.x, a_Position.y * (u_Height[int(a_Position.w)] + 0.1), a_Position.z, 1.0);" +
	"  v_Color = hsv2rgb(vec3(a_Position.w / " + COUNT + ".0, 1.0, 1.0));" +
	"  gl_Position = u_MVP * v_Position;" +
	"}";

	private final String vertexShader_bars_rainbow =
	"#version 100\n" +
	"uniform mat4 u_MVP;" +
	"uniform float u_Height[" + COUNT + "];" +
	"attribute vec4 a_Position;" +
	"varying vec4 v_Position;" +
	"varying vec3 v_Color;" +

	"vec3 hsv2rgb(vec3 c)" +
	"{" +
	"  vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);" +
	"  vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);" +
	"  return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);" +
	"}" +

	"void main() {" +
	"  v_Position = vec4(a_Position.x, a_Position.y * (u_Height[int(a_Position.w)] + 0.1), a_Position.z * (u_Height[int(a_Position.w)] + 0.1), 1.0);" +
	"  v_Color = hsv2rgb(vec3(a_Position.w / " + COUNT + ".0, 1.0, 1.0));" +
	"  gl_Position = u_MVP * v_Position;" +
	"}";

	private final String fragmentShader_bars_rainbow =
	"#version 100\n" +
	"precision mediump float;" +
	"uniform vec4 u_Color;" +
	"varying vec4 v_Position;" +
	"varying vec3 v_Color;" +
	"void main() {" +
	"  gl_FragColor = vec4(v_Color.rgb, v_Position.y < 0.0 ? clamp(v_Position.y * 0.25 + 0.75, 0.0, 1.0) * 0.9 : 1.0);" +
	"}";

	private final String vertexShader_line =
	"#version 100\n" +
	"uniform mat4 u_MVP;" +
	"attribute vec4 a_Position;" +
	"varying vec4 v_Position;" +
	"void main() {" +
	"  v_Position = a_Position;" +
	"  gl_Position = u_MVP * vec4(v_Position.xyz, 1);" +
	"}";

	private final String fragmentShader_line =
	"#version 100\n" +
	"precision mediump float;" +
	"uniform vec4 u_Color;" +
	"varying vec4 v_Position;" +
	"void main() {" +
	"  gl_FragColor = vec4(u_Color.rgb, 1.0);" +
	"}";

	private final String vertexShader_line_rainbow =
	"#version 100\n" +
	"uniform mat4 u_MVP;" +
	"attribute vec4 a_Position;" +
	"varying vec3 v_Color;" +

	"vec3 hsv2rgb(vec3 c)" +
	"{" +
	"  vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);" +
	"  vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);" +
	"  return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);" +
	"}" +

	"void main() {" +
	"  v_Color = hsv2rgb(vec3(a_Position.w, 1.0, 1.0));" +
	"  gl_Position = u_MVP * vec4(a_Position.xyz, 1);" +
	"}";

	private final String fragmentShader_line_rainbow =
	"#version 100\n" +
	"precision mediump float;" +
	"uniform vec4 u_Color;" +
	"varying vec3 v_Color;" +
	"void main() {" +
	"  gl_FragColor = vec4(v_Color.rgb, 1);" +
	"}";

	private final String vertexShader_background =
	"#version 100\n" +
	"uniform mat4 u_MVP;" +
	"attribute vec4 a_Position;" +
	"attribute vec2 a_UV;" +
	"varying vec2 v_UV;" +
	"void main() {" +
	"  v_UV = a_UV;" +
	"  gl_Position = u_MVP * a_Position;" +
	"}";

	private final String fragmentShader_background =
	"#version 100\n" +
	"precision mediump float;" +
	"uniform sampler2D u_Texture;" +
	"varying vec2 v_UV;" +
	"void main() {" +
	"  gl_FragColor = texture2D(u_Texture, v_UV);" +
	"}";

	final float[] height_bars = new float[COUNT];
	final byte[] waveform = new byte[COUNT * 2 * (LINE_COUNT + 1)];
	final byte[] fft = new byte[COUNT * 2];

	final float[] projection_bars = new float[16];
	final float[] projection_background = new float[16];
	final float[] view_bars = new float[16];
	final float[] view_background = new float[16];
	final float[] model = new float[16];

	final float[] mvp = new float[16];

	final Shape bars = new Shape();
	final Program program_bars = new Program();

	final Shape background = new Shape();
	final Program program_background = new Program();

	final Texture texture = new Texture();

	int mvpHandle_bars;
	int heightHandle_bars;

	int mvpHandle_background;

	Visualizer visualizer;
	Context context;
	SurfaceHolder holder;

	Mode mode;
	boolean wallpaper;

	public GLRenderer(Context context, SurfaceHolder holder)
	{
		super();	

		this.context = context;
		this.holder = holder;

		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);

		this.visualizer = new Visualizer(0);
		this.visualizer.setEnabled(false);
		this.visualizer.setCaptureSize(COUNT * 2);
		this.visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
		this.visualizer.setDataCaptureListener(this, 10000, true, true);
		this.visualizer.setEnabled(true);
	}

	boolean wallpaperX = false;

	public static final float tilt = 0.25F;

	@Override
	public void onSurfaceCreated(GL10 p1, javax.microedition.khronos.egl.EGLConfig p2)
	{
		String s = PreferenceManager.getDefaultSharedPreferences(context).getString("mode", "bars");
		this.mode = s.equals("line") ? Mode.LINE : s.equals("bars_circle") ? Mode.BARS_CIRCLE : Mode.BARS;

		if (mode != Mode.LINE)
		{
			FloatBuffer bars_vertices = FloatBuffer.allocate(COUNT * 4 * (mode == Mode.BARS ? 8 : 4));

			if (mode == Mode.BARS_CIRCLE)
			{
				float radius = COUNT * (BAR_WIDTH + BAR_SPACE) / (float)Math.PI / 2F;
				float angle, angle2;

				for (int i = 0; i < COUNT; ++i)
				{
					angle = (float)i / COUNT * (float)Math.PI * 2;
					angle2 = (i * (BAR_WIDTH + BAR_SPACE) + BAR_WIDTH) / (COUNT * (BAR_SPACE + BAR_WIDTH)) * (float)Math.PI * 2;

					bars_vertices.put((float) Math.cos(angle) * radius);
					bars_vertices.put(-0.5F);
					bars_vertices.put((float) Math.sin(angle) * radius);
					bars_vertices.put(i);

					bars_vertices.put((float) Math.cos(angle2) * radius);
					bars_vertices.put(-0.5F);
					bars_vertices.put((float) Math.sin(angle2) * radius);
					bars_vertices.put(i);

					bars_vertices.put((float) Math.cos(angle) * radius);
					bars_vertices.put(1F);
					bars_vertices.put((float) Math.sin(angle) * radius);
					bars_vertices.put(i);

					bars_vertices.put((float) Math.cos(angle2) * radius);
					bars_vertices.put(1F);
					bars_vertices.put((float) Math.sin(angle2) * radius);		
					bars_vertices.put(i);
				}
			}
			else
			{
				for (int i = -COUNT / 2; i < COUNT / 2; ++i)
				{
					bars_vertices.put((i + 0.5F) * (BAR_SPACE + BAR_WIDTH) - BAR_WIDTH / 2F);
					bars_vertices.put(-0.5F);
					bars_vertices.put(-0.5F * tilt);
					bars_vertices.put(i + COUNT / 2);

					bars_vertices.put((i + 0.5F) * (BAR_SPACE + BAR_WIDTH) + BAR_WIDTH / 2F);
					bars_vertices.put(-0.5F);
					bars_vertices.put(-0.5F * tilt);
					bars_vertices.put(i + COUNT / 2);

					bars_vertices.put((i + 0.5F) * (BAR_SPACE + BAR_WIDTH) - BAR_WIDTH / 2F);
					bars_vertices.put(0);
					bars_vertices.put(0);
					bars_vertices.put(i + COUNT / 2);

					bars_vertices.put((i + 0.5F) * (BAR_SPACE + BAR_WIDTH) + BAR_WIDTH / 2F);
					bars_vertices.put(0);
					bars_vertices.put(0);
					bars_vertices.put(i + COUNT / 2);

					bars_vertices.put((i + 0.5F) * (BAR_SPACE + BAR_WIDTH) - BAR_WIDTH / 2F);
					bars_vertices.put(0);
					bars_vertices.put(0);
					bars_vertices.put(i + COUNT / 2);

					bars_vertices.put((i + 0.5F) * (BAR_SPACE + BAR_WIDTH) + BAR_WIDTH / 2F);
					bars_vertices.put(0);
					bars_vertices.put(0);
					bars_vertices.put(i + COUNT / 2);

					bars_vertices.put((i + 0.5F) * (BAR_SPACE + BAR_WIDTH) - BAR_WIDTH / 2F);
					bars_vertices.put(1F);
					bars_vertices.put(-1 * tilt);
					bars_vertices.put(i + COUNT / 2);

					bars_vertices.put((i + 0.5F) * (BAR_SPACE + BAR_WIDTH) + BAR_WIDTH / 2F);
					bars_vertices.put(1F);
					bars_vertices.put(-1 * tilt);		
					bars_vertices.put(i + COUNT / 2);
				}
			}

			ShortBuffer bars_indices = ShortBuffer.allocate(COUNT * 6 * (mode == Mode.BARS ? 2 : 1));

			for (int i = 0; i < COUNT * (mode == Mode.BARS ? 2 : 1); ++i)
			{
				bars_indices.put((short) (i * 4));	
				bars_indices.put((short) (i * 4 + 1));	
				bars_indices.put((short) (i * 4 + 2));	

				bars_indices.put((short) (i * 4 + 1));	
				bars_indices.put((short) (i * 4 + 3));	
				bars_indices.put((short) (i * 4 + 2));	
			}

			bars.init(GLES20.GL_TRIANGLES, bars_vertices, bars_indices);	
		}

		wallpaper = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableBackground", false);

		if (wallpaper)
		{
			float ratioX = 1;
			float ratioY = 1;

			try
			{
				Bitmap bitmap = BitmapFactory.decodeFile(context.getFilesDir().getAbsolutePath() + "wallpaper");

				if ((float)bitmap.getWidth() / bitmap.getHeight() > (float)holder.getSurfaceFrame().width() / holder.getSurfaceFrame().height())
				{
					ratioX = (float) bitmap.getWidth() / bitmap.getHeight();
					wallpaperX = true;
				}
				else
				{
					ratioY = (float) bitmap.getHeight() / bitmap.getWidth();		
					wallpaperX = false;
				}

				texture.init(bitmap);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				texture.bind();

				wallpaper = true;
			}
			catch (Exception e) 
			{
				e.printStackTrace();

				wallpaper = false;
			}


			FloatBuffer vertices_background = FloatBuffer.allocate(4 * 5);

			vertices_background.put(-ratioX);
			vertices_background.put(-ratioY);
			vertices_background.put(0);
			vertices_background.put(0);
			vertices_background.put(1);

			vertices_background.put(ratioX);
			vertices_background.put(-ratioY);
			vertices_background.put(0);
			vertices_background.put(1);
			vertices_background.put(1);

			vertices_background.put(-ratioX);
			vertices_background.put(ratioY);
			vertices_background.put(0);
			vertices_background.put(0);
			vertices_background.put(0);

			vertices_background.put(ratioX);
			vertices_background.put(ratioY);
			vertices_background.put(0);
			vertices_background.put(1);
			vertices_background.put(0);

			ShortBuffer indices_background = ShortBuffer.allocate(6);

			indices_background.put((short) 0);
			indices_background.put((short) 1);
			indices_background.put((short) 2);

			indices_background.put((short) 1);
			indices_background.put((short) 2);
			indices_background.put((short) 3);

			background.init(GLES20.GL_TRIANGLES, vertices_background, indices_background);

			program_background.init(vertexShader_background, fragmentShader_background, "a_Position", "a_UV");

			program_background.use();
			GLES20.glUniform1i(GLES20.glGetUniformLocation(program_background.program, "u_Texture"), 0);
		}


		boolean rainbow = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enableRainbow", false);
		if (mode == Mode.LINE)
		{
			if (rainbow)
			{
				program_bars.init(vertexShader_line_rainbow, fragmentShader_line_rainbow, "a_Position");
			}
			else
			{
				program_bars.init(vertexShader_line, fragmentShader_line, "a_Position");
			}
		}
		else if (mode == Mode.BARS_CIRCLE)
		{
			if (rainbow)
			{
				program_bars.init(vertexShader_bars_circle_rainbow, fragmentShader_bars_rainbow, "a_Position");
			}
			else
			{
				program_bars.init(vertexShader_bars_circle, fragmentShader_bars, "a_Position");
			}
		}
		else
		{
			if (rainbow)
			{
				program_bars.init(vertexShader_bars_rainbow, fragmentShader_bars_rainbow, "a_Position");
			}
			else
			{
				program_bars.init(vertexShader_bars, fragmentShader_bars, "a_Position");
			}
		}

		String hex = PreferenceManager.getDefaultSharedPreferences(context).getString("barColor", "#ffffff");
		int color = 0xFFFFFF;
		try
		{
			color = Color.parseColor(hex);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		program_bars.use();
		GLES20.glUniform4f(GLES20.glGetUniformLocation(program_bars.program, "u_Color"), Color.red(color) / 255F, Color.green(color) / 255F, Color.blue(color) / 255F, 1);
		mvpHandle_bars = GLES20.glGetUniformLocation(program_bars.program, "u_MVP");
		heightHandle_bars = GLES20.glGetUniformLocation(program_bars.program, "u_Height");

		hex = PreferenceManager.getDefaultSharedPreferences(context).getString("backgroundColor", "#ff8800");
		color = 0xFF8800;
		try
		{
			color = Color.parseColor(hex);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		GLES20.glClearColor(Color.red(color) / 255F, Color.green(color) / 255F, Color.blue(color) / 255F, 0F);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glLineWidth(5F);

		if (mode == Mode.BARS_CIRCLE)
			Matrix.setLookAtM(view_bars, 0, 0, 10F, 0F, 0, 0, 0, 0, 0, 1);
		else
			Matrix.setLookAtM(view_bars, 0, 0, -0.5F, 10F, 0, 0, 0, 0, 1, 0);

		Matrix.setLookAtM(view_background, 0, 0, 0F, 1F, 0, 0, 0, 0, 1, 0);

		update = false;
	}	

	int width, height;

	@Override
	public void onSurfaceChanged(GL10 p1, int width, int height)
	{
		this.width = width;
		this.height = height;

		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) height / width;
		Matrix.frustumM(projection_bars, 0, -1, 1, -ratio, ratio, 1, 100);

		if (wallpaperX)
		{
			ratio = (float) width / height;
			Matrix.orthoM(projection_background, 0, -ratio, ratio, -1, 1, 0.5F, 1.5F);		
		}
		else
		{
			ratio = (float) height / width;
			Matrix.orthoM(projection_background, 0, -1, 1, -ratio, ratio, 0.5F, 1.5F);	
		}	
	}

	boolean update;

	@Override
	public void onDrawFrame(GL10 p1)
	{		
		if (!this.visualizer.getEnabled())
		{
			this.visualizer.release();

			this.visualizer = new Visualizer(0);
			this.visualizer.setEnabled(false);
			this.visualizer.setCaptureSize(COUNT * 2);
			//this.visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
			this.visualizer.setDataCaptureListener(this, 10000, true, true);
			this.visualizer.setEnabled(true);
		}

		if (update)
		{
			onSurfaceCreated(p1, null);
			onSurfaceChanged(p1, width, height);
		}

		if (mode == Mode.LINE)
		{
			FloatBuffer bars_vertices = FloatBuffer.allocate(COUNT * 4 * 2 * LINE_COUNT);

			int offset = (int) (Math.min(100, Math.max(0, System.currentTimeMillis() - lastTime))) * COUNT * 2 / 100;
			for (int i = 0; i < COUNT * 2 * LINE_COUNT; ++i)
			{
				bars_vertices.put((i - COUNT * LINE_COUNT) * 0.01F);
				bars_vertices.put((waveform[i + offset] / 255F + (waveform[i + offset] > 0 ? -0.5F : 0.5F)) * 40F);
				bars_vertices.put(0);
				bars_vertices.put(((float)i / COUNT / LINE_COUNT) + (SystemClock.elapsedRealtime() * 0.002F) % 1);
			}

			bars.init(GLES20.GL_LINE_STRIP, bars_vertices, COUNT * 2 * LINE_COUNT);
		}

		if (!bars.initialized() || !program_bars.initialized()
			|| (!background.initialized() && wallpaper) || (!program_background.initialized() && wallpaper)
			|| (!texture.initialized() && wallpaper))
		{
			System.err.println("Not initialized!");
			return;
		}

		if (mode != Mode.LINE)
			for (int i = 0; i < COUNT; ++i)
			{
				if (i == 0)
					height_bars[0] = height_bars[0] * 0.75F + 0.25F * 2 * nanToZero(Math.log10(fft[0] * fft[0] * 2));
				else
					height_bars[i] = height_bars[i] * 0.75F + 0.25F * 2 * nanToZero(Math.log10(Math.abs(fft[i * 2] * fft[i * 2] + fft[i * 2 + 1] * fft[i * 2 + 1])));

			}

		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		if (wallpaper)
		{
			program_background.use();

			Matrix.setIdentityM(model, 0);

			Matrix.multiplyMM(mvp, 0, view_background, 0, model, 0);
			Matrix.multiplyMM(mvp, 0, projection_background, 0, mvp, 0);

			GLES20.glUniformMatrix4fv(mvpHandle_background, 1, false, mvp, 0);

			background.draw(3, 2);
		}

		program_bars.use();

		Matrix.setIdentityM(model, 0);
		Matrix.multiplyMM(mvp, 0, view_bars, 0, model, 0);
		Matrix.multiplyMM(mvp, 0, projection_bars, 0, mvp, 0);

		GLES20.glUniformMatrix4fv(mvpHandle_bars, 1, false, mvp, 0);
		GLES20.glUniform1fv(heightHandle_bars, COUNT, height_bars, 0);

		bars.draw(4);
	}

	public void onOffsetChanged(float xOffset, float yOffset)
	{
	}

	public float nanToZero(double f)
	{
		return Double.isInfinite(f) || Double.isNaN(f) ? 0F : (float) f;
	}

	public void release()
	{
//			bars.delete();
//			program_bars.delete();
//			
//			background.delete();
//			program_background.delete();
//			
//			texture.delete();
//			
		visualizer.setEnabled(false);
		visualizer.release();

		visualizer = null;
	}

	public void update()
	{
		this.update = true;
	}

	public enum Mode
	{
		BARS,
		BARS_CIRCLE,
		LINE,
	}
}

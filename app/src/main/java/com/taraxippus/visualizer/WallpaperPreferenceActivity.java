package com.taraxippus.visualizer;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.taraxippus.visualizer.*;
import com.taraxippus.visualizer.gl.*;
import java.io.*;

public class WallpaperPreferenceActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(com.taraxippus.visualizer.R.xml.preference);
	
		Preference filePicker = findPreference("filePicker");
		filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.setType("image/*");
				
				startActivityForResult(i, 0);
				return true;
			}
		});
		
		Preference setWallpaper = findPreference("setWallpaper");
		setWallpaper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					try
					{
						Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
						intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(WallpaperPreferenceActivity.this, OpenGLESWallpaperService.class));
						startActivity(intent);
					}
					catch(Exception e)
					{
						Intent intent = new Intent();
						intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
						startActivity(intent);
					}
					
					return true;
				}
			});
			
		Preference floating = findPreference("floating");
		floating.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					if (isServiceRunning(VisualizerService.class))
					{
						stopService(new Intent(WallpaperPreferenceActivity.this, VisualizerService.class));
					}
					else
					{
						startService(new Intent(WallpaperPreferenceActivity.this, VisualizerService.class));
					}
					
					return true;
				}
			});
			
		Preference crash = findPreference("crash");
		crash.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					
					throw new RuntimeException("Debug crash");
				}
			});
			
		setSummary("Choose between different wallpaper modes", "mode");
		
		chooseColor("barColor", "#ffffff");
		chooseColor("backgroundColor", "#ff8800");
		
		sliders("position", "Change position of the bars", "Position", 0, 0, 0, 0, 10, 2);
		sliders("rotation", "Change rotation of the bars", "Rotation", 0, 0, 0, 0, (float) Math.PI, 100);
		sliders("camera", "Change the camera offset", "Camera offset", 0, 0, 10, 0, 15, 2);
		
	}
	
	private boolean isServiceRunning(Class<?> serviceClass) 
	{
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if (serviceClass.getName().equals(service.service.getClassName()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch (requestCode)
		{
			case 0:		
				if (resultCode != RESULT_OK)
					break;
				
				Uri content_describer = data.getData();
				
				InputStream in = null;
				OutputStream out = null; 
				
				try 
				{
					in = getContentResolver().openInputStream(content_describer);
					out = new FileOutputStream(getFilesDir().getPath() + "wallpaper");
				
					byte[] buffer = new byte[1024];
					int len;
					while ((len = in.read(buffer)) != -1) 
					{
						out.write(buffer, 0, len);
					}
					
					SharedPreferences customSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
					SharedPreferences.Editor editor = customSharedPreference.edit();
					editor.putString("wallpaper", content_describer.getPath());
					editor.commit();
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				} 
				finally 
				{
					try
					{
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
				}
				
				return;
			default:
				break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void chooseColor(final String sharedPreference, final String def)
	{
		final Preference p = findPreference(sharedPreference);
		p.setIcon(getIcon(0xFF000000 | Color.parseColor(PreferenceManager.getDefaultSharedPreferences(this).getString(sharedPreference, def))));

		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					int colorInt = Color.parseColor(PreferenceManager.getDefaultSharedPreferences(WallpaperPreferenceActivity.this).getString(sharedPreference, def));

					final AlertDialog alertDialog = new AlertDialog.Builder(WallpaperPreferenceActivity.this).create();
					alertDialog.setTitle("Choose color");
					final View v = getLayoutInflater().inflate(R.layout.color, null);
					alertDialog.setView(v);
					final View color = v.findViewById(R.id.color);
					color.getBackground().setColorFilter(0xFF000000 | colorInt, PorterDuff.Mode.MULTIPLY);
					final SeekBar red = (SeekBar) v.findViewById(R.id.red);
					red.setProgress(Color.red(colorInt));
					final SeekBar green = (SeekBar) v.findViewById(R.id.green);
					green.setProgress(Color.green(colorInt));
					final SeekBar blue = (SeekBar) v.findViewById(R.id.blue);
					blue.setProgress(Color.blue(colorInt));
					final EditText hex = (EditText) v.findViewById(R.id.hex);
					hex.setText(Integer.toHexString(colorInt).toUpperCase());
					SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener()
					{
						@Override
						public void onProgressChanged(SeekBar p1, int p2, boolean p3)
						{
							int colorInt = fromRGB(red.getProgress(), green.getProgress(), blue.getProgress());
							color.getBackground().setColorFilter(colorInt,PorterDuff.Mode.MULTIPLY);
							hex.setText(Integer.toHexString(colorInt).substring(2).toUpperCase());
						}

						@Override
						public void onStartTrackingTouch(SeekBar p1)
						{

						}

						@Override
						public void onStopTrackingTouch(SeekBar p1)
						{

						}
					};
					red.setOnSeekBarChangeListener(listener);
					green.setOnSeekBarChangeListener(listener);
					blue.setOnSeekBarChangeListener(listener);

					hex.setOnEditorActionListener(new EditText.OnEditorActionListener()
						{
							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								if (p2 == EditorInfo.IME_ACTION_GO)
								{

									int colorInt = Integer.parseInt(p1.getText().toString(), 16);
									color.getBackground().setColorFilter(0xFF000000 | colorInt, PorterDuff.Mode.MULTIPLY);
									red.setProgress(Color.red(colorInt));
									green.setProgress(Color.green(colorInt));
									blue.setProgress(Color.blue(colorInt));

									return true;
								}
								return false;
							}	
						});

					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Choose", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								PreferenceManager.getDefaultSharedPreferences(WallpaperPreferenceActivity.this).edit().putString(sharedPreference, "#" + hex.getText().toString()).commit();
								p.setIcon(getIcon(0xFF000000 | Color.parseColor(PreferenceManager.getDefaultSharedPreferences(WallpaperPreferenceActivity.this).getString(sharedPreference, def))));

								alertDialog.dismiss();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								alertDialog.cancel();
							}
						});

					alertDialog.show();

					return true;
				}
			});
	}

	public Drawable getIcon(int color)
	{
//		Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
//		Paint paint = new Paint();
//		paint.setColor(color);
//		new Canvas(bitmap).drawCircle(256, 256, 256 * 3 / 4F, paint);
//		return new BitmapDrawable(getResources(), bitmap);

		Drawable circle = getDrawable(R.drawable.circle);
		circle.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		return circle;
	}

	public static int fromRGB(int red, int green, int blue)
	{
		red = (red << 16) & 0x00FF0000;
		green = (green << 8) & 0x0000FF00;
		blue = blue & 0x000000FF;
		return 0xFF000000 | red | blue | green;
	}
	

	public void sliders(final String key, final String summary, final String dialogTitle, final float defX, final float defY, final float defZ, final float zero, final float range, final float scale)
	{
		final Preference p = findPreference(key);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		p.setSummary(summary + "\nCurrent: "
					 + "(X: " + (int) (preferences.getFloat(key + "X", defX) * 100) / 100F
					 + "; Y: " + (int) (preferences.getFloat(key + "Y", defY) * 100) / 100F
					 + "; Z: " + (int) (preferences.getFloat(key + "Z", defZ) * 100) / 100F
					 + ")"																			 
					 );
					 
		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					final AlertDialog alertDialog = new AlertDialog.Builder(WallpaperPreferenceActivity.this).create();
					alertDialog.setTitle(dialogTitle);

					final View v = getLayoutInflater().inflate(R.layout.sliders, null);
					alertDialog.setView(v);

					final SeekBar x = (SeekBar) v.findViewById(R.id.x);
					x.setMax((int) ((range - zero + range) * scale));
					x.setProgress((int) (scale * (range + preferences.getFloat(key + "X", defX))));
					
					final SeekBar y = (SeekBar) v.findViewById(R.id.y);
					y.setMax((int) ((range - zero + range) * scale));
					y.setProgress((int) (scale * (range + preferences.getFloat(key + "Y", defY))));
					
					final SeekBar z = (SeekBar) v.findViewById(R.id.z);
					z.setMax((int) ((range - zero + range) * scale));
					z.setProgress((int) (scale * (range + preferences.getFloat(key + "Z", defZ))));
					
					
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								preferences.edit().putFloat(key + "X", x.getProgress() / scale - range)
									.putFloat(key + "Y", y.getProgress() / scale - range)
									.putFloat(key + "Z", z.getProgress() / scale - range).commit();
								
								p.setSummary(summary + "\nCurrent: "
											 + "(X: " + (int) (preferences.getFloat(key + "X", defX) * 100) / 100F
											 + "; Y: " + (int) (preferences.getFloat(key + "Y", defY) * 100) / 100F
											 + "; Z: " + (int) (preferences.getFloat(key + "Z", defZ) * 100) / 100F
											 + ")"																			 
											 );
									
								alertDialog.dismiss();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								alertDialog.cancel();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Reset", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								preferences.edit().putFloat(key + "X", defX).putFloat(key + "Y", defY).putFloat(key + "Z", defZ).commit();
								
								p.setSummary(summary + "\nCurrent: "
											 + "(X: " + (int) (preferences.getFloat(key + "X", defX) * 100) / 100F
											 + "; Y: " + (int) (preferences.getFloat(key + "Y", defY) * 100) / 100F
											 + "; Z: " + (int) (preferences.getFloat(key + "Z", defZ) * 100) / 100F
											 + ")"																			 
											 );
								
								alertDialog.dismiss();
							}
						});

					alertDialog.show();

					return true;
				}
			});
	}

	public void setSummary(final String summary, final String key)
	{
		final Preference preference = findPreference(key);
		preference.setSummary(summary + "\nCurrent: " + PreferenceManager.getDefaultSharedPreferences(this).getString(key, "null"));
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference p1, Object p2)
				{
					preference.setSummary(summary + "\nCurrent: " + p2.toString());
					return true;
				}	
			});
	}
	
}

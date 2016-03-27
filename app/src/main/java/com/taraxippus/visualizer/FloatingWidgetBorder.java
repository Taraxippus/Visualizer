package com.taraxippus.visualizer;

import android.*;
import android.appwidget.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.util.*;

public class FloatingWidgetBorder extends FrameLayout
{
	private ImageView mLeftHandle;
    private ImageView mRightHandle;
    private ImageView mTopHandle;
    private ImageView mBottomHandle;
	
	public int activeHandle = -1;
	
	public FloatingWidgetBorder(Context context)
	{
		super(context);
		
	    setBackgroundResource(R.drawable.widget_resize_shadow);
        setForeground(getResources().getDrawable(R.drawable.widget_resize_frame));
        setPadding(0, 0, 0, 0);
		setClipChildren(false);
		
        final int handleMargin = getResources().getDimensionPixelSize(R.dimen.widget_handle_margin);
		
        LayoutParams lp;
        mLeftHandle = new ImageView(context);
        mLeftHandle.setImageResource(R.drawable.ic_widget_resize_handle);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
							  Gravity.LEFT | Gravity.CENTER_VERTICAL);
        lp.leftMargin = handleMargin;
        addView(mLeftHandle, lp);
        mRightHandle = new ImageView(context);
        mRightHandle.setImageResource(R.drawable.ic_widget_resize_handle);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
							  Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        lp.rightMargin = handleMargin;
        addView(mRightHandle, lp);
        mTopHandle = new ImageView(context);
        mTopHandle.setImageResource(R.drawable.ic_widget_resize_handle);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
							  Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        lp.topMargin = handleMargin;
        addView(mTopHandle, lp);
        mBottomHandle = new ImageView(context);
        mBottomHandle.setImageResource(R.drawable.ic_widget_resize_handle);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
							  Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        lp.bottomMargin = handleMargin;
        addView(mBottomHandle, lp);
		
	}
	
	public int getHandle(float x, float y)
	{
		int flags = 0;
		
		if (x < getWidth() / 4F)
		{
			flags = flags | 0b1;
		}
		if (x > getWidth() / 4F * 3)
		{
			flags = flags | 0b10;
		}
		if (y < getHeight() / 4F)
		{
			flags = flags | 0b100;
		}
		if (y > getHeight() / 4F * 3)
		{
			flags = flags | 0b1000;
		}
		
		return flags == 0 ? -1 : flags;
	}
		
	public void setActiveHandle(float x, float y)
	{
		this.setActiveHandle(getHandle(x, y));
	}
	
	public void setActiveHandle(int handle)
	{
		this.activeHandle = handle;
		
		this.mLeftHandle.setAlpha((handle & 0b1) != 0 ? 1F : 0F);
		this.mRightHandle.setAlpha((handle & 0b10) != 0  ? 1F : 0F);
		this.mTopHandle.setAlpha((handle & 0b100) != 0  ? 1F : 0F);
		this.mBottomHandle.setAlpha((handle & 0b1000) != 0  ? 1F : 0F);
	}
}

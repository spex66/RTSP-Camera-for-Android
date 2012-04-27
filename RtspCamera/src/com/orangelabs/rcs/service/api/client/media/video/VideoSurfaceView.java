/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.service.api.client.media.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Video surface view
 * 
 * @author jexa7410
 */
public class VideoSurfaceView extends SurfaceView {    
	/**
     * No aspect ratio
     */
    public static float NO_RATIO = 0.0f;

    /**
	 * Display area aspect ratio
	 */
	private float aspectRatio = NO_RATIO;
    
	/**
	 * Surface has been created state
	 */
	private boolean surfaceCreated = false;
	
	/**
	 * Surface holder
	 */
	private SurfaceHolder holder;

	/**
	 * Constructor
	 * 
	 * @param context Context
	 */
    public VideoSurfaceView(Context context) {
        super(context);

        init();
    }
    
    /**
     * Constructor
     * 
     * @param context Context
     * @param attrs Attributes
     */
    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init();
    }
    
    /**
     * Constructor
     * 
     * @param context Context
     * @param attrs Attributes
     * @param defStyle Style
     */
    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init();
    }

    /**
     * Set aspect ration according to desired width and height
     * 
     * @param width Width
     * @param height Height
     */
    public void setAspectRatio(int width, int height) {
        setAspectRatio((float)width / (float)height);
    }
    
    /**
     * Set aspect ratio
     * 
     * @param ratio Ratio
     */
    public void setAspectRatio(float ratio) {
        if (aspectRatio != ratio) {
            aspectRatio = ratio;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Ensure aspect ratio
     * 
     * @param widthMeasureSpec Width
     * @param heightMeasureSpec Heigh
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (aspectRatio != NO_RATIO) {
            int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

            int width = widthSpecSize;
            int height = heightSpecSize;

            if (width > 0 && height > 0) {
                float defaultRatio = ((float) width) / ((float) height);
                if (defaultRatio < aspectRatio) {
                    // Need to reduce height
                    height = (int) (width / aspectRatio);
                } else if (defaultRatio > aspectRatio) {
                    width = (int) (height * aspectRatio);
                }
                width = Math.min(width, widthSpecSize);
                height = Math.min(height, heightSpecSize);
                setMeasuredDimension(width, height);
                return;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    /**
	 * Set image from a bitmap
	 * 
	 * @param bmp Bitmap
	 */
	public void setImage(Bitmap bmp) {	
		if (surfaceCreated) {
			Canvas canvas = null;
			try {				
				synchronized(holder) {
					canvas = holder.lockCanvas();					
				}							
			} finally {
				if (canvas != null) {
					// First clear screen
					canvas.drawARGB(255, 0, 0, 0);
					
					// Then draw bmp
					canvas.drawBitmap(bmp, null, canvas.getClipBounds(), null);					
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
	
	public void clearImage() {	
		if (surfaceCreated) {
			Canvas canvas = null;
			try {				
				synchronized(holder) {
					canvas = holder.lockCanvas();					
				}							
			} finally {
				if (canvas != null) {
					// Clear screen
					canvas.drawARGB(255, 0, 0, 0);
					
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	/**
	 * Init the view
	 */
	private void init() {
		// Get a surface holder
		holder = this.getHolder();
        holder.addCallback(surfaceCallback);
	}
	
	/**
	 * Surface holder callback
	 */
	private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder _holder, int format, int w,int h) {
		}

		public void surfaceCreated(SurfaceHolder _holder) {
			surfaceCreated = true;
		}

		public void surfaceDestroyed(SurfaceHolder _holder) {
			surfaceCreated = false;
		}
	};
}

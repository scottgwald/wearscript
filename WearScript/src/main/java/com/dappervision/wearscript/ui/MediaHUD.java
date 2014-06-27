package com.dappervision.wearscript.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dappervision.wearscript.R;

import java.util.Timer;
import java.util.TimerTask;

public class MediaHUD extends SurfaceView implements SurfaceHolder.Callback {

    private DrawingThread drawingThread;
    private Canvas c;
    private Bitmap pause;
    private Bitmap stop;
    private Bitmap record;
    private Bitmap skipForward;
    private Bitmap skipBack;

    private boolean isPaused=false;
    private boolean isStopped = false;
    private boolean isRecording = false;
    private boolean isSkippingForward = false;
    private boolean isSkippingBack = false;
    private boolean wasPaused = false;
    private boolean isPresent = true;

    public MediaHUD(Context context) {
        super(context);
        getHolder().addCallback(this);
        drawingThread = new DrawingThread(getHolder(), this);
        pause = BitmapFactory.decodeResource(getResources(),
                R.drawable.videopause);
        stop = BitmapFactory.decodeResource(getResources(),
                R.drawable.stop);
        record = BitmapFactory.decodeResource(getResources(),
                R.drawable.record);
        skipForward = BitmapFactory.decodeResource(getResources(),
                R.drawable.skipforward);
        skipBack = BitmapFactory.decodeResource(getResources(),
                R.drawable.skipbackward);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (isPresent) {
            canvas.drawColor(Color.BLACK, PorterDuff.Mode.ADD);
        }
        if(isPaused) {
            canvas.drawBitmap(pause, 500, 30, null);
        }
        if (isStopped) {
            canvas.drawBitmap(stop, 500, 30, null);
        }
        if (isRecording) {
            canvas.drawBitmap(record,10,30,null);
        }
        if (isSkippingBack) {
            canvas.drawBitmap(skipBack,500,30,null);
        }
        if (isSkippingForward) {
            canvas.drawBitmap(skipForward,500,30,null);
        }



    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    public void surfaceCreated(SurfaceHolder arg0) {

        //drawingThread.setRunning(true);
        //drawingThread.start();
    }

    public void showPause() {

        this.clearSkip();

        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
                isPaused= true;
                isStopped = false;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }
    }

    public void stopRecording() {
        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
                this.isRecording = false;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }

    }

    public void clearSkip(){
        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
                this.isSkippingBack = false;
                this.isSkippingForward = false;
                if(wasPaused)
                    isPaused = true;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }
    }
    public void showSkipBack() {
        wasPaused = isPaused;
        this.clear();
        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
                this.isSkippingForward = false;
                this.isSkippingBack = true;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
               MediaHUD.this.clearSkip();
            }
        }, 600);
    }

    public void showSkipForward() {
        wasPaused = isPaused;
        this.clear();
        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
                this.isSkippingBack = false;
                this.isSkippingForward = true;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MediaHUD.this.clearSkip();
            }
        }, 600);
    }

    public void showRecording() {
        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
               this.isRecording = true;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }
    }

    public void showStop() {
        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
                this.isPaused = false;
                isStopped = true;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }
    }

    public void showPresent() {
        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
                isPresent = true;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }
    }

    public void hidePresent() {
        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
                isPresent = false;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }
    }

    public void clear() {
        try {
            c = this.getHolder().lockCanvas(null);
            synchronized (this.getHolder()) {
                isPaused = false;
                isStopped = false;
                isSkippingForward = false;
                isSkippingBack = false;
                this.onDraw(c);
            }
        } finally {
            if (c != null) {
                this.getHolder().unlockCanvasAndPost(c);
            }
        }
    }




    public void surfaceDestroyed(SurfaceHolder arg0) {
        boolean retry = true;
        drawingThread.setRunning(false);
        while (retry) {
            try {
                drawingThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
    class DrawingThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private MediaHUD _panel;
        private boolean _run = false;

        public DrawingThread(SurfaceHolder surfaceHolder, MediaHUD panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
        }

        public void setRunning(boolean run) {
            _run = run;
        }

        @Override
        public void run() {
            Canvas c;
            while (_run) {
                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {
                        _panel.onDraw(c);
                    }
                } finally {
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }

}

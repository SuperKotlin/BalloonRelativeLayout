package com.zhuyong.balloonrelativelayout;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private BalloonRelativeLayout mBalloonRelativeLayout;
    private VideoView mVideoView;

    private int TIME = 100;//这里默认100毫米添加一个气泡
    Handler mHandler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                mHandler.postDelayed(this, TIME);
                mBalloonRelativeLayout.addBalloon();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mBalloonRelativeLayout = (BalloonRelativeLayout) findViewById(R.id.balloonRelativeLayout);
        initVideoView();
    }

    private void initVideoView() {
        //设置屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mqr));
        //设置相关的监听
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
    }

    //播放准备
    @Override
    public void onPrepared(MediaPlayer mp) {
        //开始播放
        mVideoView.start();
        mHandler.postDelayed(runnable, TIME);
    }

    //播放结束
    @Override
    public void onCompletion(MediaPlayer mp) {
        //开始播放
        mVideoView.start();
    }
}

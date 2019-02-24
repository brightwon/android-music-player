package com.brightwon.music_player;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

@SuppressLint("Registered")
public class MusicPlayService extends Service implements FloatingViewListener {

    static final int NOTIFICATION_ID = 7777;
    static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";

    private FloatingViewManager manager;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        // generate floating View
        LayoutInflater inflater = LayoutInflater.from(this);
        CircleImageView iconView = (CircleImageView) inflater.inflate(R.layout.floating_play_widget, null, false);
        initFloatingView(intent, iconView);

        // service start
        startForeground(NOTIFICATION_ID, createNotification(this));

        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to music detail activity
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {

    }

    /*
        create notification
     */
    private static Notification createNotification(Context context) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                context.getString(R.string.app_name));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(context.getString(R.string.song_title));
        builder.setContentText(context.getString(R.string.song_artist));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        return builder.build();
    }

    /*
        when service is terminated, floatingView also disappears
     */
    @Override
    public void onDestroy() {
        if (manager != null) {
            manager.removeAllViewToWindow();
            manager = null;
        }
        super.onDestroy();
    }

    /*
        set FloatingView image (album art)
     */
    public void setFloatingViewImg(Uri uri, CircleImageView iconView) {
        Glide.with(this).load(uri).override(200,200).into(iconView);
    }

    /*
        initialize floatingView
     */
    public void initFloatingView(Intent intent, CircleImageView iconView) {
        // set FloatingView image
        Uri uri = intent.getData();
        setFloatingViewImg(uri, iconView);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        // floatingView manager setting
        manager = new FloatingViewManager(this, this);
        manager.setActionTrashIconImage(R.drawable.stop);
        manager.setSafeInsetRect((Rect) intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA));

        // view option setting
        FloatingViewManager.Options options = new FloatingViewManager.Options();
        options.floatingViewX = metrics.widthPixels;
        options.floatingViewY = (int) (metrics.heightPixels * 0.6);

        // add view
        manager.addViewToWindow(iconView, options);
    }
}

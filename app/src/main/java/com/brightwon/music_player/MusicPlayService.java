package com.brightwon.music_player;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

public class MusicPlayService extends Service implements FloatingViewListener {

    private final IBinder binder = new LocalBinder();

    static final int NOTIFICATION_ID = 7777;
    static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";

    private FloatingViewManager manager;
    private FloatingViewManager.Options options;

    private Parcelable parcelable;
    private CircleImageView iconView;
    public boolean isFloat = false;

    public static MusicPlayer mp;

    /* music details */
    private Uri albumArt;
    private String title;
    private String artist;

    public class LocalBinder extends Binder {
        MusicPlayService getService() {
            return MusicPlayService.this;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        int position = intent.getIntExtra("position", 0);
        int musicID = intent.getIntExtra("id", 0);
        String title = intent.getStringExtra("title");
        String artist = intent.getStringExtra("artist");
        Uri albumArt = intent.getParcelableExtra("artUri");

        setMusicDetails(albumArt, title, artist);

        // create MusicPlayer object and start
        mp = new MusicPlayer();
        playMusic(getApplicationContext(), musicID, position);

        // defines the floatingView
        parcelable = intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA);
        initFloatingView();
        setFloatingViewImg(albumArt);
        drawFloatingView();

        // starts the notification
        startForeground(NOTIFICATION_ID, createNotification(this, title, artist));

        setFloatingViewClickListener();
        return super.onStartCommand(intent, flags, startId);
    }

    /** starts the PlayerActivity */
    public void setFloatingViewClickListener() {
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to music detail activity
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("artist", artist);
                intent.putExtra("artUri", albumArt);
                startActivity(intent);
            }
        });
    }

    /** plays the music in MusicPlayer class */
    public void playMusic(Context context, int id, int position) {
        try {
            mp.playMusic(context, id, position);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** sets music details */
    public void setMusicDetails(Uri uri, String title, String artist) {
        this.albumArt = uri;
        this.title = title;
        this.artist = artist;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /** finish the MusicPlayService */
    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        mp.release();
        return super.onUnbind(intent);
    }

    /** releases the objects when the floatingView is removed */
    @Override
    public void onFinishFloatingView() {
        if (manager != null) {
            releaseAllSetting();
            mp.reset();
        }
    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {

    }

    /** sets the notification builder */
    private static Notification createNotification(Context context, String title, String artist) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                context.getString(R.string.app_name));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(title);
        builder.setContentText(artist);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        return builder.build();
    }

    /** initializes the new floatingView */
    public void setNewFloatingView(Uri uri) {
        initFloatingView();
        setFloatingViewImg(uri);
        drawFloatingView();
    }

    /** sets the floatingView image */
    public void setFloatingViewImg(Uri uri) {
        Glide.with(this).load(uri).override(200,200).into(iconView);
    }

    /** sets the floatingView settings */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void initFloatingView() {

        // initializes the floatingView
        iconView = (CircleImageView) LayoutInflater.from(this).
                inflate(R.layout.floating_play_widget, null, false);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        // sets the trash icon size and alpha
        manager = new FloatingViewManager(this, this);
        Bitmap bitmap = ((BitmapDrawable) getDrawable(R.drawable.stop)).getBitmap();
        Drawable mDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap,
                210, 210, true));
        mDrawable.setAlpha(150);
        manager.setFixedTrashIconImage(mDrawable);
        manager.setSafeInsetRect((Rect) parcelable);

        // sets the floatingView location
        // horizontal : right side, vertical : 5% from bottom
        options = new FloatingViewManager.Options();
        options.floatingViewX = metrics.widthPixels;
        options.floatingViewY = (int) (metrics.heightPixels * 0.05);
    }

    /** draws the floatingView */
    public void drawFloatingView() {
        manager.addViewToWindow(iconView, options);
        isFloat = true;
    }

    /** removes all settings */
    public void releaseAllSetting() {
        manager.removeAllViewToWindow();
        manager = null;
        iconView = null;
        options = null;
        isFloat = false;
    }

    @Override
    public void onDestroy() {
        if (manager != null) {
            releaseAllSetting();
        }
        super.onDestroy();
    }
}

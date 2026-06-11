package com.camera.gps.adsmanager;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import com.camera.gps.util.Utils.LogUtils;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-placement interstitial cooldowns and schedules preloading after cooldown.
 */
public class AdCooldownManager {
    
    private static final String TAG = "AdCooldownManager";
    private static volatile AdCooldownManager INSTANCE;
    
    private final ConcurrentHashMap<String, Long> lastAdClosedAtMsByPlacement;
    private final ConcurrentHashMap<String, Runnable> scheduledRunnablesByPlacement;
    private final Handler mainHandler;
    private long lastAdClosedAtMsGlobal = 0L;
    private Runnable scheduledGlobalRunnable;
    
    private AdCooldownManager() {
        lastAdClosedAtMsByPlacement = new ConcurrentHashMap<>();
        scheduledRunnablesByPlacement = new ConcurrentHashMap<>();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static AdCooldownManager getInstance() {
        if (INSTANCE == null) {
            synchronized (AdCooldownManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AdCooldownManager();
                }
            }
        }
        return INSTANCE;
    }
    
    public boolean isInCooldown(String placement, int cooldownSeconds) {
        if (cooldownSeconds <= 0) return false;
        
        Long lastTime = lastAdClosedAtMsByPlacement.get(placement);
        if (lastTime == null) return false;
        
        long elapsed = System.currentTimeMillis() - lastTime;
        return elapsed < cooldownSeconds * 1000L;
    }
    
    /**
     * Global cooldown irrespective of placement.
     */
    public boolean isInGlobalCooldown(int cooldownSeconds) {
        if (cooldownSeconds <= 0) return false;
        if (lastAdClosedAtMsGlobal == 0L) return false;
        
        long elapsed = System.currentTimeMillis() - lastAdClosedAtMsGlobal;
        return elapsed < cooldownSeconds * 1000L;
    }
    
    private int getRemainingGlobalCooldownSeconds(int cooldownSeconds) {
        if (lastAdClosedAtMsGlobal == 0L) return 0;
        
        long elapsed = System.currentTimeMillis() - lastAdClosedAtMsGlobal;
        long remainingMs = cooldownSeconds * 1000L - elapsed;
        return remainingMs > 0 ? (int) (remainingMs / 1000L) : 0;
    }
    
    /**
     * Call when an interstitial is dismissed. Records timestamp and schedules a preload
     * for this placement after the cooldown period.
     */
    public void onAdClosed(Activity activity, String placement, int cooldownSeconds, String publisher) {
        lastAdClosedAtMsByPlacement.put(placement, System.currentTimeMillis());
        lastAdClosedAtMsGlobal = System.currentTimeMillis();
        
        // Cancel previously scheduled tasks
        Runnable previousRunnable = scheduledRunnablesByPlacement.remove(placement);
        if (previousRunnable != null) {
            mainHandler.removeCallbacks(previousRunnable);
        }
        if (scheduledGlobalRunnable != null) {
            mainHandler.removeCallbacks(scheduledGlobalRunnable);
        }
        
        if (cooldownSeconds <= 0) return;
        
        Runnable cooldownLogger = new Runnable() {
            @Override
            public void run() {
                int remaining = getRemainingGlobalCooldownSeconds(cooldownSeconds);
                if (remaining > 0) {
                    LogUtils.logI(TAG, "Global cooldown: " + remaining + " sec remaining");
                    mainHandler.postDelayed(this, 1000L);
                } else {
                    LogUtils.logI(TAG, "Global cooldown finished — preloading ads");
                }
            }
        };
        mainHandler.post(cooldownLogger);
        
        // Schedule a single global preload after cooldown
        Runnable globalTask = new Runnable() {
            @Override
            public void run() {
                if (!isInGlobalCooldown(cooldownSeconds)) {
                    try {
                        InterstitialAdManager.getInstance().preloadPublisher(activity, publisher);
                    } catch (Throwable e) {
                        // Ignore errors
                    }
                }
            }
        };
        scheduledGlobalRunnable = globalTask;
        mainHandler.postDelayed(globalTask, cooldownSeconds * 1000L);
    }
}

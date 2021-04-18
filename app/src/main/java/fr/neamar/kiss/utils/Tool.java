package fr.neamar.kiss.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.List;

import timber.log.Timber;

public class Tool {
    public static void hideKeyboard(View view) {
        Timber.d("request hideKeyboard");
        ViewCompat.getWindowInsetsController(view).hide(WindowInsetsCompat.Type.ime());
    }

    public static void showKeyboard(View view) {
        Timber.d("request showKeyboard");
        ViewCompat.getWindowInsetsController(view).show(WindowInsetsCompat.Type.ime());
    }

    public static boolean isKeyboardShowing (View view) {
        if (ViewCompat.getRootWindowInsets(view) != null
                && ViewCompat.getRootWindowInsets(view).isVisible(WindowInsetsCompat.Type.ime()))
        return true;
        return false;
    }


    public static void toast(Context context, int str) {
        Toast.makeText(context, context.getResources().getString(str), Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static boolean isPackageInstalled(@NonNull String packageName, @NonNull PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int dp2px(float dp) {
        Resources resources = Resources.getSystem();
        float px = dp * resources.getDisplayMetrics().density;
        return (int) Math.ceil(px);
    }

    public static int sp2px(float sp) {
        Resources resources = Resources.getSystem();
        float px = sp * resources.getDisplayMetrics().scaledDensity;
        return (int) Math.ceil(px);
    }

    public static int clampInt(int target, int min, int max) {
        return Math.max(min, Math.min(max, target));
    }

    public static float clampFloat(float target, float min, float max) {
        return Math.max(min, Math.min(max, target));
    }

    public static Point convertPoint(Point fromPoint, View fromView, View toView) {
        int[] fromCoordinate = new int[2];
        int[] toCoordinate = new int[2];
        fromView.getLocationOnScreen(fromCoordinate);
        toView.getLocationOnScreen(toCoordinate);

        Point toPoint = new Point(fromCoordinate[0] - toCoordinate[0] + fromPoint.x, fromCoordinate[1] - toCoordinate[1] + fromPoint.y);
        return toPoint;
    }

    public static boolean isIntentActionAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.size() > 0;
    }

    public static String getIntentAsString(Intent intent) {
        if (intent == null) {
            return "";
        } else {
            return intent.toUri(0);
        }
    }

    public static Intent getIntentFromString(String string) {
        try {
            return Intent.parseUri(string, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Drawable getIcon(Context context, String filename) {
        Bitmap bitmap = BitmapFactory.decodeFile(context.getFilesDir() + "/icons/" + filename + ".png");
        if (bitmap != null) return new BitmapDrawable(context.getResources(), bitmap);
        return null;
    }

    public static void saveIcon(Context context, Bitmap icon, String filename) {
        File directory = new File(context.getFilesDir() + "/icons/");
        if (!directory.exists()) directory.mkdir();
        File file = new File(directory + filename + ".png");
        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            icon.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeIcon(Context context, String filename) {
        File file = new File(context.getFilesDir() + "/icons/" + filename + ".png");
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Super hacky code to display notification drawer
    // Can (and will) break in any Android release.
    @SuppressLint("PrivateApi")
    @SuppressWarnings("CatchAndPrintStackTrace")
    public static void displayNotificationDrawer(@NonNull Context activity) {
        @SuppressLint("WrongConstant") Object sbservice = activity.getSystemService("statusbar");
        Class<?> statusbarManager;
        try {
            statusbarManager = Class.forName("android.app.StatusBarManager");
            Method showStatusBar;
            if (Build.VERSION.SDK_INT >= 17) {
                showStatusBar = statusbarManager.getMethod("expandNotificationsPanel");
            } else {
                showStatusBar = statusbarManager.getMethod("expand");
            }
            showStatusBar.invoke(sbservice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void visibleViews(long duration, View... views) {
        if (views == null) return;
        for (final View view : views) {
            if (view == null) continue;
            view.animate()
                    .alpha(1)
                    .setDuration(duration)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withStartAction(() -> view.setVisibility(View.VISIBLE));
        }
    }

    public static void invisibleViews(long duration, View... views) {
        if (views == null) return;
        for (final View view : views) {
            if (view == null) continue;
            view.animate()
                    .alpha(0)
                    .setDuration(duration)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> view.setVisibility(View.INVISIBLE));
        }
    }

    public static void dumpStacktrace (String name) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element: elements) {
            Timber.tag(name).d(element.toString());
        }
    }

    public static void toastMessage (Context context, String message) {
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        int iconSize = Tool.dp2px(48);

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        int oriWidth = drawable.getIntrinsicWidth();
        int oriHeigh = drawable.getIntrinsicHeight();

        if (oriWidth <= 0 || oriHeigh <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(oriWidth, oriHeigh, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        if (oriWidth < iconSize && oriHeigh < iconSize) {
            return bitmap;
        } else {
            return BitmapUtils.getScaledBitmap(bitmap,iconSize,iconSize);
        }
    }
}

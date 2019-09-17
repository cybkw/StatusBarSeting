package com.bkw.statusbarandliuhai.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.ColorUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bkw.statusbarandliuhai.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 状态栏工具类
 *
 * @author bkw
 */
public class StatusBarUtils {
    private static final int FAKE_STATUS_BAR_VIEW_ID = R.id.fake_status_bar_view;

    /**
     * 获取Activity页面根布局
     * 给当前内容布局设置与状态栏同样高度的pading。
     */
    public static void setContentViewPading(Activity activity) {
        View childAt = ((ViewGroup) activity.getWindow().findViewById(android.R.id.content)).getChildAt(0);
        childAt.setPadding(0, getStatusBarHeight(activity.getApplicationContext()), 0, 0);
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int height = 0;
        try {
            int identifier = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (identifier > 0) {
                height = context.getResources().getDimensionPixelSize(identifier);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return height;
    }

    public static void setColor(Context context, @ColorInt int color) {
        if (context instanceof Activity) {
            setColor(((Activity) context).getWindow(), color);
        }
    }

    /**
     * 4.4版本-5.0版本實現沉浸式方法
     *
     * @param window
     * @param color
     * @param isTransparent
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setColor(Window window, int color, boolean isTransparent) {
        Context context = window.getContext();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        View fakeStatusBarView = decorView.findViewById(FAKE_STATUS_BAR_VIEW_ID);
        if (fakeStatusBarView != null) {
            fakeStatusBarView.setBackgroundColor(color);
            if (fakeStatusBarView.getVisibility() == View.GONE) {
                fakeStatusBarView.setVisibility(View.VISIBLE);
            }
        } else {
            //绘制一个和状态栏一样高的矩形
            View statusBarView = new View(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(context));
            statusBarView.setLayoutParams(layoutParams);
            statusBarView.setBackgroundColor(color);
            statusBarView.setId(FAKE_STATUS_BAR_VIEW_ID);
            decorView.addView(statusBarView);
        }
    }

    public static void setColor(Context context, int color, boolean isTransparent) {
        if (context instanceof Activity) {
            setColor(((Activity) context).getWindow(), color, isTransparent);
        }
    }


    /**
     * 设置状态栏颜色 5.0以上
     *
     * @param window 修改状态栏颜色其实就是对Window进行操作，window可以是Activity或Dialog持有的Window。
     * @hint 设置纯颜色时，我们还需要将该颜色与黑色进行1:1的混合。因为状态栏的文字和图标颜色默认是白色的，
     * 并且在Android 5.0以下是不能修改的，所以如果修改成较浅的颜色，
     * 就会导致状态栏文字看不清的现象，因此做一个比较暗的浮层效果更好一些。
     */
    public static void setColor(Window window, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.setStatusBarColor(color);
            setTextDark(window, !isDarkColor(color));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setColor(window, ColorUtils.blendARGB(Color.TRANSPARENT, color, 0.5f), false);
        }
    }


    public static void setTextDark(Context context, boolean isDark) {
        if (context instanceof Activity) {
            setTextDark(((Activity) context).getWindow(), isDark);
        }
    }


    /**
     * 将状态栏文字颜色设置为深色或浅色,6.0版本及以上;
     * 以及6.0以下版本小米与魅族的处理
     *
     * @param window
     * @param isDark
     */
    public static void setTextDark(Window window, boolean isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            int systemUiVisibility = decorView.getSystemUiVisibility();
            if (isDark) {
                decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switch (OSUtils.getRomType()) {
                case MIUI:
                    setMIUIDark(window, isDark);
                    break;
                case FLYME:
                    setFlymeDark(window, isDark);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 判斷顏色深淺
     * 根据状态栏的颜色自动设置状态栏文字的颜色
     *
     * @param color
     * @return
     */
    public static boolean isDarkColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) < 0.5;
    }


    /**
     * 设置状态栏透明
     * 设置状态栏透明时，为了也能清楚地看清状态栏的文字，我们直接设置状态栏的颜色为50%透明度的黑色。
     *
     * @param window
     */
    public static void setTransparent(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setColor(window, 0x80000000, true);
        }
    }

    public static void setTransparent(Context context) {
        if (context instanceof Activity) {
            setTransparent(((Activity) context).getWindow());
        }
    }


    /**
     * MUI
     * 6.0以下版本，目前只有小米和魅族系统提供支持
     *
     * @param window
     * @param isDark
     */
    private static void setMIUIDark(Window window, boolean isDark) {
        Class<? extends Window> windowClass = window.getClass();
        int darkModeFlag;
        try {
            Class<?> aClass = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = aClass.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(aClass);

            Method setExtraFlags = aClass.getMethod("setExtraFlags", int.class, int.class);
            setExtraFlags.invoke(window, isDark ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 魅族FlymeUI 修改状态栏颜色文字
     * 6.0以下版本，目前只有小米和魅族系统提供支持
     *
     * @param window
     * @param isDark
     */
    private static void setFlymeDark(Window window, boolean isDark) {
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field meizuFlagDarkStatus = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                meizuFlagDarkStatus.setAccessible(true);
                meizuFlags.setAccessible(true);

                int bit = meizuFlagDarkStatus.getInt(null);
                int value = meizuFlags.getInt(lp);

                if (isDark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlagDarkStatus.setInt(lp, value);
                window.setAttributes(lp);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}

package com.example.csproject.CommonUtilities;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class FullScreenUtilities {
    private static int hideSystemBars()
    {
        return View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    }
    public static void systemUiChangeManager(View decorView)//anytime you show the system toolbars, it hides them
    {
        decorView.setOnSystemUiVisibilityChangeListener(systemBarShown ->
        {
            if (systemBarShown == 0)
                decorView.setSystemUiVisibility(hideSystemBars());
        });
    }
    public static void fullscreenSetup(Window window)//before the setContentView, makes the app fullscreen
    {
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(hideSystemBars());
        window.getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
    }
}

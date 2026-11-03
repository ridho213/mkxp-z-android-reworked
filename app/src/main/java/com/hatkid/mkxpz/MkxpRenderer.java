package com.hatkid.mkxpz;

import android.content.Context;
import android.content.pm.PackageManager;
import android.system.Os;
import android.util.Log;

import java.io.File;

/**
 * Pilihan renderer.
 *  - "gles"   : OpenGL ES bawaan perangkat (default).
 *  - "vulkan" : OpenGL ES dijalankan di atas Vulkan lewat ANGLE. Butuh libEGL_angle.so dan
 *               libGLESv2_angle.so di app/src/main/jniLibs/arm64-v8a/. Tanpa itu opsi ini nonaktif.
 *               (Eksperimental: nama variabel lingkungan SDL belum diuji di mkxp-z.)
 */
public class MkxpRenderer
{
    private static final String TAG = "mkxp-z[Renderer]";

    public static boolean vulkanDeviceSupported(Context ctx)
    {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL, 0);
    }

    public static boolean angleBundled(Context ctx)
    {
        File dir = new File(ctx.getApplicationInfo().nativeLibraryDir);
        return new File(dir, "libEGL_angle.so").exists() && new File(dir, "libGLESv2_angle.so").exists();
    }

    public static boolean vulkanAvailable(Context ctx)
    {
        return vulkanDeviceSupported(ctx) && angleBundled(ctx);
    }

    /** Panggil SEBELUM super.onCreate() MainActivity (sebelum SDL memuat library). */
    public static void apply(Context ctx)
    {
        MkxpPrefs prefs = new MkxpPrefs(ctx);
        if (!"vulkan".equals(prefs.getRenderer()) || !vulkanAvailable(ctx)) return;

        String dir = ctx.getApplicationInfo().nativeLibraryDir;
        try {
            Os.setenv("SDL_VIDEO_EGL_DRIVER", dir + "/libEGL_angle.so", true);
            Os.setenv("SDL_VIDEO_GL_DRIVER", dir + "/libGLESv2_angle.so", true);
            Os.setenv("ANGLE_DEFAULT_PLATFORM", "vulkan", true);
            Log.i(TAG, "Renderer: ANGLE (Vulkan)");
        } catch (Exception e) {
            Log.w(TAG, "Gagal mengatur renderer Vulkan: " + e);
        }
    }
}

package com.hatkid.mkxpz;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/** Pengaturan launcher (disimpan di SharedPreferences). */
public class MkxpPrefs
{
    private final SharedPreferences sp;

    public MkxpPrefs(Context ctx)
    {
        sp = ctx.getSharedPreferences("mkxp_launcher", Context.MODE_PRIVATE);
    }

    // Renderer: "gles" (OpenGL ES native) atau "vulkan" (OpenGL ES di atas Vulkan lewat ANGLE)
    public String getRenderer() { return sp.getString("renderer", "gles"); }
    public void setRenderer(String v) { sp.edit().putString("renderer", v).apply(); }

    // vGamepad
    public boolean isVpadEnabled() { return sp.getBoolean("vpad", true); }
    public void setVpadEnabled(boolean v) { sp.edit().putBoolean("vpad", v).apply(); }

    public int getOpacity() { return sp.getInt("opacity", 30); }
    public void setOpacity(int v) { sp.edit().putInt("opacity", v).apply(); }

    public int getScale() { return sp.getInt("scale", 100); }
    public void setScale(int v) { sp.edit().putInt("scale", v).apply(); }

    public boolean isDiagonal() { return sp.getBoolean("diagonal", false); }
    public void setDiagonal(boolean v) { sp.edit().putBoolean("diagonal", v).apply(); }

    // Gamepad bind: keycode tombol fisik -> keycode keyboard yang dipahami mkxp
    public Map<Integer, Integer> loadBinds()
    {
        Map<Integer, Integer> m = new HashMap<>();
        String s = sp.getString("binds", "");
        if (s == null || s.isEmpty()) return m;
        for (String part : s.split(";")) {
            String[] kv = part.split("=");
            if (kv.length != 2) continue;
            try {
                m.put(Integer.parseInt(kv[0]), Integer.parseInt(kv[1]));
            } catch (NumberFormatException ignored) {
            }
        }
        return m;
    }

    public void saveBinds(Map<Integer, Integer> m)
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : m.entrySet()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        sp.edit().putString("binds", sb.toString()).apply();
    }

    public void resetBinds() { sp.edit().remove("binds").apply(); }
}

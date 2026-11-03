package com.hatkid.mkxpz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;

/** Layar awal: pengaturan renderer, vGamepad, gamepad bind, lalu tombol Main. */
public class LauncherActivity extends Activity
{
    private MkxpPrefs prefs;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        prefs = new MkxpPrefs(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        build();
    }

    private int dp(int v)
    {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private TextView heading(String t)
    {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextSize(18f);
        tv.setPadding(0, dp(24), 0, dp(8));
        return tv;
    }

    private void add(android.view.View v)
    {
        root.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private Switch toggle(String label, boolean value, CompoundButton.OnCheckedChangeListener l)
    {
        Switch s = new Switch(this);
        s.setText(label);
        s.setChecked(value);
        s.setOnCheckedChangeListener(l);
        return s;
    }

    private interface IntSink { void set(int v); }

    private void slider(final String label, final int min, int max, int value, final IntSink sink)
    {
        final TextView tv = new TextView(this);
        tv.setText(label + ": " + value);
        add(tv);
        SeekBar sb = new SeekBar(this);
        sb.setMax(max - min);
        sb.setProgress(value - min);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser)
            {
                tv.setText(label + ": " + (p + min));
                if (fromUser) sink.set(p + min);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        add(sb);
    }

    private void build()
    {
        root.removeAllViews();

        TextView title = new TextView(this);
        title.setText("Changed Special Launcher");
        title.setTextSize(24f);
        add(title);

        // ---- Game
        File gameDir = new File(Environment.getExternalStorageDirectory(), "mkxp-z");
        boolean hasGame = new File(gameDir, "Game.ini").exists();
        TextView status = new TextView(this);
        status.setText(hasGame ? "Game ditemukan di " + gameDir.getAbsolutePath()
            : "Game belum ditemukan. Taruh isi game (Game.ini, Data, Graphics, ...) di " + gameDir.getAbsolutePath());
        status.setPadding(0, dp(8), 0, 0);
        add(status);

        // ---- Renderer
        add(heading("Renderer"));
        boolean vkOk = MkxpRenderer.vulkanAvailable(this);
        RadioGroup rg = new RadioGroup(this);
        RadioButton gl = new RadioButton(this);
        gl.setId(1);
        gl.setText("OpenGL ES (default, paling kompatibel)");
        RadioButton vk = new RadioButton(this);
        vk.setId(2);
        String vkNote;
        if (vkOk) vkNote = " (eksperimental)";
        else if (!MkxpRenderer.vulkanDeviceSupported(this)) vkNote = " - HP tidak mendukung";
        else vkNote = " - library ANGLE belum disertakan";
        vk.setText("Vulkan via ANGLE" + vkNote);
        vk.setEnabled(vkOk);
        rg.addView(gl);
        rg.addView(vk);
        rg.check("vulkan".equals(prefs.getRenderer()) && vkOk ? 2 : 1);
        rg.setOnCheckedChangeListener((group, id) -> prefs.setRenderer(id == 2 ? "vulkan" : "gles"));
        add(rg);

        // ---- vGamepad
        add(heading("vGamepad"));
        add(toggle("Tampilkan vGamepad", prefs.isVpadEnabled(), (b, c) -> prefs.setVpadEnabled(c)));
        add(toggle("D-Pad diagonal (8 arah)", prefs.isDiagonal(), (b, c) -> prefs.setDiagonal(c)));
        slider("Opasitas %", 10, 100, prefs.getOpacity(), v -> prefs.setOpacity(v));
        slider("Ukuran %", 50, 150, prefs.getScale(), v -> prefs.setScale(v));

        // ---- Gamepad
        add(heading("Gamepad"));
        Button bind = new Button(this);
        bind.setText("Gamepad bind");
        bind.setOnClickListener(v -> startActivity(new Intent(this, BindActivity.class)));
        add(bind);

        // ---- Main
        Button play = new Button(this);
        play.setText("MAIN");
        play.setTextSize(20f);
        play.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(32);
        root.addView(play, lp);
    }
}

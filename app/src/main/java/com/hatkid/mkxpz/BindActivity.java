package com.hatkid.mkxpz;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Map;

/** Gamepad bind: pilih aksi RGSS, lalu tekan tombol pada gamepad fisik. */
public class BindActivity extends Activity
{
    private static final String[] NAMES = {
        "Atas", "Bawah", "Kiri", "Kanan",
        "A (Z)", "B (X)", "C (C)", "X (A)", "Y (S)", "Z (D)", "L (Q)", "R (W)",
        "CTRL", "ALT", "SHIFT"
    };
    private static final int[] TARGETS = {
        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_Z, KeyEvent.KEYCODE_X, KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_S,
        KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_W,
        KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_SHIFT_LEFT
    };

    private MkxpPrefs prefs;
    private Map<Integer, Integer> binds;
    private LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        prefs = new MkxpPrefs(this);
        binds = prefs.loadBinds();

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        list.setPadding(pad, pad, pad, pad);
        sv.addView(list);
        setContentView(sv);
        render();
    }

    private String keyName(int code)
    {
        return KeyEvent.keyCodeToString(code).replace("KEYCODE_", "");
    }

    private void render()
    {
        list.removeAllViews();

        TextView t = new TextView(this);
        t.setText("Gamepad Bind");
        t.setTextSize(22f);
        list.addView(t);

        TextView info = new TextView(this);
        info.setText("Ketuk aksi, lalu tekan tombol di gamepad. Tombol yang belum di-bind tetap memakai pengaturan bawaan mesin.");
        info.setPadding(0, 8, 0, 24);
        list.addView(info);

        for (int i = 0; i < TARGETS.length; i++) {
            final int target = TARGETS[i];
            StringBuilder keys = new StringBuilder();
            for (Map.Entry<Integer, Integer> e : binds.entrySet()) {
                if (e.getValue() == target) {
                    if (keys.length() > 0) keys.append(", ");
                    keys.append(keyName(e.getKey()));
                }
            }
            Button b = new Button(this);
            b.setAllCaps(false);
            b.setText(NAMES[i] + ":  " + (keys.length() == 0 ? "-" : keys.toString()));
            final String name = NAMES[i];
            b.setOnClickListener(v -> capture(name, target));
            list.addView(b, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        Button reset = new Button(this);
        reset.setText("Reset semua bind");
        reset.setOnClickListener(v -> {
            prefs.resetBinds();
            binds = prefs.loadBinds();
            render();
        });
        list.addView(reset);

        Button done = new Button(this);
        done.setText("Selesai");
        done.setOnClickListener(v -> finish());
        list.addView(done);
    }

    private void capture(String name, final int target)
    {
        AlertDialog dlg = new AlertDialog.Builder(this)
            .setTitle(name)
            .setMessage("Tekan tombol di gamepad...\n(Back = batal)")
            .setNeutralButton("Kosongkan", (d, w) -> {
                java.util.Iterator<Map.Entry<Integer, Integer>> it = binds.entrySet().iterator();
                while (it.hasNext()) {
                    if (it.next().getValue() == target) it.remove();
                }
                prefs.saveBinds(binds);
                render();
            })
            .create();

        dlg.setOnKeyListener((d, code, ev) -> {
            if (ev.getAction() != KeyEvent.ACTION_DOWN || ev.getRepeatCount() != 0) return true;
            if (code == KeyEvent.KEYCODE_BACK) {
                d.dismiss();
                return true;
            }
            binds.put(code, target);   // satu tombol fisik hanya untuk satu aksi
            prefs.saveBinds(binds);
            d.dismiss();
            render();
            return true;
        });
        dlg.show();
    }
}

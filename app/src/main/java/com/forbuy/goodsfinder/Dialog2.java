package com.forbuy.goodsfinder;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatDelegate;

public class Dialog2 extends DialogFragment implements DialogInterface.OnClickListener, OnClickListener {

    SharedPreferences mSettings;
    public static final String APP_PREFERENCES = "searches";



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.theme));
        View v = inflater.inflate(R.layout.dialog, null);
        v.findViewById(R.id.btnYes).setOnClickListener(this);
        v.findViewById(R.id.btnNo).setOnClickListener(this);

        return v;
    }

    public void onClick(View v) {
        if (((Button) v).getText().equals(getString(R.string.no))){
        } else if (((Button) v).getText().equals(getString(R.string.yes))){
            mSettings = getContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = mSettings.edit();

            editor.putString("theme", "light");
            editor.apply();
            editor.commit();
            Log.d("SHAR", mSettings.getString("theme", ""));

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        dismiss();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
}

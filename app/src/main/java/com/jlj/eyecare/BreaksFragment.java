package com.jlj.eyecare;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jlj.eyecare.databinding.FragmentBreaksBinding;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;


public class BreaksFragment extends Fragment {

    private static final String TAG = "BreaksFragment";
    private FragmentBreaksBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBreaksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = getActivity().getSharedPreferences(PrefConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (!preferences.contains(PrefConstants.EVERY_DURATION_KEY)) {
            editor.putLong(PrefConstants.EVERY_DURATION_KEY, 1200L /* 20 min*/);
            editor.apply();
        }


        if (!preferences.contains(PrefConstants.BREAKS_KEY)) {
            editor.putLong(PrefConstants.BREAKS_KEY, 20L /* 20 min*/);
            editor.apply();
        }

        long everySeconds = preferences.getLong(PrefConstants.EVERY_DURATION_KEY, 1200L);
        binding.everyTimeBtn.setText(FormattingUtils.formatSeconds(everySeconds));

        long breakSeconds = preferences.getLong(PrefConstants.BREAKS_KEY, 20L);
        binding.timeBreakBtn.setText(FormattingUtils.formatSeconds(breakSeconds));


        binding.everyTimeBtn.setOnClickListener(v -> {
            TimeDurationPickerDialog dialog = new TimeDurationPickerDialog(getContext(), (__, duration) -> {
                long secondsNew = duration / 1000; // Convert ms to s
                editor.putLong(PrefConstants.EVERY_DURATION_KEY, secondsNew);

                binding.everyTimeBtn.setText(FormattingUtils.formatSeconds(secondsNew));
                editor.apply();
            }, 1200L);
            dialog.show();
        });

        binding.timeBreakBtn.setOnClickListener(v -> {
            TimeDurationPickerDialog dialog = new TimeDurationPickerDialog(getContext(), (__, duration) -> {
                long secondsNew = duration / 1000;
                editor.putLong(PrefConstants.BREAKS_KEY, secondsNew);
                binding.timeBreakBtn.setText(FormattingUtils.formatSeconds(secondsNew));
                editor.apply();
            }, 1200L);
            dialog.show();
        });



    }



    public interface PrefConstants {
        String PREF_NAME = "BreakPrefs";

        String EVERY_DURATION_KEY = "EveryTime"; // Pref for storing time for every time
        String BREAKS_KEY = "Breaks";
    }
}
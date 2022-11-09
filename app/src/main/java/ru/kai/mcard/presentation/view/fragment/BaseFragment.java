/**
 * Copyright (C) 2014 android10.org. All rights reserved.
 *
 * @author Fernando Cejas (the android10 coder)
 */
package ru.kai.mcard.presentation.view.fragment;

import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.squareup.leakcanary.RefWatcher;

import ru.kai.mcard.BuildConfig;
import ru.kai.mcard.Constants;
import ru.kai.mcard.MCardApplication;

/**
 * Base {@link android.app.Fragment} class for every fragment in this application.
 */
public abstract class BaseFragment extends Fragment {
  /**
   * Shows a {@link android.widget.Toast} message.
   *
   * @param message An string representing a message to be shown.
   */
  protected void showToastMessage(String message) {
    //Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    Snackbar.make(getActivity().getWindow().getDecorView(), message, Snackbar.LENGTH_LONG).show();
  }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // LeakCanary BuildConfig.DEBUG
        if (Constants.DEBUG_MODE) {
            RefWatcher refWatcher = MCardApplication.getRefWatcher(getActivity());
            refWatcher.watch(this);
        }

    }
}

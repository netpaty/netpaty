package com.netparty.fragments;

import android.app.Service;
import android.support.v4.app.Fragment;

/**
 * Created by Valentin on 26.08.2014.
 */
public abstract class AbstractFragment extends Fragment {
    public abstract void onServiceConnected(Service service);
}

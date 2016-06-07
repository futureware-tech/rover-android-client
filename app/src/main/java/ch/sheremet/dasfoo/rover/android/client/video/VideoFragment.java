package ch.sheremet.dasfoo.rover.android.client.video;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.sheremet.dasfoo.rover.android.client.R;

/**
 * Created by Katarina Sheremet on 6/7/16 12:00 PM.
 */
public class VideoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    public void startVideo(View view) {
        //Not implemented yet
        //Todo: add button implementation
    }

    public void stopVideo(View view) {
        //Not implemented yet
        //Todo: add button implementation
    }
}

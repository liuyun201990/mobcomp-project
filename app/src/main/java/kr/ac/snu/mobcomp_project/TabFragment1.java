package kr.ac.snu.mobcomp_project;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import kr.ac.snu.mobcomp_project.afterdetection.CallManager;
import kr.ac.snu.mobcomp_project.component.AccelerometerListener;
import kr.ac.snu.mobcomp_project.component.LocationMonitor;

public class TabFragment1 extends Fragment
{
    //Calling
    private Button button;
    private final int CALL_PHONE_PERMISSIONS = 1;
    private int prev_state;
    private CallManager mCallManager;

    //Sensor
    private AccelerometerListener mAccelerometerListener;
    //View
    ConstraintLayout layout;

    //Background thread for inference
    private int mInterval = 1000;
    private Handler mHandler;
    Runnable mRunnable;

    public TabFragment1()
    {

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load components
        mAccelerometerListener = new AccelerometerListener(this);
        ((MainActivity)getActivity()).mLocationMonitor = new LocationMonitor(getActivity(), savedInstanceState,this); // Why
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void updateValue(float acceleration, float[] gravity){
        TextView txtacc = (TextView) layout.findViewById(R.id.acceleration);
        TextView txtgravity = (TextView) layout.findViewById(R.id.gravity);
        if(txtacc != null) {
            txtacc.setText(String.format("Acceleration | %.6f", acceleration));
        }
        if(txtgravity != null) {
            txtgravity.setText(String.format("Gravity | %.6f %.6f %.6f", gravity[0],gravity[1],gravity[2]));
        }
    }
    public void updateLocationValue(double latitude, double longitude, float speed){
        TextView txtgps = (TextView) layout.findViewById(R.id.gps_location);
        if(txtgps != null){
            System.out.println(latitude);
            Log.d("Fragment1", "latitude: " + String.valueOf(latitude) + "   longitude: " + String.valueOf(longitude) + "   speed: " + String.valueOf(speed) );
            txtgps.setText(String.format("GPS | %.6f , %.6f , %.6f", latitude, longitude, speed));
            //txtgps.setText(String.format("Original Value a b c d e f g", (float)latitude));
        }
        else{
            System.out.println("Cannot find txtgps");
        }
    }

    @Override
    public void onResume() {
        mAccelerometerListener.onThreadResume();
        ((MainActivity)getActivity()).mLocationMonitor.onResume();
        // load inference task
        mHandler = new Handler();
        mRunnable = new DrowsyDetector(getActivity(),mHandler,mAccelerometerListener);
        mRunnable.run();
        super.onResume();

    }

    @Override
    public void onPause() {
        mAccelerometerListener.onThreadPause();
        ((MainActivity)getActivity()).mLocationMonitor.onPause();
        // remove inference task
        ((DrowsyDetector)mRunnable).close();
        mHandler.removeCallbacks(mRunnable);
        super.onPause();

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (ConstraintLayout)inflater.inflate(R.layout.tab_fragment_1, container, false);

        // Designated Call managing part
        mCallManager = new CallManager();
        button = layout.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                mCallManager.checkPermissionandCall(TabFragment1.this, arg0); // Permission Check
            }
        });
        mCallManager.RedialListen((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE)); // Redial check
        return layout;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) { // Permisson check callback
        switch (requestCode) {
            case CALL_PHONE_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permission Granted
                    if(mCallManager != null){
                        mCallManager.start_call(this);
                    }
                } else {
                }
                return;
            }

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        ((MainActivity)getActivity()).mLocationMonitor.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
}
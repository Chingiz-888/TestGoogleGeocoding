package baydevgroup.net.testgooglegeocoding.data.manager;

import android.content.SharedPreferences;

import baydevgroup.net.testgooglegeocoding.utils.TestGooglecodingApplication;

/**
 * Created by cingiz-mac on 21.09.17.
 */


public class PreferencesManager {

    private SharedPreferences mSharedPreferences;

    public PreferencesManager() {
        this.mSharedPreferences = TestGooglecodingApplication.getSharedPreferences();
    }

    public SharedPreferences getSharedPreferences(){
        return mSharedPreferences;
    }


}
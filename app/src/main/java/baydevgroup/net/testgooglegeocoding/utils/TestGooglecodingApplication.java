package baydevgroup.net.testgooglegeocoding.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import timber.log.Timber;

/**
 * Created by cingiz-mac on 21.09.17.
 */

public class TestGooglecodingApplication extends Application {

    public static SharedPreferences sSharedPreferences;
    public static Context sContext;


    @Override
    public void onCreate() {
        super.onCreate();

        // включаем логи через Timber
        if (AppConfig.DEBUG == true) {
            Timber.plant(new Timber.DebugTree());
        }


        sContext = this.getApplicationContext();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }


    // фунция получения инстанса SharedPrefernces для работы с LocalStorage
    // SharedPrefernces - это аналог UserDefaults в iOS
    public static SharedPreferences getSharedPreferences() {
        return sSharedPreferences;
    }

    // функция для возврата контекста - очень часто будет нужно получать Context в тех
    // или иных местах приложения - и тут на выручку придет этот статический метод
    public static Context getContext() {
        return sContext;
    }








}
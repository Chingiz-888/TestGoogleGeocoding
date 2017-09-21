package baydevgroup.net.testgooglegeocoding.data.manager;

/**
 * Created by cingiz-mac on 21.09.17.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import baydevgroup.net.testgooglegeocoding.data.network.RestService;
import baydevgroup.net.testgooglegeocoding.data.network.ServiceGenerator;
import baydevgroup.net.testgooglegeocoding.data.network.response.ResponseForReverseGeocoding;
import baydevgroup.net.testgooglegeocoding.utils.AppConfig;
import baydevgroup.net.testgooglegeocoding.utils.TestGooglecodingApplication;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;



import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import timber.log.Timber;


import org.apache.commons.lang3.Range;



/**
 * Created by cingiz-mac on 07.08.17.
 */

public class DataManager  {

    private static DataManager INSTANCE = null;

    private Context mContext;
    private PreferencesManager mPreferencesManager;
    static RestService mRestService;

    // для запоминания, какие дома были выбраны

    public DataManager() {
        this.mPreferencesManager = new PreferencesManager();
        this.mContext = TestGooglecodingApplication.getContext();
        this.mRestService = ServiceGenerator.createService(RestService.class);
    }

    public static DataManager getInstance(){
        if(INSTANCE==null) {
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    public PreferencesManager getPreferencesManager(){
        return mPreferencesManager;
    }

    public Context getContext(){
        return mContext;
    }




    //=== получение описания ошибки при 400ом коде, Bad Request ====
    public String getErrorDescription(Response response){

        // На случай, если сообшение об ошибке будет в JSON под именем error_message

        String desc = "";
        try {
            JSONObject jObjError = new JSONObject(response.errorBody().string());
            if ( jObjError.has("description") )
                desc                 =  jObjError.getString("description");
            else if( jObjError.has("error_message") )
                desc                 =  jObjError.getString("error_message");
        } catch (final JSONException e) {
            Timber.d("== JSONException: '%s'", e.getMessage());
        } catch (final IOException e) {
            Timber.d("== IOException: '%s'", e.getMessage());
        }

        return desc;
    }




    //========= ЗАПРОС НА ОБРАТНОЕ ГЕОКОДИРОВАНИЕ НА GOOGLE ==================
    public Promise<ResponseForReverseGeocoding, String, Void> getReverseGeocoding(String dynamicUrl) {

        // заводим deferred объект
        final Deferred<ResponseForReverseGeocoding, String, Void> deferred = new DeferredObject<>();

        Call <ResponseForReverseGeocoding> call = mRestService.getReverseGeocoding(dynamicUrl);
        call.enqueue(new Callback<ResponseForReverseGeocoding>() {
            @Override
            public void onResponse(Call<ResponseForReverseGeocoding> call, Response<ResponseForReverseGeocoding> response) {

                // Если Google'у что-то не понравилось, то он возвращает body == null
                // Потом и сначала провеояем

                if( response.body() != null )
                {//----- если сервер вернул ответ -----------------
                    // А затем мы смотрим на статус. Он должен быть = "OK" (примеры неудачного статуса "REQUEST_DENIED"
                    // Также смотрим, что главное results массив не пустой. Значит удалось нечто разпознать

                    String status = response.body().getStatus();
                    if ( TextUtils.equals(status, "OK") ) { //----  если status == 'ok'

                        String msg    = String.format("Запрос на  обратное геокодирование прошел успешно." );
                        if (AppConfig.DEBUG)  Timber.d(msg);


                        // Отдаем массив как есть, а там уже будем смотреть
                        // не пустой ли он
                        deferred.resolve(  response.body()  ); // разрешение промиса по схеме resolve(), отдаем  List<ResponseForQNAList.Qna>
                    }//----  если status == 'ok'
                    else {
                        final String msg = String.format("Ошибка. Google сервер ответил %s", status);
                        deferred.reject(msg);
                    }
                }//----- если сервер верунл ответ -----------------
                else {
                    String errDesc          = DataManager.getInstance().getErrorDescription( response );
                    deferred.reject(errDesc);      // разрешение промиса по схеме reject(),  отдаем  String объект
                }
            }

            @Override
            public void onFailure(Call<ResponseForReverseGeocoding> call, Throwable t) {
                final String msg = String.format("Произошла ошибка при сетевом запросе. Попробуйте еще раз");
                deferred.reject(msg);
            }
        });

        return deferred.promise();
    }

    //========================================================================

    //************************************************************************************************
    //************************************************************************************************
    //************************************************************************************************


























    /**
     * Проверям есть ли соединение с интернетом
     * и это у нас будет метод инстанса, раз у нас DataManager это сингтон
     */
    public boolean connectionAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return null != activeNetwork && activeNetwork.isConnectedOrConnecting();
    }

    public static  RestService getApi() {
        return  mRestService;
    }



    //****** ПРОВЕРКА ЧТО КОД ЛЕЖИТ В УСПЕШНОМ ДИАПАЗОНЕ **********************************
    private Boolean isSuccessful(int code) {
        final Range<Integer> myRange = Range.between(200, 299);
        if (myRange.contains(code)){
            return true;
        } else {
            return false;
        }

    }

}
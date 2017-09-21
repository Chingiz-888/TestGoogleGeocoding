package baydevgroup.net.testgooglegeocoding.data.network;

/**
 * Created by cingiz-mac on 21.09.17.
 */


import baydevgroup.net.testgooglegeocoding.data.network.response.ResponseForReverseGeocoding;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;
import java.util.List;


import retrofit2.http.Url;

/**
 * Created by cingiz-mac on 07.08.17.
 */



public interface RestService {






    //========= ЗАПРОС НА ОБРАТНОЕ ГЕОКОДИРОВАНИЕ НА GOOGLE ==================
    @GET
    Call<ResponseForReverseGeocoding> getReverseGeocoding(
            @Url String url
    );
    //========================================================================



}
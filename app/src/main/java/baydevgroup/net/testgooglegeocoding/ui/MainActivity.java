package baydevgroup.net.testgooglegeocoding.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.Locale;

import baydevgroup.net.testgooglegeocoding.R;
import baydevgroup.net.testgooglegeocoding.data.manager.DataManager;
import baydevgroup.net.testgooglegeocoding.data.network.response.ResponseForReverseGeocoding;
import baydevgroup.net.testgooglegeocoding.ui.fragments.MyMapFragment;
import baydevgroup.net.testgooglegeocoding.utils.AppConfig;
import baydevgroup.net.testgooglegeocoding.utils.TestGooglecodingApplication;
import timber.log.Timber;



public class MainActivity extends AppCompatActivity
                            implements LocationListener,
                            GoogleApiClient.ConnectionCallbacks,
                            OnMapReadyCallback,
                            GoogleMap.OnCameraMoveListener,
                            GoogleMap.OnCameraIdleListener {



        //=== ПЕРЕМЕННЫЕ, ПОСВЯЩЕННЫЕ КАРТЕ и РАБОТЕ с GOOGLE PLAY SERVICES
        private GoogleMap         mMap;             // карта
        SupportMapFragment mapFragment;      // "суппортный" фрагмент
        private GoogleApiClient   mGoogleApiClient;


        //==== МЕСТОПОЛОЖЕНИЕ ЮЗЕРА
        private LatLng mCurrentPosition;
        private Location mLastLocation;    // где его видели в последний раз


        //==== ДЛЯ НАВИГАЦИИ
        public static final LatLng MOSCOW       = new LatLng(55.752115, 37.614635);  // Это Москва
        //// private static final String TAG_PLACE   = "place string";                    // для интентов это
        //// private static final String TAG_MESSAGE = "message string";
        //// private MyViewManager viewManager       = new MyViewManager();


        //===== НАСТРОЙКИ КАРТЫ
        private float zoomCurrentLevel;  // для того, чтобы ловить события изменения зума


        //===== СЛУЖЕБНОЕ
        private boolean           mIsLowerThanMarshmellow;


        //====== ИНТЕРФЕЙС
        private TextView addressLbl;


        @Override
        protected void onCreate( Bundle savedInstanceState ) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            addressLbl = (TextView) findViewById( R.id.addressLbl );

            // Получаем локатор
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Проверяю кое-что
            // check if this device has android version lower, than 23.
            mIsLowerThanMarshmellow = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;

            // Either it's not a marshmallow device or permission granted (v23-style)
            if (mIsLowerThanMarshmellow || permissionsGranted()) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            } // Either it's not a marshmallow device or permission granted (v23-style)


            // Запускаем Google API
            buildGoogleApiClient();

            // Иницирую карту
            initMap();

            // Проверяю, есть ли разрешения
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            AppConfig.REQUEST_ACCESS_FINE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }//======== end of onCreate() ===================





        //============ ПРИВАТНАЯ ФУНКЦИЯ КОЕ-ЧТО ПРОВЕРЯЕТ=================
        // вернет true если все можно и на COARSE и на FINE Location
        private boolean permissionsGranted() {
            return ContextCompat.checkSelfPermission( this,
                    Manifest.permission.ACCESS_FINE_LOCATION )   == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission( this,
                    Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED;
        }


        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               String permissions[], int[] grantResults) {
            switch (requestCode) {
                case AppConfig.REQUEST_ACCESS_FINE_LOCATION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // Иницирую карту
                        initMap();

                    } else {

                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return;
                }

                // other 'case' lines to check for other
                // permissions this app might request
            }
        }



        private void initMap(){
            FragmentManager fm = getSupportFragmentManager();
            SupportMapFragment fragment =
                    (SupportMapFragment) fm.findFragmentById(R.id.fragmentContainer);


            if(fragment == null) {
                fragment    =  new MyMapFragment();
                mapFragment = fragment;        // ловим ссылку на фрагмент на переменную класса
                fm.beginTransaction()
                        .add(R.id.fragmentContainer, fragment)
                        .commit();
                /**
                 *  Google обязывает юзать именно асинхронный вызов карты
                 *   "A GoogleMap must be acquired using getMapAsync(OnMapReadyCallback)."
                 *   ссылка: https://developers.google.com/android/reference/com/google/android/gms/maps/MapFragment
                 *
                 *   Чтобы наша активити стала слушателем callback слушателем этого вызова
                 *   нужно заимплементить OnMapReadyCallback и библиотеку
                 *   import com.google.android.gms.maps.OnMapReadyCallback
                 */
                fragment.getMapAsync(this);
            }
        }




        //=============  ПОЙМАЛИ ПОЯВЛЕНИЕ КАРТЫ и ТЕПЕРЬ НАСТРАИВАЕМ ЕЕ =======================================
        @Override
        public void onMapReady(GoogleMap map) {
            // Инициализируем переменную класса
            mMap = map;

            // Для теста ставим маркер
            mMap.addMarker(new MarkerOptions().position(new LatLng(52, 34)).title("Marker"));

            // настравиваю вид карты
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setRotateGesturesEnabled(  false);
            mMap.getUiSettings().setTiltGesturesEnabled(    false);
            mMap.getUiSettings().setZoomControlsEnabled(    true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);     // для отображение кнопки где я

            // Дополнительная настройка
            Resources r = getResources();
            //no need in exact values, just add a coeff
            int bottomPadding = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 0, r.getDisplayMetrics() );    // 136
            int leftPadding   = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 30,  r.getDisplayMetrics() );
            mMap.setPadding( leftPadding, 0, 0, bottomPadding );

            // Дополнительные настройки вида карты
            // это дает "подъем" кнопок к верху
            hackZoomPosition();

            /**
             * Перемещаем карту на нужную позицию и с задержкой изменения зума
             * Проверяем на нуль mCurrentPosition
             *
             * анимация зума идет на уменьшение, то есть по умолчанию перемещение к зуму идет с максимальным масштабом
             */
            LatLng myPosition;
            if( mCurrentPosition == null ) { myPosition = MOSCOW;           }
            else                           { myPosition = mCurrentPosition; }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 700, null);
                }
            }, 1300);

            // MARK: ВОТ ОНИ СЛУШАТЕЛИ НА КЛИКИ ПО МАРКЕРАМ И КАРТЕ
            //// mMap.setOnMarkerClickListener ( viewManager.provideOnMarkerClickListener()  );
            //// mMap.setOnMarkerDragListener  ( provideOnMarkerDragListener()   );  //TODO: событие драга
            //// mMap.setOnMapClickListener    ( viewManager.provideOnMapClickListener()     );
            //// mMap.setOnCameraChangeListener( viewManager.provideOnCameraChangeListener() );  //TODO: на изменения зума
            mMap.setOnCameraMoveListener(this);
            mMap.setOnCameraIdleListener(this);



            //устанавливаю зум
            zoomCurrentLevel = mMap.getCameraPosition().zoom;

            //Начинаем загрузку данных по объектам на карте
            //// load();
        }


        //========== ЗАПУСК GoogleAPIClient ==============================
        protected void buildGoogleApiClient() {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        //========== КОЛБЕКИ от GoogleAPIClient ==========================
        @Override
        public void onLocationChanged(Location location) {  /*NOP*/   }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { /*NOP*/ }

        @Override
        public void onProviderEnabled(String provider) { /*NOP*/  }

        @Override
        public void onProviderDisabled(String provider) { /*NOP*/  }

        @Override
        public void onConnected(Bundle connectionHint) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        @Override
        public void onConnectionSuspended(int i) {  /*NOP*/  }
        //========== / КОЛБЕКИ от GoogleAPIClient =======================







        private void hackZoomPosition(){
            // Find ZoomControl view
            final View mapView = mapFragment.getView();
            final View zoomControls = mapView.findViewById(0x1);
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if( AppConfig.DEBUG )
                        Timber.d( String.format("ZOOM, значение1='%s', значение2='%s'",
                                String.valueOf (zoomControls != null ),
                                String.valueOf(zoomControls != null && zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) )     );
                    if (zoomControls != null && zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                        // ZoomControl is inside of RelativeLayout
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) zoomControls.getLayoutParams();

                        // Align it to  - not working))
                        //params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                                getResources().getDisplayMetrics());

                        int bottomMargin = (   mapView.getMeasuredHeight() - (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 1,
                                getResources().getDisplayMetrics() )   ) / 2 - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30,
                                getResources().getDisplayMetrics());
                        params.setMargins(margin, margin, margin, bottomMargin);
                        zoomControls.setLayoutParams(params);
                    }
                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }



        //======= СОБЫТИЕ ДРАГА НА МАРКЕРЕ ========================
        public GoogleMap.OnMarkerDragListener provideOnMarkerDragListener(){
            return new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker arg0) {
                    // TODO Auto-generated method stub
                    if( AppConfig.DEBUG )
                        Timber.d( "=== onMarkerDragStart --- " + arg0.getPosition().latitude + "..." +
                                arg0.getPosition().longitude  );
                }

                @SuppressWarnings("unchecked")
                @Override
                public void onMarkerDragEnd(Marker arg0) {
                    // TODO Auto-generated method stub
                    if( AppConfig.DEBUG )
                        Timber.d( "=== onMarkerDragEnd --- onMarkerDragEnd..." + arg0.getPosition().latitude + "..." +
                                arg0.getPosition().longitude );

                    // map.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
                }

                @Override
                public void onMarkerDrag(Marker arg0) {

                    if( AppConfig.DEBUG )
                        Timber.d( "=== onMarkerDrag --- " + arg0.getPosition().latitude + "..." +
                                arg0.getPosition().longitude );

                }
            };

        }//======= СОБЫТИЕ ДРАГА НА МАРКЕРЕ ========================




        @Override
        public void onCameraMove() {
            // Timber.d( "=== The camera is moving " );
        }


        @Override
        public void onCameraIdle() {
            //******* СОБЫТИЕ ОКОНЧАНИЯ ДВИЖЕНИЯ КАРТЫ, НА НЕМ ОПРЕДЕЛЯЕМ КООРДИНАТЫ И STRING АДРЕС ЦЕНТРА ЭКРАНА ********
            Timber.d( "=== The camera has stopped moving " );
            LatLng center  = getLatLngFromCenter();
            Timber.d("==== Координаты центра %s",  center);

            // https://maps.googleapis.com/maps/api/geocode/json?latlng=55.730114813065406,37.6121620461344&key=___PASTE_HERE_YOUR_GOOGLE_API_KEY____
            final String googleBaseUrl     = "https://maps.googleapis.com/maps/api/geocode";
            final String googleApiEndpoint = "/json";
            final String googleApiKey      = "AIzaSyARML1z5NnHkvMjTHczNh4XVN4k_lXdoDA";

            // Внимание! Тут не случайно добавлен параметр Locale.US, дабы double числа кодировались с точкой, а не запятой
            // Иначе гугл будет ругаться
            // Locale.US
            final String reagyUrl = String.format( Locale.US, "%s%s?latlng=%f,%f&key=%s",
                    googleBaseUrl,
                    googleApiEndpoint,
                    center.latitude,
                    center.longitude,
                    googleApiKey    );

            DataManager.getInstance().getReverseGeocoding(reagyUrl)
                    .done(new DoneCallback<ResponseForReverseGeocoding>() {
                        @Override
                        public void onDone(ResponseForReverseGeocoding result) {
                            Timber.d("Успешно получили результат по запросу на обратное геокодирование. Ответ от сетевой функции %s", result);
                            if( result.getResults().size()  > 0 ) {
                                String address = result.getResults().get(0).getFormattedAddress().toString();

                                addressLbl.setText(address);
                            } else {
                                addressLbl.setText("Выберете нужную позицию");
                            }

                        }
                    })
                    .fail(new FailCallback<String>() {
                        @Override
                        public void onFail(String result) {
                            Timber.d("Неуспех в попытке обратного геокодирования. Ответ от сетевой функции %s", result);

                            addressLbl.setText("Выберете нужную позицию");
                        }
                    });
        }





        private LatLng getLatLngFromCenter() {

            // Получаем данные по разрешению экрана
            WindowManager wm = (WindowManager) TestGooglecodingApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            // Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;


            int xCenter = width  / 2;
            int yCenter = height / 2;

            Timber.d("==== Размеры экрана %d на %d, а экранные коордианты центра %d, %d",
                    width, height,
                    xCenter, yCenter );

            Point point          = new Point(xCenter, yCenter);
            LatLng centerLatLng  = mMap.getProjection().fromScreenLocation(point);
            return centerLatLng;

        }



    }

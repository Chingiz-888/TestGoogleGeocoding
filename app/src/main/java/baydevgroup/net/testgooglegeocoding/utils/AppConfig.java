package baydevgroup.net.testgooglegeocoding.utils;

/**
 * Created by cingiz-mac on 21.09.17.
 */

public interface AppConfig {

    // Для включения/выключения режима DEBUG'а
    final boolean DEBUG                                 = true;

    final String BASE_URL                              = "https://www.google.com/";

    // для отслеживания запроса на разрешение использования камеры
    final int REQUEST_CAMERA_PERMISSION                 = 1;
    final int REQUEST_READ_PHONE_STATE_PERMISSION       = 2;
    final int REQUEST_ACCESS_FINE_LOCATION              = 888;
}

## Демонстрация работы обратного геокодирования (получения строкового адреса на основе известного LatLng) на основе использования сервисов Google

### Проект интересен тем, что несколько технологий и моментов задействует:



1) Для обращения и парсинга JSON используется Retrofit 2
И демонстрируется, как в нем можно задействовать полностью динамические URL's (аннотация @Url). То есть, например, вы в проекте можете обращаться к API endpoit'ам не только одного сервера (заданного BASE_URL), но и некого другого.



2) Были поставлены слушатели на события перемещения (drag'а) карты.
https://developers.google.com/maps/documentation/android-api/events

И событие onCameraIdle как раз вызывается когда пользователь прекращает движение карты.



3) JSON по разрешению адреса получается по GET запросу и мы создаем url-строку через метод String.format
НО ВНИМАНИЕ!!! По дефолту эта Java функция double переменные преобразует в строки с запятыми! То есть переменная double xxx = 23.888 будет преобразована в строку "23,888". Это неприменимо и Google сервер тут же ответит, что LatLng неформатный
РЕШЕНИЕ - прямо указать так называемую локаль

```java
    // https://maps.googleapis.com/maps/api/geocode/json?latlng=55.730114813065406,37.6121620461344&key=___PASTE_HERE_YOUR_GOOGLE_API_KEY____
    final String googleBaseUrl     = "https://maps.googleapis.com/maps/api/geocode";
    final String googleApiEndpoint = "/json";
    final String googleApiKey      = "___ENTER_HERE_YOUR_API_KEY____";

    // Внимание! Тут не случайно добавлен параметр Locale.US, дабы double числа кодировались с точкой, а не запятой
    // Иначе гугл будет ругаться
    // Locale.US
    final String reagyUrl = String.format( Locale.US, "%s%s?latlng=%f,%f&key=%s",
                                        googleBaseUrl,
                    	        	googleApiEndpoint,
                    			center.latitude,
                    			center.longitude,
                    			googleApiKey    );

```

## Установка

Склонируйте репозиторий и в AndroidManifest.xml поставьте свой API_KEY
Внимание! Вы должны прямо задйествовать функционал обратного геокодирования в консоли Google для вашего приложения
И вам также надо учесть на ограничения на максимальное количество запросов.

![photo_2017-09-21_22-31-31](https://user-images.githubusercontent.com/19972649/30714943-631871ea-9f1d-11e7-9480-eea297b9ef04.jpg)

![im00000age_2017-09-21_15-59-07](https://user-images.githubusercontent.com/19972649/30715313-84cbc890-9f1e-11e7-8a33-8e5edd7d1d8f.png)

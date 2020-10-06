

# XMSLocationManager
<a href="http://developer.android.com/index.html" target="_blank"><img src="https://img.shields.io/badge/platform-android-green.svg"/></a> <a href="https://android-arsenal.com/api?level=21"><img src="https://img.shields.io/badge/API-21%2B-green.svg?style=flat" border="0" alt="API"></a> <a href="https://opensource.org/licenses/Apache-2.0" target="_blank"><img src="https://img.shields.io/badge/License-Apache_v2.0-blue.svg?style=flat"/></a> [![uses gms](https://img.shields.io/badge/USES-GMS-green.svg)](https://shields.io/) [![uses hms](https://img.shields.io/badge/USES-HMS-red.svg)](https://shields.io/)


### XMS?
#### GoogleMobileServices + HuaweiMobileSerice = (Google + Huawei) MobileServices = X(MobileServices) = XMS

XMSLocationManager will make it easy to get user's location using Google Location where GooglePlayServices are available, or use Huawei location kit where HuaweiMobileServices are available

<b>With this library you just need to provide a Configuration object with your requirements, and you will receive a location or a fail reason with all the stuff are described above handled.</b>

This library requires quite a lot of lifecycle information to handle all the steps between onCreate - onResume - onPause - onDestroy - onActivityResult - onRequestPermissionsResult.
You can simply use one of [LocationBaseActivity][2], [LocationBaseFragment][3], [LocationBaseService][4] or you can manually call those methods as required.

[See the sample application][5] for detailed usage!

## Download
Add jitpack repo to your project level `build.gradle` file:
```groovy
        allprojects {
    		repositories {
    			maven { url 'https://jitpack.io' }
    		}
    	}
```

Add library dependency to your app level `build.gradle` file:

```groovy
dependencies {
     implementation 'com.github.megaacheyounes:XMSLocationManager:2.5.0'
}
```

## Configuration

All those settings below are optional. Use only those you really want to customize. Please do not copy-paste this configuration directly. If you want to use pre-defined configurations, see [Configurations][6].

```java
XMSLocationConfiguration xmsConfiguration = new XMSLocationConfiguration.Builder()
                .keepTracking(false)
                .askForPermission(
                        new PermissionConfiguration.Builder()
                                .permissionProvider(new YourCustomPermissionProvider())
                                .rationaleMessage("Gimme the permission!")
                                .rationaleDialogProvider(new YourCustomDialogProvider())
                                .requiredPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
                                .build()
                )
                //XMS will use google location if GooglePlayServices are available,
                //otherwise it will use huawei location if HuaweiMobileServices
                .useXMS(
                        new XMSConfiguration.Builder()
                                .xmsLocationRequest(
                                        new XMSLocationRequest()
                                                .setPriority(XMSLocationRequest.PRIORITY_HIGH_ACCURACY)
                                                .setInterval(10000) //10 seconds
                                                .setFastestInterval(2000) //2 seconds
                                )
                                .fallbackToDefault(true)
                                .askForXMS(false)
                                .askForSettingsApi(true)
                                .failOnSettingsApiSuspended(false)
                                .ignoreLastKnowLocation(false)
                                .setWaitPeriod(20 * 1000)
                                .build()
                )
                .useDefaultProviders(
                        new DefaultProviderConfiguration.Builder()
                                .requiredTimeInterval(5 * 60 * 1000)
                                .requiredDistanceInterval(0)
                                .acceptableAccuracy(5.0f)
                                .acceptableTimePeriod(5 * 60 * 1000)
                                .gpsMessage("Turn on GPS?")
                                .gpsDialogProvider(new YourCustomDialogProvider())
                                .setWaitPeriod(ProviderType.GPS, 20 * 1000)
                                .setWaitPeriod(ProviderType.NETWORK, 20 * 1000)
                                .build()
                )
                .build();

```

Library is modular enough to let you create your own way for Permission request, Dialog display, or even a whole LocationProvider process. (Custom LocationProvider implementation is described below in LocationManager section)

You can create your own [PermissionProvider][7] implementation and simply set it to [PermissionConfiguration][8], and then library will use your implementation. Your custom PermissionProvider implementation will receive your configuration requirements from PermissionConfiguration object once it's built. If you don't specify any PermissionProvider to PermissionConfiguration [DefaultPermissionProvider][9] will be used. If you don't specify PermissionConfiguration to LocationConfiguration [StubPermissionProvider][10] will be used instead.

You can create your own [DialogProvider][11] implementation to display `rationale message` or `gps request message` to user, and simply set them to required configuration objects. If you don't specify any [SimpleMessageDialogProvider][12] will be used as default.

## LocationManager

Ok, we have our configuration object up to requirements, now we need a manager configured with it.

```java
// XMSLocationManager MUST be initialized with Application context in order to prevent MemoryLeaks
XMSLocationManager xmsLocationManager = new XMSLocationManager.Builder(getApplicationContext())
    .activity(activityInstance) // Only required to ask permission and/or XMSApi - SettingsApi
    .fragment(fragmentInstance) // Only required to ask permission and/or XMSApi - SettingsApi
    .configuration(awesomeConfiguration)
    .locationProvider(new YourCustomLocationProvider())
    .notify(new LocationListener() { ... })
    .build();
```

LocationManager doesn't keep strong reference of your activity **OR** fragment in order not to cause any memory leak. They are required to ask for permission and/or XMSApi - SettingsApi in case they need to be resolved.

You can create your own [LocationProvider][13] implementation and ask library to use it. If you don't set any, library will use [DispatcherLocationProvider][14], which will do all the stuff is described above, as default.

Enough, gimme the location now!

```java
xmsLocationManager.get();
```

Done! Enjoy :)

## Logging

Library has a lot of log implemented, in order to make tracking the process easy, you can simply enable or disable it.
It is highly recommended to disable in release mode.

```java 
XMSLocationManager.enableLog(false);
```

For a more fine tuned logging, you can provide a custom Logger implementation to filter and delegate logs as you need it.

```java
Logger myCustomLoggerImplementation = new MyCustomLoggerImplementation();
XMSLocationManager.setLogger(myCustomLoggerImplementation);
```

## Restrictions
If you are using LocationManager in a
- Fragment, you need to redirect your `onActivityResult` to fragment manually, because GooglePlayServices Api and SettingsApi calls `startActivityForResult` from activity. For the sample implementation please see [SampleFragmentActivity][15].
- Service, you need to have the permission already otherwise library will fail immediately with PermissionDenied error type. Because runtime permissions can be asked only from a fragment or an activity, not from a context. For the sample implementation please see [SampleService][16].

## AndroidManifest

Library requires 3 permission;
 - 2 of them `ACCESS_NETWORK_STATE` and `INTERNET` are not in `Dangerous Permissions` and they are required in order to use Network Provider. So if your configuration doesn't require them, you don't need to define them, otherwise they need to be defined.
 - The other one is `ACCESS_FINE_LOCATION` and it is marked as `Dangerous Permissions`, so you need to define it in Manifest and library will ask runtime permission for that if the application is running on Android M or higher OS  version. If you don't specify in Manifest, library will fail immediately with PermissionDenied when location is required.

```html
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

You might also need to consider information below from [the location guide page.][17]

<blockquote>
<b>Caution:</b> you must declare that your app uses the android.hardware.location.network or android.hardware.location.gps hardware feature in the manifest file, depending on whether your app receives location updates from NETWORK_PROVIDER or from GPS_PROVIDER. If your app receives location information from either of these location provider sources, you need to declare that the app uses these hardware features in your app manifest. On devices running versions prior to Android 5.0 (API 21), requesting the ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission includes an implied request for location hardware features. However, requesting those permissions does not automatically request location hardware features on Android 5.0 (API level 21) and higher.
</blockquote>


## License
THIS LIBRARY IS A BASED ON:  [yayaa/LocationManager](https://github.com/yayaa/LocationManager)
```
Copyright 2020 (c) Younes Megaache

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

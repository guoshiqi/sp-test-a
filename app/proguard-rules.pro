# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/du/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:

-keepattributes *JavascriptInterface*
-keepattributes Signature
-keepattributes *Annotation*
-ignorewarnings
-keep class wendu.spiderandroid.SpiderResponse

#spider
-keepattributes *JavascriptInterface*
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class wendu.spidersdk.JavaScriptBridge {
      public *;
   }
-keepclassmembers class wendu.spidersdk.JavaScriptBridgeForCrossWalk{
      public *;
   }
-keep class wendu.spidersdk.DSpider
-keep class wendu.spidersdk.DSpider.Result
-keep class wendu.spidersdk.CrossWalkInitializer
-keep class wendu.spidersdk.SpiderActivity
-keep class org.xwalk.core.** { *;}
-keep class org.chromium.** { *;}
-keepattributes **
-keep  class  junit.framework.**{*;}
-keep class com.tencent.smtt.**{*;}
-keep class com.tencent.tbs.**{*;}


#retrofit & okhttp
-dontwarn retrofit.**
-dontwarn okio.**
-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class javax.inject.** { *; }
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keep class com.squareup.** { *; }
-keep interface com.squareup.** { *; }
-dontwarn com.squareup.okhttp3.**

-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keep interface retrofit.** { *;}
-dontwarn rx.**
-dontwarn retrofit.**
-dontwarn com.octo.android.robospice.retrofit.RetrofitJackson**
-dontwarn retrofit.appengine.UrlFetchClient
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

#support v4
-dontwarn android.support.v4.app.ActivityCompatHoneycomb
-dontwarn android.support.v4.os.ParcelableCompatCreatorHoneycombMR2
-dontwarn android.support.v4.view.MotionEventCompatEclair
-dontwarn android.support.v4.view.VelocityTrackerCompatHoneycomb
-dontwarn android.support.v4.view.ViewConfigurationCompatFroyo
-dontwarn android.support.v4.view.MenuCompatHoneycomb

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service

-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

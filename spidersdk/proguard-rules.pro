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
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes *JavascriptInterface*
-keepattributes Signature
-keepattributes *Annotation*

-keep class wendu.spidersdk.DSpider
-keep class wendu.spidersdk.DSpider$Result{*;}
-keep class wendu.spidersdk.OnRetryListener{*;}
-keepclassmembers class wendu.spidersdk.JavaScriptBridge {
      public *;
 }
-keep class com.alipayzhima.**{*;}

-keep class com.android.moblie.zmxy.antgroup.creditsdk.**{*;}
-keep class com.antgroup.zmxy.mobile.android.container.**{*;}
-keep class org.json.alipayzhima.**{*;}
##xwalk
#-keep class org.xwalk.core.** { *;}
#-keep class org.chromium.** { *;}
#-keepattributes **
#-keep  class  junit.framework.**{*;}
##x5
#-keep class com.tencent.smtt.**{*;}
#-keep class com.tencent.tbs.**{*;}

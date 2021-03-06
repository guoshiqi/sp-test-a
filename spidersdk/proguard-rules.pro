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
-keepclassmembers class wendu.spidersdk.JavaScriptBridge {
      public *;
   }
-keep class wendu.spidersdk.DSpider{public *;}
-keep class wendu.spidersdk.DSpiderView{public *;}
-keep class wendu.spidersdk.DSpider$Result{*;}
-keep class wendu.spidersdk.IPersistence{*;}
-keep class wendu.spidersdk.OnRetryListener{*;}
-keep class wendu.spidersdk.SpiderEventListener{*;}



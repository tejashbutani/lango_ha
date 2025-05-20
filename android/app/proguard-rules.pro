-keep class com.example.lango_ha**
-keepclassmembers class com.example.lango_ha** {*;}


# Keep the WhiteBoardSpeedup class and its fields
-keep class com.nomivision.sys.WhiteBoardSpeedup { *; }
-keep class com.nomivision.sys.WhiteBoardSpeedup$* { *; }
-keep class com.nomivision.sys.input.** { *; }
-keepclassmembers class com.nomivision.sys.** { *; }

-keep class com.xbh**
-keepclassmembers class com.xbh** {*;}

# Keep all native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
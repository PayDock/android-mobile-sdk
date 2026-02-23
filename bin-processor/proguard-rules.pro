# ProGuard rules for bin-processor module

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep BinDataResponse for serialization
-keep,includedescriptorclasses class com.paydock.binprocessor.data.dto.**$$serializer { *; }
-keepclassmembers class com.paydock.binprocessor.data.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.paydock.binprocessor.data.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# This allows all SDK code to be accessible
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, SourceFile, LineNumberTable
# Keep source file name and line number
-keeppackagenames com.paydock
-keepdirectories
-keep class com.paydock.**{ *; }
# Keep all methods in a specific package and its subpackages
-keepclassmembers class com.paydock.** {
    *;
}
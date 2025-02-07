# Google Drive API
## https://github.com/googleapis/google-api-java-client/blob/main/google-api-client-assembly/proguard-google-api-client.txt
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keep class * extends com.google.api.client.json.GenericJson { *; }
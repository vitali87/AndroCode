# Add project specific ProGuard rules here.
# By default, the flags in this file are applied to ALL build variants.
# See https://developer.android.com/studio/build/shrink-code

# Hilt rules (usually handled by plugin, but good practice to keep)
-keep class * implements dagger.hilt.internal.GeneratedComponent { <init>(); }
-keep class * implements dagger.hilt.internal.GeneratedEntryPoint { <init>(); }
# ... add other rules as needed, especially for reflection or libraries

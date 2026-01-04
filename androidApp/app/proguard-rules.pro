# Copyright (c) 2025 Mohammad Sheraj Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details.

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Rules for Gson ---

# Keep the Link data class (used in ApiService.getLinks) and its members for Gson
-keep class com.example.discover.data.Link { *; }
-keepclassmembers class com.example.discover.data.Link { *; }

# General Gson rules that are good to have.
# These help Gson with reflection, especially if you use @SerializedName or generic types.

# Keep annotations and attributes Gson might need.
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes *Annotation*

# For keeping fields annotated with @SerializedName (you use this in Link for createdAt)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep constructors for classes that Gson needs to instantiate.
# This is often implicitly covered by `-keep class ... { *; }`
# but can be made more explicit if needed for complex scenarios.
# For typical data classes, the above rules are usually sufficient.

# If you use enums with @SerializedName for custom serialized values:
-keepclassmembers enum * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- End of Rules for Gson ---

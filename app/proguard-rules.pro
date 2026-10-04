# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

# Keep classes that are used as a parameter type of methods that are also marked as keep
# to preserve changing those methods' signature.

# after upgrading to gradle 8, stack traces contain "unknown source"
-keepattributes SourceFile,LineNumberTable


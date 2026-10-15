# Reglas ProGuard/R8 para ParFMI.
# El empaquetado de JSON se hace de forma manual (JsonObject), por lo que no
# necesitamos reglas de reflexion para serializadores.

# Conservar nombres de clases de kotlinx.serialization por seguridad.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Eliminar logs de depuracion en release.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

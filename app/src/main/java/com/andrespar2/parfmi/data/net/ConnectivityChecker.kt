package com.andrespar2.parfmi.data.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/** Permite comprobar la conectividad antes de gastar una consulta. */
fun interface ConnectivityChecker {
    fun isOnline(): Boolean

    companion object {
        /** Implementacion para pruebas: siempre conectado. */
        val AlwaysOnline = ConnectivityChecker { true }
    }
}

/** Comprobacion de conectividad con las APIs del sistema. */
class AndroidConnectivityChecker(context: Context) : ConnectivityChecker {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override fun isOnline(): Boolean {
        val manager = connectivityManager ?: return true
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = manager.activeNetwork ?: return false
                val capabilities = manager.getNetworkCapabilities(network) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                @Suppress("DEPRECATION")
                manager.activeNetworkInfo?.isConnected == true
            }
        } catch (e: SecurityException) {
            // Sin permiso para consultar el estado de red: se intenta igualmente.
            true
        }
    }
}

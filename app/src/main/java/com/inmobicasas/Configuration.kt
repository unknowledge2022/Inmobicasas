package com.inmobicasas

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle

class Configuration {

    var environment: String = "debug"
    var urlBase: String = ""
    var defaultZoom: Float = 0F
    var locationUpdateTime: Int = 0
    var locationUpdateDistance: Int = 0

    companion object {

        @JvmStatic
        val tag: String = "Inmobicasas"

        @JvmStatic
        fun create(context: Context): Configuration {
            val env = Configuration()
            try {
                val metaData: Bundle = context.getPackageManager()
                    .getApplicationInfo(
                        context.getPackageName(),
                        PackageManager.GET_META_DATA
                    ).metaData
                env.environment = metaData["com.inmobicasas.ENVIRONMENT"].toString()
                env.urlBase = metaData["com.inmobicasas.URLBASE"].toString()
                env.defaultZoom = metaData["com.inmobicasas.DEFAULTZOOM"].toString().toIntOrDefault().toFloat()
                env.locationUpdateTime = metaData["com.inmobicasas.LOCATIONUPDATETIME"].toString().toIntOrDefault()
                env.locationUpdateDistance = metaData["com.inmobicasas.LOCATIONUPDATEDISTANCE"].toString().toIntOrDefault()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return env
        }
    }

}
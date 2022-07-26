package com.inmobicasas

import java.io.Serializable

data class Location(
    var latitude: Double,
    var longitude: Double,
    var accuracy: Double
) : Serializable

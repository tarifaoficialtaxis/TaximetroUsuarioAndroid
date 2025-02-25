package com.mitarifamitaxi.taximetrousuario.helpers

fun <K, V> MutableMap<K, V>.putIfNotNull(key: K, value: V?) {
    if (value != null) {
        this[key] = value
    }
}
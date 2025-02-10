package com.mitarifamitaxi.taximetrousuario.helpers


fun Double.formatDigits(digits: Int) = "%.${digits}f".format(this)

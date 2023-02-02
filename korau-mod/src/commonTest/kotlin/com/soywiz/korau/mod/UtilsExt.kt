package com.soywiz.korau.mod

import com.soywiz.korio.util.*

val skipIOTest: Boolean get() = OS.isJs || OS.isAndroid
//val skipIOTest: Boolean get() = OS.isAndroid
val doIOTest: Boolean get() = !skipIOTest

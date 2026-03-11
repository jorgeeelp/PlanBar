package com.losjorges.planbar.utils

import java.security.MessageDigest

fun String.toSHA256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
package com.loloof64.chessexercisesorganizer.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun <T> MutableStateFlow<T>.update(
    block: T.(T) -> T
) {
    value = value.run {
        block(this)
    }
}

fun String.stripPgnExtension(): String {
    return if (this.endsWith(".pgn")) {
        this.subSequence(0 until this.lastIndexOf(".pgn")).toString()
    } else this
}

fun String.encodePath(): String {
    return this.replace("/".toRegex(), "#")
}

fun String.decodePath(): String {
    return this.replace("#".toRegex(), "/")
}
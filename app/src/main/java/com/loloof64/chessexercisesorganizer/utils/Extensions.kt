package com.loloof64.chessexercisesorganizer.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun <T> MutableStateFlow<T>.update(
    block: T.(T) -> T
)  {
    value = value.run{
        block(this)
    }
}
package com.loloof64.chessexercisesorganizer.ui.components

import androidx.compose.runtime.Composable
import java.lang.reflect.Modifier

sealed class MovesNavigatorElement(text: String)
data class MoveNumber(val text: String) : MovesNavigatorElement(text)
data class HalfMoveSAN(val text: String) : MovesNavigatorElement(text)

@Composable
fun MovesNavigator(modifier: Modifier, elements: Array<MovesNavigatorElement>) {

}
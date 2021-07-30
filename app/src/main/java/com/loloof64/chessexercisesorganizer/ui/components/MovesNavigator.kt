package com.loloof64.chessexercisesorganizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow

sealed class MovesNavigatorElement(open val text: String)
data class MoveNumber(override val text: String) : MovesNavigatorElement(text)
data class HalfMoveSAN(override val text: String) : MovesNavigatorElement(text)

@Composable
fun MovesNavigator(modifier: Modifier = Modifier, elements: Array<MovesNavigatorElement>) {
    FlowRow(modifier = modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .background(color = Color.Yellow.copy(alpha = 0.3f)),
        mainAxisSpacing = 10.dp,
        crossAxisSpacing = 15.dp,) {
        elements.map {
            Text(text = it.text, fontSize = 34.sp, color = Color.Blue)
        }
    }
}

@Preview
@Composable
fun MovesNavigatorPreview() {
    MovesNavigator(modifier = Modifier.width(400.dp).height(450.dp),
        elements = arrayOf(
            MoveNumber("1."),
            HalfMoveSAN("e4"),
            HalfMoveSAN("e5"),

            MoveNumber("2."),
            HalfMoveSAN("\u2658f3"),
            HalfMoveSAN("\u265ec6"),

            MoveNumber("3."),
            HalfMoveSAN("\u2657b5"),

            ))
}
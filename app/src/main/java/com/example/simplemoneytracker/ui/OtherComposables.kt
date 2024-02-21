package com.example.simplemoneytracker.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplemoneytracker.R
import com.example.simplemoneytracker.ui.theme.SimpleMoneyTrackerTheme


@Composable
fun ClickableBackground(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(onClick = onClick)
    ) {
        content()
    }
}


enum class Directions(val value: Int) {
    START(0),
    TOP(1),
    END(2),
    BOTTOM(3);

}

@Composable
fun HelperCard(
    direction: Directions,
    @StringRes text: Int,
    modifier: Modifier = Modifier,
    screenWidth: Int = 45,
) {

    Column(modifier = modifier
        .widthIn(max = (LocalConfiguration.current.screenWidthDp.dp.times(screenWidth).div(100)))
    ) {
        if (direction == Directions.TOP) {
            Card(
                shape = TriangleEdgeShape(40, TriangleEdgeShape.TOP),
                modifier = Modifier
                    .width(0.dp)
                    .height(24.dp)
                    .align(Alignment.CenterHorizontally)
            ) {}
        }
        Row {
            if (direction == Directions.START) {
                Card(
                    shape = TriangleEdgeShape(30, TriangleEdgeShape.START),
                    modifier = Modifier
                        .width(24.dp)
                        .height(0.dp)
                        .align(Alignment.CenterVertically)
                ) {}
            }
            Card {
                Text(
                    text = stringResource(id = text),
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_16dp))
                        .widthIn(
                            max = (LocalConfiguration.current.screenWidthDp
                                .times(80)
                                .div(100).dp)
                        )
                )
            }
            if (direction == Directions.END) {
                Card(
                    shape = TriangleEdgeShape(30, TriangleEdgeShape.END),
                    modifier = Modifier
                        .width(24.dp)
                        .height(0.dp)
                        .align(Alignment.CenterVertically)
                ) {}
            }
        }
        if (direction == Directions.BOTTOM) {
            Card(
                shape = TriangleEdgeShape(40, TriangleEdgeShape.BOTTOM),
                modifier = Modifier
                    .width(0.dp)
                    .height(24.dp)
                    .align(Alignment.CenterHorizontally)
            ) {}
        }
    }
}

class TriangleEdgeShape(private val offset: Int, private val direction: Int) : Shape {

    companion object {
        const val START = 0
        const val TOP = 1
        const val END = 2
        const val BOTTOM = 3
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {

        val topPath = Path().apply {
            moveTo(x = 0f, y = size.height-offset)
            lineTo(x = 0f - offset, y = size.height)
            lineTo(x = 0f + offset, y = size.height)
        }

        val startPath = Path().apply {
            moveTo(x = size.width - offset, y = 0f)
            lineTo(x = size.width, y = 0f + offset)
            lineTo(x = size.width, y = 0f - offset)
        }

        val endPath = Path().apply {
            moveTo(x = 0f + offset, y = 0f)
            lineTo(x = 0f, y = 0f + offset)
            lineTo(x = 0f, y = 0f - offset)
        }

        val bottomPath = Path().apply {
            moveTo(x = 0f, y = 0f + offset)
            lineTo(x = 0f - offset, y = 0f)
            lineTo(x = 0f + offset, y = 0f)
        }
        return Outline.Generic(path = when (direction) {
            START -> startPath
            TOP -> topPath
            END -> endPath
            else -> bottomPath
        })
    }
}

@Preview
@Composable
fun HelperCardPreview(){
    SimpleMoneyTrackerTheme {
        HelperCard(
            Directions.BOTTOM,
            R.string.item_entry_copy_warning_content
        )
    }
}
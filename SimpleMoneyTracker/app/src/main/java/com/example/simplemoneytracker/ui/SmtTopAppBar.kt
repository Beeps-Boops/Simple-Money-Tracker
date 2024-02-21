package com.example.simplemoneytracker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.simplemoneytracker.R
import com.example.simplemoneytracker.ui.navigation.SmtNavHost
import com.example.simplemoneytracker.ui.theme.SimpleMoneyTrackerTheme

@Composable
fun SmtApp(navController: NavHostController = rememberNavController()) {
    SmtNavHost(navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmtTopAppBar(modifier: Modifier = Modifier,
                 title: String = "Simple Money Tracker",
                 icon: ImageVector = Icons.Filled.Close,
                 onActionButtonClick: () -> Unit = {},
                 onTutorialButtonClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
             Image(
                 painter = painterResource(id = R.drawable.little_guy_foreground),
                 contentDescription = null,
                 modifier = Modifier
                     .size(40.dp)
                     .clickable(onClick = onTutorialButtonClick)
                 )
        },
        actions = {
            IconButton(onClick = onActionButtonClick) {
                Icon(
                    imageVector = icon, contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                )
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun TopBarPreview(){
    SimpleMoneyTrackerTheme {
        SmtTopAppBar()
    }
}
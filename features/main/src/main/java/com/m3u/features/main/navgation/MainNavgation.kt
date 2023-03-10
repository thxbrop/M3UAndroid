package com.m3u.features.main.navgation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.google.accompanist.navigation.animation.composable
import com.m3u.features.main.MainRoute

typealias NavigateToFeed = (String) -> Unit

const val mainNavigationRoute = "main_route"

fun NavController.navigateToMain(navOptions: NavOptions? = null) {
    this.navigate(mainNavigationRoute, navOptions)
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.mainScreen(
    navigateToFeed: NavigateToFeed,
) {
    composable(mainNavigationRoute) {
        MainRoute(
            navigateToFeed = navigateToFeed,
        )
    }
}

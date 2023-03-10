package com.m3u.features.live.navigation

import androidx.compose.animation.*
import androidx.navigation.*
import com.google.accompanist.navigation.animation.composable
import com.m3u.data.util.transform.encrypt.IntIterativeTransferable
import com.m3u.features.live.LiveEvent
import com.m3u.features.live.LiveRoute

private const val LIVE_ROUTE_PATH = "live_route"
private const val LIVE_PLAYLIST_ROUTE_PATH = "live_playlist_route"

private const val TYPE_ID = "id"
const val liveRoute = "$LIVE_ROUTE_PATH/{$TYPE_ID}"
private fun createLiveRoute(id: Int) = "$LIVE_ROUTE_PATH/$id"

private const val TYPE_INITIAL_INDEX = "index"
private const val TYPE_IDS = "ids"
const val livePlaylistRoute = "$LIVE_PLAYLIST_ROUTE_PATH/{$TYPE_IDS}/{$TYPE_INITIAL_INDEX}"

private fun createLivePlaylistRoute(ids: List<Int>, initialIndex: Int) =
    "$LIVE_PLAYLIST_ROUTE_PATH/${IntIterativeTransferable.transfer(ids)}/$initialIndex"

fun NavController.navigateToLive(id: Int) {
    val navOptions = navOptions {
        launchSingleTop = true
    }
    val route = createLiveRoute(id)
    this.navigate(route, navOptions)
}

fun NavController.navigateToLivePlayList(ids: List<Int>, initialIndex: Int) {
    val navOptions = navOptions {
        launchSingleTop = true
    }
    val route = createLivePlaylistRoute(ids, initialIndex)
    this.navigate(route, navOptions)
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.liveScreen() {
    composable(
        route = liveRoute,
        arguments = listOf(
            navArgument(TYPE_ID) {
                type = NavType.IntType
                nullable = false
            },
        ),
        enterTransition = { slideInVertically { it } },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutVertically { it } }
    ) { navBackStackEntry ->
        val id = navBackStackEntry
            .arguments
            ?.getInt(TYPE_ID)
            ?: return@composable
        LiveRoute(
            init = LiveEvent.Init.SingleLive(id)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.livePlaylistScreen() {
    composable(
        route = livePlaylistRoute,
        arguments = listOf(
            navArgument(TYPE_IDS) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(TYPE_INITIAL_INDEX) {
                type = NavType.IntType
                nullable = false
            },
        ),
        enterTransition = { slideInVertically { it } },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutVertically { it } }
    ) { navBackStackEntry ->
        val ids = navBackStackEntry
            .arguments
            ?.getString(TYPE_IDS)
            ?.let(IntIterativeTransferable::accept)
            ?.toList()
            ?: return@composable
        val initialIndex = navBackStackEntry
            .arguments
            ?.getInt(TYPE_INITIAL_INDEX)
            ?: return@composable
        LiveRoute(
            init = LiveEvent.Init.PlayList(
                initialIndex = initialIndex,
                ids = ids
            )
        )
    }
}
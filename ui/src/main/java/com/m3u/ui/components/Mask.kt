package com.m3u.ui.components

import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.m3u.ui.model.Icon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
interface MaskState {
    val visible: Boolean
    fun touch()
    fun keepAlive()
    fun sleep()
}

@Stable
class MaskStateCoroutineImpl(
    @IntRange(from = 1) private val minDuration: Long = MaskDefaults.minDuration,
    coroutineScope: CoroutineScope,
    private val onChanged: (Boolean) -> Unit
) : MaskState {
    private var currentTime: Long by mutableStateOf(fetchCurrentTime)
    private var lastTime: Long by mutableStateOf(0L)

    private var last: Boolean? = null
    override val visible: Boolean
        get() = (currentTime - lastTime <= minDuration).also {
            if (it != last) {
                last = it
                onChanged(it)
            }
        }

    init {
        if (minDuration < 1L) error("minSecondDuration cannot less than 1s.")
        coroutineScope.launch {
            while (true) {
                delay(1000L)
                currentTime += 1
            }
        }
    }

    private val fetchCurrentTime: Long get() = System.currentTimeMillis() / 1000

    override fun touch() {
        lastTime = if (!visible) currentTime else 0
    }

    override fun keepAlive() {
        lastTime = currentTime
    }

    override fun sleep() {
        lastTime = 0
    }
}

@Composable
fun rememberMaskState(
    @IntRange(from = 1) minDuration: Long = MaskDefaults.minDuration,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChanged: (Boolean) -> Unit
): MaskStateCoroutineImpl {
    val currentOnChanged by rememberUpdatedState(onChanged)
    return remember(minDuration, coroutineScope) {
        MaskStateCoroutineImpl(
            minDuration = minDuration,
            coroutineScope = coroutineScope,
            onChanged = currentOnChanged
        )
    }
}

@Composable
fun Mask(
    state: MaskState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = LocalContentColor.current,
    content: @Composable BoxScope.() -> Unit
) {
    AnimatedVisibility(
        visible = state.visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            OuterBox(
                modifier = modifier.background(backgroundColor),
                content = content
            )
        }
    }
}

@Composable
fun MaskPanel(
    state: MaskState,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    OuterColumn(
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        modifier = modifier
            .fillMaxSize()
            .clickable(
                onClick = state::touch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        content = content
    )
}

@Composable
fun MaskButton(
    state: MaskState,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
) {
    val animatedColor by animateColorAsState(tint)
    val currentKeepAlive by rememberUpdatedState(state::keepAlive)
    IconButton(
        icon = Icon.ImageVectorIcon(icon),
        contentDescription = null,
        onClick = {
            currentKeepAlive()
            onClick()
        },
        modifier = modifier,
        tint = animatedColor
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MaskCircleButton(
    state: MaskState,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Surface(
        shape = CircleShape,
        onClick = {
            state.keepAlive()
            onClick()
        },
        modifier = modifier,
        color = Color.Unspecified
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = tint
        )
    }
}

object MaskDefaults {
    const val minDuration = 4L
}
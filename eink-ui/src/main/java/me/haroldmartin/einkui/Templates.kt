package me.haroldmartin.einkui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class EinkLayoutMode {
    Auto,
    SinglePane,
    TwoPane,
}

enum class EinkPane {
    Primary,
    Secondary,
}

@Composable
fun EinkAdaptivePaneLayout(
    primaryPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    layoutMode: EinkLayoutMode = EinkLayoutMode.Auto,
    activePane: EinkPane = EinkPane.Primary,
    secondaryPane: (@Composable () -> Unit)? = null,
) {
    val primaryContent = rememberMovablePaneContent(primaryPane)
    val secondaryContent = rememberOptionalPaneContent(secondaryPane)
    BoxWithConstraints(modifier = modifier) {
        val resolvedMode = when {
            secondaryContent == null -> EinkLayoutMode.SinglePane
            layoutMode != EinkLayoutMode.Auto -> layoutMode
            maxWidth >= EinkTheme.layout.twoPaneBreakpoint &&
                maxHeight >= EinkTheme.layout.twoPaneMinimumHeight -> EinkLayoutMode.TwoPane
            else -> EinkLayoutMode.SinglePane
        }
        if (resolvedMode == EinkLayoutMode.TwoPane && secondaryContent != null) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(EinkTheme.layout.paneGap),
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { primaryContent() }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { secondaryContent() }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = EinkTheme.layout.singlePaneMaxWidth)) {
                    if (activePane == EinkPane.Primary || secondaryContent == null) {
                        primaryContent()
                    } else {
                        secondaryContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberMovablePaneContent(pane: @Composable () -> Unit): @Composable () -> Unit {
    val paneState = rememberUpdatedState(pane)
    return remember { movableContentOf { paneState.value() } }
}

@Composable
private fun rememberOptionalPaneContent(
    pane: (@Composable () -> Unit)?,
): (@Composable () -> Unit)? {
    val paneState = rememberUpdatedState(pane)
    return remember(pane != null) {
        pane?.let { movableContentOf { paneState.value?.invoke() } }
    }
}

@Composable
@Suppress("LongParameterList")
fun EinkSettingsScreen(
    primaryPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    layoutMode: EinkLayoutMode = EinkLayoutMode.Auto,
    activePane: EinkPane = EinkPane.Primary,
    floatingActionButton: @Composable () -> Unit = {},
    secondaryPane: (@Composable () -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EinkTheme.colors.background,
        floatingActionButton = floatingActionButton,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EinkTheme.colors.background)
                .padding(innerPadding)
                .padding(EinkTheme.layout.screenPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            EinkAdaptivePaneLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = EinkTheme.layout.dialogMaxWidth),
                layoutMode = layoutMode,
                activePane = activePane,
                primaryPane = { ScrollableSettingsPane(primaryPane) },
                secondaryPane = secondaryPane?.let { pane ->
                    {
                        ScrollableSettingsPane(pane)
                    }
                },
            )
        }
    }
}

@Composable
private fun ScrollableSettingsPane(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.extraLarge),
    ) {
        content()
    }
}

@Composable
@Suppress("LongParameterList")
fun EinkPickerDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    primaryPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    layoutMode: EinkLayoutMode = EinkLayoutMode.Auto,
    activePane: EinkPane = EinkPane.Primary,
    secondaryPane: (@Composable () -> Unit)? = null,
    confirmButton: @Composable RowScope.() -> Unit = {},
    dismissButton: @Composable RowScope.() -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            EinkSurface(
                modifier = Modifier
                    .fillMaxWidth(EinkTheme.layout.dialogWidthFraction)
                    .fillMaxHeight(EinkTheme.layout.dialogHeightFraction)
                    .widthIn(max = EinkTheme.layout.dialogMaxWidth),
                borderWidth = EinkTheme.borders.strong,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(EinkTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.medium),
                ) {
                    title()
                    EinkAdaptivePaneLayout(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        layoutMode = layoutMode,
                        activePane = activePane,
                        primaryPane = primaryPane,
                        secondaryPane = secondaryPane,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = EinkTheme.spacing.small,
                            alignment = Alignment.End,
                        ),
                    ) {
                        dismissButton()
                        confirmButton()
                    }
                }
            }
        }
    }
}

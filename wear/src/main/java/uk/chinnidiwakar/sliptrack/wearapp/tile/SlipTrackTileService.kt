package uk.chinnidiwakar.sliptrack.wearapp.tile

import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.tiles.*
import androidx.wear.tiles.ActionBuilders.*
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.ModifiersBuilders.*
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.wearapp.MainActivity
import uk.chinnidiwakar.sliptrack.wearapp.sync.WearStateStore

private const val RESOURCES_VERSION = "1"

class SlipTrackTileService : TileService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> {

        return CallbackToFutureAdapter.getFuture { completer ->

            scope.launch {
                val state = WearStateStore(applicationContext).state.first()

                val layout = LayoutElementBuilders.Column.Builder()
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("SlipTrack")
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Spacer.Builder()
                            .setHeight(DimensionBuilders.dp(6f))
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("${state.currentStreak}d current")
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("${state.longestStreak}d best")
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("🛡 ${state.shieldCharges}")
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Spacer.Builder()
                            .setHeight(DimensionBuilders.dp(8f))
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Box.Builder()
                            .setModifiers(
                                Modifiers.Builder()
                                    .setClickable(
                                        Clickable.Builder()
                                            .setId("open")
                                            .setOnClick(
                                                LaunchAction.Builder()
                                                    .setAndroidActivity(
                                                        AndroidActivity.Builder()
                                                            .setPackageName(packageName)
                                                            .setClassName(MainActivity::class.java.name)
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .setBackground(
                                        Background.Builder()
                                            .setColor(ColorBuilders.argb(0xFF2C2C2C.toInt()))
                                            .setCorner(
                                                Corner.Builder()
                                                    .setRadius(DimensionBuilders.dp(14f))
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .setPadding(
                                        Padding.Builder()
                                            .setStart(DimensionBuilders.dp(10f))
                                            .setEnd(DimensionBuilders.dp(10f))
                                            .setTop(DimensionBuilders.dp(6f))
                                            .setBottom(DimensionBuilders.dp(6f))
                                            .build()
                                    )
                                    .build()
                            )
                            .addContent(
                                LayoutElementBuilders.Text.Builder()
                                    .setText("Open")
                                    .build()
                            )
                            .build()
                    )
                    .build()

                val entry = TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(layout)
                            .build()
                    )
                    .build()

                val tile = TileBuilders.Tile.Builder()
                    .setResourcesVersion(RESOURCES_VERSION)
                    .setTileTimeline(
                        TimelineBuilders.Timeline.Builder()
                            .addTimelineEntry(entry)
                            .build()
                    )
                    .build()

                completer.set(tile)
            }

            "SlipTrackTileRequest"
        }
    }

    override fun onResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> {

        return CallbackToFutureAdapter.getFuture { completer ->

            val resources = ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()

            completer.set(resources)
            "SlipTrackResourcesRequest"
        }
    }
}

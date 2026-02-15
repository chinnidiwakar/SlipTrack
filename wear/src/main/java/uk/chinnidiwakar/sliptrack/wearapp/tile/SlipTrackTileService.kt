package uk.chinnidiwakar.sliptrack.wearapp.tile

import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.TimelineBuilders
import kotlinx.coroutines.flow.first
import uk.chinnidiwakar.sliptrack.wearapp.MainActivity
import uk.chinnidiwakar.sliptrack.wearapp.sync.WearStateStore

private const val RESOURCES_VERSION = "1"

class SlipTrackTileService : TileService() {

    override suspend fun onTileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val state = WearStateStore(applicationContext).state.first()
        val layout = LayoutElementBuilders.Column.Builder()
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("SlipTrack")
                    .build()
            )
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(6f)).build())
            .addContent(LayoutElementBuilders.Text.Builder().setText("${state.currentStreak}d current").build())
            .addContent(LayoutElementBuilders.Text.Builder().setText("${state.longestStreak}d best").build())
            .addContent(LayoutElementBuilders.Text.Builder().setText("🛡 ${state.shieldCharges}").build())
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(8f)).build())
            .addContent(
                LayoutElementBuilders.Box.Builder()
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setClickable(
                                ModifiersBuilders.Clickable.Builder()
                                    .setId("open")
                                    .setOnClick(
                                        ActionBuilders.LaunchAction.Builder()
                                            .setAndroidActivity(
                                                ActionBuilders.AndroidActivity.Builder()
                                                    .setPackageName(packageName)
                                                    .setClassName(MainActivity::class.java.name)
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .setBackground(
                                ModifiersBuilders.Background.Builder()
                                    .setColor(ColorBuilders.argb(0xFF2C2C2C.toInt()))
                                    .setCorner(ModifiersBuilders.Corner.Builder().setRadius(DimensionBuilders.dp(14f)).build())
                                    .build()
                            )
                            .setPadding(
                                ModifiersBuilders.Padding.Builder()
                                    .setStart(DimensionBuilders.dp(10f))
                                    .setEnd(DimensionBuilders.dp(10f))
                                    .setTop(DimensionBuilders.dp(6f))
                                    .setBottom(DimensionBuilders.dp(6f))
                                    .build()
                            )
                            .build()
                    )
                    .addContent(LayoutElementBuilders.Text.Builder().setText("Open").build())
                    .build()
            )
            .build()

        val entry = TimelineBuilders.TimelineEntry.Builder()
            .setLayout(LayoutElementBuilders.Layout.Builder().setRoot(layout).build())
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(TimelineBuilders.Timeline.Builder().addTimelineEntry(entry).build())
            .build()
    }

    override suspend fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
    }
}

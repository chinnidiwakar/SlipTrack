package uk.chinnidiwakar.sliptrack.wear.presentation

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import uk.chinnidiwakar.sliptrack.wear.data.SlipCountStore

private const val RESOURCES_VERSION = "1"

class SlipTrackTileService : TileService() {

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> {

        // 1. Create the Click Action
        val onClickAction = ModifiersBuilders.Clickable.Builder()
            .setId("add_button")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName(packageName)
                            .setClassName(
                                "uk.chinnidiwakar.sliptrack.wear.presentation.AddSlipProxyActivity"
                            )
                            .build()
                    )
                    .build()
            )
            .build()   // ← THIS WAS MISSING



        // 2. Build the UI manually (Avoids Material library bugs)
        val rootLayout = LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("SlipTrack")
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(dp(6f))
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("Today: ${SlipCountStore.get(this)}")
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(dp(12f))
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Box.Builder()
                    .setWidth(DimensionBuilders.wrap())
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setClickable(onClickAction)
                            .setBackground(
                                ModifiersBuilders.Background.Builder()
                                    .setColor(ColorBuilders.argb(0xFF4CAF50.toInt()))
                                    .setCorner(
                                        ModifiersBuilders.Corner.Builder()
                                            .setRadius(dp(16f))
                                            .build()
                                    )
                                    .build()
                            )
                            .setPadding(
                                ModifiersBuilders.Padding.Builder()
                                    .setStart(dp(16f))
                                    .setEnd(dp(16f))
                                    .setTop(dp(8f))
                                    .setBottom(dp(8f))
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText("Add Slip")
                            .build()
                    )
                    .build()
            )
            .build()


        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(LayoutElementBuilders.Layout.Builder().setRoot(rootLayout).build())
                            .build()
                    ).build()
            ).build()

        return Futures.immediateFuture(tile)
    }

    override fun onResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build()
        )
    }
}
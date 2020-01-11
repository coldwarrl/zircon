package org.hexworks.zircon.examples.components


import org.hexworks.zircon.api.*
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.extensions.box
import org.hexworks.zircon.api.screen.Screen
import org.hexworks.zircon.internal.component.renderer.NoOpComponentRenderer

object PanelsExampleIssue {

    private val theme = ColorThemes.techLight()
    private val tileset = CP437TilesetResources.rexPaint20x20()

    @JvmStatic
    fun main(args: Array<String>) {

        val tileGrid = LibgdxApplications.startTileGrid(AppConfig.newBuilder()
                .withDefaultTileset(tileset)
                .withSize(Size.create(60, 30))
                .build())

        val screen = Screen.create(tileGrid)


        val component = Components.button()
                .withDecorations(box())
                .withSize(18, 5)
                .withComponentRenderer(NoOpComponentRenderer())
                .withAlignmentWithin(screen, ComponentAlignment.BOTTOM_RIGHT)
                .build()

        screen.addComponent(component)

        screen.display()
        screen.theme = theme

    }
}



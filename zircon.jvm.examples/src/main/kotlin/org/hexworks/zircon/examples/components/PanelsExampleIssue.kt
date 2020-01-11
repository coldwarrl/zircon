package org.hexworks.zircon.examples.components


import org.hexworks.zircon.api.*
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.extensions.box
import org.hexworks.zircon.api.graphics.StyleSet
import org.hexworks.zircon.api.screen.Screen
import org.hexworks.zircon.examples.FadeInExample
import org.hexworks.zircon.internal.component.renderer.NoOpComponentRenderer

object PanelsExampleIssue {

    private val theme = ColorThemes.techLight()
    private val tileset = CP437TilesetResources.rexPaint20x20()

    @JvmStatic
    fun main(args: Array<String>) {

        val tileGrid = SwingApplications.startTileGrid(AppConfig.newBuilder()
                .withDefaultTileset(tileset)
                .withSize(Size.create(40, 30))
                .withDebugMode(true)
                .build())


        val screen = Screen.create(tileGrid)

        val tile = Tile.createCharacterTile('d', StyleSet.defaultStyle())


        val component = Components.button()
                .withDecorations(box())
                .withSize(18, 5)
                .withComponentRenderer(NoOpComponentRenderer())
                .withAlignmentWithin(screen, ComponentAlignment.BOTTOM_RIGHT)
                .build()

        screen.addComponent(component)
        //tileGrid.draw(tile, Position.create(39,29 ))

        screen.display()
        screen.theme = theme

    }
}



package org.hexworks.zircon.internal.renderer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import org.hexworks.cobalt.datatypes.Maybe
import org.hexworks.zircon.api.Maybes
import org.hexworks.zircon.api.application.CursorStyle
import org.hexworks.zircon.api.behavior.TilesetOverride
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.internal.data.LayerState
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.internal.data.PixelPosition
import org.hexworks.zircon.api.tileset.Tileset
import org.hexworks.zircon.internal.RunTimeStats
import org.hexworks.zircon.internal.config.RuntimeConfig
import org.hexworks.zircon.internal.grid.InternalTileGrid
import org.hexworks.zircon.internal.tileset.LibgdxTilesetLoader


@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class LibgdxRenderer(private val grid: InternalTileGrid,
                     private val debug: Boolean = false) : Renderer {

    private val config = RuntimeConfig.config
    private var maybeBatch: Maybe<SpriteBatch> = Maybes.empty()
    private lateinit var cursorRenderer: ShapeRenderer
    private val tilesetLoader = LibgdxTilesetLoader()
    private var blinkOn = true
    private var timeSinceLastBlink: Float = 0f

    private lateinit var backgroundTexture: Texture
    private var backgroundWidth: Int = 0
    private var backgroundHeight: Int = 0

    override fun create() {
        maybeBatch = Maybes.of(SpriteBatch().apply {
            val camera = OrthographicCamera()
            camera.setToOrtho(true)
            projectionMatrix = camera.combined
        })
        cursorRenderer = ShapeRenderer()
        val whitePixmap = Pixmap(grid.widthInPixels, grid.heightInPixels, Pixmap.Format.RGBA8888)
        whitePixmap.setColor(Color.WHITE)
        whitePixmap.fill()
        backgroundTexture = Texture(whitePixmap)

        backgroundWidth = whitePixmap.width / grid.width
        backgroundHeight = whitePixmap.height / grid.height

        whitePixmap.dispose()
    }

    override fun render() {
        if (debug) {
            RunTimeStats.addTimedStatFor("debug.render.time") {
                doRender(Gdx.app.graphics.deltaTime)
            }
        } else doRender(Gdx.app.graphics.deltaTime)
    }

    override fun close() {
        maybeBatch.map(SpriteBatch::dispose)
    }

    private fun doRender(delta: Float) {
        handleBlink(delta)

        maybeBatch.map { batch ->
            batch.begin()
            grid.layerStates.forEach { state ->
                renderTiles(
                        batch = batch,
                        state = state,
                        tileset = tilesetLoader.loadTilesetFrom(grid.tileset),
                        offset = state.position.toPixelPosition(grid.tileset)
                )
            }
            batch.end()
            cursorRenderer.projectionMatrix = batch.projectionMatrix
            if (shouldDrawCursor()) {
                grid.getTileAt(grid.cursorPosition).map {
                    drawCursor(cursorRenderer, it, grid.cursorPosition)
                }
            }
        }
    }

    private fun renderTiles(batch: SpriteBatch,
                            state: LayerState,
                            tileset: Tileset<SpriteBatch>,
                            offset: PixelPosition = PixelPosition(0, 0)) {
        /*
         * I can already see you reaching for that ctrl-x ctrl-v to move that single
         * drawBack() method call into the next loop. Why would two identical loops
         * be required for two methods? Just but both into one loop, right? Wrong.
         * This runs a beautiful 60fps in the test. Now try moving both into the same
         * loop. I dare you. Have fun with the 14 fps, freak. I can't explain it, I
         * can only hope to save those that think they can optimize this. Leave it,
         * for your own sanity
         *
         * I think the problem resolved itself, magically. If this turns out to be not
         * the case uncomment the commented block of code and delete the first
         * actualTileset.drawTile()
         */
        state.tiles.forEach { (pos, tile) ->
            val actualPos = pos + state.position
            if (tile !== Tile.empty()) {
                val actualTile =
                        if (tile.isBlinking /*&& blinkOn*/) {
                            tile.withBackgroundColor(tile.foregroundColor)
                                    .withForegroundColor(tile.backgroundColor)
                        } else {
                            tile
                        }
                val actualTileset: Tileset<SpriteBatch> =
                        if (actualTile is TilesetOverride) {
                            tilesetLoader.loadTilesetFrom(actualTile.tileset)
                        } else {
                            tileset
                        }

                val pixelPos = Position.create(actualPos.x * actualTileset.width, actualPos.y * actualTileset.height)
                drawBack(
                        tile = actualTile,
                        surface = batch,
                        position = pixelPos
                )
                actualTileset.drawTile(
                        tile = actualTile,
                        surface = batch,
                        position = pixelPos
                )
            }
        }
        /*state.tiles.forEach { (pos, tile) ->
            val actualPos = pos + state.position
            if (tile !== Tile.empty()) {
                val actualTile =
                        if (tile.isBlinking /*&& blinkOn*/) {
                            tile.withBackgroundColor(tile.foregroundColor)
                                    .withForegroundColor(tile.backgroundColor)
                        } else {
                            tile
                        }
                val actualTileset: Tileset<SpriteBatch> =
                        if (actualTile is TilesetOverride) {
                            tilesetLoader.loadTilesetFrom(actualTile.tileset)
                        } else {
                            tileset
                        }
                val pixelPos = Position.create(actualPos.x * actualTileset.width, actualPos.y * actualTileset.height)
                actualTileset.drawTile(
                        tile = actualTile,
                        surface = batch,
                        position = pixelPos
                )
            }
        }*/
    }

    private fun drawBack(tile: Tile, surface: SpriteBatch, position: Position) {
        val x = position.x.toFloat()
        val y = position.y.toFloat()
        val backSprite = Sprite(backgroundTexture)
        backSprite.setSize(backgroundWidth.toFloat(), backgroundHeight.toFloat())
        backSprite.setOrigin(0f, 0f)
        backSprite.setOriginBasedPosition(x, y)
        backSprite.flip(false, true)
        backSprite.color = Color(
                tile.backgroundColor.red.toFloat() / 255,
                tile.backgroundColor.green.toFloat() / 255,
                tile.backgroundColor.blue.toFloat() / 255,
                tile.backgroundColor.alpha.toFloat() / 255
        )
        backSprite.draw(surface)
    }

    private fun handleBlink(delta: Float) {
        timeSinceLastBlink += delta
        if (timeSinceLastBlink > config.blinkLengthInMilliSeconds) {
            blinkOn = !blinkOn
        }
    }

    private fun drawCursor(shapeRenderer: ShapeRenderer, character: Tile, position: Position) {
        val tileWidth = grid.tileset.width
        val tileHeight = grid.tileset.height
        val x = (position.x * tileWidth).toFloat()
        val y = (position.y * tileHeight).toFloat()
        val cursorColor = colorToGDXColor(config.cursorColor)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = cursorColor
        when (config.cursorStyle) {
            CursorStyle.USE_CHARACTER_FOREGROUND -> {
                if (blinkOn) {
                    shapeRenderer.color = colorToGDXColor(character.foregroundColor)
                    shapeRenderer.rect(x, y, tileWidth.toFloat(), tileHeight.toFloat())
                }
            }
            CursorStyle.FIXED_BACKGROUND -> shapeRenderer.rect(x, y, tileWidth.toFloat(), tileHeight.toFloat())
            CursorStyle.UNDER_BAR -> shapeRenderer.rect(x, y + tileHeight - 3, tileWidth.toFloat(), 2.0f)
            CursorStyle.VERTICAL_BAR -> shapeRenderer.rect(x, y + 1, 2.0f, tileHeight - 2.0f)
        }
        shapeRenderer.end()
    }

    private fun shouldDrawCursor(): Boolean {
        return grid.isCursorVisible &&
                (config.isCursorBlinking.not() || config.isCursorBlinking && blinkOn)
    }

    private fun colorToGDXColor(color: TileColor): Color {
        return Color(
                color.red / 255.0f,
                color.green / 255.0f,
                color.blue / 255.0f,
                color.alpha / 255.0f
        )
    }

    fun TileColor.toGdxColor(): Color {
        return Color(
                this.red / 255.0f,
                this.green / 255.0f,
                this.blue / 255.0f,
                this.alpha / 255.0f
        )
    }
}

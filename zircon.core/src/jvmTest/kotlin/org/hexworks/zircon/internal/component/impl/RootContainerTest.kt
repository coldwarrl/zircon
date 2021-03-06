package org.hexworks.zircon.internal.component.impl

import org.hexworks.zircon.api.builder.component.ComponentStyleSetBuilder
import org.hexworks.zircon.api.builder.graphics.StyleSetBuilder
import org.hexworks.zircon.api.component.ComponentStyleSet
import org.hexworks.zircon.api.component.data.ComponentMetadata
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.internal.component.renderer.DefaultComponentRenderingStrategy
import org.junit.Before

class RootContainerTest : ComponentImplementationTest<RootContainer>() {

    override lateinit var target: RootContainer

    override val expectedComponentStyles: ComponentStyleSet
        get() = ComponentStyleSetBuilder.newBuilder()
                .withDefaultStyle(StyleSetBuilder.newBuilder()
                        .withForegroundColor(DEFAULT_THEME.secondaryForegroundColor)
                        .withBackgroundColor(DEFAULT_THEME.secondaryBackgroundColor)
                        .build())
                .build()

    @Before
    override fun setUp() {
        rendererStub = ComponentRendererStub()
        componentStub = ComponentStub(Position.create(1, 1), Size.create(2, 2))
        target = RootContainer(
                componentMetadata = ComponentMetadata(
                        relativePosition = POSITION_2_3,
                        size = SIZE_3_4,
                        tileset = TILESET_REX_PAINT_20X20,
                        componentStyleSet = COMPONENT_STYLES),
                renderingStrategy = DefaultComponentRenderingStrategy(
                        componentRenderer = rendererStub))
    }

}

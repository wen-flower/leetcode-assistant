package icu.twtool.leetcode.ui.component

import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Color
import java.awt.Graphics
import java.awt.geom.RoundRectangle2D

class RoundedBorderLayoutPanel(private val arc: Int) : BorderLayoutPanel() {

    private var bg: Color? = null

    override fun setBackground(bg: Color?) {
        this.bg = bg
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        g?.create()?.let {
            it.color = bg
            it.fillRoundRect(0, 0, width, height, arc, arc)

            it.dispose()
        }

    }

    override fun paint(g: Graphics?) {
        super.paint(g)

    }
}
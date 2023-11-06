package icu.twtool.leetcode.util

import com.intellij.util.ui.UIUtil
import java.awt.Color
import javax.swing.UIManager

val darkMode: Boolean get() = UIUtil.getPanelBackground().run { (red + green + blue) / 3 } < 128

fun Color.toRgb(): String = "rgb(${red},${green},${blue})"
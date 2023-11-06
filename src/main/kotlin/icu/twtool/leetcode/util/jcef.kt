package icu.twtool.leetcode.util

import com.intellij.ui.jcef.JBCefBrowser
import icu.twtool.leetcode.constants.PluginConstants
import io.ktor.http.*
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest

fun createBrowser(): JBCefBrowser = JBCefBrowser.createBuilder()
    .build().apply {
        jbCefClient.addRequestHandler(object : CefRequestHandlerAdapter() {
            override fun getResourceRequestHandler(
                browser: CefBrowser?,
                frame: CefFrame?,
                request: CefRequest?,
                isNavigation: Boolean,
                isDownload: Boolean,
                requestInitiator: String?,
                disableDefaultHandling: BoolRef?
            ): CefResourceRequestHandler {

                return object : CefResourceRequestHandlerAdapter() {
                    override fun onBeforeResourceLoad(
                        browser: CefBrowser?,
                        frame: CefFrame?,
                        request: CefRequest?
                    ): Boolean {
                        request?.setHeaderByName(HttpHeaders.UserAgent, PluginConstants.USER_AGENT, true)
                        return false
                    }
                }
            }

        }, cefBrowser)
    }


fun CefFrame.injectStyleSheet(stylesheet: String?) {
    if (stylesheet == null) return
    executeJavaScript(
        //language=javascript
        """
        {
          function injectStyleSheet() {
            let style = document.createElement("style")
            style.innerHTML = `$stylesheet`
            document.head.appendChild(style)
          }
          
          if (document.readyState === "loading") {
            window.addEventListener("DOMContentLoaded", () => injectStyleSheet())
          } else {
            injectStyleSheet()
          }
        }
        """.trimIndent(),
        "injectStyleSheet",
        0
    )
}
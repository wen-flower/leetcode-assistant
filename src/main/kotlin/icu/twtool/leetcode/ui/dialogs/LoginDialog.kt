package icu.twtool.leetcode.ui.dialogs

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.UIUtil
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.api.model.getUserStatus
import icu.twtool.leetcode.constants.MyRegistry
import icu.twtool.leetcode.constants.PluginConstants.LEET_CODE_URL
import icu.twtool.leetcode.services.LeetCodeRequestService
import icu.twtool.leetcode.services.LeetCodeStatusService
import icu.twtool.leetcode.util.createBrowser
import icu.twtool.leetcode.util.createCoroutinesScope
import icu.twtool.leetcode.util.darkMode
import icu.twtool.leetcode.util.injectStyleSheet
import icu.twtool.leetcode.util.loadResourceText
import icu.twtool.leetcode.util.toRgb
import io.ktor.client.plugins.cookies.addCookie
import io.ktor.http.Cookie
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLifeSpanHandlerAdapter
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.SwingUtilities

/**
 * 登录弹窗
 * @author wen
 * @since 2023-11-02
 */
class LoginDialog(private val project: Project?) : DialogWrapper(project, false) {

    companion object {
        private const val LEET_CODE_LOGIN_URL = "$LEET_CODE_URL/accounts/login/"
        private const val WECHAT_URL = "https://open.weixin.qq.com"
    }

    private val log = thisLogger()

    private val scope = createCoroutinesScope(Dispatchers.IO)

    private val leetcodeInjectStyleSheet = loadResourceText("/stylesheet/leetcode.css")
    private val wechatStyleSheet = loadResourceText("/stylesheet/wechat.css")

    private val myBrowser = createBrowser().apply {
        jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadStart(
                browser: CefBrowser?,
                frame: CefFrame?,
                transitionType: CefRequest.TransitionType?
            ) {
                val url = frame?.url ?: ""
                log.info("load start: $url, $transitionType")
                if (frame == null) return
                frame.injectStyleSheet(
                    //language=css
                    """
                    :root {
                      --idea-fg: ${UIUtil.getLabelForeground().toRgb()};
                      --idea-bg: ${UIUtil.getPanelBackground().toRgb()};
                    }
                    """.trimIndent()
                )

                if (url.startsWith(LEET_CODE_LOGIN_URL)) {
                    frame.injectStyleSheet(leetcodeInjectStyleSheet)
                } else if (url.startsWith(WECHAT_URL)) {
                    frame.injectStyleSheet(wechatStyleSheet)
                }
            }

            override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                val url = frame.url
                log.info("load end: $url")

                if (url.startsWith(LEET_CODE_URL)) {
                    val mode = if (darkMode) "dark" else "light"
                    frame.executeJavaScript(
                        //language=javascript
                        """
                        localStorage.setItem("lc-dark-side", "$darkMode")
                        document.body.className = "$mode"
                        """.trimIndent(), "", 0
                    )
                }
            }
        }, cefBrowser)
        jbCefClient.addLifeSpanHandler(object : CefLifeSpanHandlerAdapter() {
            override fun onBeforePopup(
                browser: CefBrowser?, frame: CefFrame?,
                targetUrl: String?, targetFrameName: String?
            ): Boolean {
                // 不打开新窗口
                targetUrl?.let { loadURL(it) }
                return true
            }
        }, cefBrowser)
        loadURL(LEET_CODE_LOGIN_URL)
    }

    private val devToolsAction = object : DialogWrapperAction(MyBundle.getDevToolButtonText()) {

        override fun doAction(e: ActionEvent?) {
            myBrowser.openDevtools()
        }
    }

    private val backAction = object : DialogWrapperAction(MyBundle.getBackButtonText()) {

        override fun doAction(e: ActionEvent?) {
            myBrowser.loadURL(LEET_CODE_LOGIN_URL)
        }
    }

    private val centerPanel = JBPanel<JBPanel<*>>(BorderLayout())

    override fun createCenterPanel(): JComponent = centerPanel

    init {
        val minSize = Dimension(800, 600)

        centerPanel.add(myBrowser.component, BorderLayout.CENTER)
        centerPanel.minimumSize = minSize

        this.setSize(minSize.width, minSize.height)

        title = MyBundle.message("please.login.leetcode")
        setOKButtonText(MyBundle.getLoggedButtonText())
        init()
    }

    override fun createActions(): Array<Action> =
        if (MyRegistry.debug) arrayOf(devToolsAction, backAction, okAction)
        else arrayOf(backAction, okAction)

    override fun dispose() {
        log.info("dispose ...")
        centerPanel.remove(myBrowser.component)
        myBrowser.dispose()
        scope.cancel()
        super.dispose()
    }

    override fun doOKAction() {
        log.info("doOkAction")
        scope.launch {
            val requestService = LeetCodeRequestService.getInstance()
            myBrowser.jbCefCookieManager.getCookies(LEET_CODE_URL, true).get().forEach {
                requestService.addCookie(
                    LEET_CODE_URL, Cookie(
                        name = it.name,
                        value = it.value,
                        domain = it.domain,
                        path = it.path,
                        secure = it.isSecure,
                        httpOnly = it.isHttpOnly,
                        expires = it.expires?.time?.let { time -> GMTDate(time) },
                    )
                )
            }

            val userStatus = LeetCodeRequestService.getInstance().api.getUserStatus()
            SwingUtilities.invokeLater {
                val result = LeetCodeStatusService.getInstance().login(project, userStatus)
                if (result) {
                    close(OK_EXIT_CODE)
                }
            }
        }
    }
}
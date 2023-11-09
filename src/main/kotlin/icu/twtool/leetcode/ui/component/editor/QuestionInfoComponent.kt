package icu.twtool.leetcode.ui.component.editor

import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.Disposable
import com.intellij.util.ui.components.BorderLayoutPanel
import icu.twtool.leetcode.editor.LeetCodeFileBinaryData
import icu.twtool.leetcode.util.createBrowser
import icu.twtool.leetcode.util.darkMode
import icu.twtool.leetcode.util.messageBusConnect

class QuestionInfoComponent : BorderLayoutPanel(), Disposable {

    private val messageBusConnection = messageBusConnect()

    private val browser = createBrowser()

    init {
        messageBusConnection.subscribe(LafManagerListener.TOPIC, LafManagerListener { refresh() })
    }

    private var questionInfo: LeetCodeFileBinaryData? = null

    fun updateInfo(data: LeetCodeFileBinaryData?) {
        questionInfo = data
        refresh()
    }

    private fun refresh() {
        val (translatedContent, translatedTitle) = questionInfo?.translations ?: return browser.loadHTML("")
        browser.loadHTML("""
        ${computedStyle()}
        <div class="title">${questionInfo?.question?.frontendQuestionId}. ${translatedTitle}</div>
        $translatedContent
        """.trimIndent())
    }

    init {
        addToCenter(browser.component)
    }

    override fun dispose() {
        browser.dispose()
        messageBusConnection.dispose()
    }
}

private fun computedStyle(): String = """
<style>
    ${
    if (darkMode) """:root {
        --title: rgb(245, 245, 245);
        --color: rgb(245, 245, 245);
        --code: rgba(239, 241, 246, 0.75);
        --code-border: rgba(247, 250, 255, 0.12);
        --code-bg: rgba(255, 255, 255, 0.07);
        --pre: rgba(255, 255, 255, 0.6);
        --pre-border: rgba(255, 255, 255, 0.14);
        --strong: rgb(255, 255, 255);
    }"""
    else """:root {
        --title: rgb(26, 26, 26);
        --color: rgb(38, 38, 38);
        --code: rgba(38, 38, 38, 0.9);
        --code-border: rgba(0, 0, 0, 0.05);
        --code-bg: rgba(0, 10, 32, 0.03);
        --pre: rgba(0, 0, 0, 0.55);
        --pre-border: rgba(0, 0, 0, 0.08);
        --strong: rgb(38, 38, 38);
    }"""
}
    ::-webkit-scrollbar {
        display: none;
    }

    * {
        box-sizing: border-box;
    }

    body {
        color: var(--color);
        padding: 0.5em;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
    }

    .title {
        color: var(--title);
        font-size: 24px;
        font-weight: 600;
        line-height: 32px;
        text-overflow: ellipsis;
    }

    code {
        color: var(--code);
        background-color: var(--code-bg);
        border-color: var(--code-border);
        border-style: solid;
        border-radius: 5px;
        border-width: 1px;
        font-family: Menlo, sans-serif;
        font-size: .75rem;
        line-height: 1rem;
        padding: .25rem;
        white-space: pre-wrap;
    }

    pre {
        color: var(--pre);
        border-color: var(--pre-border);
        border-left-width: 2px;
        font-family: Menlo, sans-serif;
        font-size: .875rem;
        line-height: 1.25rem;
        margin-bottom: 1rem;
        margin-top: 1rem;
        overflow: visible;
        padding-left: 1rem;
        white-space: pre-wrap;
        border-left-style: solid;
    }

    strong {
        color: var(--strong);
    }

    li {
        margin-bottom: .75rem;
    }
</style>
"""
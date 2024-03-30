package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.youtube.YoutubeJavaScriptExtractor.extractJavaScriptPlayerCode
import org.schabi.newpipe.extractor.services.youtube.YoutubeJavaScriptExtractor.extractJavaScriptUrlWithEmbedWatchPage
import org.schabi.newpipe.extractor.services.youtube.YoutubeJavaScriptExtractor.extractJavaScriptUrlWithIframeResource
import java.io.IOException

class YoutubeJavaScriptExtractorTest {
    @BeforeEach
    @Throws(IOException::class)
    fun setup() {
        init(DownloaderTestImpl.Companion.getInstance())
    }

    @Test
    @Throws(ParsingException::class)
    fun testExtractJavaScriptUrlIframe() {
        Assertions.assertTrue(extractJavaScriptUrlWithIframeResource()
                .endsWith("base.js"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testExtractJavaScriptUrlEmbed() {
        Assertions.assertTrue(extractJavaScriptUrlWithEmbedWatchPage("d4IGg5dqeO8")
                .endsWith("base.js"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testExtractJavaScript__success() {
        val playerJsCode = extractJavaScriptPlayerCode("d4IGg5dqeO8")
        assertPlayerJsCode(playerJsCode)
    }

    @Test
    @Throws(ParsingException::class)
    fun testExtractJavaScript__invalidVideoId__success() {
        var playerJsCode = extractJavaScriptPlayerCode("not_a_video_id")
        assertPlayerJsCode(playerJsCode)
        playerJsCode = extractJavaScriptPlayerCode("11-chars123")
        assertPlayerJsCode(playerJsCode)
    }

    private fun assertPlayerJsCode(playerJsCode: String) {
        ExtractorAsserts.assertContains(""" Copyright The Closure Library Authors.
 SPDX-License-Identifier: Apache-2.0""", playerJsCode)
        ExtractorAsserts.assertContains("var _yt_player", playerJsCode)
    }
}
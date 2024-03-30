package org.schabi.newpipe.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudSubscriptionExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor.InvalidSourceException

/**
 * Test for [SoundcloudSubscriptionExtractor]
 */
class SoundcloudSubscriptionExtractorTest {
    @ParameterizedTest
    @ValueSource(strings = ["https://soundcloud.com/monstercat", "http://soundcloud.com/monstercat", "soundcloud.com/monstercat", "monstercat",  // Empty followings user
        "some-random-user-184047028"])
    @Throws(Exception::class)
    fun testFromChannelUrl(channelUrl: String?) {
        for (item in subscriptionExtractor!!.fromChannelUrl(channelUrl)) {
            Assertions.assertNotNull(item.name)
            Assertions.assertNotNull(item.url)
            Assertions.assertTrue(urlHandler!!.acceptUrl(item.url))
            Assertions.assertNotEquals(-1, item.serviceId)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["httttps://invalid.com/user", ".com/monstercat", "ithinkthatthisuserdontexist", ""])
    fun testInvalidSourceException(invalidUser: String?) {
        Assertions.assertThrows(
                InvalidSourceException::class.java
        ) { subscriptionExtractor!!.fromChannelUrl(invalidUser) }
    }

    // null can't be added to the above value source because it's not a constant
    @Test
    fun testInvalidSourceExceptionWhenUrlIsNull() {
        Assertions.assertThrows(
                InvalidSourceException::class.java
        ) { subscriptionExtractor!!.fromChannelUrl(null) }
    }

    companion object {
        private var subscriptionExtractor: SoundcloudSubscriptionExtractor? = null
        private var urlHandler: LinkHandlerFactory? = null
        @BeforeAll
        fun setupClass() {
            init(DownloaderTestImpl.Companion.getInstance())
            subscriptionExtractor = SoundcloudSubscriptionExtractor(ServiceList.SoundCloud)
            urlHandler = ServiceList.SoundCloud.channelLHFactory
        }
    }
}

package org.schabi.newpipe.extractor.services.youtube.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils
import java.io.UnsupportedEncodingException

class YoutubeSearchQueryHandlerFactory() : SearchQueryHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(searchString: String?,
                               @Nonnull contentFilters: List<String?>?,
                               sortFilter: String?): String? {
        try {
            if (!contentFilters!!.isEmpty()) {
                val contentFilter: String? = contentFilters.get(0)
                when (contentFilter) {
                    VIDEOS -> return (SEARCH_URL + Utils.encodeUrlUtf8(searchString)
                            + "&sp=EgIQAfABAQ%253D%253D")

                    CHANNELS -> return (SEARCH_URL + Utils.encodeUrlUtf8(searchString)
                            + "&sp=EgIQAvABAQ%253D%253D")

                    PLAYLISTS -> return (SEARCH_URL + Utils.encodeUrlUtf8(searchString)
                            + "&sp=EgIQA_ABAQ%253D%253D")

                    MUSIC_SONGS, MUSIC_VIDEOS, MUSIC_ALBUMS, MUSIC_PLAYLISTS, MUSIC_ARTISTS -> return MUSIC_SEARCH_URL + Utils.encodeUrlUtf8(searchString)
                }
            }
            return SEARCH_URL + Utils.encodeUrlUtf8(searchString) + "&sp=8AEB"
        } catch (e: UnsupportedEncodingException) {
            throw ParsingException("Could not encode query", e)
        }
    }

    override val availableContentFilter: Array<String?>
        get() {
            return arrayOf(
                    ALL,
                    VIDEOS,
                    CHANNELS,
                    PLAYLISTS,
                    MUSIC_SONGS,
                    MUSIC_VIDEOS,
                    MUSIC_ALBUMS,
                    MUSIC_PLAYLISTS // MUSIC_ARTISTS
            )
        }

    companion object {
        @get:Nonnull
        val instance: YoutubeSearchQueryHandlerFactory = YoutubeSearchQueryHandlerFactory()
        val ALL: String = "all"
        @JvmField
        val VIDEOS: String = "videos"
        @JvmField
        val CHANNELS: String = "channels"
        @JvmField
        val PLAYLISTS: String = "playlists"
        @JvmField
        val MUSIC_SONGS: String = "music_songs"
        @JvmField
        val MUSIC_VIDEOS: String = "music_videos"
        @JvmField
        val MUSIC_ALBUMS: String = "music_albums"
        @JvmField
        val MUSIC_PLAYLISTS: String = "music_playlists"
        @JvmField
        val MUSIC_ARTISTS: String = "music_artists"
        private val SEARCH_URL: String = "https://www.youtube.com/results?search_query="
        private val MUSIC_SEARCH_URL: String = "https://music.youtube.com/search?q="
        @Nonnull
        fun getSearchParameter(contentFilter: String?): String {
            if (Utils.isNullOrEmpty(contentFilter)) {
                return "8AEB"
            }
            when (contentFilter) {
                VIDEOS -> return "EgIQAfABAQ%3D%3D"
                CHANNELS -> return "EgIQAvABAQ%3D%3D"
                PLAYLISTS -> return "EgIQA_ABAQ%3D%3D"
                MUSIC_SONGS, MUSIC_VIDEOS, MUSIC_ALBUMS, MUSIC_PLAYLISTS, MUSIC_ARTISTS -> return ""
                else -> return "8AEB"
            }
        }
    }
}

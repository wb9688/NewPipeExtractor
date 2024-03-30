package org.schabi.newpipe.extractor.stream

import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.youtube.ItagItem
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.utils.LocaleCompat
import java.util.Locale
import java.util.function.Supplier

class SubtitlesStream private constructor(@Nonnull id: String,
                                          @Nonnull content: String,
                                          isUrl: Boolean,
                                          private override val format: MediaFormat?,
                                          @Nonnull deliveryMethod: DeliveryMethod,
                                          /**
                                           * Get the language tag of the subtitles.
                                           *
                                           * @return the language tag of the subtitles
                                           */
                                          @param:Nonnull val languageTag: String,
                                          /**
                                           * Return whether if the subtitles are auto-generated.
                                           *
                                           *
                                           * Some streaming services can generate subtitles for their contents, like YouTube.
                                           *
                                           *
                                           * @return `true` if the subtitles are auto-generated, `false` otherwise
                                           */
                                          val isAutoGenerated: Boolean,
                                          manifestUrl: String?) : Stream(id, content, isUrl, format, deliveryMethod, manifestUrl) {
    /**
     * Get the [locale][Locale] of the subtitles.
     *
     * @return the [locale][Locale] of the subtitles
     */
    val locale: Locale?

    /**
     * Class to build [SubtitlesStream] objects.
     */
    class Builder
    /**
     * Create a new [Builder] instance with default values.
     */
    () {
        private var id: String? = null
        private var content: String? = null
        private var isUrl: Boolean = false
        private var deliveryMethod: DeliveryMethod? = DeliveryMethod.PROGRESSIVE_HTTP
        private var mediaFormat: MediaFormat? = null
        private var manifestUrl: String? = null
        private var languageCode: String? = null

        // Use of the Boolean class instead of the primitive type needed for setter call check
        private var autoGenerated: Boolean? = null

        /**
         * Set the identifier of the [SubtitlesStream].
         *
         * @param id the identifier of the [SubtitlesStream], which should not be null
         * (otherwise the fallback to create the identifier will be used when building
         * the builder)
         * @return this [Builder] instance
         */
        fun setId(@Nonnull id: String?): Builder {
            this.id = id
            return this
        }

        /**
         * Set the content of the [SubtitlesStream].
         *
         *
         *
         * It must not be null, and should be non empty.
         *
         *
         * @param content the content of the [SubtitlesStream], which must not be null
         * @param isUrl   whether the content is a URL
         * @return this [Builder] instance
         */
        fun setContent(@Nonnull content: String?,
                       isUrl: Boolean): Builder {
            this.content = content
            this.isUrl = isUrl
            return this
        }

        /**
         * Set the [MediaFormat] used by the [SubtitlesStream].
         *
         *
         *
         * It should be one of the subtitles [MediaFormat]s ([SRT][MediaFormat.SRT],
         * [TRANSCRIPT1][MediaFormat.TRANSCRIPT1], [ TRANSCRIPT2][MediaFormat.TRANSCRIPT2], [TRANSCRIPT3][MediaFormat.TRANSCRIPT3], [ TTML][MediaFormat.TTML], or [VTT][MediaFormat.VTT]) but can be `null` if the media format could
         * not be determined.
         *
         *
         *
         *
         * The default value is `null`.
         *
         *
         * @param mediaFormat the [MediaFormat] of the [SubtitlesStream], which can be
         * null
         * @return this [Builder] instance
         */
        fun setMediaFormat(mediaFormat: MediaFormat?): Builder {
            this.mediaFormat = mediaFormat
            return this
        }

        /**
         * Set the [DeliveryMethod] of the [SubtitlesStream].
         *
         *
         *
         * It must not be null.
         *
         *
         *
         *
         * The default delivery method is [DeliveryMethod.PROGRESSIVE_HTTP].
         *
         *
         * @param deliveryMethod the [DeliveryMethod] of the [SubtitlesStream], which
         * must not be null
         * @return this [Builder] instance
         */
        fun setDeliveryMethod(@Nonnull deliveryMethod: DeliveryMethod?): Builder {
            this.deliveryMethod = deliveryMethod
            return this
        }

        /**
         * Sets the URL of the manifest this stream comes from (if applicable, otherwise null).
         *
         * @param manifestUrl the URL of the manifest this stream comes from or `null`
         * @return this [Builder] instance
         */
        fun setManifestUrl(manifestUrl: String?): Builder {
            this.manifestUrl = manifestUrl
            return this
        }

        /**
         * Set the language code of the [SubtitlesStream].
         *
         *
         *
         * It **must not be null** and should not be an empty string.
         *
         *
         * @param languageCode the language code of the [SubtitlesStream]
         * @return this [Builder] instance
         */
        fun setLanguageCode(@Nonnull languageCode: String?): Builder {
            this.languageCode = languageCode
            return this
        }

        /**
         * Set whether the subtitles have been auto-generated by the streaming service.
         *
         * @param autoGenerated whether the subtitles have been generated by the streaming
         * service
         * @return this [Builder] instance
         */
        fun setAutoGenerated(autoGenerated: Boolean): Builder {
            this.autoGenerated = autoGenerated
            return this
        }

        /**
         * Build a [SubtitlesStream] using the builder's current values.
         *
         *
         *
         * The content (and so the `isUrl` boolean), the language code and the `isAutoGenerated` properties must have been set.
         *
         *
         *
         *
         * If no identifier has been set, an identifier will be generated using the language code
         * and the media format suffix, if the media format is known.
         *
         *
         * @return a new [SubtitlesStream] using the builder's current values
         * @throws IllegalStateException if `id`, `content` (and so `isUrl`),
         * `deliveryMethod`, `languageCode` or the `isAutogenerated` have been
         * not set, or have been set as `null`
         */
        @Nonnull
        @Throws(ParsingException::class)
        fun build(): SubtitlesStream {
            if (content == null) {
                throw IllegalStateException(("No valid content was specified. Please specify a "
                        + "valid one with setContent."))
            }
            if (deliveryMethod == null) {
                throw IllegalStateException(
                        ("The delivery method of the subtitles stream has been set as null, which "
                                + "is not allowed. Pass a valid one instead with"
                                + "setDeliveryMethod."))
            }
            if (languageCode == null) {
                throw IllegalStateException(("The language code of the subtitles stream has "
                        + "been not set or is null. Make sure you specified an non null language "
                        + "code with setLanguageCode."))
            }
            if (autoGenerated == null) {
                throw IllegalStateException(("The subtitles stream has been not set as an "
                        + "autogenerated subtitles stream or not. Please specify this information "
                        + "with setIsAutoGenerated."))
            }
            if (id == null) {
                id = languageCode + (if (mediaFormat != null) "." + mediaFormat!!.suffix else "")
            }
            return SubtitlesStream(id!!, content!!, isUrl, mediaFormat, deliveryMethod!!,
                    languageCode!!, autoGenerated!!, manifestUrl)
        }
    }

    /**
     * Create a new subtitles stream.
     *
     * @param id             the identifier which uniquely identifies the stream, e.g. for YouTube
     * this would be the itag
     * @param content        the content or the URL of the stream, depending on whether isUrl is
     * true
     * @param isUrl          whether content is the URL or the actual content of e.g. a DASH
     * manifest
     * @param mediaFormat    the [MediaFormat] used by the stream
     * @param deliveryMethod the [DeliveryMethod] of the stream
     * @param languageCode   the language code of the stream
     * @param autoGenerated  whether the subtitles are auto-generated by the streaming service
     * @param manifestUrl    the URL of the manifest this stream comes from (if applicable,
     * otherwise null)
     */
    init {
        locale = LocaleCompat.forLanguageTag(languageTag).orElseThrow(
                Supplier({
                    ParsingException(
                            "not a valid locale language code: " + languageTag)
                }))
    }

    val extension: String?
        /**
         * Get the extension of the subtitles.
         *
         * @return the extension of the subtitles
         */
        get() {
            return format!!.suffix
        }

    /**
     * {@inheritDoc}
     */
    public override fun equalStats(cmp: Stream?): Boolean {
        return (super.equalStats(cmp)
                && cmp is SubtitlesStream
                && (languageTag == cmp.languageTag) && (isAutoGenerated == cmp.isAutoGenerated))
    }

    val displayLanguageName: String
        /**
         * Get the display language name of the subtitles.
         *
         * @return the display language name of the subtitles
         */
        get() {
            return locale!!.getDisplayName(locale)
        }
    override val itagItem: ItagItem?
        /**
         * No subtitles which are currently extracted use an [ItagItem], so `null` is
         * returned by this method.
         *
         * @return `null`
         */
        get() {
            return null
        }
}

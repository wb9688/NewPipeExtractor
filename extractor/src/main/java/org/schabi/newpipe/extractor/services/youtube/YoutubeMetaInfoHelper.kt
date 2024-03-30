package org.schabi.newpipe.extractor.services.youtube

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.utils.Utils
import java.net.MalformedURLException
import java.net.URL
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

object YoutubeMetaInfoHelper {
    @Nonnull
    @Throws(ParsingException::class)
    fun getMetaInfo(contents: JsonArray): List<MetaInfo> {
        val metaInfo: MutableList<MetaInfo> = ArrayList()
        for (content: Any in contents) {
            val resultObject: JsonObject = content as JsonObject
            if (resultObject.has("itemSectionRenderer")) {
                for (sectionContentObject: Any in resultObject.getObject("itemSectionRenderer").getArray("contents")) {
                    val sectionContent: JsonObject = sectionContentObject as JsonObject
                    if (sectionContent.has("infoPanelContentRenderer")) {
                        metaInfo.add(getInfoPanelContent(sectionContent
                                .getObject("infoPanelContentRenderer")))
                    }
                    if (sectionContent.has("clarificationRenderer")) {
                        metaInfo.add(getClarificationRenderer(sectionContent
                                .getObject("clarificationRenderer")
                        ))
                    }
                    if (sectionContent.has("emergencyOneboxRenderer")) {
                        getEmergencyOneboxRenderer(
                                sectionContent.getObject("emergencyOneboxRenderer"), Consumer({ e: MetaInfo -> metaInfo.add(e) }))
                    }
                }
            }
        }
        return metaInfo
    }

    @Nonnull
    @Throws(ParsingException::class)
    private fun getInfoPanelContent(infoPanelContentRenderer: JsonObject): MetaInfo {
        val metaInfo: MetaInfo = MetaInfo()
        val sb: StringBuilder = StringBuilder()
        for (paragraph: Any in infoPanelContentRenderer.getArray("paragraphs")) {
            if (sb.length != 0) {
                sb.append("<br>")
            }
            sb.append(YoutubeParsingHelper.getTextFromObject(paragraph as JsonObject?))
        }
        metaInfo.setContent(Description(sb.toString(), Description.Companion.HTML))
        if (infoPanelContentRenderer.has("sourceEndpoint")) {
            val metaInfoLinkUrl: String? = YoutubeParsingHelper.getUrlFromNavigationEndpoint(
                    infoPanelContentRenderer.getObject("sourceEndpoint"))
            try {
                metaInfo.addUrl(URL(Objects.requireNonNull(YoutubeParsingHelper.extractCachedUrlIfNeeded(
                        metaInfoLinkUrl))))
            } catch (e: NullPointerException) {
                throw ParsingException("Could not get metadata info URL", e)
            } catch (e: MalformedURLException) {
                throw ParsingException("Could not get metadata info URL", e)
            }
            val metaInfoLinkText: String? = YoutubeParsingHelper.getTextFromObject(
                    infoPanelContentRenderer.getObject("inlineSource"))
            if (Utils.isNullOrEmpty(metaInfoLinkText)) {
                throw ParsingException("Could not get metadata info link text.")
            }
            metaInfo.addUrlText(metaInfoLinkText)
        }
        return metaInfo
    }

    @Nonnull
    @Throws(ParsingException::class)
    private fun getClarificationRenderer(
            clarificationRenderer: JsonObject): MetaInfo {
        val metaInfo: MetaInfo = MetaInfo()
        val title: String? = YoutubeParsingHelper.getTextFromObject(clarificationRenderer
                .getObject("contentTitle"))
        val text: String? = YoutubeParsingHelper.getTextFromObject(clarificationRenderer
                .getObject("text"))
        if (title == null || text == null) {
            throw ParsingException("Could not extract clarification renderer content")
        }
        metaInfo.setTitle(title)
        metaInfo.setContent(Description(text, Description.Companion.PLAIN_TEXT))
        if (clarificationRenderer.has("actionButton")) {
            val actionButton: JsonObject = clarificationRenderer.getObject("actionButton")
                    .getObject("buttonRenderer")
            try {
                val url: String? = YoutubeParsingHelper.getUrlFromNavigationEndpoint(actionButton
                        .getObject("command"))
                metaInfo.addUrl(URL(Objects.requireNonNull(YoutubeParsingHelper.extractCachedUrlIfNeeded(url))))
            } catch (e: NullPointerException) {
                throw ParsingException("Could not get metadata info URL", e)
            } catch (e: MalformedURLException) {
                throw ParsingException("Could not get metadata info URL", e)
            }
            val metaInfoLinkText: String? = YoutubeParsingHelper.getTextFromObject(
                    actionButton.getObject("text"))
            if (Utils.isNullOrEmpty(metaInfoLinkText)) {
                throw ParsingException("Could not get metadata info link text.")
            }
            metaInfo.addUrlText(metaInfoLinkText)
        }
        if (clarificationRenderer.has("secondaryEndpoint") && clarificationRenderer
                        .has("secondarySource")) {
            val url: String? = YoutubeParsingHelper.getUrlFromNavigationEndpoint(clarificationRenderer
                    .getObject("secondaryEndpoint"))
            // Ignore Google URLs, because those point to a Google search about "Covid-19"
            if (url != null && !YoutubeParsingHelper.isGoogleURL(url)) {
                try {
                    metaInfo.addUrl(URL(url))
                    val description: String? = YoutubeParsingHelper.getTextFromObject(clarificationRenderer
                            .getObject("secondarySource"))
                    metaInfo.addUrlText(if (description == null) url else description)
                } catch (e: MalformedURLException) {
                    throw ParsingException("Could not get metadata info secondary URL", e)
                }
            }
        }
        return metaInfo
    }

    @Throws(ParsingException::class)
    private fun getEmergencyOneboxRenderer(
            emergencyOneboxRenderer: JsonObject,
            addMetaInfo: Consumer<MetaInfo>
    ) {
        val supportRenderers: List<JsonObject> = emergencyOneboxRenderer.values
                .stream()
                .filter(Predicate({ o: Any? ->
                    (o is JsonObject
                            && o.has("singleActionEmergencySupportRenderer"))
                }))
                .map(Function({ o: Any -> (o as JsonObject).getObject("singleActionEmergencySupportRenderer") }))
                .collect(Collectors.toList())
        if (supportRenderers.isEmpty()) {
            throw ParsingException("Could not extract any meta info from emergency renderer")
        }
        for (r: JsonObject in supportRenderers) {
            val metaInfo: MetaInfo = MetaInfo()

            // usually an encouragement like "We are with you"
            val title: String? = YoutubeParsingHelper.getTextFromObjectOrThrow(r.getObject("title"), "title")
            // usually a phone number
            val action: String? = YoutubeParsingHelper.getTextFromObjectOrThrow(r.getObject("actionText"), "action")
            // usually details about the phone number
            val details: String? = YoutubeParsingHelper.getTextFromObjectOrThrow(r.getObject("detailsText"), "details")
            // usually the name of an association
            val urlText: String? = YoutubeParsingHelper.getTextFromObjectOrThrow(r.getObject("navigationText"),
                    "urlText")
            metaInfo.setTitle(title)
            metaInfo.setContent(Description(details + "\n" + action, Description.Companion.PLAIN_TEXT))
            metaInfo.addUrlText(urlText)

            // usually the webpage of the association
            val url: String? = YoutubeParsingHelper.getUrlFromNavigationEndpoint(r.getObject("navigationEndpoint"))
            if (url == null) {
                throw ParsingException("Could not extract emergency renderer url")
            }
            try {
                metaInfo.addUrl(URL(Utils.replaceHttpWithHttps(url)))
            } catch (e: MalformedURLException) {
                throw ParsingException("Could not parse emergency renderer url", e)
            }
            addMetaInfo.accept(metaInfo)
        }
    }
}

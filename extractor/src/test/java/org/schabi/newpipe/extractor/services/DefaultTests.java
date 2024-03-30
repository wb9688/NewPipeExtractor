package org.schabi.newpipe.extractor.services;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmptyErrors;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreaterOrEqual;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertNotEmpty;
import static org.schabi.newpipe.extractor.StreamingService.LinkType;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public final class DefaultTests {
    public static void defaultTestListOfItems(StreamingService expectedService, List<? extends InfoItem> itemsList, List<Throwable> errors) throws ParsingException {
        assertFalse(itemsList.isEmpty(), "List of items is empty");
        assertFalse(itemsList.contains(null), "List of items contains a null element");
        assertEmptyErrors("Errors during extraction", errors);

        for (InfoItem item : itemsList) {
            assertIsSecureUrl(item.url);

            final List<Image> thumbnails = item.thumbnails;
            if (!isNullOrEmpty(thumbnails)) {
                defaultTestImageCollection(thumbnails);
            }
            assertNotNull(item.infoType, "InfoItem type not set: " + item);
            assertEquals(expectedService.serviceId, item.serviceId, "Unexpected item service id");
            assertNotEmpty("Item name not set: " + item, item.name);

            if (item instanceof StreamInfoItem) {
                StreamInfoItem streamInfoItem = (StreamInfoItem) item;
                final String uploaderUrl = streamInfoItem.uploaderUrl;
                if (!isNullOrEmpty(uploaderUrl)) {
                    assertIsSecureUrl(uploaderUrl);
                    assertExpectedLinkType(expectedService, uploaderUrl, LinkType.CHANNEL);
                }

                final List<Image> uploaderAvatars = streamInfoItem.uploaderAvatars;
                if (!isNullOrEmpty(uploaderAvatars)) {
                    defaultTestImageCollection(uploaderAvatars);
                }

                assertExpectedLinkType(expectedService, streamInfoItem.url, LinkType.STREAM);

                if (!isNullOrEmpty(streamInfoItem.textualUploadDate)) {
                    final DateWrapper uploadDate = streamInfoItem.uploadDate;
                    assertNotNull(uploadDate,"No parsed upload date");
                }

            } else if (item instanceof ChannelInfoItem) {
                final ChannelInfoItem channelInfoItem = (ChannelInfoItem) item;
                assertExpectedLinkType(expectedService, channelInfoItem.url, LinkType.CHANNEL);

            } else if (item instanceof PlaylistInfoItem) {
                final PlaylistInfoItem playlistInfoItem = (PlaylistInfoItem) item;
                assertExpectedLinkType(expectedService, playlistInfoItem.url, LinkType.PLAYLIST);
            }
        }
    }

    private static void assertExpectedLinkType(StreamingService expectedService, String url, LinkType expectedLinkType) throws ParsingException {
        final LinkType linkTypeByUrl = expectedService.getLinkTypeByUrl(url);

        assertNotEquals(LinkType.NONE, linkTypeByUrl,
                "Url is not recognized by its own service: \"" + url + "\"");
        assertEquals(expectedLinkType, linkTypeByUrl,
                "Service returned wrong link type for: \"" + url + "\"");
    }

    public static void assertOnlyContainsType(ListExtractor.InfoItemsPage<? extends InfoItem> items, InfoItem.InfoType expectedType) {
        for (InfoItem item : items.getItems()) {
            assertEquals(expectedType, item.infoType,
                    "Item list contains unexpected info types");
        }
    }

    public static <T extends InfoItem> void assertNoMoreItems(ListExtractor<T> extractor) throws Exception {
        final ListExtractor.InfoItemsPage<T> initialPage = extractor.initialPage;
        assertFalse(initialPage.hasNextPage(), "More items available when it shouldn't");
    }

    public static void assertNoDuplicatedItems(StreamingService expectedService,
                                               ListExtractor.InfoItemsPage<InfoItem> page1,
                                               ListExtractor.InfoItemsPage<InfoItem> page2) throws Exception {
        defaultTestListOfItems(expectedService, page1.getItems(), page1.errors);
        defaultTestListOfItems(expectedService, page2.getItems(), page2.errors);

        final Set<String> urlsSet = new HashSet<>();
        for (InfoItem item : page1.getItems()) {
            urlsSet.add(item.url);
        }

        for (InfoItem item : page2.getItems()) {
            final boolean wasAdded = urlsSet.add(item.url);
            if (!wasAdded) {
                fail("Same item was on the first and second page item list");
            }
        }
    }

    public static <T extends InfoItem> ListExtractor.InfoItemsPage<T> defaultTestRelatedItems(ListExtractor<T> extractor) throws Exception {
        final ListExtractor.InfoItemsPage<T> page = extractor.initialPage;
        final List<T> itemsList = page.getItems();
        List<Throwable> errors = page.errors;

        defaultTestListOfItems(extractor.service, itemsList, errors);
        return page;
    }

    public static <T extends InfoItem> ListExtractor.InfoItemsPage<T> defaultTestMoreItems(ListExtractor<T> extractor) throws Exception {
        final ListExtractor.InfoItemsPage<T> initialPage = extractor.initialPage;
        assertTrue(initialPage.hasNextPage(), "Doesn't have more items");
        ListExtractor.InfoItemsPage<T> nextPage = extractor.getPage(initialPage.nextPage);
        final List<T> items = nextPage.getItems();
        assertFalse(items.isEmpty(), "Next page is empty");
        assertEmptyErrors("Next page have errors", nextPage.errors);

        defaultTestListOfItems(extractor.service, nextPage.getItems(), nextPage.errors);
        return nextPage;
    }

    public static void defaultTestGetPageInNewExtractor(ListExtractor<? extends InfoItem> extractor, ListExtractor<? extends InfoItem> newExtractor) throws Exception {
        final Page nextPage = extractor.initialPage.nextPage;

        final ListExtractor.InfoItemsPage<? extends InfoItem> page = newExtractor.getPage(nextPage);
        defaultTestListOfItems(extractor.service, page.getItems(), page.errors);
    }

    public static void defaultTestImageCollection(
            @Nullable final Collection<Image> imageCollection) {
        assertNotNull(imageCollection);
        imageCollection.forEach(image -> {
            assertIsSecureUrl(image.url);
            assertGreaterOrEqual(Image.HEIGHT_UNKNOWN, image.height,
                    "Unexpected image height: " + image.height);
            assertGreaterOrEqual(Image.WIDTH_UNKNOWN, image.width,
                    "Unexpected image width: " + image.width);
        });
    }
}

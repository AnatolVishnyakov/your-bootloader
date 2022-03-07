package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YoutubeIETest {

    private final YoutubeIE youtubeIE;
    private final YDProperties ydProperties;

    // https://www.youtube.com/watch?v=OCJi5hVdiZU
    // https://www.youtube.com/watch?v=OGc9W-_C9u0

    @BeforeEach
    void beforeMethod() {
        ydProperties.setDownloadPath("D:\\Trash");
    }

    @Test
    @SneakyThrows
    void realExtract() {
        String url = "https://www.youtube.com/watch?v=OCJi5hVdiZU";
        Path folderData = Paths.get("src/test/resources/testdata/");
        Path webPageFile = folderData.resolve("test_yt_page.html");
        String webPage = new String(Files.readAllBytes(webPageFile));
        YtVideoInfo ytVideoInfo = youtubeIE.realExtract(url, webPage);

        assertEquals("OCJi5hVdiZU", ytVideoInfo.getId());
        assertEquals("Architects - \"Animals\" (Orchestral Version) - Live at Abbey Road", ytVideoInfo.getTitle());
        assertEquals("\"Animals\" (Orchestral Version) by Architects, recorded live at Abbey Road Studios for Amazon Originals. Listen Here: https://architects.ffm.to/amazonoriginal/amazon\n\n\"WARNING: This video has been identified to potentially trigger seizures for people with photosensitive epilepsy. Viewer discretion is advised.\"\n\nArranged and orchestrated by Rosie Danvers\n\nOriginal Version of \"Animals\" by @Architects\u200b from the album 'For Those That Wish To Exist,' available now\nListen to the full album: http://bit.ly/3dVrqVB\u200b\nOrder at https://architects.ffm.to/fttwte\n\nLyrics\n\nI do my best but everything seems ominous\nNot feeling blessed, quite the opposite\nThis shouldn’t feel so monotonous\nIt never rains but it pours\n \nWe’re just a bunch of fucking animals\nBut we’re afraid of the outcome\nDon’t cry to me because the fiction that we’re living in\nSays I should pull the pin\n \nShould I just pull the pin?\n \nI dug my heels\nI thought that I could stop the rot\nThe ground gave way\nNow I’ve lost the plot\nFucked it again\nThat was all I’ve got\nIt never rains but it pours\n \nLife is just a dream within a….\n \nBuried under dirt\nA diamond in the mud\nInfinity is waiting there\n‘Cause nobody can burn a glass cathedral\n \n….dream within a dream within a….\n\nOfficial Site: http://www.architectsofficial.com/\u200b\nFacebook: http://www.facebook.com/architectsuk\u200b\nTwitter: http://www.twitter.com/architectsuk\u200b\nInstagram: http://instagram.com/architects\u200b\n\nEpitaph Records is an artist-first indie label founded in Los Angeles by Bad Religion guitarist, Brett Gurewitz.  Early releases from a variety of punk heavyweights helped launch the 90s punk explosion.  Along the way, Epitaph has grown and evolved creatively while sticking to its mission of helping real artists make great recordings on their own terms.\n\n\n\n#Architects #Epitaph #AmazonOriginal", ytVideoInfo.getDescription());
        assertEquals("2021-03-25", ytVideoInfo.getUploadDate());
        assertEquals("Architects", ytVideoInfo.getUploader());
//        assertEquals("wearearchitects", ytVideoInfo.getUploaderId());
        assertEquals("http://www.youtube.com/user/wearearchitects", ytVideoInfo.getUploaderUrl());
        assertEquals("UCdp-kaIi7YO2WmNQ-LafmpA", ytVideoInfo.getChannelId());
        assertEquals("https://www.youtube.com/channel/UCdp-kaIi7YO2WmNQ-LafmpA", ytVideoInfo.getChannelUrl());
        assertEquals(269, ytVideoInfo.getDuration());
        assertEquals(4301780, ytVideoInfo.getViewCount());
//        assertEquals("None", ytVideoInfo.getAverageRating());
        assertEquals(0, ytVideoInfo.getAgeLimit());
        assertEquals("https://www.youtube.com/watch?v=OCJi5hVdiZU", ytVideoInfo.getWebpageUrl());
//        assertEquals("None", ytVideoInfo.isLive());
        assertEquals(109260, ytVideoInfo.getLikeCount());
        assertEquals("Architects", ytVideoInfo.getChannel());
        assertEquals("Animals", ytVideoInfo.getTrack());
        assertEquals("Architects", ytVideoInfo.getArtist());
        assertEquals("For Those That Wish To Exist", ytVideoInfo.getAlbum());
        assertEquals("Architects", ytVideoInfo.getCreator());
        assertEquals("Animals", ytVideoInfo.getAltTitle());
    }
}
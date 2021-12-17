package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class StreamDownloaderTest {

    private final StreamDownloader streamDownloader;
    private final YDProperties ydProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void init() {
        ydProperties.setDownloadPath(tempDir.toString());
    }

    @Test
    void realDownload() throws Exception {
        streamDownloader.realDownload(
                3,
                "https://rr2---sn-jvhnu5g-c35k.googlevideo.com/videoplayback?expire=1639791257&ei=Oea8YbmZJovs7QS8kbnIDA&ip=46.138.209.28&id=o-ADbAOuwKEVYvJxGYUktymLay4SG4n9IEqlxCrZvYSJ6A&itag=140&source=youtube&requiressl=yes&mh=0_&mm=31%2C29&mn=sn-jvhnu5g-c35k%2Csn-jvhnu5g-n8ve7&ms=au%2Crdu&mv=m&mvi=2&pl=19&initcwndbps=1623750&vprv=1&mime=audio%2Fmp4&ns=QaGVSDVQgl5iTBiFLUeCsRgG&gir=yes&clen=81007369&dur=5100.402&lmt=1537070776729873&mt=1639769335&fvip=4&keepalive=yes&fexp=24001373%2C24007246&c=WEB&n=SrItUWXOxR91WoHP&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRQIgZXTVY36rsFhVVi8H5rzaBCDIVdtVdW21dlaiSts49PwCIQCgTsL0JfUjE7MW0IXHQuh0zTkvRdkG4l_VCHCgjOgl7w%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIhAPhjIevYM_DNFEeYMezB7LfQjpLTAlxUZlKgXXecQ18wAiAFvNWRtZEbZdsDnzBT3cH8s6I8Cf6U-fMtfwpfFh84uw%3D%3D",
                "test",
                10175248L,
                new DefaultHttpHeaders()
        );
    }
}
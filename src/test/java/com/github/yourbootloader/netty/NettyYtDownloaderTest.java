package com.github.yourbootloader.netty;

import com.github.yourbootloader.yt.extractor.legacy.YoutubePageParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.asynchttpclient.netty.channel.ChannelManager.CHUNKED_WRITER_HANDLER;
import static org.asynchttpclient.netty.channel.ChannelManager.HTTP_CLIENT_CODEC;

@Slf4j
public class NettyYtDownloaderTest {

    private static final int CHUNK_SIZE = 8_192 * 6;

    @TempDir
    Path tempDir;
    private File resource;

    @BeforeEach
    @SneakyThrows
    public void init() {
        String url = "https://www.youtube.com/watch?v=cdCwimgOOg8";
        String fileAbsolutePath = "D:\\IdeaProjects\\your-bootloader\\src\\test\\resources\\trash\\yt-url.txt";
        resource = new File(fileAbsolutePath);

        if (!resource.exists() && resource.createNewFile()) {
            YoutubePageParser youtubePageParser = new YoutubePageParser(url);
            List<Map<String, Object>> formats = youtubePageParser.parse();
            Map<String, Object> format = StreamEx.of(formats)
                    .findFirst(fmt -> fmt.get("ext").equals("m4a"))
                    .orElse(null);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileAbsolutePath))) {
                writer.write(((String) Objects.requireNonNull(format).get("url")));
            }
        }
    }

    @Test
    @SneakyThrows
    void ytDownload() {
        String ytUrl = Files.readAllLines(resource.toPath()).get(0);
        Matcher matcher = Pattern.compile("https://(?<host>.*)/").matcher(ytUrl);
        if (!matcher.find()) {
            throw new RuntimeException("Doesn't parse url! " + ytUrl);
        }

        String host = matcher.group("host");
        int port = 443;

        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline()
                                .addLast(HTTP_CLIENT_CODEC, newHttpClientCodec())
//                                .addLast(INFLATER_HANDLER, newHttpContentDecompressor())
                                .addLast(CHUNKED_WRITER_HANDLER, new ChunkedWriteHandler())
                                .addLast(new DownloadClientHandler());
                    }
                });
        ChannelFuture f = bootstrap.connect().sync();
    }

    private ChannelHandler newHttpClientCodec() {
        return new HttpClientCodec(
                8_192,
                8_192,
                8_192,
                false,
                false,
                8_912);
    }

    class DownloadClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            log.info("Channel read: {}", msg.readableBytes());
        }
    }
}

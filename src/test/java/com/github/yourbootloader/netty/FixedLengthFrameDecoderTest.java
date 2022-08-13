package com.github.yourbootloader.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FixedLengthFrameDecoderTest {

    @Test
    void testFramesDecoded1() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }

        ByteBuf input = buf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));
        assertTrue(channel.writeInbound(input.retain()));
        assertTrue(channel.finish());

        ByteBuf read = channel.readInbound();
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = channel.readInbound();
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = channel.readInbound();
        assertEquals(buf.readSlice(3), read);
        read.release();

        assertNull(channel.readInbound());
        buf.release();
    }

    @Test
    void testFramesDecoded2() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 90_000; i++) {
            buf.writeByte(i);
        }

        ByteBuf input = buf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(8_193 * 2));
        channel.writeInbound(input.retain());

        ByteBuf read = channel.readInbound();
        assertEquals(buf.readSlice(8_193 * 2), read);
        read.release();

//        assertFalse(channel.writeInbound(input.readBytes(2)));
//        assertTrue(channel.writeInbound(input.readBytes(7)));
//
//        ByteBuf read = (ByteBuf) channel.readInbound();
//        assertEquals(buf.readSlice(3), read);
//        read.release();
//
//        read = (ByteBuf) channel.readInbound();
//        assertEquals(buf.readSlice(3), read);
//        read.release();
//
//        read = (ByteBuf) channel.readInbound();
//        assertEquals(buf.readSlice(3), read);
//        read.release();
//
//        assertNull(channel.readInbound());
//        buf.release();
    }
}

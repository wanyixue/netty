/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.socket.nio;

import static io.netty.channel.Channels.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.netty.channel.AbstractServerChannel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelSink;
import io.netty.channel.socket.DefaultServerSocketChannelConfig;
import io.netty.channel.socket.ServerSocketChannelConfig;
import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;

final class NioServerSocketChannel extends AbstractServerChannel
                             implements io.netty.channel.socket.ServerSocketChannel {

    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(NioServerSocketChannel.class);

    final ServerSocketChannel socket;
    final Lock shutdownLock = new ReentrantLock();
    volatile Selector selector;
    private final ServerSocketChannelConfig config;

    static NioServerSocketChannel create(ChannelFactory factory,
            ChannelPipeline pipeline, ChannelSink sink) {
        NioServerSocketChannel instance =
                new NioServerSocketChannel(factory, pipeline, sink);
        fireChannelOpen(instance);
        return instance;
    }

    private NioServerSocketChannel(
            ChannelFactory factory,
            ChannelPipeline pipeline,
            ChannelSink sink) {

        super(factory, pipeline, sink);

        try {
            socket = ServerSocketChannel.open();
        } catch (IOException e) {
            throw new ChannelException(
                    "Failed to open a server socket.", e);
        }

        try {
            socket.configureBlocking(false);
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e2) {
                logger.warn(
                        "Failed to close a partially initialized socket.", e2);
            }

            throw new ChannelException("Failed to enter non-blocking mode.", e);
        }

        config = new DefaultServerSocketChannelConfig(socket.socket());
    }

    @Override
    public ServerSocketChannelConfig getConfig() {
        return config;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) socket.socket().getLocalSocketAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public boolean isBound() {
        return isOpen() && socket.socket().isBound();
    }

    @Override
    protected boolean setClosed() {
        return super.setClosed();
    }
}

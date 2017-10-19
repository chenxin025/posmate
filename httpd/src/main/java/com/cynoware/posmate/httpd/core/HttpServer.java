/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.posmate.httpd.core;


import android.util.Log;

import com.cynoware.posmate.httpd.ServerService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;


public class HttpServer {

    public static final String ACTION_SYNC_ORDER = "sync_order";

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));

    private int mPort;

    private Channel mActiveChannel = null;
    private ServerService mContext;
    public static boolean isSSL;

    public interface onServerEventListener {
        void onStarted();
        void onStopped();
    }

    private onServerEventListener mListener = null;

    public HttpServer(ServerService context, int port, onServerEventListener listener) {
        mContext = context;
        mPort = port;
        mListener = listener;
    }


    public void start() throws Exception  {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer(mContext, sslCtx));


            ChannelFuture f= b.bind(PORT).sync();
            if(f.isSuccess()){
                Log.d( "HttpServer", "Server started!");
                if( mListener != null ){
                    mListener.onStarted();
                }
            }

            System.err.println("Open your web browser and navigate to " +
                    (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

//            ch.closeFuture().sync();
            mActiveChannel = f.channel();
            mActiveChannel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            if( mListener != null )
                mListener.onStopped();
        }

    }


    public void stop() {
        Log.d("HttpServer", "Stopping server ... ");

        if (mActiveChannel == null) {
            Log.d("HttpServer", "null ActiveChannel instance");
            return;
        }

//		LSPChannelManager.getInstance().closeAll();
        mActiveChannel.close();
    }

//
//	public static void sendResponse(ChannelHandlerContext ctx, String ref, JSONObject jsonResponse) {
//		if (jsonResponse == null)
//			return;
//
//		Log.v( "HttpServer", "> Response [" + ref + "] " + jsonResponse );
//
//		JSONObject json = new JSONObject();
//		try {
//			json.put("type", "response");
//			json.put("ref", ref);
//			json.put("content", jsonResponse.toString());
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		ctx.channel().writeAndFlush(json);
//	}
}

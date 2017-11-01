/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.netcds.core;


import com.cynoware.netcds.R;
import com.cynoware.netcds.ServerService;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AsciiString;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    private static final int CODE_OK = 0;
    private static final int CODE_NOT_SUPPORT = 1;
    private static final int CODE_EXECUTE_FAIL = 2;

    private static final String MSG_OK = "OK";
    private static final String MSG_NOT_SUPPORT = "Not Support";
    private static final String MSG_EXECUTE_FAIL = "Execute Fail";

    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");
    ServerService mServerService;
    ContentManager mContentManager;
    private ByteBufToBytes reader;

    public HttpServerHandler(ServerService serverService) {
        mServerService = serverService;
        mContentManager = ContentManager.getInstance();
    }

    private HttpRequest mRequest = null;
    private String mContent = null;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {


        if (msg instanceof HttpRequest) {

            mRequest = (HttpRequest) msg;
            HttpLog.Log(mRequest.toString());

            if (HttpHeaders.isContentLengthSet(mRequest)) {
                reader = new ByteBufToBytes((int) HttpHeaders.getContentLength(mRequest));
            }
        }

        if (msg instanceof HttpContent) {
            if (reader != null) {
                HttpContent httpContent = (HttpContent) msg;
                ByteBuf content = httpContent.content();

                reader.reading(content);

                content.release();

                if (reader.isEnd()) {
                    mContent = new String(reader.readFull());
                }
            }

            dispatch(ctx, mRequest, mContent);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    public void dispatch(ChannelHandlerContext ctx, HttpRequest req, String content) {
        String uri = "http://127.0.0.1" + req.uri();
        URL url = null;
        Map param = null;

        try {
            url = new URL(uri);
            param = splitQuery(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url == null)
            return;

        String action = url.getPath();

        String method = req.method().name();

        boolean isPost = (method==null)? false: method.equals("POST");

        if (isPost ) {

            if (action.equals("/")) {
                if (mContent != null && !mContent.isEmpty()) {
                    mServerService.openUrl(mContent);
                } else {
                    mServerService.openUrl("http://127.0.0.1:8080/welcome");
                }

                response(ctx, req, CODE_OK, MSG_OK, null);
            } else {
                response(ctx, req, CODE_NOT_SUPPORT, MSG_NOT_SUPPORT, null);
            }
        }else{
            if (action.equals("/welcome")) {
                doReturnPage(ctx, req, R.raw.welcome);
            } else {
                response(ctx, req, CODE_NOT_SUPPORT, MSG_NOT_SUPPORT, null);
            }
        }
    }


    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        if (query == null)
            return null;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }


    private void response(ChannelHandlerContext ctx, HttpRequest req, int code, String msg, String data) {
        String content = "result({\"code\":" + code;

        if (msg != null && !msg.isEmpty())
            content += ",\"msg\":\"" + msg + "\"";

        if (data != null && !data.isEmpty())
            content += ",\"data\":" + data;

        content += "})";

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content.getBytes()));
        response.headers().set(CONTENT_TYPE, "application/javascript");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());


        if (!HttpUtil.isKeepAlive(req)) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }

//    private void doReturnDeviceID( ChannelHandlerContext ctx, HttpRequest req ){
//
//        String devType = mServerService.getDeviceType();
//        String devID = mServerService.getDeviceID();
//
//        String data = "{\"device_type\":\"" + devType + "\",\"device_id\":\"" + devID + "\"}";
//
//        response(ctx, req, CODE_OK, MSG_OK, data );
//    }


    private void doReturnPage(ChannelHandlerContext ctx, HttpRequest req, int resID) {
        String content = mContentManager.getContent(resID);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content.getBytes()));
        response.headers().set(CONTENT_TYPE, "text/html");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (!HttpUtil.isKeepAlive(req)) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }


}

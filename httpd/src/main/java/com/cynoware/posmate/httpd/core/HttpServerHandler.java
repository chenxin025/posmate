/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.posmate.httpd.core;


import com.cynoware.posmate.httpd.R;
import com.cynoware.posmate.httpd.ServerService;
import com.cynoware.posmate.sdk.cmd.EscCommand;
import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.led.LED;
import com.cynoware.posmate.sdk.listener.ResultCallBack;
import com.cynoware.posmate.sdk.printer.Printer;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AsciiString;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    private static final byte[] CONTENT = {'o', 'k'};

    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");
    ServerService mServerService;
    ContentManager mContentManager;

    public HttpServerHandler(ServerService serverService) {
        mServerService = serverService;
        mContentManager = ContentManager.getInstance();
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {


            HttpRequest req = (HttpRequest) msg;
            HttpLog.Log( req.toString() );

            dispatch(ctx, req);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    public void dispatch( ChannelHandlerContext ctx, HttpRequest req ){
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


        if( url == null )
            return;

        String action = url.getPath();

        if( action.equals("/")){
            doReturnPage(ctx, req, R.raw.pos_web_demo);
        }else if( action.equalsIgnoreCase("/deviceid")) {
            doReturnDeviceID(ctx, req);
        }else if( action.equalsIgnoreCase("/cashdrawer")) {
             doOpenCashDrawer(ctx, req);
        }else if( action.equalsIgnoreCase("/led")){
            doShowLED(ctx, param, req );
        }else if( action.equalsIgnoreCase("/print")){
            doPrint(ctx, param, req );
        }else if( action.equalsIgnoreCase("/scan")){
            doScan(ctx, param, req );
        }
    }


    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        if(query == null)
            return null;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }


    private void responseOK(ChannelHandlerContext ctx, boolean isKeepAlive) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (!isKeepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }

    private void responseFail(ChannelHandlerContext ctx, boolean isKeepAlive) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(CONTENT));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (!isKeepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }

    private void doReturnDeviceID( ChannelHandlerContext ctx, HttpRequest req ){

        String devID = mServerService.getDeviceID();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(devID.getBytes()));
        response.headers().set(CONTENT_TYPE, "text/html");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (!HttpUtil.isKeepAlive(req)) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }


    private void doReturnPage( ChannelHandlerContext ctx, HttpRequest req, int resID ){
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


    private void doOpenCashDrawer(ChannelHandlerContext ctx, HttpRequest req){
            mServerService.openCachDrawer(null, mServerService.getHandler());
            responseOK(ctx, HttpUtil.isKeepAlive(req));
    }


    private void doShowLED(ChannelHandlerContext ctx, Map params, HttpRequest req){

        String paramType = (String)params.get("type");

        int type = -1;
        if( paramType != null ){
            if( paramType.equalsIgnoreCase("price") ){
                type = LED.CMD_PRICE_TYPE;
            }else if( paramType.equalsIgnoreCase("collect") ){
                type = LED.CMD_COLLECT_TYPE;
            }else if( paramType.equalsIgnoreCase("change") ){
                type = LED.CMD_CHANGE_TYPE;
            }else if( paramType.equalsIgnoreCase("total") ){
                type = LED.CMD_TOTAL_TYPE;
            }else if( paramType.equalsIgnoreCase("clear") ){
                type = LED.CMD_INIT_TYPE;
            }
        }

        if( type == -1 ){
            return;
        }

        String paramNum = (type == LED.CMD_INIT_TYPE)? "" : (String)params.get("num");

        mServerService.showLedText(BaseConfig.COM_PORT_0, type, paramNum, null, mServerService.getHandler() );
        mServerService.showLedText(BaseConfig.COM_PORT_1, type, paramNum, null, mServerService.getHandler() );
        mServerService.showLedText(BaseConfig.COM_PORT_2, type, paramNum, null, mServerService.getHandler() );

        responseOK(ctx, HttpUtil.isKeepAlive(req));
    }


    private void doPrint(ChannelHandlerContext ctx, Map params, HttpRequest req){

        String paramContent = (String)params.get("content");
        responseOK(ctx, HttpUtil.isKeepAlive(req));

        if( paramContent == null ){
            return;
        }

        EscCommand escCmd = new EscCommand();

        escCmd.addInitializePrinter();
        escCmd.addSelectMode(Printer.SELECT_CENTER_MODE);
        escCmd.addSetCharset(EscCommand.CHARSET_KOREAN);
        escCmd.add(paramContent.getBytes());
        escCmd.add(new byte[]{0x0D, 0x0A, 0x0D, 0x0A});
        escCmd.addPrintAndFeedPaper( (byte) 3);

        mServerService.printText(escCmd.createCommandBuffer(), null, mServerService.getHandler() );

    }

    private void doScan(final ChannelHandlerContext ctx, Map params, final HttpRequest req){
        mServerService.startScanner(new ResultCallBack() {
            @Override
            public void onFailed() {
                responseFail(ctx, HttpUtil.isKeepAlive(req));
            }

            @Override
            public void onStrResult(String s) {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(s.getBytes()));
                response.headers().set(CONTENT_TYPE, "text/plain");
                response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

                if (!HttpUtil.isKeepAlive(req)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                    ctx.write(response);
                }
            }
        }, mServerService.getHandler());
    }
}

package org.nettyChat.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.nettyChat.process.ChatProcesser;
import org.nettyChat.session.ServerSession;
import org.nettyChat.session.SessionMap;


public class ServerExceptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) throws Exception{
        ServerSession session = ServerSession.getSession(ctx);
        SessionMap.inst().remove(session);
        session.processError(t);
    }

    /**
     * 刷新
     * @param ctx
     * @throws Exception
     */
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        ServerSession session = ServerSession.getSession(ctx);
        SessionMap.inst().remove(session);

        String s = ChatProcesser.inst().onClose(session);//返回的是关闭连接前的一些信息
        SessionMap.inst().sendToAll(s,session);

        SessionMap.inst().closeSession(session);//关闭session
    }
}

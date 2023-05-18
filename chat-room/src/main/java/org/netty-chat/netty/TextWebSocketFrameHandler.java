package org.crazymaker.websocket.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.crazymaker.websocket.process.ChatProcesser;
import org.crazymaker.websocket.session.ServerSession;
import org.crazymaker.websocket.session.SessionMap;

import java.util.Map;

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        ServerSession session = ServerSession.getSession(ctx);
        Map<String, String> result = ChatProcesser.inst().onMessage(msg.text(), session);
        if (result != null && null!=result.get("type"))
        {
            switch (result.get("type"))
            {
                case "msg":
                    SessionMap.inst().sendToOthers(result, session);
                    break;
                case "init":
                    SessionMap.inst().addSession(result, session);
                    break;
            }
        }
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        //WebSocketServerProtocolHandler是Netty中用于实现WebSocket的Handler。
        // 在它内部，定义了一个内部类ServerHandshakeStateEvent，该事件表示WebSocket握手阶段的事件。
        // ServerHandshakeStateEvent中定义了握手的状态事件，包括：HANDSHAKE_ISSUED：握手请求已经发出;HANDSHAKE_COMPLETE：握手已完成，WebSocket握手以后可以开始接受数据和消息。
        //在WebSocketServerProtocolHandler中，当收到一个HTTP连接请求时，会自动创建一个ServerHandshakeStateEvent事件来表示WebSocket握手请求，并通过pipeline传递到下一个Handler中处理。而当WebSocket握手成功后，
        // 又会创建一个ServerHandshakeStateEvent事件来通知下一个Handler WebSocket已经握手成功，并可以进行通信了。
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE)
        {
            // 握手成功，移除 HttpRequestHandler，因此将不会接收到任何消息
            // 并把握手成功的 Channel 加入到 ChannelGroup 中
            ServerSession session = new ServerSession(ctx.channel());
            String echo = ChatProcesser.inst().onOpen(session);
            SessionMap.inst().sendMsg(ctx, echo);
        } else if (evt instanceof IdleStateEvent)
        {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            if (stateEvent.state() == IdleState.READER_IDLE)
            {
                ServerSession session = ServerSession.getSession(ctx);
                SessionMap.inst().remove(session);
                session.processError(null);
            }
        } else
        {
            super.userEventTriggered(ctx, evt);
        }
    }

}

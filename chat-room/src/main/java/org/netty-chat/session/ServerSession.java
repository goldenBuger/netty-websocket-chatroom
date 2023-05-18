package org.crazymaker.websocket.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.crazymaker.websocket.Model.User;
import org.crazymaker.websocket.process.ChatProcesser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * 实现服务器Socket Session会话
 */
@Data
@Slf4j
public class ServerSession {
    public static final AttributeKey<String> KEY_USER_ID =
            AttributeKey.valueOf("key_user_id");

    public static final AttributeKey<ServerSession> SESSION_KEY =
            AttributeKey.valueOf("SESSION_KEY");

    /**
     * 一个session中，保存了用户对象、session的ID，CS的通道，组别这些信息，以及map
     */
    private Channel channel;
    //用户
    private User user;

    //session唯一标示
    private final String sessionId;

    private String group;

    //登录状态
    private boolean isLogin = false;

    private Map<String, Object> map = new HashMap<String, Object>();

    /**
     * Netty中的Channel对象支持添加属性（Attribute），属性可以附加到 Channel 上。
     * 而channel.attr()方法就是用来获取一个 Channel 对应的 AttributeMap 对象，对 AttributeMap进行读写操作可以实现在 Channel 上绑定属性的功能。
     * 这样，在一个Channel中，可以绑定多个属性，并且这些属性可以在ChannelPipeline中的所有Handler中共享使用。
     * 如果需要在 Netty 应用中保存一些状态信息，建议使用Channel属性来存储。
     * @param channel
     */
    public ServerSession(Channel channel)
    {
        this.channel = channel;
        this.sessionId = buildNewSessionId();
        log.info(" ServerSession 绑定会话 " + channel.remoteAddress());
        channel.attr(ServerSession.SESSION_KEY).set(this);
    }


    public static ServerSession getSession(ChannelHandlerContext ctx){
        Channel channel = ctx.channel();
        return channel.attr(ServerSession.SESSION_KEY).get();
    }


    public static ServerSession getSession(Channel channel)
    {
        return channel.attr(ServerSession.SESSION_KEY).get();
    }

    public String getId()
    {
        return sessionId;
    }


    public synchronized void set(String key, Object value)
    {
        map.put(key, value);
    }

    public synchronized <T> T get(String key)
    {
        return (T) map.get(key);
    }

    public boolean isValid()
    {
        return getUser() != null ? true : false;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public void processError(Throwable error)
    {

        /**
         * 处理错误，得到处理结果
         */
        String result = ChatProcesser.inst().onError(this, error);
        /**
         * 发送处理结果到其他的组内用户
         */
        SessionMap.inst().sendToAll(result, this);


        String echo = ChatProcesser.inst().onClose(this);
        /**
         * 关闭连接， 关闭前发送一条通知消息
         */

        SessionMap.inst().closeSession(this, echo);
    }
    /**
     * 通过uuid生成sessionID
     * @return
     */
    private static String buildNewSessionId()
    {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }
}

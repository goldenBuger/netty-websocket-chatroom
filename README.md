# netty-websocket-chatroom
# 基于长连接的异步聊天室
这是一个简单的netty应用实例，主要实现了基于websocket长连接的群聊功能。用到了springbbot+netty+websocket
流程大致可分为三块：使用 Json  传递实体消息；ServerSession 存储了每个会话，保存对 Channel和 User，使用User 表示连接上来用户；前端要求填入用户和房间（群组）后，模拟登录，并返回用户列表。进入后可以发送群组消息。
项目重点在于报文处理器、业务处理器、会话管理三方面的设计。
Netty搭建的服务器基本上都是差不多的写法：绑定主线程组和工作线程组，添加channel上的handler。

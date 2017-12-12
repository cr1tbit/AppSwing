package network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import message.Message;

import java.util.concurrent.BlockingQueue;

public class ConnectionInitializer extends ChannelInitializer<SocketChannel> {

    private BlockingQueue<Message> inQueue;

    public ConnectionInitializer(BlockingQueue<Message> inQueue) {
        this.inQueue = inQueue;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {

        ch.pipeline().addLast(new ObjectEncoder(),
                new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(Message.class.getClassLoader())),
                new ConnectionHandler(inQueue));
    }
}

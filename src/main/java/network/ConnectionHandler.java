package network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import message.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class ConnectionHandler extends SimpleChannelInboundHandler<Message> {

    private BlockingQueue<Message> inQueue;
    private ChannelHandlerContext ctx;
    private int partSize = (int) 0.5 * 1024 * 1024;
    private enum State {
        IDLE, DWN, UPL
    }
    private State state;
    private String filepath;

    public ConnectionHandler(BlockingQueue<Message> inQueue) {
        this.inQueue = inQueue;
        this.state = State.IDLE;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) {
        System.out.println("Otrzymano:");
        System.out.println(msg.toString());
        if (msg.getType() == Message.Type.CHUNK && state == State.DWN)
            fileAppend(filepath, ((MsgFileChunk) msg).getData(), ((MsgFileChunk) msg).getPart() * partSize);
        else
            if (msg.getType() == Message.Type.OK)
                state = State.IDLE;
            try {
                inQueue.put(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        //ChannelFuture f = ctx.writeAndFlush(new Message(Message.Type.REPLY));
        /*String path = FileSystems.getDefault().getPath("uses.conf").toString();
        String user = "domp";
        MsgAddFile addFile = new MsgAddFile(path, user);
        System.out.println(addFile.toString());
        ChannelFuture f = ctx.writeAndFlush(addFile);
        //ChannelFuture f = ctx.writeAndFlush(new MsgPing());
        //ChannelFuture f = ctx.writeAndFlush(new UnixTime());
        f.addListener(ChannelFutureListener.CLOSE);*/

    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
        ctx.writeAndFlush(new MsgLogin("dominik","passwd"));

        int test = 2;
        if(test == 1) {
            try {
                RandomAccessFile file = new RandomAccessFile("p.gif", "rw");
                ctx.writeAndFlush(new MsgAddFile("p.gif", "dominik", file.length()));
                int parts = (int) (file.length() + partSize) / partSize;
                System.out.println(file.length() + " " + partSize + " " + parts);
                for (int currPart = 0; currPart < parts; currPart++) {
                    byte[] data = getPart(file, currPart);
                    ctx.writeAndFlush(new MsgFileChunk(data, currPart));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            ctx.writeAndFlush(new MsgGetFile("p.gif", "dominik"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void send(Message m) {
        ctx.writeAndFlush(m);
    }

    public void getFile(MsgGetFile msg) {
        filepath = msg.getPath();
        state = State.DWN;
        ctx.writeAndFlush(msg);
    }

    public void sendFile(MsgAddFile msg) {
        filepath = msg.getPath();
        state = State.UPL;
        ctx.writeAndFlush(msg);

        try {
            RandomAccessFile file = new RandomAccessFile(filepath, "rw");
            int parts = (int) (file.length() + partSize) / partSize;
            //System.out.println(file.length() + " " + partSize + " " + parts);
            for (int currPart = 0; currPart < parts; currPart++) {
                byte[] data = getPart(file, currPart);
                ctx.writeAndFlush(new MsgFileChunk(data, currPart));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fileAppend(String pathstr, byte[] buff, int pos) {
        try {
            Path path = Paths.get(pathstr);
            RandomAccessFile file = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");
            file.seek(pos);
            file.write(buff);
            file.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public byte[] getPart(RandomAccessFile file, int part) {
        try {
            if (part * partSize >= file.length()) {
                throw new IllegalArgumentException("Argument part jest zbyt duzy.");
            }
            file.seek(part * partSize);
            byte[] buff = new byte[partSize];
            int n = file.read(buff);
            byte[] slice = Arrays.copyOfRange(buff, 0, n);
            return slice;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}

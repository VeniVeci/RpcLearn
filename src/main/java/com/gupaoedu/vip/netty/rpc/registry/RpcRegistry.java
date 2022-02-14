package com.gupaoedu.vip.netty.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.junit.Test;

public class RpcRegistry {
    /*
    新建一个注册中心并启动
     */
    private int port; // 服务发布的端口  例如8080
    public RpcRegistry(int port){
        this.port = port;
    }
    /*
          利用netty新建一个服务器（注册中心） 启动该服务器并监听port端口
     */
    public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 非阻塞的事件循环组 里面有多个事件循环  bossGroup负责
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {// 信道初始化 主要是在该信道中添加一些处理器
                ChannelPipeline pipeline = ch.pipeline();
                //对象参数类型编码器
                pipeline.addLast("encoder", new ObjectEncoder()); // netty中的编码器 把对象转化为Byte 然后进行网络传输
                //对象参数类型解码器  ObjectDecoder extends LengthFieldBasedFrameDecoder
                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));// netty中的解码器 把Byte转化为对象
                // 在信道初始化时就 进行服务的注册
                RegistryHandler registryHandler = new RegistryHandler();
                pipeline.addLast(registryHandler);
            }
        };
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
            		.channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = b.bind(port).sync(); // 绑定端口
            System.out.println("RPC Registry has started and is listening at " + port );
            future.channel().closeFuture().sync();
        } catch (Exception e) {
             bossGroup.shutdownGracefully();
             workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new RpcRegistry(8080).start();// 新建一个端口号为8080的注册中心 并启动
    }
}

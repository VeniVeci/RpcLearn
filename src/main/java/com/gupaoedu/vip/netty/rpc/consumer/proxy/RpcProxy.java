package com.gupaoedu.vip.netty.rpc.consumer.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.gupaoedu.vip.netty.rpc.protocol.InvokerProtocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RpcProxy {
	// PRC代理类
	public static <T> T create(Class<?> clazz){
		// 返回这个实例 方便客户端使用
        MethodProxy proxy = new MethodProxy(clazz);
        Class<?> [] interfaces = clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces();// 如果clazz.isInterface() 就找到所有的实现类 否则就直接使用该接口
        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(),interfaces,proxy);
        return result;
    }
	// new MethodProxy(IRpcHelloService.class);
	private static class MethodProxy implements InvocationHandler {
		private Class<?> clazz;  //clazz = IRpcHelloService.class
		public MethodProxy(Class<?> clazz){
			this.clazz = clazz;//IRpcHelloService.class
		}
		// method:hello
		// args: Tom老师
		public Object invoke(Object proxy, Method method, Object[] args)  throws Throwable {
			//如果传进来是一个已实现的具体类（本次演示略过此逻辑)
			if (Object.class.equals(method.getDeclaringClass())) {
				try {
//					System.out.println("LPC");
					return method.invoke(this, args);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				//如果传进来的是一个接口（核心)
			} else {
//				System.out.println("RPC");
				return rpcInvoke(proxy, method, args);
			}
			return null;
		}
		/**
		 * 实现接口的核心方法
		 *  把调用的方法封装为消息  与服务器建立连接  发送消息得  在服务器上调用该方法  返回对应的值
		 * @param method
		 * @param args
		 * @return
		 */
		public Object rpcInvoke(Object proxy,Method method,Object[] args){
			//传输协议封装 调用的方法 参数 成一个InvokerProtocol 对象 方便后面编解码
			InvokerProtocol msg = new InvokerProtocol();
			msg.setClassName(this.clazz.getName()); //IRpcHelloService
			msg.setMethodName(method.getName());    //hello
			msg.setParames(method.getParameterTypes()); // string
			msg.setValues(args);  // zhangsan
			final RpcProxyHandler consumerHandler = new RpcProxyHandler();

			EventLoopGroup group = new NioEventLoopGroup();
			try {
				Bootstrap b = new Bootstrap();
				b.group(group)
						.channel(NioSocketChannel.class)
						.option(ChannelOption.TCP_NODELAY, true)
						.handler(new ChannelInitializer<SocketChannel>() {
							@Override
							public void initChannel(SocketChannel ch) throws Exception {
								ChannelPipeline pipeline = ch.pipeline();
								//对象参数类型编码器
								pipeline.addLast("encoder", new ObjectEncoder());
								//对象参数类型解码器
								pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
								pipeline.addLast("handler",consumerHandler);
							}
						});

				ChannelFuture future = b.connect("localhost", 8080).sync();
				future.channel().writeAndFlush(msg).sync();// 建立连接之后进行编解码 然后传输 等待结果
				future.channel().closeFuture().sync();// 在这里等待closeFuture
			} catch(Exception e){
				e.printStackTrace();
			}finally {
				group.shutdownGracefully();// 最后关闭这个请求连接
			}
			return consumerHandler.getResponse();// 在建立连接的时候已经将响应存储在了consumerHandler中
		}

	}
}




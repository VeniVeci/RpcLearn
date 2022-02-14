package com.gupaoedu.vip.netty.rpc.registry;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.gupaoedu.vip.netty.rpc.protocol.InvokerProtocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RegistryHandler  extends ChannelInboundHandlerAdapter {
	//用来保存所有可用的服务
    public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String,Object>();
    //保存所有相关的服务类
    private List<String> classNames = new ArrayList<>();

    public RegistryHandler(){
    	//递归扫描provider文件夹，其中所有服务的全类名都被保存在了classNames这个链表中
		// 比如com.gupaoedu.vip.netty.rpc.provider.RpcHelloServiceImpl
    	scannerClass("com.gupaoedu.vip.netty.rpc.provider");
    	//将classNames这个链表中的服务 注册到registryMap中
    	doRegister();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	Object result = new Object();
        InvokerProtocol request = (InvokerProtocol)msg;
        //当客户端建立连接时，需要从自定义协议中获取信息，拿到具体的服务和实参
		//使用反射调用
        if(registryMap.containsKey(request.getClassName())){
        	Object clazz = registryMap.get(request.getClassName());
        	Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParames());
        	result = method.invoke(clazz, request.getValues());
        	// clazz.method( request.getParames()(type)  request.getValues())
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();// 关闭该通道
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         cause.printStackTrace();
         ctx.close();
    }
    /*
     * 递归扫描
     */
	private void scannerClass(String packageName){
		URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		for (File file : dir.listFiles()) {
			//如果是一个文件夹，继续递归
			if(file.isDirectory()){
				scannerClass(packageName + "." + file.getName());
			}else{
				classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
			}
		}
	}
	/**
	 * 完成注册
	 */
	private void doRegister(){
		if(classNames.size() == 0){ return; }
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className); // 实现类
				Class<?> i = clazz.getInterfaces()[0];    // 上层接口
				registryMap.put(i.getName(), clazz.newInstance());// 类名 服务名 和 对应的服务实例
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

package com.gupaoedu.vip.netty.rpc.consumer;

import com.gupaoedu.vip.netty.rpc.api.IRpcService;
import com.gupaoedu.vip.netty.rpc.api.IRpcHelloService;
import com.gupaoedu.vip.netty.rpc.consumer.proxy.*;
import com.gupaoedu.vip.netty.rpc.provider.RpcHelloServiceImpl;
import com.gupaoedu.vip.netty.rpc.provider.RpcServiceImpl;

public class RpcConsumer {
    public static void main(String [] args){
        // 如果想要使用RpcHelloServiceImpl中的hello方法
        //         和 RpcServiceImpl中的4个算术方法
        // 可以直接新建对应的对象并使用  也可以是远程调用

        // 本地调用示例
        System.out.println("本地调用示例");
        IRpcHelloService LpcHello = new RpcHelloServiceImpl();
        System.out.println(LpcHello.hello("Tom老师"));

        IRpcService LpcService = new RpcServiceImpl();
        System.out.println("8 + 2 = " + LpcService.add(8, 2));
        System.out.println("8 - 2 = " + LpcService.sub(8, 2));
        System.out.println("8 * 2 = " + LpcService.mult(8, 2));
        System.out.println("8 / 2 = " + LpcService.div(8, 2));

        System.out.println("=======================");
        // 远程调用示例
        System.out.println("远程调用示例");
        IRpcHelloService RpcHello = RpcProxy.create(IRpcHelloService.class);
        System.out.println(RpcHello.hello("Tom老师"));

        IRpcService RpcService = RpcProxy.create(IRpcService.class);
        System.out.println("8 + 2 = " + RpcService.add(8, 2));
        System.out.println("8 - 2 = " + RpcService.sub(8, 2));
        System.out.println("8 * 2 = " + RpcService.mult(8, 2));
        System.out.println("8 / 2 = " + RpcService.div(8, 2));
    }


}

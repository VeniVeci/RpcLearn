package com.gupaoedu.vip.netty.rpc.consumer;

import com.gupaoedu.vip.netty.rpc.api.IRpcHelloService;
import com.gupaoedu.vip.netty.rpc.consumer.proxy.RpcProxy;
import com.gupaoedu.vip.netty.rpc.provider.RpcHelloServiceImpl;

/**
 * @Description
 * @create 2022/2/13 - 21:20
 */
public class TestLpcRpc {
    public static void main(String[] args) {
        int num = 100;
        Lpc(num);
        Rpc(num);
    }
    public static void Lpc(int num){
        long startTime = System.currentTimeMillis(); //获取开始时间
        for (int i = 0; i < num; i++) {
            IRpcHelloService LpcHello = new RpcHelloServiceImpl();
            LpcHello.hello("Tom老师");
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("Lpc程序运行时间：" + (endTime - startTime) + "ms");
    }
    public static void Rpc(int num){
        long startTime = System.currentTimeMillis(); //获取开始时间
        for (int i = 0; i < num; i++) {
            IRpcHelloService RpcHello = RpcProxy.create(IRpcHelloService.class);
            RpcHello.hello("Tom老师");
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("Rpc程序运行时间：" + (endTime - startTime) + "ms");
    }
}

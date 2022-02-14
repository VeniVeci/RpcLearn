package com.gupaoedu.vip.netty.rpc.provider;

import com.gupaoedu.vip.netty.rpc.api.IRpcHelloService;
// 类名 RpcHelloServiceImpl 函数名 hello
// 形参列表 name 实参列表 张三
public class RpcHelloServiceImpl implements IRpcHelloService {

    public String hello(String name) {
        return "Hello " + name + "!";
    }

}

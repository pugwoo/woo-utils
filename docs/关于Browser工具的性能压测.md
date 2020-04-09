_(2019年12月27日 08:55:12)_

我看到有些woo-utils的用户会使用Browser来做简单的http url转发，做类似网关gateway的功能，示例代码如下，这样访问服务器的`/to`就等于访问了另外一个url（演示暂不涉及头部信息等）：

```java
// 由Spring mvc提供web服务

@RestController
public class ProxyController {

    @GetMapping("/to")
    public String toSomewhere() {
        String url = "http://某ip:某端口/路径";
        Browser browser = new Browser();
        // 复杂情况可以拿请求的头部设置到browser的请求头部中
        return browser.get(url).getContentString();
    }
    
}
```

这个功能是没有问题的，但这样的用法只适合于并发量小的场景，原因后面会说明。那么如果是并发量小，那性能怎样呢？Browser会成为性能瓶颈吗？下面做了一个测试。

测试环境是一台常规配置的linux服务器，使用docker启动最新nginx镜像，压测其静态index文件。

直接请求的压测数据，1000并发数请求1000万次：
```bash
bombardier -n 10000000 -c 1000 http://ip:port/

Statistics        Avg      Stdev        Max
  Reqs/sec     14182.57    2403.19   32650.91
  Latency       64.98ms     1.75s       3.49m
  HTTP codes:
    1xx - 0, 2xx - 9998357, 3xx - 0, 4xx - 0, 5xx - 0
    others - 1643
  Throughput:    12.39MB/s
```

如果是走代理，1000并发数请求1000万次：
```bash
bombardier -n 10000000 -c 1000 http://ip:port/

Statistics        Avg      Stdev        Max
  Reqs/sec     13122.42    2122.92   27187.88
  Latency       76.20ms    52.40ms      7.14s
  HTTP codes:
    1xx - 0, 2xx - 10000000, 3xx - 0, 4xx - 0, 5xx - 0
    others - 0
  Throughput:    9.94MB/s
```

结论：Browser本身的稳定性非常高，性能达到直接请求的92.5%，可以支持1万以上qps的请求。

但是：最开始的方案是放在servlet容器中跑的，天然受到servlet容器线程数的约束，而Browser一般也是在线程中阻塞地执行，所以对于后台请求比较慢、不稳定，或者高并发的场景，这个技术方案并不适用。然而，由于这种方案非常简单也好理解，在中低负载和应急情况下也是可行的。
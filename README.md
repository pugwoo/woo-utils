# woo-utils

Java常用工具包，提供字符串、集合、IO、网络、JSON、YAML等常用功能的封装。

## Maven依赖

```xml
<dependency>
    <groupId>com.pugwoo</groupId>
    <artifactId>woo-utils</artifactId>
    <version>1.3.12</version>
</dependency>
```

## 主要功能模块

### collect - 集合工具
- `SortingUtils`: 友好的排序工具，支持多字段排序和null值处理
- `ListUtils`: List工具方法
- `MapUtils`: Map工具方法  
- `MergeSortUtils`: 归并排序工具

### string - 字符串工具
- `StringTools`: 字符串判空、判断空白等常用方法
- `RegexUtils`: 正则表达式工具
- `Base64`: Base64编解码
- `Hash`: MD5、SHA等哈希算法

### json - JSON处理
- `JSON`: JSON序列化和反序列化，支持多种日期格式
- 内置优化的ObjectMapper配置

### yaml - YAML处理  
- `YAML`: YAML序列化和反序列化

### net - 网络工具
- `Browser`: 功能强大的HTTP客户端，支持GET/POST、异步请求、文件上传、Cookie管理
- `NetUtils`: 网络相关工具方法

### io - IO工具
- `IOUtils`: 文件读写、流复制等操作
- `BinaryReader`: 二进制数据读取

### task - 任务管理
- `EasyRunTask`: 简单的任务控制框架，支持开始、停止、恢复、多线程执行

### thread - 线程工具  
- `ThreadPoolUtils`: 线程池创建和管理，支持MDC上下文继承

### lang - 语言增强
- `DateUtils`: 日期时间处理工具
- `NumberUtils`: 数字工具
- `EqualUtils`: 相等性判断工具

### 其他模块
- `algorithm`: 算法工具（动态进制转换等）
- `compress`: 压缩工具（ZIP压缩解压）  
- `tree`: 树形结构工具
- `log`: 日志工具（MDC工具）

## 详细文档

更多使用说明请参见 [docs/常用功能.md](docs/常用功能.md)

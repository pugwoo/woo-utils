# woo-utils
我自己的常用工具包。使用jdk1.8。除默认依赖外，所有第三方依赖，都需要使用者自行引入对应依赖，详见文档或pom.xml文件中scope为provided的依赖。

```xml
<dependency>
    <groupId>com.pugwoo</groupId>
    <artifactId>woo-utils</artifactId>
    <version>1.2.2</version>
</dependency>
```

### 1. 默认依赖的jar包(主要是一些常用、轻入侵、稳定的jar)：

提供json的基本功能：

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```


# woo-utils
我自己的常用工具包。使用jdk1.8。除默认依赖外，所有第三方依赖，都需要使用者自行引入对应依赖，详见文档或pom.xml文件中scope为provided的依赖。

```xml
<dependency>
    <groupId>com.pugwoo</groupId>
    <artifactId>woo-utils</artifactId>
    <version>1.0.1</version>
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

### 2. 需要使用者根据实际使用的功能自行导入的jar包：

servlet一般由运行容器提供，实际项目中也不用特别提供，所以使用provided方式引入：

```xml
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
</dependency>
```

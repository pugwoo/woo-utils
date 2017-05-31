# woo-utils
我自己的常用工具包。

## collect

还在为排序写Comparator，疑惑应该返回-1还是1吗？实际上，最友好的排序写法，应该是像SQL语句一样，直接指定order by一个可以比较大小的值。collect中的SortingUtils就可以这样来用。

而且，你还不用担心List中烦人的null值，你只需要指定null排在最前面还是最后面就可以了。

而且，排序还支持按多个值来排序，例如先按年龄排，再按收入排。

# Redis

为什么选择Redis？在主流分布式系统中，常用Redis作为分布式缓存，来存放登录态等信息。由于Redis的持久化特性和广泛被使用的运维基础，因此Redis会和MySQL一样的地位一样成为首选必选的存储方案。

我倾向于维护尽可能少的服务器。Zookeeper作为分布式的配置和分布式事务管理也是非常不错的方案，但没有办法充当redis的功能。而Redis则可以基本实现Zookeeper的特性。此外Redis是C程序，在Linux上少开一个Java进程将节省不少内存空间。因此作为服务器，我倾向于使用Redis代替Zookeeper。

在这个项目中，我使用Redis实现了关于事务非常常用的两种功能：

1. 限制分布式系统在指定的单位时间内，对某个业务的请求次数。参见limit.

2. 分布式锁。参见transaction。





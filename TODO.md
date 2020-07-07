## 开发计划

1. redis helper消息队列支持批量发送、清理队列【done】
2. regex utils支持replaceGroup【done】
3. 高速缓存支持LRU，设置缓存个数上限，使用SynchronizedSortedMap代替TreeMap

## 兼容性问题

1. (2019年11月7日 14:32:31) 目前使用jackson 2.10.0，MyObjectMapper中有4个feature被标记为已废弃，但是这几个在jackson 2.10.0以下版本并没有出现，所以为了兼容旧版本，这几个暂不换掉。等待2.10.0发布一年之后，即2020年9月份再替换掉；或者jackson新版本中已经移除了这些定义，再替换掉。

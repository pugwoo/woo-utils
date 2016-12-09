# woo-utils
我自己的常用工具包。不到万不得已，我不会自己多些一个工具。推荐使用Guava和Apache Commons工具类。

## collect

还在为排序写Comparator，疑惑应该返回-1还是1吗？实际上，最友好的排序写法，应该是像SQL语句一样，直接指定order by一个可以比较大小的值。collect中的SortingUtils就可以这样来用。

而且，你还不用担心List中烦人的null值，你只需要指定null排在最前面还是最后面就可以了。

而且，排序还支持按多个值来排序，例如先按年龄排，再按收入排。





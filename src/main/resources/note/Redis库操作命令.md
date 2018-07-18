#Redis数据库操作
Redis支持五种数据类型：string（字符串），hash（哈希），list（列表），set（集合）及zset(sorted set：有序集合)。
+ String 最常用的类型 只是一个String而已 (key-String)
+ hash 类似java中 Map 类型 (key-[field-value,field-value,...])
+ list 类似java中 List 类型 (key-[value,value,...])
+ set 类似java中 Set 类型 不重复的List (key-[value1,value2,....])
+ zset 支持排序的Set类型 (key-[{value1,score},{value2,score},...])

####具体操作方式参考文档 http://www.runoob.com/redis/redis-tutorial.html
####可视化工具 Redis Desktop Manager : https://redisdesktop.com

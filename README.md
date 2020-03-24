# FstDiskLruCache

##  一.本库包括内存缓存工具类(MemoryLruCacheUtils)和磁盘缓存工具类(DiskLruCacheUtils),磁盘缓存相关类来自Jake Wharton大神,并追加了第三段所描述的功能

##  二.磁盘缓存的序列化和反序列化采用fst,简介如下
全称Fast-serialization,地址:https://github.com/RuedigerMoeller/fast-serialization.git
官方自夸:up to 10 times faster 100% JDK Serialization compatible drop-in replacement (Ok, might be 99% ..)

##  三.本库还有一大亮点:磁盘缓存zip文件的同时,一次性缓存zip包中所有文件
比如这种需求:
一本书电子化后,每一页都是一个可序列化文件（不包括图片，与图片共同展示）。在移动端展示时，为了翻页体验更好更流畅（或者校园网太差老师想在家下载好再带到学校），
产品经理叫你在展示这本书之前先将所有页都加载下来，再进入展示场景。此时如果按页循环下载显然太慢，这种可序列化的文档往往能被压缩为原来的四分之一，所以采取
下载所有页对应的电子化文件压缩包的方式;此时你会发现,当你下载解压后,其中的每一页的电子化文件并没有在lru机制内,重新再每一页都走一遍DiskLruCache的缓存写操作??

那是不可能的!这种速度,岂不是...去幼稚园的车?我们不开这种车:)
读完DiskLruCache的源码发现,其关键操作只有三步:将某文件写到指定的统一的磁盘缓存目录中,将该文件按指定规则重命名,在journal文件中做一次记录。
所以我们的车应该这么开：
###  1.将某本书的所有页对应的电子化文件保存到oss（或私服）的某路径下
###  2.将该路径下的所有电子化文件压缩打包成XXX.zip
###  3.移动端下载zip文件后通过DiskLruCache缓存起来
###  4.将该zip文件解压到当前目录（磁盘缓存的指定目录）
###  5.将解压出来的文件安装指定规则重命名
###  6.到journal文件中做相应记录
细节见源码，您到站了！谢谢star!


##  使用姿势
###  1.项目根目录的gradle文件
buildscript.repositories{ maven { url "https://jitpack.io" } }

allprojects.repositories{ maven { url "https://jitpack.io" } }

###  2.module的gradle文件
implementation 'com.github.jj532655203:FstDiskLruCache:1.1.3'


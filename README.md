<h1>项目简介</h1>
1：mdrill是阿里妈妈-adhoc-海量数据多维自助即席查询平台下的一个子项目。<br>
2：mdrill旨在帮助用户在几秒到几十秒的时间内，分析百亿级别的任意维度组合的数据。<br>
3：mdrill是一个分布式的在线分析查询系统，基于hadoop,lucene,solr,jstorm等开源系统作为实现，基于SQL的查询语法。 mdrill是一个能够对大量数据进行分布式处理的软件框架。mdrill是快速的高性能的，他的底层因使用了索引、列式存储、以及内存cache等技术，使得数据扫描的速度大为增加。mdrill是分布式的，它以并行的方式工作，通过并行处理加快处理速度。<br>
4：基于mdrill应用的adhoc项目，使用了10台机器,存储了400亿的数据<br>
&nbsp;&nbsp;==&gt;每次扫描30亿的行数，响应时间在20秒~120秒左右(取决不同的查询条件与扫描的列数)。<br>
&nbsp;&nbsp;==&gt;对100亿数据进行count(*),耗时为2秒，单列sum耗时在25秒,按照日期分组求count和sum耗时47秒，按照用户id分组并且按照成交笔数排序去TopN 耗时 243秒。<br>

<h1> 发行日志</h1>
 2013.07.24 version 0.18-beta  初始化版本  <br>
 2013.08.07 version 0.18.1-beta bug fix <a href="https://github.com/alibaba/mdrill/wiki/018_1">see detail</a>  <br>
 2013.08.17 version 0.18.2-beta speed up <a href="https://github.com/alibaba/mdrill/wiki/018_2">see detail</a>  (<a href="http://yunpan.cn/QXn4tRAzx8NIL" target="_blank">下载</a>) <br>
 2013.09.01 version 0.19-alpha HA by replication <a href="https://github.com/alibaba/mdrill/wiki/019_alpha">see detail</a>  (此版本需要一定时间的测试与调整，慎用) <br>
 2013.09.26 version 0.19.1-beta Bug Fix <a href="https://github.com/alibaba/mdrill/wiki/019_beta_1">see detail</a>  (<a href="http://yunpan.cn/QGprqdtDD7r2x" target="_blank">下载</a>) <br>
 2013.09.29 version 0.19.2-beta Bug Fix (<a href="http://yunpan.cn/QGC7TX2tupBcn" target="_blank">下载</a>) <br>
 2013.10.09 version 0.19.3-beta speed up (此版本有严重BUG,请勿使用,<a href="http://yunpan.cn/QbreqTZbusDtu" target="_blank">下载</a>) <br>
 2013.10.13 version 0.19.4-beta mergerServer优化&&bugfix (推荐版本,<a href="http://yunpan.cn/Qbk4ebcjuUyjL" target="_blank">下载</a>,依赖的zeromq从<a href="http://yunpan.cn/QGp3QIMaBbnpy" target="_blank">这里下载</a>) <br>
 2013.11.19 version 0.20.1-alpha 使用hdfs进行检索&&实时append <a href="https://github.com/alibaba/mdrill/wiki/020_1">see detail</a>(alpha版本，慎用。 <a href="http://yunpan.cn/QUypgcZpmvwwv" target="_blank">源码下载</a>) <br>

https://github.com/alibaba/mdrill/wiki/020_1

 
 <h1>版本源码路径</h1>
 https://github.com/alibaba/mdrill/tree/master/release  <br>


<h1>资源列表</h1>
<ul>
<li><a href="https://github.com/alibaba/mdrill/wiki/info" target="_blank">mdrill介绍</a></li>
<li><a href="https://github.com/alibaba/mdrill/blob/master/doc/INSTALL.docx?raw=true" target="_blank">安装部署</a></li>
<li><a href="https://github.com/alibaba/mdrill/blob/master/doc/MSql.docx?raw=true" target="_blank">sql使用手册</a></li>
<li><a href="https://github.com/alibaba/mdrill/wiki/plan" target="_blank">版本开发计划</a></li>

<li><a hfef="http://www.apache.org/licenses/LICENSE-2.0" target="_blank">LICENSE</a></li>
</ul>

<h1>mdrill Core contributors</h1>
<ul>
<li><a href="https://github.com/muyannian">母延年(子落)</a>、<a href="http://user.qzone.qq.com/2253209">秦剑(含光)</a>、<a href="https://github.com/bwzheng2010">郑博文(士远)</a>、陈鹏(伯时)、木晗、逸客、张壮、凌凝</li>
</ul>
<h1>jstorm Core contributors <a href="https://github.com/alibaba/jstorm" target="_blank">点击进入</a></h1>
<ul>
<li><a href="https://github.com/longdafeng">封仲淹(纪君祥)</a>、<a href="https://github.com/tumen">李鑫(丙吉)</a>、<a href="https://github.com/muyannian">母延年(子落)</a>、<a href="https://github.com/zhouxinxust">周鑫(陈均)</a></li>



</ul>


<h1>其他</h1>
<ul>
<li><a href="https://github.com/alibaba/mdrill/wiki/faq" target="_blank">FAQ</a></li>
<li>mdrill技术交流群:171465049</li>
<li>微博：<a href="http://weibo.com/mynyannian" >http://weibo.com/mynyannian</a></li>

</ul>

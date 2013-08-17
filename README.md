<h1>项目简介</h1>
1：mdrill是阿里妈妈-adhoc-海量数据多维自助即席查询平台下的一个子项目。<br>
2：mdrill旨在帮助用户在几秒到几十秒的时间内，分析百亿级别的任意维度组合的数据。<br>
3：mdrill是一个分布式的在线分析查询系统，基于hadoop,lucene,solr,jstorm等开源系统作为实现，基于SQL的查询语法。 mdrill是一个能够对大量数据进行分布式处理的软件框架。mdrill是快速的高性能的，他的底层因使用了索引、列式存储、以及内存cache等技术，使得数据扫描的速度大为增加。mdrill是分布式的，它以并行的方式工作，通过并行处理加快处理速度。<br>
4：基于mdrill应用的adhoc项目，使用了10台机器,存储了400亿的数据<br>
&nbsp;&nbsp;==&gt;每次扫描30亿的行数，响应时间在20秒~120秒左右(取决不同的查询条件与扫描的列数)。<br>
&nbsp;&nbsp;==&gt;对100亿数据进行count(*),耗时为2秒，单列sum耗时在25秒,按照日期分组求count和sum耗时47秒，按照用户id分组并且按照成交笔数排序去TopN 耗时 243秒。<br>

<h1> 发行日志</h1>
 2013.7.24 version 0.18-beta  初始化版本  <br>
 2013.8.07 version 0.18.1-beta bug fix <a href="https://github.com/alibaba/mdrill/wiki/018_1">see detail</a>  <br>
 2013.8.17 version 0.18.2-beta speed up <a href=" https://github.com/alibaba/mdrill/wiki/018_2">see detail</a>  (推荐版本) <br>

 
 <h1>版本源码路径</h1>
 https://github.com/alibaba/mdrill/tree/master/release  <br>


<h1>资源列表</h1>
<ul>
<li><a href="https://github.com/alibaba/mdrill/wiki/info" target="_blank">mdrill介绍</a></li>
<li><a href="https://github.com/alibaba/mdrill/blob/master/doc/INSTALL.docx?raw=true" target="_blank">安装部署</a></li>
<li><a href="https://github.com/alibaba/mdrill/blob/master/doc/MSql.docx?raw=true" target="_blank">sql使用手册</a></li>
<li><a href="https://github.com/alibaba/mdrill/wiki/plan" target="_blank">版本开发计划</a></li>
<li><a href="https://github.com/muyannian/higo/blob/master/doc/doc.rar?raw=true" target="_blank">文档逐步整理中，之前的海狗ppt请看这里</a></li>
<li><a href="https://github.com/muyannian/higo" target="_blank">mdrill之前的旧文档以及开发日志入口</a></li>
<li><a href="https://github.com/alibaba/mdrill/blob/master/doc/improve.docx?raw=true" target="_blank">adhoc项目我们一年走过的路</a></li>

<li><a hfef="http://www.apache.org/licenses/LICENSE-2.0" target="_blank">LICENSE</a></li>
</ul>

<h1>mdrill Core contributors</h1>
<ul>
<li><a href="https://github.com/muyannian">母延年(子落)</a>、<a href="http://user.qzone.qq.com/2253209">秦剑(含光)</a>、<a href="https://github.com/bwzheng2010">郑博文(士远)</a>、陈鹏(伯时)、木晗、逸客、张壮、凌凝</li>
</ul>
<h1>jstorm Core contributors</h1>
<ul>
<li><a href="https://github.com/longdafeng">封仲淹(纪君祥)</a>、<a href="https://github.com/tumen">李鑫(丙吉)</a>、<a href="https://github.com/muyannian">母延年(子落)</a>、<a href="https://github.com/zhouxinxust">周鑫(陈均)</a></li>



</ul>


<h1>其他</h1>
<ul>
<li><a href="https://github.com/alibaba/mdrill/wiki/faq" target="_blank">FAQ</a></li>
<li>mdrill技术交流群:171465049</li>
<li>微博：<a href="http://weibo.com/mynyannian" >http://weibo.com/mynyannian</a></li>

</ul>

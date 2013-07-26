<h1>项目简介</h1>
1：mdrill是阿里妈妈-adhoc-海量数据多维自助即席查询平台下的一个子项目。<br>
2：mdrill旨在帮助用户在几秒到几十秒的时间内，分析百亿级别的任意维度组合的数据。<br>
3：mdrill是一个分布式的在线分析查询系统，基于hadoop,lucene,solr,jstorm等开源系统作为实现，基于SQL的查询语法。 mdrill是一个能够对大量数据进行分布式处理的软件框架。mdrill是快速的高性能的，他的底层因使用了索引、列式存储、以及内存cache等技术，使得数据扫描的速度大为增加。mdrill是分布式的，它以并行的方式工作，通过并行处理加快处理速度。<br>
4：基于mdrill应用的adhoc项目，使用了10台机器，存储了400亿的数据，每次扫描30亿的行数，响应时间在20秒~120秒左右(取决不同的查询条件)。<br>
<h1> 发行日志</h1>
 2013.7.24 version 0.18-beta 存储路径<a href="https://github.com/alibaba/mdrill/tree/master/release/0.18-beta" target="_blank">https://github.com/alibaba/mdrill/tree/master/release/0.18-beta</a>  <br>

<h1>资源列表</h1>
<ul>
<li><a href="https://github.com/alibaba/mdrill/wiki/info" target="_blank">mdrill介绍</a></li>
<li><a href="https://github.com/alibaba/mdrill/blob/master/doc/INSTALL.docx?raw=true" target="_blank">安装部署</a></li>
<li><a href="https://github.com/alibaba/mdrill/blob/master/doc/MSql.docx?raw=true" target="_blank">sql使用手册</a></li>
<li><a href="https://github.com/alibaba/mdrill/blob/master/doc/improve.docx?raw=true" target="_blank">技术原理，我们一年走过的路</a></li>
<li><a href="https://github.com/muyannian/higo/blob/master/doc/doc.rar?raw=true" target="_blank">文档逐步整理中，想深入了解请看先前的旧文档</a></li>

</ul>

<h1>mdrill Core contributors</h1>
<ul>
<li><a href="https://github.com/muyannian">母延年(子落)</a>、<a href="http://user.qzone.qq.com/2253209">秦剑(含光)</a>、<a href="https://github.com/bwzheng2010">郑博文(士远)</a>、木晗、逸客、张壮、凌凝</li>
</ul>
<h1>jstorm Core contributors</h1>
<ul>
<li><a href="https://github.com/longdafeng">封仲淹(纪君祥)</a>、<a href="https://github.com/tumen">李鑫(丙吉)</a>、<a href="https://github.com/muyannian">母延年(子落)</a>、<a href="https://github.com/zhouxinxust">周鑫(陈均)</a></li>



</ul>


<h1>其他</h1>
mdrill技术交流群:171465049<br>

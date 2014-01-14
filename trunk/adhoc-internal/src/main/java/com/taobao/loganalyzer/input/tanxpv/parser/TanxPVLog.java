package com.taobao.loganalyzer.input.tanxpv.parser;

import java.util.ArrayList;
import java.util.List;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;

/**
 * Tanx PV日志内容获取的封装
 *
 * @see <a
 *      href="http://sps.corp.taobao.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *  <br> ChangeLog at 20110509 by kangtian</br>
 *  <li> 1.日志版本变更为1.1 </li>
 *  <li> 2.section8 UserSection添加新的字段</li>
 *  <br> ChangeLog at 20111117 by kangtian</br>
 *  <li> 1. 日志版本变更为1.2 </li>
 *  <li> 2. 5.6.5字段内容修改为DSP结算价格，5.6.8修改为结算价格
 *  <br> ChangeLog at 20121023 by yuanhang.ghj</br>
 *  <li> 1. 日志版本变更为1.3 </li>
 *  <li> 2.  5_7_23字段为过滤原因,5_7_24字段为过滤标记
 *  <br> ChangeLog at 20130729 by yuanhang.ghj</br>
 *  <li> 1. 日志版本变更为1.4 </li>
 *  <li> 2.  增加5_4_10字段为广告位投放尺寸 </li>
 *  <br> ChangeLog at 20131105 by yuanhang.ghj</br>
 *  <li>1.日志版本升级为2.0. 在原有的TanxPV日志中新增无线专有section.5_12 </li>
 *  <li>2.采用 ”广告位类型”区分pc流量和无线流量 </li>
 *	<li>广告位类型在http://sps.corp.taobao.com/ad/LOG/Lists/List3/TanxPV.aspx中的5_4_9字段。 </li>
 *	<li>广告位类型值的含义如下： </li>
 *	<li>  值为空或者为0，标示是来自pc流量 </li>
 *	<li>  值为1，标示是来自无线流量 </li>
 * <br> ChangeLog at 20131217 by yuanhang.ghj</br>
 * <li>1.日志版本升级为2.1. 启用5_6_16和 5_6_17字段，定向属性和定向类型 </li>
 */
public class TanxPVLog
{
    private LogRecord	lr	= null;

    TanxPVLog(LogRecord lr)
    {
        this.lr = lr;
    }

    private String getFieldContent(String name)
    {
        if (lr != null)
        {
            LogField lf = lr.getField(name);
            return lf == null ? "" : (String) lf.getContent();
        }
        return "";
    };

    private List getFieldContentList(String name) {
        if (lr != null) {
            LogField lf = lr.getField(name);
            return lf==null?null:(List)lf.getContent();
        }
        return null;
    };

    /**
     *1.1 版本信息 版本信息初始为1.0
     *
     * @return 1.1 版本信息 版本信息初始为1.0
     */
    public String getVersion()
    {
        return getFieldContent("Version");
    }

    /**
     *2.1 时间戳
     *
     * @return 2.1 时间戳
     */
    public String getTimestamp()
    {
        return getFieldContent("Timestamp");
    }

    /**
     *2.2 PID(publisherid/networkid+siteid+adzoneid)样式: mm_nid_sid_adzid
     *
     * @return 2.2 PID(publisherid/networkid+siteid+adzoneid)样式:
     *         mm_nid_sid_adzid
     */
    public String getPid()
    {
        return getFieldContent("Pid");
    }

    /**
     *2.3 处理路径
     *
     * @return 2.3 处理路径
     */
    public String getProcessPath()
    {
        return getFieldContent("ProcessPath");
    }

    /**
     *2.4 浏览用户IP
     *
     * @return 2.4 浏览用户IP
     */
    public String getUserIP()
    {
        return getFieldContent("UserIP");
    }

    /**
     *2.5 Acookie
     *
     * @return 2.5 Acookie
     */
    public String getCookie()
    {
        return getFieldContent("Cookie");
    }

    /**
     *2.6 SessionID
     *
     * @return 2.6 SessionID
     */
    public String getSessionID()
    {
        return getFieldContent("SessionID");
    }

    /**
     *2.7 CG(不记录)
     *
     * @return 2.7 CG(不记录)
     */
    public String getCg()
    {
        return getFieldContent("Cg");
    }

    /**
     *2.8 展现类型(不记录)
     *
     * @return 2.8 展现类型(不记录)
     */
    public String getViewType()
    {
        return getFieldContent("ViewType");
    }

    /**
     *2.9 展现子类型(不记录)
     *
     * @return 2.9 展现子类型(不记录)
     */
    public String getViewSubCategory()
    {
        return getFieldContent("ViewSubCategory");
    }

    /**
     *2.10 Pub接口代码类型(不记录)
     *
     * @return 2.10 Pub接口代码类型(不记录)
     */
    public String getPubCodeType()
    {
        return getFieldContent("PubCodeType");
    }

    /**
     *2.11 ABTag(不记录)
     *
     * @return 2.11 ABTag(不记录)
     */
    public String getAbTag()
    {
        return getFieldContent("AbTag");
    }

    /**
     *2.12 是否后续请求(不记录)
     *
     * @return 2.12 是否后续请求(不记录)
     */
    public String getFollowRequest()
    {
        return getFieldContent("FollowRequest");
    }

    /**
     *2.13 广告展示的url
     *
     * @return 2.13 广告展示的url
     */
    public String getFromURL()
    {
        return getFieldContent("FromURL");
    }

    /**
     *2.14 topframe 的referrer(不记录)
     *
     * @return 2.14 topframe 的referrer(不记录)
     */
    public String getTopFrameRefer()
    {
        return getFieldContent("TopFrameRefer");
    }

    /**
     *2.15 当前window的referrer(不记录)
     *
     * @return 2.15 当前window的referrer(不记录)
     */
    public String getWindowRefer()
    {
        return getFieldContent("WindowRefer");
    }

    /**
     *2.16 浏览器UserAgent标识
     *
     * @return 2.16 浏览器UserAgent标识
     */
    public String getUserAgent()
    {
        return getFieldContent("UserAgent");
    }

    /**
     *2.17 refPID(不记录)
     *
     * @return 2.17 refPID(不记录)
     */
    public String getRefPid()
    {
        return getFieldContent("RefPid");
    }

    /**
     *3.1 网站分类(待定义)
     *
     * @return 3.1 网站分类(待定义)
     */
    public String getSiteCategory()
    {
        return getFieldContent("SiteCategory");
    }

    /**
     *3.2 网站分级(不记录)
     *
     * @return 3.2 网站分级(不记录)
     */
    public String getSiteLevel()
    {
        return getFieldContent("SiteLevel");
    }

    /**
     *4.1 广告位类目(不记录)
     *
     * @return 4.1 广告位类目(不记录)
     */
    public String getAdzoneCategory()
    {
        return getFieldContent("AdzoneCategory");
    }

    /**
     *4.2 广告位位置(不记录)
     *
     * @return 4.2 广告位位置(不记录)
     */
    public String getAdzonePosition()
    {
        return getFieldContent("AdzonePosition");
    }

    /**
     *4.3 广告位sizecode(待定义)
     *
     * @return 4.3 广告位sizecode(待定义)
     */
    public String getAdzoneSize()
    {
        return getFieldContent("AdzoneSize");
    }

    /**
     *4.4 广告位大小
     *
     * @return 4.4 广告位大小
     */
    public String getAdzoneArea()
    {
        return getFieldContent("AdzoneArea");
    }

    /**
     *4.5 是否首屏(不记录)
     *
     * @return 4.5 是否首屏(不记录)
     */
    public String getIsFirstPage()
    {
        return getFieldContent("IsFirstPage");
    }

    /**
     *4.6 广告位展现方式
     *
     * @return 4.6 广告位展现方式
     */
    public String getViewMode()
    {
        return getFieldContent("ViewMode");
    }

    /**
     *4.7 广告位最低限价(最低竞价额)
     *
     * @return 4.7 广告位最低限价(最低竞价额)
     */
    public String getLimitPrice()
    {
        return getFieldContent("LimitPrice");
    }

    /**
     *4.8 广告位属性(不记录)
     *
     * @return 4.8 广告位属性(不记录)
     */
    public String getAdzoneAttribute()
    {
        return getFieldContent("AdzoneAttribute");
    }

    /**
     *4.9 广告位类型 区分pc流量和无线流量.<li>值为空或者为0，标示是来自pc流量</li>.<li>值为1，标示是来自无线流量</li>
     *
     * @return 4.9 广告位类型
     */
    public String getAdzoneType()
    {
        return getFieldContent("AdzoneType");
    }
    /**
     *4.10 广告位投放尺寸
     *
     * @return 4.10 广告位类型
     */
    public String getAdzonePutOnSize()
    {
        return getFieldContent("AdzonePutOnSize");
    }

    /**
     *6.1 ServiceID: transid_adid_adbid_0 (广告ID) 样式: transid_adid_adbid_0 <br>
     * 请注意: 我们不强调这四个ID的长度，希望大家在使用这个ID的时候按照”_”进行分割，而不是用长度来做硬编码。
     * Id结尾的_0 是为了兼容现有KGB系统而设立的，目前Tanx系统中可以置为0，为了有需求时可以直接启用
     *
     * @return 6.1 ServiceID: transid_adid_adbid_0 (广告ID) 样式:
     */
    public String getServiceID()
    {
        return getFieldContent("ServiceID");
    }

    /**
     *6.2 广告客户ID(广告主ID) 直投广告主会记录广告主的ID，Exchange中为空
     *
     * @return 6.2 广告客户ID(广告主ID) 直投广告主会记录广告主的ID，Exchange中为空
     */
    public String getCustomerID()
    {
        return getFieldContent("CustomerID");
    }

    /**
     *6.3 产品类型(待定义)
     *
     * @return 6.3 产品类型(待定义)
     */
    public String getProductType()
    {
        return getFieldContent("ProductType");
    }

    /**
     *6.4 产品子类型(待定义)
     *
     * @return 6.4 产品子类型(待定义)
     */
    public String getSubProductType()
    {
        return getFieldContent("SubProductType");
    }

    /**
     *6.8 广告结算价格
     *  <br> 注:1.2及之后 广告结算价格实际上取的5.6.8的内容，5.6.5已经修改为Dsp结算价格
     * @return 6.8 广告结算价格
     */
    public String getCostPrice()
    {
        return getFieldContent("CostPrice");
    }
    /**
     *6.5 版本1.2之后5.6.5已经修改为Dsp结算价格
     *  <br> 注:1.2及之后 广告结算价格实际上取的5.6.8的内容，5.6.5已经修改为Dsp结算价格
     * @return 6.5 广告结算价格
     */
    public String getDspCostPrice()
    {
        return getFieldContent("DspCostPrice");
    }

    /**
     *6.6 客户竞价过程 此字段记录Exchange广告的竞价过程，由于一次pv通过exchange竞价中会有多条广告参与，
     * 所以需要将不同network提供的广告竞价过程均记录下来。关心此字段的同学，请自行按相应的记录格式处理。
     * <br>竞价id^CNetworkID^Dprice,ret[;price,ret][^C NetworkID^Dprice,ret[;price,ret]]……
     * <br>ret的格式参考《Tan(X) Exchange 实时竞价API.pdf》的返回结果
     * <br>例如: 某次竞价请求后，共有两个network参与竞价，一共3条结果，记录为:12345^C23456^D2.5,0;1.8;1^C45678^D1.9,2
     * @return 6.6 客户竞价过程
     */
    public String getBidProcess()
    {
        return getFieldContent("BidProcess");
    }

    /**
     *6.7 流量类型(不记录)
     *
     * @return 6.7 流量类型(不记录)
     */
    public String getFlowType()
    {
        return getFieldContent("FlowType");
    }

    /**
     *6.8 页面内位置(不记录) --修改为广告结算价格
     * <br>版本1.2之后5.6.8记录的为广告结算价格，请用getCostPrice()
     * @return 6.8 页面内位置(不记录)
     */
    @Deprecated
    public String getAbsolutePosition()
    {
        return getFieldContent("AbsolutePosition");
    }


    /**
     *6.9 匹配模式(不记录)
     *
     * @return 6.9 匹配模式(不记录)
     */
    public String getMatchType()
    {
        return getFieldContent("MatchType");
    }

    /**
     *6.10 匹配子模式(不记录)
     *
     * @return 6.10 匹配子模式(不记录)
     */
    public String getSubMatchMode()
    {
        return getFieldContent("SubMatchMode");
    }

    /**
     *6.11 广告类目(待定义)
     *
     * @return 6.11 广告类目(待定义)
     */
    public String getAdCategory()
    {
        return getFieldContent("AdCategory");
    }

    /**
     *6.12 广告类目confidence(不记录)
     *
     * @return 6.12 广告类目confidence(不记录)
     */
    public String getAdCateConfidence()
    {
        return getFieldContent("AdCateConfidence");
    }

    /**
     *6.13 广告来源
     * 记录竞价成功的广告的所属NetworkID,Exchange中，此ID来源于竞价API中的Network反馈直投广告中此字段暂不记录，为空
     *
     * @return 6.13 广告来源
     */
    public String getAdSource()
    {
        return getFieldContent("AdSource");
    }

    /**
     *6.14 广告牌类型<br>
     * 文字 1 <br>
     * 图片 2 <br>
     * Flash 3<br>
     * 视频 4<br>
     * 文字链 5<br>
     * 图文 6<br>
     * 自定义html/js代码广告 7
     *
     * @return 6.14 广告牌类型<br>
     */
    public String getAdBoardType()
    {
        return getFieldContent("AdBoardType");
    }

    /**
     *6.15 广告属性<br>
     * Network提供的广告属性Guid, 在钻石展位接入时(如果钻石展位广告在此pv中竞价成功)，则记录钻石展位广告的计划ID
     *
     * @return 6.15 广告属性<br>
     */
    public String getAdAttribute()
    {
        return getFieldContent("AdAttribute");
    }

    /**
     *6.16 定向类型
     *
     * @return 6.16 定向类型(不记录)
     */
    public String getTargetType()
    {
        return getFieldContent("TargetType");
    }

    /**
     *6.17 定向属性
     *
     * @return 6.17 定向属性.内部为整个串，不解析
     */
    public String getTargetAttribute()
    {
        return getFieldContent("TargetAttribute");
    }

    /**
     *7.1 随机数 直投广告中不记录(为空)
     *
     * @return 7.1 随机数 直投广告中不记录(为空)
     */
    public String getRandomNum()
    {
        return getFieldContent("RandomNum");
    }

    /**
     *7.2 随机数变量中对应的ascii 直投广告中不记录(为空)
     *
     * @return 7.2 随机数变量中对应的ascii 直投广告中不记录(为空)
     */
    public String getAscii()
    {
        return getFieldContent("Ascii");
    }

    /**
     *7.3 分辨率宽 直投广告中不记录(为空)
     *
     * @return 7.3 分辨率宽 直投广告中不记录(为空)
     */
    public String getScreenWidth()
    {
        return getFieldContent("ScreenWidth");
    }

    /**
     *7.4 分辨率高 直投广告中不记录(为空)
     *
     * @return 7.4 分辨率高 直投广告中不记录(为空)
     */
    public String getScreenHeight()
    {
        return getFieldContent("ScreenHeight");
    }

    /**
     *7.5 可用工作区宽度 直投广告中不记录(为空)
     *
     * @return 7.5 可用工作区宽度 直投广告中不记录(为空)
     */
    public String getScreenAvailWidth()
    {
        return getFieldContent("ScreenAvailWidth");
    }

    /**
     *7.6 可用工作区高度 直投广告中不记录(为空)
     *
     * @return 7.6 可用工作区高度 直投广告中不记录(为空)
     */
    public String getScreenAvailHeight()
    {
        return getFieldContent("ScreenAvailHeight");
    }

    /**
     *7.7 网页可见宽度 直投广告中不记录(为空)
     *
     * @return 7.7 网页可见宽度 直投广告中不记录(为空)
     */
    public String getBodyClientWidth()
    {
        return getFieldContent("BodyClientWidth");
    }

    /**
     *7.8 网页可见高度 直投广告中不记录(为空)
     *
     * @return 7.8 网页可见高度 直投广告中不记录(为空)
     */
    public String getBodyClientHeight()
    {
        return getFieldContent("BodyClientHeight");
    }

    /**
     *7.9 色深 直投广告中不记录(为空)
     *
     * @return 7.9 色深 直投广告中不记录(为空)
     */
    public String getColorDepth()
    {
        return getFieldContent("ColorDepth");
    }

    /**
     *7.10 时区 直投广告中不记录(为空)
     *
     * @return 7.10 时区 直投广告中不记录(为空)
     */
    public String getTimeArea()
    {
        return getFieldContent("TimeArea");
    }

    /**
     *7.11 History长度 直投广告中不记录(为空)
     *
     * @return 7.11 History长度 直投广告中不记录(为空)
     */
    public String getHistoryLength()
    {
        return getFieldContent("HistoryLength");
    }

    /**
     *7.12 是否开启java 直投广告中不记录(为空)
     *
     * @return 7.12 是否开启java 直投广告中不记录(为空)
     */
    public String getIsOpenJava()
    {
        return getFieldContent("IsOpenJava");
    }

    /**
     *7.13 插件个数 直投广告中不记录(为空)
     *
     * @return 7.13 插件个数 直投广告中不记录(为空)
     */
    public String getPluginNum()
    {
        return getFieldContent("PluginNum");
    }

    /**
     *7.14 Mime类型个数 直投广告中不记录(为空)
     *
     * @return 7.14 Mime类型个数 直投广告中不记录(为空)
     */
    public String getMimeNum()
    {
        return getFieldContent("MimeNum");
    }

    /**
     *7.15 Flash版本 直投广告中不记录(为空)
     *
     * @return 7.15 Flash版本 直投广告中不记录(为空)
     */
    public String getFlashVersion()
    {
        return getFieldContent("FlashVersion");
    }

    /**
     *7.16 HTTP_VIA
     *
     * @return 7.16 HTTP_VIA
     */
    public String getHttpVia()
    {
        return getFieldContent("HttpVia");
    }

    /**
     *7.17 HTTP_X_FORWARDED_FOR|HTTP_PROXY_CONNECTION
     *
     * @return 7.17 HTTP_X_FORWARDED_FOR|HTTP_PROXY_CONNECTION
     */
    public String getHttpX()
    {
        return getFieldContent("HttpX");
    }

    /**
     *7.18 HTTP_ACCEPT
     *
     * @return 7.18 HTTP_ACCEPT
     */
    public String getHttpAccept()
    {
        return getFieldContent("HttpAccept");
    }

    /**
     *7.19 HTTP_ACCEPT_CHARSET
     *
     * @return 7.19 HTTP_ACCEPT_CHARSET
     */
    public String getHttpAccCharset()
    {
        return getFieldContent("HttpAccCharset");
    }

    /**
     *7.20 HTTP_ACCEPT_LANGUAGE
     *
     * @return 7.20 HTTP_ACCEPT_LANGUAGE
     */
    public String getHttpAccLanguage()
    {
        return getFieldContent("HttpAccLanguage");
    }

    /**
     *7.21 HTTP_ACCEPT_ENCODING
     *
     * @return 7.21 HTTP_ACCEPT_ENCODING
     */
    public String getHttpAccEncoding()
    {
        return getFieldContent("HttpAccEncoding");
    }

    /**
     *7.22 HTTP_KEEP_ALIVE
     *
     * @return 7.22 HTTP_KEEP_ALIVE
     */
    public String getHttpKeepAlive()
    {
        return getFieldContent("HttpKeepAlive");
    }
    /**
     *7.23 FILTER_CODE
     *
     * @return 7.23 FILTER_CODE
     */
    public String getFilterCode()
    {
        return getFieldContent("FilterCode");
    }

    /**
     *7_24    FILTER_FLAG
     *
     * @return 7_24    FILTER_FLAG
     */
    public String getFilterFlag()
    {
        return getFieldContent("FilterFlag");
    }

    /**
     *8.1 最近行为信息
     *
     * @return 8.1 最近行为信息
     */
    public String getUserActionInfo()
    {
        return getFieldContent("UserActionInfo");
    }
    /**
     *8.2 人群分类信息
     *
     * @return 8.2 人群分类信息
     */
    public java.util.List<CrowdInfo> getCrowdInfoList() {
        return getCrowdInfoFieldContent("CrowdInfoList");
    }

    @SuppressWarnings("unchecked")
    private List<CrowdInfo> getCrowdInfoFieldContent(String name) {
        List<LogRecord> list = (List<LogRecord>)getFieldContentList(name);
        List<CrowdInfo> crowds = null;
        if (list != null) {
            crowds = new ArrayList<CrowdInfo>();
            for (LogRecord lr : list) {
                crowds.add(new CrowdInfo(lr));
            }
        }
        return crowds;
    }

    /**
     *8.3 性别
     *
     * @return 8.3 性别
     */
    public String getUserSex()
    {
        return getFieldContent("UserSex");
    }
    /**
     *8.4 整体消费能力
     *
     * @return 8.4 整体消费能力
     */
    public String getWholeConsumeAblity()
    {
        return getFieldContent("WholeConsumeAblity");
    }
    /**
     *11.1 Session ID前4个bytes
     *
     * @return 11.1 Session ID前4个bytes
     */
    public String getVerify()
    {
        return getFieldContent("Verify");
    }

    /**
     *12.1 Session App包名
     *
     * @return 12.1 Session App包名
     */
    public String getAppPackage()
    {
        return getFieldContent("12AppPackage");
    }
    /**
     *12.2 Session 设备平台
     *
     * @return 12.1 Session App包名
     */
    public String getDevicePlatform()
    {
        return getFieldContent("12DevicePlatform");
    }
    /**
     *12.3 Session 操作系统
     *
     * @return 12.3 Session 操作系统
     */
    public String getOS()
    {
        return getFieldContent("12OS");
    }
    /**
     *12.4 Session 操作系统版本号
     *
     * @return 12.4 Session 操作系统版本号
     */
    public String getOS_Version()
    {
        return getFieldContent("12OS_Version");
    }
    /**
     *12.5 Session 制造厂商
     *
     * @return 12.5 Session 制造厂商
     */
    public String getManufacturer()
    {
        return getFieldContent("12Manufacturer");
    }
    /**
     *12.6 Session 设备型号
     *
     * @return 12.6 Session 设备型号
     */
    public String getDeviceModel()
    {
        return getFieldContent("12DeviceModel");
    }
    /**
     *12.7 Session 设备分辨率
     *
     * @return 12.7 Session 设备分辨率
     */
    public String getDeviceScreen()
    {
        return getFieldContent("12DeviceScreen");
    }
    /**
     *12.8 设备IMEI号
     *
     * @return 12.8 设备IMEI号
     */
    public String getDeviceIMEI()
    {
        return getFieldContent("12DeviceIMEI");
    }
    /**
     *12.9 Session 设备MAC号
     *
     * @return 12.9 Session 设备MAC号
     */
    public String getDeviceMAC()
    {
        return getFieldContent("12DeviceMAC");
    }
    /**
     *12.10 Session iOS设备的IDFA号
     *
     * @return 12.10 Session iOS设备的IDFA号
     */
    public String getiOS_IDFA()
    {
        return getFieldContent("12iOS_IDFA");
    }
    /**
     *12.11 网络连接类型
     *
     * @return 12.11 网络连接类型
     */
    public String getDeviceNetwork()
    {
        return getFieldContent("12DeviceNetwork");
    }
    /**
     *12.12 Session 经度
     *
     * @return 12.12 Session 经度
     */
    public String getLongitude()
    {
        return getFieldContent("12Longitude");
    }

    /**
     *12.13 Session 纬度
     *
     * @return 12.13 Session 纬度
     */
    public String getLatitude()
    {
        return getFieldContent("12Latitude");
    }

    public List<String[]> getGoogleWeight() {
        return getFieldContentList("googleweight");
    }

    public static class CrowdInfo {
        private LogRecord lr = null;

        private CrowdInfo(LogRecord lr) {
            this.lr = lr;
        }

        private String getFieldContent(String name) {
            if (lr != null) {
                LogField lf = lr.getField(name);
                return lf==null?"":(String)lf.getContent();
            }
            return "";
        };

        /**
         * Section 8 字段 2 中的兴趣类目
         * @return Section 8 字段 2 中的兴趣类目
         */
        public String getCategory() {
            return getFieldContent("Category");
        }
        /**
         * Section 8 字段 2 中的分数
         * @return Section 8 字段 2 中的分数
         */
        public String getScore() {
            return getFieldContent("Score");
        }
        /**
         * Section 8 字段 2 中的消费能力
         * @return Section 8 字段 2 中的消费能力
         */
        public String getConsumeAbility() {
            return getFieldContent("ConsumeAbility");
        }
    }
}

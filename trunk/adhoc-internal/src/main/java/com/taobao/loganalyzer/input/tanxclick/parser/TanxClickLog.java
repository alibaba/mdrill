package com.taobao.loganalyzer.input.tanxclick.parser;

import com.taobao.loganalyzer.input.tanxclick.common.LogField;
import com.taobao.loganalyzer.input.tanxclick.common.LogRecord;

/**
 * 点击日志内容获取的封装
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 *         修改点： - 日志版本号(字段编号1_1_1)为"1.0"
 *
 */
public class TanxClickLog
{
    private LogRecord	lr	= null;

    TanxClickLog(LogRecord lr)
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
     *2.1 Src pv id(SessionID)
     *
     * @return 2.1 Src pv id(SessionID)
     */
    public String getPvID()
    {
        return getFieldContent("PvID");
    }

    /**
     *2.2 Click id
     *
     * @return 2.2 Click id
     */
    public String getClickID()
    {
        return getFieldContent("ClickID");
    }

    /**
     *2.3 Pv time
     *
     * @return 2.3 Pv time
     */
    public String getPvTime()
    {
        return getFieldContent("PvTime");
    }

    /**
     *2.4 Click time
     *
     * @return 2.4 Click time
     */
    public String getClickTime()
    {
        return getFieldContent("ClickTime");
    }

    /**
     *2.5 点击用户ip
     *
     * @return 2.5 点击用户ip
     */
    public String getClickIP()
    {
        return getFieldContent("ClickIP");
    }

    /**
     *2.6 浏览用户ip
     *
     * @return 2.6 浏览用户ip
     */
    public String getPvIP()
    {
        return getFieldContent("PvIP");
    }

    /**
     *2.7 点击用户cookie
     *
     * @return 2.7 点击用户cookie
     */
    public String getClickCookie()
    {
        return getFieldContent("ClickCookie");
    }

    /**
     *2.8 点击用户cookie时间
     *
     * @return 2.8 点击用户cookie时间
     */
    public String getClickCookieTime()
    {
        return getFieldContent("ClickCookieTime");
    }

    /**
     *2.9 浏览用户cookie
     *
     * @return 2.9 浏览用户cookie
     */
    public String getPvCookie()
    {
        return getFieldContent("PvCookie");
    }

    /**
     *2.10 合作伙伴id (pid: publisherid/networked+siteid+adzoneid) 同pv日志
     *
     * @return 2.10 合作伙伴id (pid: publisherid/networked+siteid+adzoneid) 同pv日志
     */
    public String getPid()
    {
        return getFieldContent("Pid");
    }

    /**
     *2.11 Refer
     *
     * @return 2.11 Refer
     */
    public String getReferer()
    {
        return getFieldContent("Referer");
    }

    /**
     *2.12 广告id 同pv日志3.6.1
     *
     * @return 2.12 广告id 同pv日志3.6.1
     */
    public String getServiceID()
    {
        return getFieldContent("ServiceID");
    }

    /**
     *2.13 广告主id 同pv,3.6.2
     *
     * @return 2.13 广告主id 同pv,3.6.2
     */
    public String getCustomerID()
    {
        return getFieldContent("CustomerID");
    }

    /**
     *2.14 点击/结算价格
     *
     * @return 2.14 点击/结算价格
     */
    public String getClickPrice()
    {
        return getFieldContent("ClickPrice");
    }

    /**
     *2.15 产品类型(同pv，待定义)
     *
     * @return 2.15 产品类型(同pv，待定义)
     */
    public String getProType()
    {
        return getFieldContent("ProType");
    }

    /**
     *2.16 产品子类型(同pv，待定义)
     *
     * @return 2.16 产品子类型(同pv，待定义)
     */
    public String getProSubtype()
    {
        return getFieldContent("ProSubtype");
    }

    /**
     *2.17 流量类型(不记录)
     *
     * @return 2.17 流量类型(不记录)
     */
    public String getFlowType()
    {
        return getFieldContent("FlowType");
    }

    /**
     *2.18 Abtag(不记录)
     *
     * @return 2.18 Abtag(不记录)
     */
    public String getAbtag()
    {
        return getFieldContent("Abtag");
    }

    /**
     *2.19 旺旺ID(不记录)
     *
     * @return 2.19 旺旺ID(不记录)
     */
    public String getWangWangID()
    {
        return getFieldContent("WangWangID");
    }

    /**
     *2.20 refpid(不记录)
     *
     * @return 2.20 refpid(不记录)
     */
    public String getRefPID()
    {
        return getFieldContent("RefPID");
    }

    /**
     *2.21 refcreativeid(不记录)
     *
     * @return 2.21 refcreativeid(不记录)
     */
    public String getRefCreativeID()
    {
        return getFieldContent("RefCreativeID");
    }

    /**
     *3.1 广告分类(待定义)
     *
     * @return 3.1 广告分类(待定义)
     */
    public String getAdTypeID()
    {
        return getFieldContent("AdTypeID");
    }

    /**
     *3.2 广告来源 同pv日志定义中的3.6.13
     *
     * @return 3.2 广告来源 同pv日志定义中的3.6.13
     */
    public String getAdOrigin()
    {
        return getFieldContent("AdOrigin");
    }

    /**
     *3.3 第三方ID(NetworkUID) 同pv日志定义中的3.6.15
     *
     * @return 3.3 第三方ID(NetworkUID) 同pv日志定义中的3.6.15
     */
    public String getThridPartyID()
    {
        return getFieldContent("ThridPartyID");
    }

    /**
     *3.4 Keyword(不记录)
     *
     * @return 3.4 Keyword(不记录)
     */
    public String getKeyword()
    {
        return getFieldContent("Keyword");
    }

    /**
     *3.5 广告竞价价格(最高出价)
     *
     * @return 3.5 广告竞价价格(最高出价)
     */
    public String getHighestPrice()
    {
        return getFieldContent("HighestPrice");
    }

    /**
     *3.6 广告结算价格(结算价格)
     *
     * @return 3.6 广告结算价格(结算价格)
     */
    public String getCostPrice()
    {
        return getFieldContent("CostPrice");
    }

    /**
     *3.7 折扣(不记录)
     *
     * @return 3.7 折扣(不记录)
     */
    public String getDiscountRate()
    {
        return getFieldContent("DiscountRate");
    }

    /**
     *3.8  广告的尺寸(长x宽)
     *
     * @return 3.8  广告的尺寸(长x宽)
     */
    public String getAdzoneSize()
    {
        return getFieldContent("AdzoneSize");
    }

    /**
     *3.9 保留位(不记录)
     *
     * @return 3.9 保留位(不记录)
     */
    public String getUndefine2()
    {
        return getFieldContent("Undefine2");
    }

    /**
     *3.10 点击目标地址
     *
     * @return 3.10 点击目标地址
     */
    public String getCustomURL()
    {
        return getFieldContent("CustomURL");
    }

    /**
     *3.11 定向类型(不记录)
     *
     * @return 3.11 定向类型(不记录)
     */
    public String getTargetType()
    {
        return getFieldContent("TargetType");
    }

    /**
     *3.12 定向属性(不记录)
     *
     * @return 3.12 定向属性(不记录)
     */
    public String getTargetAttribute()
    {
        return getFieldContent("TargetAttribute");
    }

    /**
     *6.1 跳转方式
     *
     * @return 6.1 跳转方式
     */
    public String getJumpType()
    {
        return getFieldContent("JumpType");
    }

    /**
     *6.2 HTTP_VIA
     *
     * @return 6.2 HTTP_VIA
     */
    public String getHttpVia()
    {
        return getFieldContent("HttpVia");
    }

    /**
     *6.3 HTTP_X_FORWARDED_FOR
     *
     * @return 6.3 HTTP_X_FORWARDED_FOR
     */
    public String getHttpXForwardedFor()
    {
        return getFieldContent("HttpXForwardedFor");
    }

    /**
     *6.4 HTTP_PROXY_CONNECTION
     *
     * @return 6.4 HTTP_PROXY_CONNECTION
     */
    public String getHttpProxyConnection()
    {
        return getFieldContent("HttpProxyConnection");
    }

    /**
     *6.5 HTTP_ACCEPT
     *
     * @return 6.5 HTTP_ACCEPT
     */
    public String getHttpAccept()
    {
        return getFieldContent("HttpAccept");
    }

    /**
     *6.6 HTTP_ACCEPT_CHARSET
     *
     * @return 6.6 HTTP_ACCEPT_CHARSET
     */
    public String getHttpAcceptCharset()
    {
        return getFieldContent("HttpAcceptCharset");
    }

    /**
     *6.7 HTTP_ACCEPT_LANGUAGE
     *
     * @return 6.7 HTTP_ACCEPT_LANGUAGE
     */
    public String getHttpAcceptLanguage()
    {
        return getFieldContent("HttpAcceptLanguage");
    }

    /**
     *6.8 HTTP_ACCEPT_ENCODING
     *
     * @return 6.8 HTTP_ACCEPT_ENCODING
     */
    public String getHttpAcceptEncoding()
    {
        return getFieldContent("HttpAcceptEncoding");
    }

    /**
     *6.9 HTTP_KEEP_ALIVE
     *
     * @return 6.9 HTTP_KEEP_ALIVE
     */
    public String getHttpKeepAlive()
    {
        return getFieldContent("HttpKeepAlive");
    }

    /**
     *6.10 HTTP_X_MOZ
     *
     * @return 6.10 HTTP_X_MOZ
     */
    public String getHttpXMoz()
    {
        return getFieldContent("HttpXMoz");
    }

    /**
     *6.11 User-agent
     *
     * @return 6.11 User-agent
     */
    public String getUserAgent()
    {
        return getFieldContent("UserAgent");
    }

    /**
     *6.12 扩展信息
     *
     * @return 6.12 扩展信息
     */
    public String getExtraInfo()
    {
        return getFieldContent("ExtraInfo");
    }

    /**
     *6.13 前端过滤明细代码
     *
     * @return 6.13 前端过滤明细代码
     */
    public String getFFCode()
    {
        return getFieldContent("FFCode");
    }

    /**
     *6.14 后端过滤明细代码
     *
     * @return 6.14 后端过滤明细代码
     */
    public String getBFCode()
    {
        return getFieldContent("BFCode");
    }

    /**
     *6.15 前端过滤是否被过滤
     *
     * @return 6.15 前端过滤是否被过滤
     */
    public String getFFIsFilter()
    {
        return getFieldContent("FFIsFilter");
    }

    /**
     *6.16 是否被过滤
     *
     * @return 6.16 是否被过滤
     */
    public String getIsFilter()
    {
        return getFieldContent("IsFilter");
    }
}

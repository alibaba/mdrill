package com.taobao.loganalyzer.input.tanxclick.parser;


import com.taobao.loganalyzer.input.tanxclick.common.LogField;
import com.taobao.loganalyzer.input.tanxclick.common.LogParser;
import com.taobao.loganalyzer.input.tanxclick.common.LogRecord;
import com.taobao.loganalyzer.input.tanxclick.common.SectionParser;

/**
 * Section 2: common section解析类
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class CommonSectionParser implements SectionParser {

    /**
     * 对common section进行解析
     * section2-共用section为空--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            return false ;
            //	if (!lp.hasNextChar()) return false;
        }
        else {
            //src pv id
            lr.addField("PvID", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //click id
            lr.addField("ClickID", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //pv time
            lr.addField("PvTime", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //click time
            lr.addField("ClickTime", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //点击用户的ip
            lr.addField("ClickIP", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //浏览用户的ip
            lr.addField("PvIP", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //点击用户的cookie
            lr.addField("ClickCookie", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //点击用户cookie种植时间，时间戳格式
            lr.addField("ClickCookieTime", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //浏览用户cookie
            lr.addField("PvCookie", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //pubid
            lr.addField("Pid", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //referer
            lr.addField("Referer", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //广告ID
            lr.addField("ServiceID", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //customer id
            lr.addField("CustomerID", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //点击价格(如果有打折,取打折后的)
            lr.addField("ClickPrice", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //产品类型
            lr.addField("ProType", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //产品子类型
            lr.addField("ProSubtype", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //流量类型
            lr.addField("FlowType", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //abtag
            lr.addField("Abtag", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //  旺旺ID
            lr.addField("WangWangID",       new LogField(LogField.TYPE_STRING, lp.getNextCB()));
            // refPID
            lr.addField("RefPID", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //refCreativeID
            lr.addField("RefCreativeID", 	new LogField(LogField.TYPE_STRING, lp.getNextCA()));

        }

        if (lp.isError()) {
            return false;
        }

        return true;
    }


}

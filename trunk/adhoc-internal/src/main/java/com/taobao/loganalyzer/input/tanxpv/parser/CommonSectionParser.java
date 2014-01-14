package com.taobao.loganalyzer.input.tanxpv.parser;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 2：共用Section解析类。该Section保存所有的公用信息。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class CommonSectionParser implements SectionParser {

    /**
     * 对common Section进行解析
     * b)	section2-common section为空--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            return false ;
            //	if (!lp.hasNextChar()) return false;
        }
        else{
            //time stamp
            lr.addField("Timestamp",       new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //pid
            lr.addField("Pid",             new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //0:+处理路径
            lr.addField("ProcessPath",     new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //浏览用户IP
            lr.addField("UserIP",          new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //用户Cookie
            lr.addField("Cookie",          new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //Session ID
            lr.addField("SessionID",       new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //cg
            lr.addField("Cg",              new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //展现类型
            lr.addField("ViewType",        new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //展现子类型
            lr.addField("ViewSubCategory", new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //Pub接口代码类型
            lr.addField("PubCodeType",      new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //abtag
            lr.addField("AbTag",           new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //标示是否后续请求
            lr.addField("FollowRequest",   new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //from
            lr.addField("FromURL",         new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //topframe的referrer
            lr.addField("TopFrameRefer",   new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //当前window的referrer
            lr.addField("WindowRefer",     new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //user agent
            lr.addField("UserAgent",       new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //ref pid
            lr.addField("RefPid",          new LogField(LogField.TYPE_STRING, lp.getNextCA()));

        }

        if (lp.isError())
            return false;

        return true;
    }
}

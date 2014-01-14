package com.taobao.loganalyzer.input.tanxclick.parser;


import com.taobao.loganalyzer.input.tanxclick.common.LogField;
import com.taobao.loganalyzer.input.tanxclick.common.LogParser;
import com.taobao.loganalyzer.input.tanxclick.common.LogRecord;
import com.taobao.loganalyzer.input.tanxclick.common.SectionParser;

/**
 * Section 6: AF section解析类
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class AFSectionParser implements SectionParser {

    /**
     * 对AF section进行解析
     * section6-反作弊section为空--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {

        if (lp.isCurCharCA()) {
            return false;
        }
        else {
            //跳转类型
            lr.addField("JumpType", 		 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //error message
            //lr.addField("ErrorMsg", 		 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // HTTP_VIA
            lr.addField("HttpVia", 			 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // HTTP_X_FORWARDED_FOR
            lr.addField("HttpXForwardedFor", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // HTTP_PROXY_CONNECTION
            lr.addField("HttpProxyConnection",	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // HTTP_ACCEPT
            lr.addField("HttpAccept", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // HTTP_ACCEPT_CHARSET
            lr.addField("HttpAcceptCharset", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // HTTP_ACCPET_LANGUAGE
            lr.addField("HttpAcceptLanguage", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // HTTP_ACCEPT_ENCODING
            lr.addField("HttpAcceptEncoding", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // HTTP_KEEP_ALIVE
            lr.addField("HttpKeepAlive", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // HTTP_X_MOZ
            lr.addField("HttpXMoz", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // User-agent
            lr.addField("UserAgent", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // extra info
            lr.addField("ExtraInfo", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 前端过滤明细代码
            lr.addField("FFCode", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 后端过滤明细代码
            lr.addField("BFCode", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 前端过滤是否被过滤标志1，0
            lr.addField("FFIsFilter", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 是否被过滤
            String IsFilter = lp.getAllRemained();
            if (IsFilter==null || IsFilter.indexOf(LogParser.CONTROL_A)!=-1 || IsFilter.indexOf(LogParser.CONTROL_B)!=-1)
                return false;
            lr.addField("IsFilter", 				new LogField(LogField.TYPE_STRING, IsFilter));
        }

        if (lp.isError()) {
            return false;
        }

        return true;
    }
}

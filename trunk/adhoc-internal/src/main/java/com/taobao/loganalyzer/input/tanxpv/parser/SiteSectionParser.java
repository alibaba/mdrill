package com.taobao.loganalyzer.input.tanxpv.parser;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 3：网站Section解析类。该Section保存和这个网站有关的信息。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class SiteSectionParser implements SectionParser {

    /**
     * 对网站Section进行解析
     * c)	section3-网站section为空--------允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            if (!lp.hasNextChar()) return false;
        }
        else{
            //网站类型
            lr.addField("SiteCategory",	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //网站分级
            lr.addField("SiteLevel",	new LogField(LogField.TYPE_STRING, lp.getNextCA()));
        }

        if (lp.isError())
            return false;

        return true;
    }

}

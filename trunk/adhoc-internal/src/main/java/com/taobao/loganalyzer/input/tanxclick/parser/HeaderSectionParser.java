package com.taobao.loganalyzer.input.tanxclick.parser;

import com.taobao.loganalyzer.input.tanxclick.common.LogField;
import com.taobao.loganalyzer.input.tanxclick.common.LogParser;
import com.taobao.loganalyzer.input.tanxclick.common.LogRecord;
import com.taobao.loganalyzer.input.tanxclick.common.SectionParser;

/**
 * Section 1: 版本Section01解析类
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class HeaderSectionParser implements SectionParser {

    /**
     * 对版本Section01进行解析
     * section1-版本section为空--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            //if (!lp.hasNextChar()) return false;
            //如果版本号字段为空，解析失败;
            return false ;
        }
        else {
            // version
            lr.addField("Version", new LogField(LogField.TYPE_STRING, lp.getNextCA()));
        }

        if (lp.isError()) {
            return false;
        }

        return true;
    }
}

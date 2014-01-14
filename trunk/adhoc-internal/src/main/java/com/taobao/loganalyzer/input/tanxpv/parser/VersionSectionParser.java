package com.taobao.loganalyzer.input.tanxpv.parser;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 1：版本Section解析类。版本号由2部分组成，表现为1.2。第一部分在“.”之前，只会在Section发生增减的时候才会递增。第二部分在“.”之后，任何Session中的字段发生增减的时候才会递增。。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class VersionSectionParser implements SectionParser {

    /**
     * 对版本Section进行解析
     * a)	section1-版本section为空--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            //if (!lp.hasNextChar()) return false;
            //日志版本号字段不能为空,若为空，解析失败。日志版本号字段不能为空,若为空，解析失败。
            return false ;
        }
        else{
            // Version
            lr.addField("Version",	new LogField(LogField.TYPE_STRING, lp.getNextCA()));
        }

        if (lp.isError())
            return false;

        return true;
    }

}

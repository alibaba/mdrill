package com.taobao.loganalyzer.input.tanxpv.parser;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 12：校验Section解析类。取Session ID的前4个bytes。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class VerifySectionParser implements SectionParser {

    /**
     * 对校验Section进行解析
     * k)	section11-校验section--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            return false;
        }
        else {
            // Verify
            if (lr.version_cmp("2.0") < 0) {
                lr.addField("Verify",	new LogField(LogField.TYPE_STRING, lp.getAllRemained()));
            } else {
                lr.addField("Verify",	new LogField(LogField.TYPE_STRING, lp.getNextCA()));
            }

        }

        if (lp.isError())
            return false;

        return true;
    }

}

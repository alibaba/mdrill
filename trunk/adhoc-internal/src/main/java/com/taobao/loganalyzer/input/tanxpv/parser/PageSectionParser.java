package com.taobao.loganalyzer.input.tanxpv.parser;

import java.util.ArrayList;
import java.util.List;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 6：网页Section解析类。该Section保存和网页有关的信息。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class PageSectionParser implements SectionParser {

    /**
     * 对网页Section进行解析   (版本1.0不记录)
     * e)	section5-网页section为空（不记录）--------允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            if (!lp.hasNextChar()) return false;
        }
        else {

            String ws = lp.getNextCB();
            if (ws == null) {
                return false;
            }
            String[] warr = ws.split("\003" , -1);

            List<String[]> googleweight = new ArrayList<String[]>();

            for (String str : warr) {
                String[] pair = str.split("\004", -1);
                if (pair.length != 2) {
                    continue;
                }
                googleweight.add(pair);
            }
            lr.addField("googleweight",    new LogField(LogField.TYPE_LIST, googleweight));
            lp.getNextCB();
            lp.getNextCB();
            lp.getNextCA();
        }

        if (lp.isError())
            return false;

        return true;
    }

}

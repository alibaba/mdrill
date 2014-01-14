package com.taobao.loganalyzer.input.tanxpv.parser;

import java.util.ArrayList;
import java.util.List;


import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 10：CPA Section解析类。该Section保存和CPA有关的信息。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class CPASectionParser implements SectionParser {

    /**
     * 对CPA Section进行解析(版本1.0不记录)
     * i)	section9-CPA section（不记录）--------允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            if (!lp.hasNextChar()) return false;
        }
        else {
            List<LogRecord> list = new ArrayList<LogRecord>();
            do {
                if (lp.curChar() == LogParser.CONTROL_B) {
                    if (!lp.hasNextChar()) return false;
                    continue;
                } else if (lp.curChar() == LogParser.CONTROL_A) {
                    if (!lp.hasNextChar()) return false;
                    break;
                }

                LogRecord inner_lr = new LogRecord();

                //service id
                inner_lr.addField("ServiceID", new LogField(LogField.TYPE_STRING, lp.getNextCC()));

                //origin  商品来源
                inner_lr.addField("Origin",    new LogField(LogField.TYPE_STRING, lp.getNextCC()));

                //客户ID
                inner_lr.addField("MemberID",  new LogField(LogField.TYPE_STRING, lp.getNextCC()));

                //本次商品的价格
                inner_lr.addField("Price",     new LogField(LogField.TYPE_STRING, lp.getNextCC()));

                //客户的回扣率， 小于1.0的3位小数
                inner_lr.addField("Brokerage", new LogField(LogField.TYPE_STRING, lp.getNextCC()));

                //绝对位置
                String abs_pos = lp.getNextField(new char[]{LogParser.CONTROL_B, LogParser.CONTROL_A});
                inner_lr.addField("AbsPos",    new LogField(LogField.TYPE_STRING, abs_pos));

                list.add(inner_lr);
            }
            while (lp.getMatchedChar() != 0 && lp.getMatchedChar() != LogParser.CONTROL_A);

            lr.addField("CPAList", new LogField(LogField.TYPE_LIST, list));
        }

        if (lp.isError())
            return false;

        return true;
    }

}

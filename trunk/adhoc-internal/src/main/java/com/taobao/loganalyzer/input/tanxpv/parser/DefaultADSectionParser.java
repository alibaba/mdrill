package com.taobao.loganalyzer.input.tanxpv.parser;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 11：默认广告Section解析类。该Section保存用户自定义的广告信息。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class DefaultADSectionParser implements SectionParser {

    /**
     * 对默认广告Section进行解析 (版本1.0不记录)
     * j)	section10-默认广告section（不记录）--------允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            if (!lp.hasNextChar()) return false;
        }
        else{
            //是否黑名单  0 否 1 是
            lr.addField("Blacklist",    new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //黑名单出现的次数
            lr.addField("BlacklistDup", new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //是否自定义  0 否 1 是
            lr.addField("SelfDef",      new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //自定义出现的次数
            lr.addField("SelfDefDup",   new LogField(LogField.TYPE_STRING, lp.getNextCA()));
        }

        if (lp.isError())
            return false;

        return true;
    }

}

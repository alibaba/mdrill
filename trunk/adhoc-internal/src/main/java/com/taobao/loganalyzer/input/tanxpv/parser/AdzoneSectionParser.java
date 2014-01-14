package com.taobao.loganalyzer.input.tanxpv.parser;

import java.util.ArrayList;
import java.util.List;


import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 4：广告位Section解析类。该Section保存和这个广告有关的信息。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class AdzoneSectionParser implements SectionParser {

    /**
     * 对广告位Section进行解析
     * d)	section4-广告位section为空--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            return false ;
            //	if (!lp.hasNextChar()) return false;
        }
        else {
            // 广告位类目
            lr.addField("AdzoneCategory",  new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 广告位位置
            lr.addField("AdzonePosition",  new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 广告位size code
            lr.addField("AdzoneSize",      new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 广告位大小
            lr.addField("AdzoneArea",      new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 是否首屏
            lr.addField("IsFirstPage",     new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 广告位展现方式
            lr.addField("ViewMode",        new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 广告位最低限价(最低竞价额)
            lr.addField("LimitPrice",      new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            // 广告位属性
            lr.addField("AdzoneAttribute", new LogField(LogField.TYPE_STRING, lp.getNextCB()));


            if (lr.version_cmp("1.4") >= 0) {////1.4以后版本的
                // 广告位类型
                lr.addField("AdzoneType", new LogField(LogField.TYPE_STRING, lp.getNextCB()));
                // 广告位投放尺寸
                lr.addField("AdzonePutOnSize", new LogField(LogField.TYPE_STRING, lp.getNextCA()));
            } else { //1.4前版本的

                // 广告位类型
                lr.addField("AdzoneType", new LogField(LogField.TYPE_STRING, lp.getNextCA()));
            }
        }

        if (lp.isError()) {
            return false;
        }

        return true;
    }

}

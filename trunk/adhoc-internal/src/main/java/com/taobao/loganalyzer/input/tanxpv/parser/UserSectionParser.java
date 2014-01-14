package com.taobao.loganalyzer.input.tanxpv.parser;

import java.util.ArrayList;
import java.util.List;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 9：User Section解析类。该Section保存和用户有关的信息。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class UserSectionParser implements SectionParser {

    /**
     * 对用户Section08进行解析  (版本1.0不记录)
     * h)	section8-用户section（不记录）--------允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            if (!lp.hasNextChar()) return false;
        }
        else {
            //用户行为信息
            lr.addField("UserActionInfo",  new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //人群信息
            if ( lr.version_cmp("1.0") > 0 )
            {
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
                    //兴趣类目
                    inner_lr.addField("Category",        new LogField(LogField.TYPE_STRING, lp.getNextCD()));
                    //分数
                    inner_lr.addField("Score",        new LogField(LogField.TYPE_STRING, lp.getNextCD()));

                    //消费能力
                    String ablity = lp.getNextField(new char[]{LogParser.CONTROL_C, LogParser.CONTROL_B,LogParser.CONTROL_A});
                    //不允许再包含^D
                    if (ablity!=null && ablity.indexOf(LogParser.CONTROL_D)>=0 )
                    {
                        return false ;
                    }
                    inner_lr.addField("ConsumeAbility",   new LogField(LogField.TYPE_STRING, ablity ));

                    list.add(inner_lr);

                } while (lp.getMatchedChar() != 0 && lp.getMatchedChar() != LogParser.CONTROL_B);

                //人群信息
                lr.addField("CrowdInfoList", new LogField(LogField.TYPE_LIST, list));

                //性别
                lr.addField("UserSex",  new LogField(LogField.TYPE_STRING, lp.getNextCB()));

                //整体消费能力
                lr.addField("WholeConsumeAblity",  new LogField(LogField.TYPE_STRING, lp.getNextCA()));


            }else
            {
                //取群体行为方式的标志
                lr.addField("CrowdActionType",	new LogField(LogField.TYPE_STRING, lp.getNextCA()));
            }

        }

        if (lp.isError())
            return false;

        return true;
    }

}

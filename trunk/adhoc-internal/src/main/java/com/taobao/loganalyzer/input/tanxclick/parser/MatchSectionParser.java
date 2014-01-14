package com.taobao.loganalyzer.input.tanxclick.parser;

import com.taobao.loganalyzer.input.tanxclick.common.LogField;
import com.taobao.loganalyzer.input.tanxclick.common.LogParser;
import com.taobao.loganalyzer.input.tanxclick.common.LogRecord;
import com.taobao.loganalyzer.input.tanxclick.common.SectionParser;

/**
 * Section 4: 匹配Section04解析类
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class MatchSectionParser implements SectionParser {

    /**
     * 对匹配Section04进行解析,版本1.0不记录。
     * section4-匹配section为空（不记录）--------允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            if (!lp.hasNextChar()) return false;
        }
        else{
            String version=(lr.getField("Version")==null)?"":lr.getField("Version").getContent().toString();
            //search keyword
            lr.addField("SearchKeyword", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //匹配模式
            //lr.addField("MatchMode", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //匹配子模式
            //lr.addField("MatchSubmode", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //页码
            lr.addField("PageNum", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //每页的广告数
            lr.addField("AdsPerPage", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //相对位置
            lr.addField("RelativePosition", new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //区域
            lr.addField("Area", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //广告位分类ID
            lr.addField("AdzoneTypeID", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));
            if("2.1".equals(version)){
                //query rewrite的结果
                lr.addField("QueryAfterRewrite",  new LogField(LogField.TYPE_STRING, lp.getNextCA()));
            }else{
                lr.addField("QueryAfterRewrite",  new LogField(LogField.TYPE_STRING, lp.getNextCB()));
                lr.addField("AdRankingOrder",  new LogField(LogField.TYPE_STRING, lp.getNextCA()));
            }
        }

        if (lp.isError()) {
            return false;
        }

        return true;
    }


}

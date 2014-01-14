package com.taobao.loganalyzer.input.tanxclick.parser;


import com.taobao.loganalyzer.input.tanxclick.common.LogField;
import com.taobao.loganalyzer.input.tanxclick.common.LogParser;
import com.taobao.loganalyzer.input.tanxclick.common.LogRecord;
import com.taobao.loganalyzer.input.tanxclick.common.SectionParser;

/**
 * Section 3: 广告Section解析类
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class AdSectionParser implements SectionParser {

    /**
     * 对广告Section进行解析
     * section3-广告section为空--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            //if (!lp.hasNextChar()) return false;
            return false ;
        }
        else{
            //广告分类ID
            lr.addField("AdTypeID", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //广告来源
            lr.addField("AdOrigin", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //第三方ID
            lr.addField("ThridPartyID", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //客户购买的归一化之前的关键词
            lr.addField("Keyword", 			new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //最高出价
            lr.addField("HighestPrice", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //结算价格
            lr.addField("CostPrice", new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //折扣率  小于1.0的小数
            lr.addField("DiscountRate", new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //广告位尺寸
            lr.addField("AdzoneSize", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            ////保留
            lr.addField("Undefine2", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));


            //客户URL
            lr.addField("CustomURL", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //定向类型
            lr.addField("TargetType", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //定向属性
            lr.addField("TargetAttribute", 	new LogField(LogField.TYPE_STRING, lp.getNextCA()));

        }

        if (lp.isError()) {
            return false;
        }

        return true;
    }

}

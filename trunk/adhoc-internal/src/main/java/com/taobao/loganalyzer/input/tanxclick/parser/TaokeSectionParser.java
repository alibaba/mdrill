package com.taobao.loganalyzer.input.tanxclick.parser;

import com.taobao.loganalyzer.input.tanxclick.common.LogField;
import com.taobao.loganalyzer.input.tanxclick.common.LogParser;
import com.taobao.loganalyzer.input.tanxclick.common.LogRecord;
import com.taobao.loganalyzer.input.tanxclick.common.SectionParser;

/**
 * Section 5: 推广Section05解析类
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class TaokeSectionParser implements SectionParser {

    /**
     * 对推广Section05进行解析(版本1.0不记录)
     *section5-推广section为空（不记录）--------允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {

        if (lp.isCurCharCA()) {
            if (!lp.hasNextChar()) return false;
        }
        else {
            //部署类型
            lr.addField("DeployType", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //宝贝价格
            lr.addField("ItemPrice", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //价格佣金率
            lr.addField("PriceComRate", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //实际佣金率
            lr.addField("ActualComRate", 	new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //店铺佣金率
            lr.addField("ShopComRate", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //origintype
            lr.addField("OriginType", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //展示模式
            lr.addField("ShowMode", 		new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //推广方式相应的id
            lr.addField("PromotionType", 	new LogField(LogField.TYPE_STRING, lp.getNextCA()));
        }

        if (lp.isError()) {
            return false;
        }

        return true;
    }
}

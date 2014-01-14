package com.taobao.loganalyzer.input.tanxpv.parser;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 7：广告Section解析类,。该Section保存展现的广告的信息
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class AdSectionParser implements SectionParser {

    /**
     * 对广告Section进行解析
     * f)	section6-广告section为空--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA())
        {
            //if (!lp.hasNextChar()) return false;
            return false ;
        } else
        {
            // Service ID
            lr.addField("ServiceID", 	new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 客户ID
            lr.addField("CustomerID", 	new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 产品类型
            lr.addField("ProductType", 	new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 子产品类型
            lr.addField("SubProductType", new LogField(LogField.TYPE_STRING, lp.getNextCB()));



            // DSP广告结算价格
            String s6_5 = lp.getNextCB() ;
            //lr.addField("DspCostPrice", 	new LogField(LogField.TYPE_STRING, 		lp.getNextCB()));

            // 客户竞价过程
            lr.addField("BidProcess", 		new LogField(LogField.TYPE_LIST,	lp.getNextCB()));

            // 流量类型(不记录)
            lr.addField("FlowType", 		new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 5.6.8 结算价格
            String s6_8 = lp.getNextCB() ;
            //lr.addField("CostPrice", new LogField(LogField.TYPE_STRING,	lp.getNextCB()));

            // 匹配模式
            lr.addField("MatchType", 		new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 匹配子模式
            lr.addField("SubMatchMode", 	new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 广告类目
            lr.addField("AdCategory", 		new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 广告类目confidence
            lr.addField("AdCateConfidence", new LogField(LogField.TYPE_STRING,	lp.getNextCB()));

            // 广告来源
            lr.addField("AdSource", 		new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 广告牌类型
            lr.addField("AdBoardType", 		new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 广告的属性Tag
            lr.addField("AdAttribute", 		new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 定向类型(不记录)
            lr.addField("TargetType", 		new LogField(LogField.TYPE_STRING, 	lp.getNextCB()));

            // 定向属性(不记录)
            lr.addField("TargetAttribute", 	new LogField(LogField.TYPE_STRING,	lp.getNextCA()));

            //志侠协商后,修改.日志中不区分DspCostPrice,CostPrice的版本

            lr.addField("DspCostPrice", 	new LogField(LogField.TYPE_STRING, 	s6_5)) ;
            lr.addField("CostPrice", 		new LogField(LogField.TYPE_STRING, 	s6_8)) ;
            lr.addField("AbsolutePosition", new LogField(LogField.TYPE_STRING, 	"")) ;


        }

        if (lp.isError()) return false;

        return true;
    }



}

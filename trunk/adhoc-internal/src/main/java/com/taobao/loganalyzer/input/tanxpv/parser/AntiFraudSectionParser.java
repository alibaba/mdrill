package com.taobao.loganalyzer.input.tanxpv.parser;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 8：AntiFraud Section解析类。该Section保存和防作弊有关的信息。
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class AntiFraudSectionParser implements SectionParser {

    /**
     * 对AntiFraud Section进行解析
     * g)	section7-反作弊section为空--------不允许
     */
    public boolean parse(LogParser lp, LogRecord lr) {
        if (lp.isCurCharCA()) {
            //if (!lp.hasNextChar()) return false;
            return false ;
        }
        else {
            //随机数
            lr.addField("RandomNum",		 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //随机数变量中对应的ascii
            lr.addField("Ascii",			 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //window.screen.width分辨率宽
            lr.addField("ScreenWidth",		 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //window.screen.height分辨率高
            lr.addField("ScreenHeight",		 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //window.screen.availHeight屏幕可用工作区宽度
            lr.addField("ScreenAvailWidth",	 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //window.screen.availHeight屏幕可用工作区高度
            lr.addField("ScreenAvailHeight", new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //document.body.clientWidth 网页可见宽度
            lr.addField("BodyClientWidth",	 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //document.body.clientHeight 网页可见高度
            lr.addField("BodyClientHeight",	 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //色深
            lr.addField("ColorDepth",		 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //时区
            lr.addField("TimeArea",			 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //history的长度
            lr.addField("HistoryLength",	 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //是否开启java
            lr.addField("IsOpenJava",		 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //插件个数
            lr.addField("PluginNum",		 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //mime类型个数
            lr.addField("MimeNum",			 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //flash版本
            lr.addField("FlashVersion",   	 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //HTTP_VIA
            lr.addField("HttpVia",   		 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //HTTP_X_FORWARDED_FOR|HTTP_PROXY_CONNECTION
            lr.addField("HttpX",			 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //HTTP_ACCEPT
            lr.addField("HttpAccept",		 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //HTTP_ACCEPT_CHARSET
            lr.addField("HttpAccCharset",	 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //HTTP_ACCPET_LANGUAGE
            lr.addField("HttpAccLanguage",	 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //HTTP_ACCEPT_ENCODING
            lr.addField("HttpAccEncoding",	 new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //1.1和1.2版本. 兼容代码.下一版本可以去除.
            if("1.1".equals(lr.getField("Version").getContent()) || "1.2".equals(lr.getField("Version").getContent())){

                //HTTP_KEEP_ALIVE
                lr.addField("HttpKeepAlive",	 new LogField(LogField.TYPE_STRING, lp.getNextCA()));

            }
            //1.3版本以后
            if(lr.version_cmp("1.3") >= 0){
                //HTTP_KEEP_ALIVE
                lr.addField("HttpKeepAlive",	new LogField(LogField.TYPE_STRING, lp.getNextCB()));
                //FILTER_CODE
                lr.addField("FilterCode", 		new LogField(LogField.TYPE_STRING,lp.getNextCB()));
                //FILTER_FLAG
                lr.addField("FilterFlag", 		new LogField(LogField.TYPE_STRING,lp.getNextCA()));
            }

        }

        if (lp.isError()) {
            return false;
        }

        return true;
    }

}

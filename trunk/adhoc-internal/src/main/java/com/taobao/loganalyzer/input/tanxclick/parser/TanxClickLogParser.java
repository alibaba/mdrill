package com.taobao.loganalyzer.input.tanxclick.parser;

import com.taobao.loganalyzer.input.tanxclick.common.LogParser;
import com.taobao.loganalyzer.input.tanxclick.common.LogRecord;
import com.taobao.loganalyzer.input.tanxclick.common.SectionParser;

/**
 * 点击日志的解析
 * @version 1.0
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class TanxClickLogParser {
    private static SectionParser[] sp = {
            new HeaderSectionParser(),
            new CommonSectionParser(),
            new AdSectionParser(),
            new MatchSectionParser(),
            new TaokeSectionParser(),
            new AFSectionParser()
    };
    private static final int CLICK_SECTIONS = sp.length;

    /**
     * 解析点击日志指定的section，当前版本的section个数为6
     * @param line 需要解析的点击日志
     * @param flags 需要点击的点击日志的section的列表，true为需要解析，false为不需要解析；建议熟悉底层解析逻辑者使用。
     * @return 解析的结果，存储在hashtable中，null表示格式不符合
     */
    public static LogRecord parseClick(String line, boolean[] flags) {
        if (flags.length != CLICK_SECTIONS) {
            throw new RuntimeException("Expected " + flags.length + " sections, but only " + CLICK_SECTIONS + " section exist.");
        }
        //5.4 匹配Section04(6)(不记录)
        //5.5 推广Section05(8)(不记录)
        flags[3] = false ;
        flags[4] = false ;
        LogRecord lr = new LogRecord();
        LogParser lp = new LogParser(line);

        for (int i = 0; i < sp.length; i++) {
            if (flags[i] == true) {
                boolean ret = sp[i].parse(lp, lr);
                if (ret == false) {
                    return null;
                }
            } else {
                if (lp.skipNextFieldCA() == false && i != sp.length-1) {
                    return null;
                }
            }
        }

        return lr;
    }

    /**
     * 解析点击日志指定的section，当前版本的section个数为6
     * @param line 需要解析的点击日志
     * @param flags 需要点击的点击日志的section的列表，true为需要解析，false为不需要解析；建议熟悉底层解析逻辑者使用。
     * @return 解析的结果，存储在ClickLog中，null表示格式不符合
     */
    public static TanxClickLog parse(String line, boolean[] flags) {
        LogRecord lr = parseClick(line, flags);
        if (lr == null) return null;
        return new TanxClickLog(lr);
    }

    /**
     * 解析该点击日志的所有section
     * 日志的版本号字段不能为空，为空会使解析失败。
     * @param line 需要解析的点击日志
     * @return 解析的结果，存储在ClickLog中，null表示格式不符合
     */
    public static TanxClickLog parse(String line) {
        boolean[] flags = new boolean[]{true, true, true, true, true, true};
        return parse(line, flags);
    }
    public static void main(String[] args){
        String line="2.2f051a1fb8cc225dc118671c7ed4583bc0af34d8ae742a9e8f8dc269e970d137412452254301245225768121.12.249.194StCRTAntOZRQBAcL5DHlP2my91238120116S419252_1006http://search1.taobao.com/browse/0/n-1----------------------0-----------------------g,2g44x5nu7q----------------40--commend-40-all-0.htm?ssid=p101-s510052_20000660_20000832_300257171100000061397600,0redbaby10921 50002711T3c3c19147ac0e688b837226880ac3ad0压缩袋3993971http://item.taobao.com/auction/item_detail.jhtml?item_id=3c3c19147ac0e688b837226880ac3ad0压缩袋25502haha1.1*/*zh-cngzip, deflateMozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; MAXTHON 2.0)00";
        TanxClickLog cl=TanxClickLogParser.parse(line);
        //	System.out.println(cl.getAdRankingOrder());
    }
}

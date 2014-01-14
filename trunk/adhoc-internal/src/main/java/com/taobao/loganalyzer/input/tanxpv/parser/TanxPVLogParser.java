package com.taobao.loganalyzer.input.tanxpv.parser;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Tanx PV日志的解析
 * @version 1.0
 * @see <a href="http://sps.corp.alimama.com/ad/ADExchange/DocLib/Tan(X)%E7%B3%BB%E7%BB%9F%E6%97%A5%E5%BF%97%E8%AE%BE%E8%AE%A1.pdf">点击日志格式</a>
 * @author kangtian
 *
 */
public class TanxPVLogParser {
    private static SectionParser[] sp = {
            new VersionSectionParser(),
            new CommonSectionParser(),
            new SiteSectionParser(),
            new AdzoneSectionParser(),
            new PageSectionParser(),
            new AdSectionParser(),
            new AntiFraudSectionParser(),
            new UserSectionParser(),
            new CPASectionParser(),
            new DefaultADSectionParser(),
            new VerifySectionParser(),
            new WirlessSectionParser()
    };
    private static final int PV_SECTIONS = sp.length;

    /**
     * 解析Tanx PV日志中指定的section，当前版本的section个数为11
     * @param line 需要解析的pv日志
     * @param flags 需要解析的pv日志的section的列表，true为需要解析，false为不需要解析；建议熟悉底层解析逻辑者使用。
     * @return 解析的结果，存储在hashtable中，null表示格式不符合
     */
    public static LogRecord parseTanxPV(String line, boolean[] flags) {
        if (flags.length != PV_SECTIONS) {
            throw new RuntimeException("Expected " + flags.length + " sections, but only " + PV_SECTIONS + " section exist.");
        }

        // important:版本1.0不记录如下字段，因此，我没改相应的sectionoParpser的东西，如果以后需要记录，则要注意修改相应的sectionParser的所有内容。
        //skip section 5 : PageSection ;
        //skip section 8 : UserSection ---> don't skip @20110509;
        //skip section 9 : CPASection ;
        //skip section 10: defaultADsection ;

        //flags[4] = false ;don't skip any more
        //flags[7] = false ; don't skip any more
        flags[8] = false ;
        flags[9] = false ;

        LogRecord lr = new LogRecord();
        LogParser lp = new LogParser(line);

        for (int i = 0; i < PV_SECTIONS; i++) {
            if (flags[i] == true) {
                boolean ret = sp[i].parse(lp, lr);
                if (ret == false) {
                    System.err.println(sp[i].getClass() + " run error");
                    return null;
                }
            } else {
                if (lp.skipNextFieldCA() == false && i != sp.length-1) {
                    return null;
                }
            }
        }

        if (!check(lr)) return null;

        return lr;
    }

    /**
     * 解析Tanx PV日志中指定的section，当前版本的section个数为12
     * @param line 需要解析的pv日志
     * @param flags 需要解析的pv日志的section的列表，true为需要解析，false为不需要解析；建议熟悉底层解析逻辑者使用。
     * @return 解析的结果，存储在TanxPVLog中，null表示格式不符合
     */
    public static TanxPVLog parse(String line, boolean[] flags) {
        LogRecord lr = parseTanxPV(line, flags);
        if (lr == null) return null;
        return new TanxPVLog(lr);
    }

    /**
     * 解析该Tanx PV日志的所有section
     * <br>日志版本号字段不能为空,若为空，解析失败。
     * <br>parser会对日志进行验证，取校验Section和共用section 的Session ID的前4个bytes进行校验，若两者不相等，会解析失败。
     * @param line 需要解析的PV日志
     * @return 解析的结果，存储在TanxPVLog中，null表示格式不符合
     */
    public static TanxPVLog parse(String line) {
        boolean[] flags = new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true};
        return parse(line, flags);
    }

    private static boolean check(LogRecord lr) {
        // 校验Section：1.2取Session ID的前4个bytes
        String verify = getFieldContent(lr, "Verify");

        String sessionID = getFieldContent(lr, "SessionID");
        if (verify == null || verify.length() != 4 || sessionID == null || !sessionID.startsWith(verify))
            return false;

        return true;
    }

    private static String getFieldContent(LogRecord lr, String name) {
        if (lr != null) {
            LogField lf = lr.getField(name);
            return lf==null?null:(String)lf.getContent();
        }
        return null;
    };

}


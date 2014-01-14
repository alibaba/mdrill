package com.taobao.loganalyzer.input.tanxclick.common;

/**
 * 用于各个section解析的接口 
 * @author liyuntao
 *
 */
public interface SectionParser {
    /**
     * 特定section的解析
     * @param lp 日志解析工具类
     * @param lr 日志结果存储
     * @return 该Section是否符合格式
     */
    boolean parse(LogParser lp, LogRecord lr);
}

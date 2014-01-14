package com.taobao.loganalyzer.input.tanxclick.common;

/**
 * 日志解析工具，传入一条日志，利用特定的分隔符进行字段分割
 * @author liyuntao
 *
 */
public class LogParser {
    /**
     * 字段分隔符CTRL+A
     */
    public static final char CONTROL_A = '\u0001';

    /**
     * 字段分隔符CTRL+B
     */
    public static final char CONTROL_B = '\u0002';

    /**
     * 字段分隔符CTRL+C
     */
    public static final char CONTROL_C = '\u0003';

    /**
     * 字段分隔符CTRL+D
     */
    public static final char CONTROL_D = '\u0004';

    private String    log = null;
    private int       pos = 0;
    private int    length = 0;
    private boolean error = false;
    private String errMsg = null;
    private char matchedChar = 0;

    /**
     * 构造一个从日志某一特定位置开始解析的LogParser
     * @param log 需要解析的日志
     * @param pos 解析的开始位置
     */
    public LogParser(String log, int pos) {
        this.log = log;
        this.pos = pos;
        this.length = log.length();
    }

    /**
     * 构造一个从日志起始位置开始解析的LogParser
     * @param log 需要解析的日志
     */
    public LogParser(String log) {
        this(log, 0);
    }

    /**
     * 判断当前字符是否是CTRL+A
     * @return true为是，false为不是
     */
    public boolean isCurCharCA() {
        if (pos >= length) return false;

        return log.charAt(pos) == CONTROL_A;
    }

    /**
     * 获取当前字符
     * @return 返回当前位置解析的字符
     */
    public char curChar() {
        if (pos >= length) return 0;

        return log.charAt(pos);
    }

    /**
     * 获取{@link #getNextField(char[] ch)}中匹配的字符
     * @return 匹配的字符
     */
    public char getMatchedChar() {
        return matchedChar;
    }

    /**
     * 将解析的指针指向下一个字符，并判断该字符是否存在
     * @return true为有，false为没有
     */
    public boolean hasNextChar() {
        return ++pos < length;
    }

    /**
     * 未进行解析的日志中是否还有ch存在
     * @param ch 需要判断是否存在的字符
     * @return true为有，false为没有
     */
    public boolean hasNextField(char ch) {
        return !(log.indexOf(ch, pos)==-1);
    }

    /**
     * 未进行解析的日志中是否还有CTRL+A存在
     * @return true为有，false为没有
     */
    public boolean hasNextCA() {
        return hasNextField(CONTROL_A);
    }

    /**
     * 跳过下一个以ch为分隔符的字段
     * @param ch 分隔符
     * @return　true为成功，false为解析失败
     */
    public boolean skipNextField(char ch) {
        int index = pos;

        pos = log.indexOf(ch, pos);
        if (pos != -1) {
            pos ++;     // skip the ch
            return true;
        }

        if (error == false) {
            error = true;
            errMsg = new StringBuilder().append("error: expected ").append(ch)
                    .append(" from ").append(index).toString();
        }
        pos = length + 1;   // error happens

        return false;
    }

    /**
     * 跳过下一个以CTRL+A分隔的字段
     * @return　true为成功，false为解析失败
     */
    public boolean skipNextFieldCA() {
        return skipNextField(CONTROL_A);
    }

    /**
     * 获取下一个以ch中的任一个元素为分隔符的字段，可通过{@link #getMatchedChar()}获取匹配的分隔符
     * @param ch 分隔符的数组
     * @return 下一个以ch中的任一个元素为分隔符的字段
     */
    public String getNextField(char[] ch) {
        int index = pos;
        int i = 0;
        char c;
        matchedChar = 0;

        while (pos < length) {
            c = log.charAt(pos);
            for (i = 0; i < ch.length && ch[i] != c; i++);

            if (i == ch.length)
                pos ++;
            else {
                matchedChar = c;
                return log.substring(index, pos++); // skip the ch
            }
        }

        if (error == false) {
            error = true;
            errMsg = new StringBuilder().append("error: expected ").append(ch)
                    .append(" from ").append(index).toString();
        }
        pos = length + 1;   // error happens

        return null;
    }

    /**
     * 获取下一个以ch分隔的字段
     * @param ch 分隔符
     * @return 下一个以ch分隔的字段，null为解析出错
     */
    public String getNextField(char ch) {
        int index = pos;

        pos = log.indexOf(ch, pos);
        if (pos != -1)
            return log.substring(index, pos++); // skip the ch

        if (error == false) {
            error = true;
            errMsg = new StringBuilder().append("error: expected ").append(ch)
                    .append(" from ").append(index).toString();
        }
        pos = length + 1;   // error happens

        return null;
    }

    /**
     * 获取下一个以CTRL+A分隔的字段
     * @return 下一个以CTRL+A分隔的字段
     */
    public String getNextCA() {
        String field = getNextField(new char[]{CONTROL_A, CONTROL_B});

        if (getMatchedChar() != CONTROL_A) {
            if (error == false) {
                error = true;
                errMsg = new StringBuilder().append("error: expected ").append(CONTROL_B)
                        .append(" from ").append(pos).toString();
            }
            pos = length + 1;   // error happens
            return null;
        }

        return field;
    }

    /**
     * 获取下一个以CTRL+B分隔的字段
     * @return 下一个以CTRL+B分隔的字段
     */
    public String getNextCB() {
        String field = getNextField(new char[]{CONTROL_A, CONTROL_B});

        if (getMatchedChar() != CONTROL_B) {
            if (error == false) {
                error = true;
                errMsg = new StringBuilder().append("error: expected ").append(CONTROL_B)
                        .append(" from ").append(pos).toString();
            }
            pos = length + 1;   // error happens
            return null;
        }

        return field;
    }

    /**
     * 获取下一个以CTRL+C分隔的字段
     * @return 下一个以CTRL+C分隔的字段
     */
    public String getNextCC() {
        String field = getNextField(new char[]{CONTROL_A, CONTROL_B, CONTROL_C});

        if (getMatchedChar() != CONTROL_C) {
            if (error == false) {
                error = true;
                errMsg = new StringBuilder().append("error: expected ").append(CONTROL_C)
                        .append(" from ").append(pos).toString();
            }
            pos = length + 1;   // error happens
            return null;
        }

        return field;
    }

    /**
     * 获取下一个以CTRL+D分隔的字段
     * @return 下一个以CTRL+D分隔的字段
     */
    public String getNextCD() {
        String field = getNextField(new char[]{CONTROL_A, CONTROL_B, CONTROL_C, CONTROL_D});

        if (getMatchedChar() != CONTROL_D) {
            if (error == false) {
                error = true;
                errMsg = new StringBuilder().append("error: expected ").append(CONTROL_D)
                        .append(" from ").append(pos).toString();
            }
            pos = length + 1;   // error happens
            return null;
        }

        return field;
    }

    /**
     * 获取剩下的所有未解析的日志
     * @return 为解析的日志，null为已经解析出错
     */
    public String getAllRemained() {
        if (pos < length)
            return log.substring(pos);
        return null;
    }

    /**
     * 获取当前解析的整条日志
     * @return 解析的日志
     */
    public String getLog() {
        return log;
    }

    /**
     * 获取出错时的相关状态信息
     * @return 出错信息
     */
    public String getErrMsg() {
        return errMsg;
    }

    /**
     * 当前解析的状态，是否已经发现日志不符合格式
     * @return true为解析出错，false为解析正常
     */
    public boolean isError() {
        return error;
    }
}

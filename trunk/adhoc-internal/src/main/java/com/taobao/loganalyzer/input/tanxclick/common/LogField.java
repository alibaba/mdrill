package com.taobao.loganalyzer.input.tanxclick.common;

/**
 * 单个日志字段的内容以及类型的存储，分为字符串和列表两种
 * @author liyuntao
 *
 */
public class LogField
{
    /**
     * 未定义
     */
    public static final int TYPE_NULL		= 0;

    /**
     * 字符串类型
     */
    public static final int TYPE_STRING	= 1;

    /**
     * 列表类型
     */
    public static final int TYPE_LIST		= 2;

    /**
     * 常量类型
     */
    public static final int TYPE_CONST	= 3;

    public LogField()
    {
        type = TYPE_NULL;
        content = null;
    }

    public LogField(int type, Object content)
    {
        this.type = type;
        this.content = content;
    }

    /**
     * 获取字段的类型
     * @return 字段类型
     */
    public int getType()
    {
        return type;
    }

    /**
     * 获取字段的内容
     * @return 字段内容
     */
    public Object getContent()
    {
        return content;
    }

    private int type;
    private Object content;
}


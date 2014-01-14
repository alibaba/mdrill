package com.taobao.loganalyzer.input.tanxpv.common;

import java.util.Hashtable;

/**
 * 根据特定的字段名存储多个字段的日志信息，其中字段名为key，封装日志信息的LogField对象为value
 * @author liyuntao
 *
 */
public class LogRecord
{
    /**
     * 构造一个新的LogRecord对象
     *
     */
    public LogRecord()
    {
        fields = new Hashtable<String, LogField>();
    }

    /**
     * 添加一个新的字段或者覆盖已有的同名字段
     * @param key 字段名
     * @param field 字段内容
     */
    public void addField(String key, LogField field)
    {
        fields.put(key, field);
    }

    /**
     * 获取特定字段的信息
     * @param key 字段名
     * @return 字段内容
     */
    public LogField getField(String key)
    {
        return fields.get(key);
    }

    /**
     * 递归打印所有已经存储的信息到标准输出
     *
     */
    public void printAll() {
        java.util.Enumeration<String> s = fields.keys();
        while (s.hasMoreElements()) {
            String name = s.nextElement();
            LogField lf = fields.get(name);
            if (lf.getType() == LogField.TYPE_STRING)
                System.out.println(name + ": " + lf.getContent());
            else if (lf.getType() == LogField.TYPE_LIST) {
                System.out.println(name + ": LIST ");
                java.util.List list = (java.util.List)lf.getContent();
                for (int i = 0; i < list.size(); i++) {
                    LogRecord l = (LogRecord)list.get(i);
                    l.printAll();
                }
            } else {
                System.out.println(name + " is null");
            }
        }
    }

    /**
     * 将本记录的版本号与给定的版本号比较。
     * @return  0 ：equals
     * 			>0: 记录版本大于缩写版本。
     * 			<0：记录版本小于给定版本。
     */
    public int version_cmp(String version)
    {
        String ver_cur = "" ;
        LogField ver = getField("Version") ;
        if(ver !=null)	ver_cur = ver.getContent().toString() ;

        String []ver1 = ver_cur.split("\\.", -1) ;
        String []ver2 = version.split("\\.", -1) ;
        if(ver1.length ==2 && ver2.length==2)
        {
            int ret = ver1[0].compareTo(ver2[0]) ;
            if(ret == 0) ret = ver1[1].compareTo(ver2[1]) ;

            return ret ;
        }
        //	throw new RuntimeException("version should like X.Y!");
        //如果不合规范，默认按新的版本处理。
        return 	1 ;
    }
    //content
    private	Hashtable<String, LogField> fields;
}
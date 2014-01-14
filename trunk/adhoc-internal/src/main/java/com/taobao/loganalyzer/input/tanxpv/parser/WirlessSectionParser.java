package com.taobao.loganalyzer.input.tanxpv.parser;

import com.taobao.loganalyzer.input.tanxpv.common.LogField;
import com.taobao.loganalyzer.input.tanxpv.common.LogParser;
import com.taobao.loganalyzer.input.tanxpv.common.LogRecord;
import com.taobao.loganalyzer.input.tanxpv.common.SectionParser;

/**
 * Section 12 无线流量信息Section
 * @see
 * @author
 *
 */
public class WirlessSectionParser implements SectionParser {


    public boolean parse(LogParser lp, LogRecord lr) {
        if (lr.version_cmp("2.0") < 0) {
            return true;
        }

        if (lp.isCurCharCA()) {
            return false ;
            //	if (!lp.hasNextChar()) return false;
        }
        else{
            //App包名
            lr.addField("12AppPackage",       new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //设备平台
            lr.addField("12DevicePlatform",             new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //操作系统
            lr.addField("12OS",     new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //操作系统版本号
            lr.addField("12OS_Version",          new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //制造厂商
            lr.addField("12Manufacturer",          new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //设备型号
            lr.addField("12DeviceModel",       new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //设备分辨率
            lr.addField("12DeviceScreen",              new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //设备IMEI号
            lr.addField("12DeviceIMEI",        new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //设备MAC号
            lr.addField("12DeviceMAC", new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //iOS设备的IDFA号
            lr.addField("12iOS_IDFA",      new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //网络连接类型
            lr.addField("12DeviceNetwork",           new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //经度
            lr.addField("12Longitude",   new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //纬度
            lr.addField("12Latitude",         new LogField(LogField.TYPE_STRING, lp.getNextCB()));

            //5_12_14
            lp.getNextCB();
            //5_12_15
            lp.getNextCB();
            //5_12_16
            lp.getNextCB();
            //5_12_17
            lp.getNextCB();
            //5_12_18
            lp.getNextCB();
            //5_12_19
            lp.getNextCB();
            //5_12_20
            lp.getAllRemained();

        }

        if (lp.isError())
            return false;

        return true;
    }
}

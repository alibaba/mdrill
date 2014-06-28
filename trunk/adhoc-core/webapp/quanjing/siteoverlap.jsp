<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.mdrill.ui.service.*" %>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>重合度分析</title>
  

  <script>
quanjing_logcheck=0;
</script>
 <script language="javascript" type="text/javascript" src="http://adhoc.etao.com:9999/quanjing_check_ch.jsp"></script>

<script>
	if(quanjing_logcheck!=1)
	{
		location.href='http://adhoc.etao.com:9999/quanjing_ch.jsp';
	}
	</script>
	
    
     <script language="javascript" type="text/javascript" src="../js/jquery.js"></script>
    <script language="javascript" type="text/javascript" src="../js/My97DatePicker/WdatePicker.js"></script>
    	
    
 <link rel="stylesheet" type="text/css"   href="./themes/redmond/jquery-ui-custom.css" />
<link rel="stylesheet" type="text/css"    href="./themes/ui.jqgrid.css" />
<link rel="stylesheet" type="text/css"     href="./themes/ui.multiselect.css" />
    
    <link  type="text/css" href="../css/a.tbcdn.cn.css" />
    <link  type="text/css" href="../css/adhoc.css" />

    <script src="../js/jquery.mousewheel.js" type="text/javascript"></script>
    <script src="../js/jquery.jqChart.min.js" type="text/javascript"></script>
    <script src="../js/jquery.jqRangeSlider.min.js" type="text/javascript"></script>

    <script language="javascript" type="text/javascript" src="../js/json2.js"></script>
    <!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../js/excanvas.min.js"></script><![endif]-->
     

<script>
	
	(function($){
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
    defaults : {
        recordtext: "{0} - {1}\u3000共 {2} 条", // 共字前是全角空格
        emptyrecords: "无数据显示",
        loadtext: "读取中...",
        pgtext : " {0} 共 {1} 页"
    },
    search : {
        caption: "搜索...",
        Find: "查找",
        Reset: "重置",
        odata: [{ oper:'eq', text:'等于\u3000\u3000'},{ oper:'ne', text:'不等\u3000\u3000'},{ oper:'lt', text:'小于\u3000\u3000'},{ oper:'le', text:'小于等于'},{ oper:'gt', text:'大于\u3000\u3000'},{ oper:'ge', text:'大于等于'},{ oper:'bw', text:'开始于'},{ oper:'bn', text:'不开始于'},{ oper:'in', text:'属于\u3000\u3000'},{ oper:'ni', text:'不属于'},{ oper:'ew', text:'结束于'},{ oper:'en', text:'不结束于'},{ oper:'cn', text:'包含\u3000\u3000'},{ oper:'nc', text:'不包含'}],
        groupOps: [ { op: "AND", text: "所有" },    { op: "OR",  text: "任一" } ],
    },
    edit : {
        addCaption: "添加记录",
        editCaption: "编辑记录",
        bSubmit: "提交",
        bCancel: "取消",
        bClose: "关闭",
        saveData: "数据已改变，是否保存？",
        bYes : "是",
        bNo : "否",
        bExit : "取消",
        msg: {
            required:"此字段必需",
            number:"请输入有效数字",
            minValue:"输值必须大于等于 ",
            maxValue:"输值必须小于等于 ",
            email: "这不是有效的e-mail地址",
            integer: "请输入有效整数",
            date: "请输入有效时间",
            url: "无效网址。前缀必须为 ('http://' 或 'https://')",
            nodefined : " 未定义！",
            novalue : " 需要返回值！",
            customarray : "自定义函数需要返回数组！",
            customfcheck : "Custom function should be present in case of custom checking!"
        }
    },
    view : {
        caption: "查看记录",
        bClose: "关闭"
    },
    del : {
        caption: "删除",
        msg: "删除所选记录？",
        bSubmit: "删除",
        bCancel: "取消"
    },
    nav : {
        edittext: "",
        edittitle: "编辑所选记录",
        addtext:"",
        addtitle: "添加新记录",
        deltext: "",
        deltitle: "删除所选记录",
        searchtext: "",
        searchtitle: "查找",
        refreshtext: "",
        refreshtitle: "刷新表格",
        alertcap: "注意",
        alerttext: "请选择记录",
        viewtext: "",
        viewtitle: "查看所选记录"
    },
    col : {
        caption: "选择列",
        bSubmit: "确定",
        bCancel: "取消"
    },
    errors : {
        errcap : "错误",
        nourl : "没有设置url",
        norecords: "没有要处理的记录",
        model : "colNames 和 colModel 长度不等！"
    },
    formatter : {
        integer : {thousandsSeparator: ",", defaultValue: '0'},
        number : {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'},
        currency : {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
        date : {
            dayNames:   [
                "日", "一", "二", "三", "四", "五", "六",
                "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六",
            ],
            monthNames: [
                "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二",
                "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"
            ],
            AmPm : ["am","pm","上午","下午"],
            S: function (j) {return j < 11 || j > 13 ? ['st', 'nd', 'rd', 'th'][Math.min((j - 1) % 10, 3)] : 'th';},
            srcformat: 'Y-m-d',
            newformat: 'Y-m-d',
            parseRe : /[Tt\\\/:_;.,\t\s-]/,
            masks : {
                // see http://php.net/manual/en/function.date.php for PHP format used in jqGrid
                // and see http://docs.jquery.com/UI/Datepicker/formatDate
                // and https://github.com/jquery/globalize#dates for alternative formats used frequently
                // one can find on https://github.com/jquery/globalize/tree/master/lib/cultures many
                // information about date, time, numbers and currency formats used in different countries
                // one should just convert the information in PHP format
                ISO8601Long:"Y-m-d H:i:s",
                ISO8601Short:"Y-m-d",
                // short date:
                //    n - Numeric representation of a month, without leading zeros
                //    j - Day of the month without leading zeros
                //    Y - A full numeric representation of a year, 4 digits
                // example: 3/1/2012 which means 1 March 2012
                ShortDate: "n/j/Y", // in jQuery UI Datepicker: "M/d/yyyy"
                // long date:
                //    l - A full textual representation of the day of the week
                //    F - A full textual representation of a month
                //    d - Day of the month, 2 digits with leading zeros
                //    Y - A full numeric representation of a year, 4 digits
                LongDate: "l, F d, Y", // in jQuery UI Datepicker: "dddd, MMMM dd, yyyy"
                // long date with long time:
                //    l - A full textual representation of the day of the week
                //    F - A full textual representation of a month
                //    d - Day of the month, 2 digits with leading zeros
                //    Y - A full numeric representation of a year, 4 digits
                //    g - 12-hour format of an hour without leading zeros
                //    i - Minutes with leading zeros
                //    s - Seconds, with leading zeros
                //    A - Uppercase Ante meridiem and Post meridiem (AM or PM)
                FullDateTime: "l, F d, Y g:i:s A", // in jQuery UI Datepicker: "dddd, MMMM dd, yyyy h:mm:ss tt"
                // month day:
                //    F - A full textual representation of a month
                //    d - Day of the month, 2 digits with leading zeros
                MonthDay: "F d", // in jQuery UI Datepicker: "MMMM dd"
                // short time (without seconds)
                //    g - 12-hour format of an hour without leading zeros
                //    i - Minutes with leading zeros
                //    A - Uppercase Ante meridiem and Post meridiem (AM or PM)
                ShortTime: "g:i A", // in jQuery UI Datepicker: "h:mm tt"
                // long time (with seconds)
                //    g - 12-hour format of an hour without leading zeros
                //    i - Minutes with leading zeros
                //    s - Seconds, with leading zeros
                //    A - Uppercase Ante meridiem and Post meridiem (AM or PM)
                LongTime: "g:i:s A", // in jQuery UI Datepicker: "h:mm:ss tt"
                SortableDateTime: "Y-m-d\\TH:i:s",
                UniversalSortableDateTime: "Y-m-d H:i:sO",
                // month with year
                //    Y - A full numeric representation of a year, 4 digits
                //    F - A full textual representation of a month
                YearMonth: "F, Y" // in jQuery UI Datepicker: "MMMM, yyyy"
            },
            reformatAfterEdit : false
        },
        baseLinkUrl: '',
        showAction: '',
        target: '',
        checkbox : {disabled:true},
        idName : 'id'
    }
});
})(jQuery);

</script>

     <script src="./js/jquery.jqGrid.min.js" type="text/javascript"></script>
    
<script>  
	
	
		   
	function dayToTimestamp2(str)
	{
		if(str.length<=10)
		{
			str+=" 00:00:00";
		}
	var new_str = str.replace(/:/g,'-');
		new_str = new_str.replace(/ /g,'-');
	var arr = new_str.split("-");
	if(arr.length<=5)
	{
		return 0;
	}
	
		var datum = new Date(Date.UTC(arr[0],arr[1]-1,arr[2],arr[3]-8,arr[4],arr[5]));
	
		return datum.getTime();
	}
	
			function dayToTimestampDay(s1)
	{

		return dayToTimestamp2(s1.substring(0,4)+"-"+s1.substring(4,6)+"-"+s1.substring(6));
	}
	
			function dayToTimestampDayHourMin(s1,hour)
	{

			var splitday=s1.substring(0,4)+"-"+s1.substring(4,6)+"-"+s1.substring(6)+" "+hour.substring(0,2)+":"+hour.substring(2,4)+":00";
			
		return dayToTimestamp2(splitday);
	}
	
   
		function dayToTimestamp(s1,s2)
	{

		var t1= dayToTimestamp2(s1.substring(0,4)+"-"+s1.substring(4,6)+"-"+s1.substring(6));
		var t2= dayToTimestamp2(s2.substring(0,4)+"-"+s2.substring(4,6)+"-"+s2.substring(6));
		
		return (t2-t1)/(1000*3600*24);
	}
	
	function showload()
	{
		
		      document.getElementById("loading").style.display="";  

          jQuery('#loading-one').empty().append('loading...').parent().fadeIn('slow'); 
	}
	
	
	function hideload()
	{
		
		          document.getElementById("loading").style.display="none";  

	}
	

function parseDay(date)
{
    var month = date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1);   
    var day = date.getDate() <= 9 ? "0" + (date.getDate()) : (date.getDate());   
    var hour = date.getHours() <= 9 ? "0" + (date.getHours()) : (date.getHours());
    var yyyymmdd= (date.getFullYear() + "" + month + "" + day);         
    return yyyymmdd;
}
     	
</script>
<style type="text/css">

	.demo-container {
		position: relative;
		height: 400px;
	}
body, button, input, select, textarea {
    font: 12px/1.5 tahoma,"microsoft yahei","微软雅黑";
}


	#menu {
		position: absolute;
		top: 20px;
		left: 625px;
		bottom: 20px;
		right: 20px;
		width: 200px;
	}

	#menu button {
		display: inline-block;
		width: 200px;
		padding: 3px 0 2px 0;
		margin-bottom: 4px;
		background: #eee;
		border: 1px solid #999;
		border-radius: 2px;
		font-size: 16px;
		-o-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		-ms-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		-moz-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		-webkit-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		box-shadow: 0 1px 2px rgba(0,0,0,0.15);
		cursor: pointer;
	}


	#code {
		display: block;
		width: 870px;
		padding: 15px;
		margin: 10px auto;
		border: 1px dashed #999;
		background-color: #f8f8f8;
		font-size: 16px;
		line-height: 20px;
		color: #666;
	}

	ul {
		font-size: 10pt;
	}

	ul li {
		margin-bottom: 0.5em;
	}

	ul.options li {
		list-style: none;
		margin-bottom: 1em;
	}

	ul li i {
		color: #999;
	}





/*!
 * jQuery UI CSS Framework 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Theming/API
 */

/* Layout helpers
----------------------------------*/
.ui-helper-hidden { display: none; }
.ui-helper-hidden-accessible { position: absolute !important; clip: rect(1px 1px 1px 1px); clip: rect(1px,1px,1px,1px); }
.ui-helper-reset { margin: 0; padding: 0; border: 0; outline: 0; line-height: 1.3; text-decoration: none; font-size: 100%; list-style: none; }
.ui-helper-clearfix:before, .ui-helper-clearfix:after { content: ""; display: table; }
.ui-helper-clearfix:after { clear: both; }
.ui-helper-clearfix { zoom: 1; }
.ui-helper-zfix { width: 100%; height: 100%; top: 0; left: 0; position: absolute; opacity: 0; filter:Alpha(Opacity=0); }


/* Interaction Cues
----------------------------------*/
.ui-state-disabled { cursor: default !important; }


/* Icons
----------------------------------*/

/* states and images */
.ui-icon { display: block; text-indent: -99999px; overflow: hidden; background-repeat: no-repeat; }


/* Misc visuals
----------------------------------*/

/* Overlays */
.ui-widget-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }


/*!
 * jQuery UI CSS Framework 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Theming/API
 *
 * To view and modify this theme, visit http://jqueryui.com/themeroller/?ffDefault=Lucida%20Grande,%20Lucida%20Sans,%20Arial,%20sans-serif&fwDefault=bold&fsDefault=1.1em&cornerRadius=5px&bgColorHeader=5c9ccc&bgTextureHeader=12_gloss_wave.png&bgImgOpacityHeader=55&borderColorHeader=4297d7&fcHeader=ffffff&iconColorHeader=d8e7f3&bgColorContent=fcfdfd&bgTextureContent=06_inset_hard.png&bgImgOpacityContent=100&borderColorContent=a6c9e2&fcContent=222222&iconColorContent=469bdd&bgColorDefault=dfeffc&bgTextureDefault=02_glass.png&bgImgOpacityDefault=85&borderColorDefault=c5dbec&fcDefault=2e6e9e&iconColorDefault=6da8d5&bgColorHover=d0e5f5&bgTextureHover=02_glass.png&bgImgOpacityHover=75&borderColorHover=79b7e7&fcHover=1d5987&iconColorHover=217bc0&bgColorActive=f5f8f9&bgTextureActive=06_inset_hard.png&bgImgOpacityActive=100&borderColorActive=79b7e7&fcActive=e17009&iconColorActive=f9bd01&bgColorHighlight=fbec88&bgTextureHighlight=01_flat.png&bgImgOpacityHighlight=55&borderColorHighlight=fad42e&fcHighlight=363636&iconColorHighlight=2e83ff&bgColorError=fef1ec&bgTextureError=02_glass.png&bgImgOpacityError=95&borderColorError=cd0a0a&fcError=cd0a0a&iconColorError=cd0a0a&bgColorOverlay=aaaaaa&bgTextureOverlay=01_flat.png&bgImgOpacityOverlay=0&opacityOverlay=30&bgColorShadow=aaaaaa&bgTextureShadow=01_flat.png&bgImgOpacityShadow=0&opacityShadow=30&thicknessShadow=8px&offsetTopShadow=-8px&offsetLeftShadow=-8px&cornerRadiusShadow=8px
 */


/* Component containers
----------------------------------*/
.ui-widget { font-family: Lucida Grande, Lucida Sans, Arial, sans-serif; font-size: 1.1em; }
.ui-widget .ui-widget { font-size: 1em; }
.ui-widget input, .ui-widget select, .ui-widget textarea, .ui-widget button { font-family: Lucida Grande, Lucida Sans, Arial, sans-serif; font-size: 1em; }
.ui-widget-content { border: 1px solid #a6c9e2; background: #fcfdfd url(images/ui-bg_inset-hard_100_fcfdfd_1x100.png) 50% bottom repeat-x; color: #222222; }
.ui-widget-content a { color: #222222; }
.ui-widget-header { border: 1px solid #4297d7; background: #5c9ccc url(images/ui-bg_gloss-wave_55_5c9ccc_500x100.png) 50% 50% repeat-x; color: #ffffff; font-weight: bold; }
.ui-widget-header a { color: #ffffff; }

/* Interaction states
----------------------------------*/
.ui-state-default, .ui-widget-content .ui-state-default, .ui-widget-header .ui-state-default { border: 1px solid #c5dbec; background: #dfeffc url(images/ui-bg_glass_85_dfeffc_1x400.png) 50% 50% repeat-x; font-weight: bold; color: #2e6e9e; }
.ui-state-default a, .ui-state-default a:link, .ui-state-default a:visited { color: #2e6e9e; text-decoration: none; }
.ui-state-hover, .ui-widget-content .ui-state-hover, .ui-widget-header .ui-state-hover, .ui-state-focus, .ui-widget-content .ui-state-focus, .ui-widget-header .ui-state-focus { border: 1px solid #79b7e7; background: #d0e5f5 url(images/ui-bg_glass_75_d0e5f5_1x400.png) 50% 50% repeat-x; font-weight: bold; color: #1d5987; }
.ui-state-hover a, .ui-state-hover a:hover { color: #1d5987; text-decoration: none; }
.ui-state-active, .ui-widget-content .ui-state-active, .ui-widget-header .ui-state-active { border: 1px solid #79b7e7; background: #f5f8f9 url(images/ui-bg_inset-hard_100_f5f8f9_1x100.png) 50% 50% repeat-x; font-weight: bold; color: #e17009; }
.ui-state-active a, .ui-state-active a:link, .ui-state-active a:visited { color: #e17009; text-decoration: none; }
.ui-widget :active { outline: none; }

/* Interaction Cues
----------------------------------*/
.ui-state-highlight, .ui-widget-content .ui-state-highlight, .ui-widget-header .ui-state-highlight  {border: 1px solid #fad42e; background: #fbec88 url(images/ui-bg_flat_55_fbec88_40x100.png) 50% 50% repeat-x; color: #363636; }
.ui-state-highlight a, .ui-widget-content .ui-state-highlight a,.ui-widget-header .ui-state-highlight a { color: #363636; }
.ui-state-error, .ui-widget-content .ui-state-error, .ui-widget-header .ui-state-error {border: 1px solid #cd0a0a; background: #fef1ec url(images/ui-bg_glass_95_fef1ec_1x400.png) 50% 50% repeat-x; color: #cd0a0a; }
.ui-state-error a, .ui-widget-content .ui-state-error a, .ui-widget-header .ui-state-error a { color: #cd0a0a; }
.ui-state-error-text, .ui-widget-content .ui-state-error-text, .ui-widget-header .ui-state-error-text { color: #cd0a0a; }
.ui-priority-primary, .ui-widget-content .ui-priority-primary, .ui-widget-header .ui-priority-primary { font-weight: bold; }
.ui-priority-secondary, .ui-widget-content .ui-priority-secondary,  .ui-widget-header .ui-priority-secondary { opacity: .7; filter:Alpha(Opacity=70); font-weight: normal; }
.ui-state-disabled, .ui-widget-content .ui-state-disabled, .ui-widget-header .ui-state-disabled { opacity: .35; filter:Alpha(Opacity=35); background-image: none; }

/* Icons
----------------------------------*/

/* states and images */
.ui-icon { width: 16px; height: 16px; background-image: url(images/ui-icons_469bdd_256x240.png); }
.ui-widget-content .ui-icon {background-image: url(images/ui-icons_469bdd_256x240.png); }
.ui-widget-header .ui-icon {background-image: url(images/ui-icons_d8e7f3_256x240.png); }
.ui-state-default .ui-icon { background-image: url(images/ui-icons_6da8d5_256x240.png); }
.ui-state-hover .ui-icon, .ui-state-focus .ui-icon {background-image: url(images/ui-icons_217bc0_256x240.png); }
.ui-state-active .ui-icon {background-image: url(images/ui-icons_f9bd01_256x240.png); }
.ui-state-highlight .ui-icon {background-image: url(images/ui-icons_2e83ff_256x240.png); }
.ui-state-error .ui-icon, .ui-state-error-text .ui-icon {background-image: url(images/ui-icons_cd0a0a_256x240.png); }

/* positioning */
.ui-icon-carat-1-n { background-position: 0 0; }
.ui-icon-carat-1-ne { background-position: -16px 0; }
.ui-icon-carat-1-e { background-position: -32px 0; }
.ui-icon-carat-1-se { background-position: -48px 0; }
.ui-icon-carat-1-s { background-position: -64px 0; }
.ui-icon-carat-1-sw { background-position: -80px 0; }
.ui-icon-carat-1-w { background-position: -96px 0; }
.ui-icon-carat-1-nw { background-position: -112px 0; }
.ui-icon-carat-2-n-s { background-position: -128px 0; }
.ui-icon-carat-2-e-w { background-position: -144px 0; }
.ui-icon-triangle-1-n { background-position: 0 -16px; }
.ui-icon-triangle-1-ne { background-position: -16px -16px; }
.ui-icon-triangle-1-e { background-position: -32px -16px; }
.ui-icon-triangle-1-se { background-position: -48px -16px; }
.ui-icon-triangle-1-s { background-position: -64px -16px; }
.ui-icon-triangle-1-sw { background-position: -80px -16px; }
.ui-icon-triangle-1-w { background-position: -96px -16px; }
.ui-icon-triangle-1-nw { background-position: -112px -16px; }
.ui-icon-triangle-2-n-s { background-position: -128px -16px; }
.ui-icon-triangle-2-e-w { background-position: -144px -16px; }
.ui-icon-arrow-1-n { background-position: 0 -32px; }
.ui-icon-arrow-1-ne { background-position: -16px -32px; }
.ui-icon-arrow-1-e { background-position: -32px -32px; }
.ui-icon-arrow-1-se { background-position: -48px -32px; }
.ui-icon-arrow-1-s { background-position: -64px -32px; }
.ui-icon-arrow-1-sw { background-position: -80px -32px; }
.ui-icon-arrow-1-w { background-position: -96px -32px; }
.ui-icon-arrow-1-nw { background-position: -112px -32px; }
.ui-icon-arrow-2-n-s { background-position: -128px -32px; }
.ui-icon-arrow-2-ne-sw { background-position: -144px -32px; }
.ui-icon-arrow-2-e-w { background-position: -160px -32px; }
.ui-icon-arrow-2-se-nw { background-position: -176px -32px; }
.ui-icon-arrowstop-1-n { background-position: -192px -32px; }
.ui-icon-arrowstop-1-e { background-position: -208px -32px; }
.ui-icon-arrowstop-1-s { background-position: -224px -32px; }
.ui-icon-arrowstop-1-w { background-position: -240px -32px; }
.ui-icon-arrowthick-1-n { background-position: 0 -48px; }
.ui-icon-arrowthick-1-ne { background-position: -16px -48px; }
.ui-icon-arrowthick-1-e { background-position: -32px -48px; }
.ui-icon-arrowthick-1-se { background-position: -48px -48px; }
.ui-icon-arrowthick-1-s { background-position: -64px -48px; }
.ui-icon-arrowthick-1-sw { background-position: -80px -48px; }
.ui-icon-arrowthick-1-w { background-position: -96px -48px; }
.ui-icon-arrowthick-1-nw { background-position: -112px -48px; }
.ui-icon-arrowthick-2-n-s { background-position: -128px -48px; }
.ui-icon-arrowthick-2-ne-sw { background-position: -144px -48px; }
.ui-icon-arrowthick-2-e-w { background-position: -160px -48px; }
.ui-icon-arrowthick-2-se-nw { background-position: -176px -48px; }
.ui-icon-arrowthickstop-1-n { background-position: -192px -48px; }
.ui-icon-arrowthickstop-1-e { background-position: -208px -48px; }
.ui-icon-arrowthickstop-1-s { background-position: -224px -48px; }
.ui-icon-arrowthickstop-1-w { background-position: -240px -48px; }
.ui-icon-arrowreturnthick-1-w { background-position: 0 -64px; }
.ui-icon-arrowreturnthick-1-n { background-position: -16px -64px; }
.ui-icon-arrowreturnthick-1-e { background-position: -32px -64px; }
.ui-icon-arrowreturnthick-1-s { background-position: -48px -64px; }
.ui-icon-arrowreturn-1-w { background-position: -64px -64px; }
.ui-icon-arrowreturn-1-n { background-position: -80px -64px; }
.ui-icon-arrowreturn-1-e { background-position: -96px -64px; }
.ui-icon-arrowreturn-1-s { background-position: -112px -64px; }
.ui-icon-arrowrefresh-1-w { background-position: -128px -64px; }
.ui-icon-arrowrefresh-1-n { background-position: -144px -64px; }
.ui-icon-arrowrefresh-1-e { background-position: -160px -64px; }
.ui-icon-arrowrefresh-1-s { background-position: -176px -64px; }
.ui-icon-arrow-4 { background-position: 0 -80px; }
.ui-icon-arrow-4-diag { background-position: -16px -80px; }
.ui-icon-extlink { background-position: -32px -80px; }
.ui-icon-newwin { background-position: -48px -80px; }
.ui-icon-refresh { background-position: -64px -80px; }
.ui-icon-shuffle { background-position: -80px -80px; }
.ui-icon-transfer-e-w { background-position: -96px -80px; }
.ui-icon-transferthick-e-w { background-position: -112px -80px; }
.ui-icon-folder-collapsed { background-position: 0 -96px; }
.ui-icon-folder-open { background-position: -16px -96px; }
.ui-icon-document { background-position: -32px -96px; }
.ui-icon-document-b { background-position: -48px -96px; }
.ui-icon-note { background-position: -64px -96px; }
.ui-icon-mail-closed { background-position: -80px -96px; }
.ui-icon-mail-open { background-position: -96px -96px; }
.ui-icon-suitcase { background-position: -112px -96px; }
.ui-icon-comment { background-position: -128px -96px; }
.ui-icon-person { background-position: -144px -96px; }
.ui-icon-print { background-position: -160px -96px; }
.ui-icon-trash { background-position: -176px -96px; }
.ui-icon-locked { background-position: -192px -96px; }
.ui-icon-unlocked { background-position: -208px -96px; }
.ui-icon-bookmark { background-position: -224px -96px; }
.ui-icon-tag { background-position: -240px -96px; }
.ui-icon-home { background-position: 0 -112px; }
.ui-icon-flag { background-position: -16px -112px; }
.ui-icon-calendar { background-position: -32px -112px; }
.ui-icon-cart { background-position: -48px -112px; }
.ui-icon-pencil { background-position: -64px -112px; }
.ui-icon-clock { background-position: -80px -112px; }
.ui-icon-disk { background-position: -96px -112px; }
.ui-icon-calculator { background-position: -112px -112px; }
.ui-icon-zoomin { background-position: -128px -112px; }
.ui-icon-zoomout { background-position: -144px -112px; }
.ui-icon-search { background-position: -160px -112px; }
.ui-icon-wrench { background-position: -176px -112px; }
.ui-icon-gear { background-position: -192px -112px; }
.ui-icon-heart { background-position: -208px -112px; }
.ui-icon-star { background-position: -224px -112px; }
.ui-icon-link { background-position: -240px -112px; }
.ui-icon-cancel { background-position: 0 -128px; }
.ui-icon-plus { background-position: -16px -128px; }
.ui-icon-plusthick { background-position: -32px -128px; }
.ui-icon-minus { background-position: -48px -128px; }
.ui-icon-minusthick { background-position: -64px -128px; }
.ui-icon-close { background-position: -80px -128px; }
.ui-icon-closethick { background-position: -96px -128px; }
.ui-icon-key { background-position: -112px -128px; }
.ui-icon-lightbulb { background-position: -128px -128px; }
.ui-icon-scissors { background-position: -144px -128px; }
.ui-icon-clipboard { background-position: -160px -128px; }
.ui-icon-copy { background-position: -176px -128px; }
.ui-icon-contact { background-position: -192px -128px; }
.ui-icon-image { background-position: -208px -128px; }
.ui-icon-video { background-position: -224px -128px; }
.ui-icon-script { background-position: -240px -128px; }
.ui-icon-alert { background-position: 0 -144px; }
.ui-icon-info { background-position: -16px -144px; }
.ui-icon-notice { background-position: -32px -144px; }
.ui-icon-help { background-position: -48px -144px; }
.ui-icon-check { background-position: -64px -144px; }
.ui-icon-bullet { background-position: -80px -144px; }
.ui-icon-radio-off { background-position: -96px -144px; }
.ui-icon-radio-on { background-position: -112px -144px; }
.ui-icon-pin-w { background-position: -128px -144px; }
.ui-icon-pin-s { background-position: -144px -144px; }
.ui-icon-play { background-position: 0 -160px; }
.ui-icon-pause { background-position: -16px -160px; }
.ui-icon-seek-next { background-position: -32px -160px; }
.ui-icon-seek-prev { background-position: -48px -160px; }
.ui-icon-seek-end { background-position: -64px -160px; }
.ui-icon-seek-start { background-position: -80px -160px; }
/* ui-icon-seek-first is deprecated, use ui-icon-seek-start instead */
.ui-icon-seek-first { background-position: -80px -160px; }
.ui-icon-stop { background-position: -96px -160px; }
.ui-icon-eject { background-position: -112px -160px; }
.ui-icon-volume-off { background-position: -128px -160px; }
.ui-icon-volume-on { background-position: -144px -160px; }
.ui-icon-power { background-position: 0 -176px; }
.ui-icon-signal-diag { background-position: -16px -176px; }
.ui-icon-signal { background-position: -32px -176px; }
.ui-icon-battery-0 { background-position: -48px -176px; }
.ui-icon-battery-1 { background-position: -64px -176px; }
.ui-icon-battery-2 { background-position: -80px -176px; }
.ui-icon-battery-3 { background-position: -96px -176px; }
.ui-icon-circle-plus { background-position: 0 -192px; }
.ui-icon-circle-minus { background-position: -16px -192px; }
.ui-icon-circle-close { background-position: -32px -192px; }
.ui-icon-circle-triangle-e { background-position: -48px -192px; }
.ui-icon-circle-triangle-s { background-position: -64px -192px; }
.ui-icon-circle-triangle-w { background-position: -80px -192px; }
.ui-icon-circle-triangle-n { background-position: -96px -192px; }
.ui-icon-circle-arrow-e { background-position: -112px -192px; }
.ui-icon-circle-arrow-s { background-position: -128px -192px; }
.ui-icon-circle-arrow-w { background-position: -144px -192px; }
.ui-icon-circle-arrow-n { background-position: -160px -192px; }
.ui-icon-circle-zoomin { background-position: -176px -192px; }
.ui-icon-circle-zoomout { background-position: -192px -192px; }
.ui-icon-circle-check { background-position: -208px -192px; }
.ui-icon-circlesmall-plus { background-position: 0 -208px; }
.ui-icon-circlesmall-minus { background-position: -16px -208px; }
.ui-icon-circlesmall-close { background-position: -32px -208px; }
.ui-icon-squaresmall-plus { background-position: -48px -208px; }
.ui-icon-squaresmall-minus { background-position: -64px -208px; }
.ui-icon-squaresmall-close { background-position: -80px -208px; }
.ui-icon-grip-dotted-vertical { background-position: 0 -224px; }
.ui-icon-grip-dotted-horizontal { background-position: -16px -224px; }
.ui-icon-grip-solid-vertical { background-position: -32px -224px; }
.ui-icon-grip-solid-horizontal { background-position: -48px -224px; }
.ui-icon-gripsmall-diagonal-se { background-position: -64px -224px; }
.ui-icon-grip-diagonal-se { background-position: -80px -224px; }


/* Misc visuals
----------------------------------*/

/* Corner radius */
.ui-corner-all, .ui-corner-top, .ui-corner-left, .ui-corner-tl { -moz-border-radius-topleft: 5px; -webkit-border-top-left-radius: 5px; -khtml-border-top-left-radius: 5px; border-top-left-radius: 5px; }
.ui-corner-all, .ui-corner-top, .ui-corner-right, .ui-corner-tr { -moz-border-radius-topright: 5px; -webkit-border-top-right-radius: 5px; -khtml-border-top-right-radius: 5px; border-top-right-radius: 5px; }
.ui-corner-all, .ui-corner-bottom, .ui-corner-left, .ui-corner-bl { -moz-border-radius-bottomleft: 5px; -webkit-border-bottom-left-radius: 5px; -khtml-border-bottom-left-radius: 5px; border-bottom-left-radius: 5px; }
.ui-corner-all, .ui-corner-bottom, .ui-corner-right, .ui-corner-br { -moz-border-radius-bottomright: 5px; -webkit-border-bottom-right-radius: 5px; -khtml-border-bottom-right-radius: 5px; border-bottom-right-radius: 5px; }

/* Overlays */
.ui-widget-overlay { background: #aaaaaa url(images/ui-bg_flat_0_aaaaaa_40x100.png) 50% 50% repeat-x; opacity: .30;filter:Alpha(Opacity=30); }
.ui-widget-shadow { margin: -8px 0 0 -8px; padding: 8px; background: #aaaaaa url(images/ui-bg_flat_0_aaaaaa_40x100.png) 50% 50% repeat-x; opacity: .30;filter:Alpha(Opacity=30); -moz-border-radius: 8px; -khtml-border-radius: 8px; -webkit-border-radius: 8px; border-radius: 8px; }/*!
 * jQuery UI Resizable 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Resizable#theming
 */
.ui-resizable { position: relative;}
.ui-resizable-handle { position: absolute;font-size: 0.1px; display: block; }
.ui-resizable-disabled .ui-resizable-handle, .ui-resizable-autohide .ui-resizable-handle { display: none; }
.ui-resizable-n { cursor: n-resize; height: 7px; width: 100%; top: -5px; left: 0; }
.ui-resizable-s { cursor: s-resize; height: 7px; width: 100%; bottom: -5px; left: 0; }
.ui-resizable-e { cursor: e-resize; width: 7px; right: -5px; top: 0; height: 100%; }
.ui-resizable-w { cursor: w-resize; width: 7px; left: -5px; top: 0; height: 100%; }
.ui-resizable-se { cursor: se-resize; width: 12px; height: 12px; right: 1px; bottom: 1px; }
.ui-resizable-sw { cursor: sw-resize; width: 9px; height: 9px; left: -5px; bottom: -5px; }
.ui-resizable-nw { cursor: nw-resize; width: 9px; height: 9px; left: -5px; top: -5px; }
.ui-resizable-ne { cursor: ne-resize; width: 9px; height: 9px; right: -5px; top: -5px;}/*!
 * jQuery UI Selectable 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Selectable#theming
 */
.ui-selectable-helper { position: absolute; z-index: 100; border:1px dotted black; }
/*!
 * jQuery UI Accordion 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Accordion#theming
 */
/* IE/Win - Fix animation bug - #4615 */
.ui-accordion { width: 100%; }
.ui-accordion .ui-accordion-header { cursor: pointer; position: relative; margin-top: 1px; zoom: 1; }
.ui-accordion .ui-accordion-li-fix { display: inline; }
.ui-accordion .ui-accordion-header-active { border-bottom: 0 !important; }
.ui-accordion .ui-accordion-header a { display: block; font-size: 1em; padding: .5em .5em .5em .7em; }
.ui-accordion-icons .ui-accordion-header a { padding-left: 2.2em; }
.ui-accordion .ui-accordion-header .ui-icon { position: absolute; left: .5em; top: 50%; margin-top: -8px; }
.ui-accordion .ui-accordion-content { padding: 1em 2.2em; border-top: 0; margin-top: -2px; position: relative; top: 1px; margin-bottom: 2px; overflow: auto; display: none; zoom: 1; }
.ui-accordion .ui-accordion-content-active { display: block; }
/*!
 * jQuery UI Autocomplete 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Autocomplete#theming
 */
.ui-autocomplete { position: absolute; cursor: default; }	

/* workarounds */
* html .ui-autocomplete { width:1px; } /* without this, the menu expands to 100% in IE6 */

/*
 * jQuery UI Menu 1.8.23
 *
 * Copyright 2010, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Menu#theming
 */
.ui-menu {
	list-style:none;
	padding: 2px;
	margin: 0;
	display:block;
	float: left;
}
.ui-menu .ui-menu {
	margin-top: -3px;
}
.ui-menu .ui-menu-item {
	margin:0;
	padding: 0;
	zoom: 1;
	float: left;
	clear: left;
	width: 100%;
}
.ui-menu .ui-menu-item a {
	text-decoration:none;
	display:block;
	padding:.2em .4em;
	line-height:1.5;
	zoom:1;
}
.ui-menu .ui-menu-item a.ui-state-hover,
.ui-menu .ui-menu-item a.ui-state-active {
	font-weight: normal;
	margin: -1px;
}
/*!
 * jQuery UI Button 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Button#theming
 */
.ui-button { display: inline-block; position: relative; padding: 0; margin-right: .1em; text-decoration: none !important; cursor: pointer; text-align: center; zoom: 1; overflow: visible; } /* the overflow property removes extra width in IE */
.ui-button-icon-only { width: 2.2em; } /* to make room for the icon, a width needs to be set here */
button.ui-button-icon-only { width: 2.4em; } /* button elements seem to need a little more width */
.ui-button-icons-only { width: 3.4em; } 
button.ui-button-icons-only { width: 3.7em; } 

/*button text element */
.ui-button .ui-button-text { display: block; line-height: 1.4;  }
.ui-button-text-only .ui-button-text { padding: .4em 1em; }
.ui-button-icon-only .ui-button-text, .ui-button-icons-only .ui-button-text { padding: .4em; text-indent: -9999999px; }
.ui-button-text-icon-primary .ui-button-text, .ui-button-text-icons .ui-button-text { padding: .4em 1em .4em 2.1em; }
.ui-button-text-icon-secondary .ui-button-text, .ui-button-text-icons .ui-button-text { padding: .4em 2.1em .4em 1em; }
.ui-button-text-icons .ui-button-text { padding-left: 2.1em; padding-right: 2.1em; }
/* no icon support for input elements, provide padding by default */
input.ui-button { padding: .4em 1em; }

/*button icon element(s) */
.ui-button-icon-only .ui-icon, .ui-button-text-icon-primary .ui-icon, .ui-button-text-icon-secondary .ui-icon, .ui-button-text-icons .ui-icon, .ui-button-icons-only .ui-icon { position: absolute; top: 50%; margin-top: -8px; }
.ui-button-icon-only .ui-icon { left: 50%; margin-left: -8px; }
.ui-button-text-icon-primary .ui-button-icon-primary, .ui-button-text-icons .ui-button-icon-primary, .ui-button-icons-only .ui-button-icon-primary { left: .5em; }
.ui-button-text-icon-secondary .ui-button-icon-secondary, .ui-button-text-icons .ui-button-icon-secondary, .ui-button-icons-only .ui-button-icon-secondary { right: .5em; }
.ui-button-text-icons .ui-button-icon-secondary, .ui-button-icons-only .ui-button-icon-secondary { right: .5em; }

/*button sets*/
.ui-buttonset { margin-right: 7px; }
.ui-buttonset .ui-button { margin-left: 0; margin-right: -.3em; }

/* workarounds */
button.ui-button::-moz-focus-inner { border: 0; padding: 0; } /* reset extra padding in Firefox */
/*!
 * jQuery UI Dialog 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Dialog#theming
 */
.ui-dialog { position: absolute; padding: .2em; width: 300px; overflow: hidden; }
.ui-dialog .ui-dialog-titlebar { padding: .4em 1em; position: relative;  }
.ui-dialog .ui-dialog-title { float: left; margin: .1em 16px .1em 0; } 
.ui-dialog .ui-dialog-titlebar-close { position: absolute; right: .3em; top: 50%; width: 19px; margin: -10px 0 0 0; padding: 1px; height: 18px; }
.ui-dialog .ui-dialog-titlebar-close span { display: block; margin: 1px; }
.ui-dialog .ui-dialog-titlebar-close:hover, .ui-dialog .ui-dialog-titlebar-close:focus { padding: 0; }
.ui-dialog .ui-dialog-content { position: relative; border: 0; padding: .5em 1em; background: none; overflow: auto; zoom: 1; }
.ui-dialog .ui-dialog-buttonpane { text-align: left; border-width: 1px 0 0 0; background-image: none; margin: .5em 0 0 0; padding: .3em 1em .5em .4em; }
.ui-dialog .ui-dialog-buttonpane .ui-dialog-buttonset { float: right; }
.ui-dialog .ui-dialog-buttonpane button { margin: .5em .4em .5em 0; cursor: pointer; }
.ui-dialog .ui-resizable-se { width: 14px; height: 14px; right: 3px; bottom: 3px; }
.ui-draggable .ui-dialog-titlebar { cursor: move; }
/*!
 * jQuery UI Slider 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Slider#theming
 */
.ui-slider { position: relative; text-align: left; }
.ui-slider .ui-slider-handle { position: absolute; z-index: 2; width: 1.2em; height: 1.2em; cursor: default; }
.ui-slider .ui-slider-range { position: absolute; z-index: 1; font-size: .7em; display: block; border: 0; background-position: 0 0; }

.ui-slider-horizontal { height: .8em; }
.ui-slider-horizontal .ui-slider-handle { top: -.3em; margin-left: -.6em; }
.ui-slider-horizontal .ui-slider-range { top: 0; height: 100%; }
.ui-slider-horizontal .ui-slider-range-min { left: 0; }
.ui-slider-horizontal .ui-slider-range-max { right: 0; }

.ui-slider-vertical { width: .8em; height: 100px; }
.ui-slider-vertical .ui-slider-handle { left: -.3em; margin-left: 0; margin-bottom: -.6em; }
.ui-slider-vertical .ui-slider-range { left: 0; width: 100%; }
.ui-slider-vertical .ui-slider-range-min { bottom: 0; }
.ui-slider-vertical .ui-slider-range-max { top: 0; }/*!
 * jQuery UI Tabs 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Tabs#theming
 */
.ui-tabs { position: relative; padding: .2em; zoom: 1; } /* position: relative prevents IE scroll bug (element with position: relative inside container with overflow: auto appear as "fixed") */
.ui-tabs .ui-tabs-nav { margin: 0; padding: .2em .2em 0; }
.ui-tabs .ui-tabs-nav li { list-style: none; float: left; position: relative; top: 1px; margin: 0 .2em 1px 0; border-bottom: 0 !important; padding: 0; white-space: nowrap; }
.ui-tabs .ui-tabs-nav li a { float: left; padding: .5em 1em; text-decoration: none; }
.ui-tabs .ui-tabs-nav li.ui-tabs-selected { margin-bottom: 0; padding-bottom: 1px; }
.ui-tabs .ui-tabs-nav li.ui-tabs-selected a, .ui-tabs .ui-tabs-nav li.ui-state-disabled a, .ui-tabs .ui-tabs-nav li.ui-state-processing a { cursor: text; }
.ui-tabs .ui-tabs-nav li a, .ui-tabs.ui-tabs-collapsible .ui-tabs-nav li.ui-tabs-selected a { cursor: pointer; } /* first selector in group seems obsolete, but required to overcome bug in Opera applying cursor: text overall if defined elsewhere... */
.ui-tabs .ui-tabs-panel { display: block; border-width: 0; padding: 1em 1.4em; background: none; }
.ui-tabs .ui-tabs-hide { display: none !important; }
/*!
 * jQuery UI Datepicker 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Datepicker#theming
 */
.ui-datepicker { width: 17em; padding: .2em .2em 0; display: none; }
.ui-datepicker .ui-datepicker-header { position:relative; padding:.2em 0; }
.ui-datepicker .ui-datepicker-prev, .ui-datepicker .ui-datepicker-next { position:absolute; top: 2px; width: 1.8em; height: 1.8em; }
.ui-datepicker .ui-datepicker-prev-hover, .ui-datepicker .ui-datepicker-next-hover { top: 1px; }
.ui-datepicker .ui-datepicker-prev { left:2px; }
.ui-datepicker .ui-datepicker-next { right:2px; }
.ui-datepicker .ui-datepicker-prev-hover { left:1px; }
.ui-datepicker .ui-datepicker-next-hover { right:1px; }
.ui-datepicker .ui-datepicker-prev span, .ui-datepicker .ui-datepicker-next span { display: block; position: absolute; left: 50%; margin-left: -8px; top: 50%; margin-top: -8px;  }
.ui-datepicker .ui-datepicker-title { margin: 0 2.3em; line-height: 1.8em; text-align: center; }
.ui-datepicker .ui-datepicker-title select { font-size:1em; margin:1px 0; }
.ui-datepicker select.ui-datepicker-month-year {width: 100%;}
.ui-datepicker select.ui-datepicker-month, 
.ui-datepicker select.ui-datepicker-year { width: 49%;}
.ui-datepicker table {width: 100%; font-size: .9em; border-collapse: collapse; margin:0 0 .4em; }
.ui-datepicker th { padding: .7em .3em; text-align: center; font-weight: bold; border: 0;  }
.ui-datepicker td { border: 0; padding: 1px; }
.ui-datepicker td span, .ui-datepicker td a { display: block; padding: .2em; text-align: right; text-decoration: none; }
.ui-datepicker .ui-datepicker-buttonpane { background-image: none; margin: .7em 0 0 0; padding:0 .2em; border-left: 0; border-right: 0; border-bottom: 0; }
.ui-datepicker .ui-datepicker-buttonpane button { float: right; margin: .5em .2em .4em; cursor: pointer; padding: .2em .6em .3em .6em; width:auto; overflow:visible; }
.ui-datepicker .ui-datepicker-buttonpane button.ui-datepicker-current { float:left; }

/* with multiple calendars */
.ui-datepicker.ui-datepicker-multi { width:auto; }
.ui-datepicker-multi .ui-datepicker-group { float:left; }
.ui-datepicker-multi .ui-datepicker-group table { width:95%; margin:0 auto .4em; }
.ui-datepicker-multi-2 .ui-datepicker-group { width:50%; }
.ui-datepicker-multi-3 .ui-datepicker-group { width:33.3%; }
.ui-datepicker-multi-4 .ui-datepicker-group { width:25%; }
.ui-datepicker-multi .ui-datepicker-group-last .ui-datepicker-header { border-left-width:0; }
.ui-datepicker-multi .ui-datepicker-group-middle .ui-datepicker-header { border-left-width:0; }
.ui-datepicker-multi .ui-datepicker-buttonpane { clear:left; }
.ui-datepicker-row-break { clear:both; width:100%; font-size:0em; }

/* RTL support */
.ui-datepicker-rtl { direction: rtl; }
.ui-datepicker-rtl .ui-datepicker-prev { right: 2px; left: auto; }
.ui-datepicker-rtl .ui-datepicker-next { left: 2px; right: auto; }
.ui-datepicker-rtl .ui-datepicker-prev:hover { right: 1px; left: auto; }
.ui-datepicker-rtl .ui-datepicker-next:hover { left: 1px; right: auto; }
.ui-datepicker-rtl .ui-datepicker-buttonpane { clear:right; }
.ui-datepicker-rtl .ui-datepicker-buttonpane button { float: left; }
.ui-datepicker-rtl .ui-datepicker-buttonpane button.ui-datepicker-current { float:right; }
.ui-datepicker-rtl .ui-datepicker-group { float:right; }
.ui-datepicker-rtl .ui-datepicker-group-last .ui-datepicker-header { border-right-width:0; border-left-width:1px; }
.ui-datepicker-rtl .ui-datepicker-group-middle .ui-datepicker-header { border-right-width:0; border-left-width:1px; }

/* IE6 IFRAME FIX (taken from datepicker 1.5.3 */
.ui-datepicker-cover {
    position: absolute; /*must have*/
    z-index: -1; /*must have*/
    filter: mask(); /*must have*/
    top: -4px; /*must have*/
    left: -4px; /*must have*/
    width: 200px; /*must have*/
    height: 200px; /*must have*/
}/*!
 * jQuery UI Progressbar 1.8.23
 *
 * Copyright 2012, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Progressbar#theming
 */
.ui-progressbar { height:2em; text-align: left; overflow: hidden; }
.ui-progressbar .ui-progressbar-value {margin: -1px; height:100%; }


/*Grid*/
.ui-jqgrid {position: relative;}
.ui-jqgrid .ui-jqgrid-view {position: relative;left:0; top: 0; padding: 0; font-size:11px;}
/* caption*/
.ui-jqgrid .ui-jqgrid-titlebar {padding: .3em .2em .2em .3em; position: relative; border-left: 0 none;border-right: 0 none; border-top: 0 none;}
.ui-jqgrid .ui-jqgrid-title { float: left; margin: .1em 0 .2em; }
.ui-jqgrid .ui-jqgrid-titlebar-close { position: absolute;top: 50%; width: 19px; margin: -10px 0 0 0; padding: 1px; height:18px;}.ui-jqgrid .ui-jqgrid-titlebar-close span { display: block; margin: 1px; }
.ui-jqgrid .ui-jqgrid-titlebar-close:hover { padding: 0; }
/* header*/
.ui-jqgrid .ui-jqgrid-hdiv {position: relative; margin: 0;padding: 0; overflow-x: hidden; border-left: 0 none !important; border-top : 0 none !important; border-right : 0 none !important;}
.ui-jqgrid .ui-jqgrid-hbox {float: left; padding-right: 20px;}
.ui-jqgrid .ui-jqgrid-htable {table-layout:fixed;margin:0;}
.ui-jqgrid .ui-jqgrid-htable th {height:22px;padding: 0 2px 0 2px;}
.ui-jqgrid .ui-jqgrid-htable th div {overflow: hidden; position:relative; height:17px;}
.ui-th-column, .ui-jqgrid .ui-jqgrid-htable th.ui-th-column {overflow: hidden;white-space: nowrap;text-align:center;border-top : 0 none;border-bottom : 0 none;}
.ui-th-ltr, .ui-jqgrid .ui-jqgrid-htable th.ui-th-ltr {border-left : 0 none;}
.ui-th-rtl, .ui-jqgrid .ui-jqgrid-htable th.ui-th-rtl {border-right : 0 none;}
.ui-first-th-ltr {border-right: 1px solid; }
.ui-first-th-rtl {border-left: 1px solid; }
.ui-jqgrid .ui-th-div-ie {white-space: nowrap; zoom :1; height:17px;}
.ui-jqgrid .ui-jqgrid-resize {height:20px !important;position: relative; cursor :e-resize;display: inline;overflow: hidden;}
.ui-jqgrid .ui-grid-ico-sort {overflow:hidden;position:absolute;display:inline; cursor: pointer !important;}
.ui-jqgrid .ui-icon-asc {margin-top:-3px; height:12px;}
.ui-jqgrid .ui-icon-desc {margin-top:3px;height:12px;}
.ui-jqgrid .ui-i-asc {margin-top:0;height:16px;}
.ui-jqgrid .ui-i-desc {margin-top:0;margin-left:13px;height:16px;}
.ui-jqgrid .ui-jqgrid-sortable {cursor:pointer;}
.ui-jqgrid tr.ui-search-toolbar th { border-top-width: 1px !important; border-top-color: inherit !important; border-top-style: ridge !important }
tr.ui-search-toolbar input {margin: 1px 0 0 0}
tr.ui-search-toolbar select {margin: 1px 0 0 0}
/* body */ 
.ui-jqgrid .ui-jqgrid-bdiv {position: relative; margin: 0; padding:0; overflow: auto; text-align:left;}
.ui-jqgrid .ui-jqgrid-btable {table-layout:fixed; margin:0; outline-style: none; }
.ui-jqgrid tr.jqgrow { outline-style: none; }
.ui-jqgrid tr.jqgroup { outline-style: none; }
.ui-jqgrid tr.jqgrow td {font-weight: normal; overflow: hidden; white-space: pre; height: 22px;padding: 0 2px 0 2px;border-bottom-width: 1px; border-bottom-color: inherit; border-bottom-style: solid;}
.ui-jqgrid tr.jqgfirstrow td {padding: 0 2px 0 2px;border-right-width: 1px; border-right-style: solid;}
.ui-jqgrid tr.jqgroup td {font-weight: normal; overflow: hidden; white-space: pre; height: 22px;padding: 0 2px 0 2px;border-bottom-width: 1px; border-bottom-color: inherit; border-bottom-style: solid;}
.ui-jqgrid tr.jqfoot td {font-weight: bold; overflow: hidden; white-space: pre; height: 22px;padding: 0 2px 0 2px;border-bottom-width: 1px; border-bottom-color: inherit; border-bottom-style: solid;}
.ui-jqgrid tr.ui-row-ltr td {text-align:left;border-right-width: 1px; border-right-color: inherit; border-right-style: solid;}
.ui-jqgrid tr.ui-row-rtl td {text-align:right;border-left-width: 1px; border-left-color: inherit; border-left-style: solid;}
.ui-jqgrid td.jqgrid-rownum { padding: 0 2px 0 2px; margin: 0; border: 0 none;}
.ui-jqgrid .ui-jqgrid-resize-mark { width:2px; left:0; background-color:#777; cursor: e-resize; cursor: col-resize; position:absolute; top:0; height:100px; overflow:hidden; display:none; border:0 none; z-index: 99999;}
/* footer */
.ui-jqgrid .ui-jqgrid-sdiv {position: relative; margin: 0;padding: 0; overflow: hidden; border-left: 0 none !important; border-top : 0 none !important; border-right : 0 none !important;}
.ui-jqgrid .ui-jqgrid-ftable {table-layout:fixed; margin-bottom:0;}
.ui-jqgrid tr.footrow td {font-weight: bold; overflow: hidden; white-space:nowrap; height: 21px;padding: 0 2px 0 2px;border-top-width: 1px; border-top-color: inherit; border-top-style: solid;}
.ui-jqgrid tr.footrow-ltr td {text-align:left;border-right-width: 1px; border-right-color: inherit; border-right-style: solid;}
.ui-jqgrid tr.footrow-rtl td {text-align:right;border-left-width: 1px; border-left-color: inherit; border-left-style: solid;}
/* Pager*/
.ui-jqgrid .ui-jqgrid-pager { border-left: 0 none !important;border-right: 0 none !important; border-bottom: 0 none !important; margin: 0 !important; padding: 0 !important; position: relative; height: 25px;white-space: nowrap;overflow: hidden;font-size:11px;}
.ui-jqgrid .ui-pager-control {position: relative;}
.ui-jqgrid .ui-pg-table {position: relative; padding-bottom:2px; width:auto; margin: 0;}
.ui-jqgrid .ui-pg-table td {font-weight:normal; vertical-align:middle; padding:1px;}
.ui-jqgrid .ui-pg-button  { height:19px !important;}
.ui-jqgrid .ui-pg-button span { display: block; margin: 1px; float:left;}
.ui-jqgrid .ui-pg-button:hover { padding: 0; }
.ui-jqgrid .ui-state-disabled:hover {padding:1px;}
.ui-jqgrid .ui-pg-input { height:13px;font-size:.8em; margin: 0;}
.ui-jqgrid .ui-pg-selbox {font-size:.8em; line-height:18px; display:block; height:18px; margin: 0;}
.ui-jqgrid .ui-separator {height: 18px; border-left: 1px solid #ccc ; border-right: 1px solid #ccc ; margin: 1px; float: right;}
.ui-jqgrid .ui-paging-info {font-weight: normal;height:19px; margin-top:3px;margin-right:4px;}
.ui-jqgrid .ui-jqgrid-pager .ui-pg-div {padding:1px 0;float:left;position:relative;}
.ui-jqgrid .ui-jqgrid-pager .ui-pg-button { cursor:pointer; }
.ui-jqgrid .ui-jqgrid-pager .ui-pg-div  span.ui-icon {float:left;margin:0 2px;}
.ui-jqgrid td input, .ui-jqgrid td select .ui-jqgrid td textarea { margin: 0;}
.ui-jqgrid td textarea {width:auto;height:auto;}
.ui-jqgrid .ui-jqgrid-toppager {border-left: 0 none !important;border-right: 0 none !important; border-top: 0 none !important; margin: 0 !important; padding: 0 !important; position: relative; height: 25px !important;white-space: nowrap;overflow: hidden;}
.ui-jqgrid .ui-jqgrid-toppager .ui-pg-div {padding:1px 0;float:left;position:relative;}
.ui-jqgrid .ui-jqgrid-toppager .ui-pg-button { cursor:pointer; }
.ui-jqgrid .ui-jqgrid-toppager .ui-pg-div  span.ui-icon {float:left;margin:0 2px;}
/*subgrid*/
.ui-jqgrid .ui-jqgrid-btable .ui-sgcollapsed span {display: block;}
.ui-jqgrid .ui-subgrid {margin:0;padding:0; width:100%;}
.ui-jqgrid .ui-subgrid table {table-layout: fixed;}
.ui-jqgrid .ui-subgrid tr.ui-subtblcell td {height:18px;border-right-width: 1px; border-right-color: inherit; border-right-style: solid;border-bottom-width: 1px; border-bottom-color: inherit; border-bottom-style: solid;}
.ui-jqgrid .ui-subgrid td.subgrid-data {border-top:  0 none !important;}
.ui-jqgrid .ui-subgrid td.subgrid-cell {border-width: 0 0 1px 0;}
.ui-jqgrid .ui-th-subgrid {height:20px;}
/* loading */
.ui-jqgrid .loading {position: absolute; top: 45%;left: 45%;width: auto;z-index:101;padding: 6px; margin: 5px;text-align: center;font-weight: bold;display: none;border-width: 2px !important; font-size:11px;}
.ui-jqgrid .jqgrid-overlay {display:none;z-index:100;}
* html .jqgrid-overlay {width: expression(this.parentNode.offsetWidth+'px');height: expression(this.parentNode.offsetHeight+'px');}
* .jqgrid-overlay iframe {position:absolute;top:0;left:0;z-index:-1;width: expression(this.parentNode.offsetWidth+'px');height: expression(this.parentNode.offsetHeight+'px');}
/* end loading div */
/* toolbar */
.ui-jqgrid .ui-userdata {border-left: 0 none;    border-right: 0 none;	height : 21px;overflow: hidden;	}
/*Modal Window */
.ui-jqdialog { display: none; width: 300px; position: absolute; padding: .2em; font-size:11px; overflow:visible;}
.ui-jqdialog .ui-jqdialog-titlebar { padding: .3em .2em; position: relative;  }
.ui-jqdialog .ui-jqdialog-title { margin: .1em 0 .2em; } 
.ui-jqdialog .ui-jqdialog-titlebar-close { position: absolute;  top: 50%; width: 19px; margin: -10px 0 0 0; padding: 1px; height: 18px; }

.ui-jqdialog .ui-jqdialog-titlebar-close span { display: block; margin: 1px; }
.ui-jqdialog .ui-jqdialog-titlebar-close:hover, .ui-jqdialog .ui-jqdialog-titlebar-close:focus { padding: 0; }
.ui-jqdialog-content, .ui-jqdialog .ui-jqdialog-content { border: 0; padding: .3em .2em; background: none; height:auto;}
.ui-jqdialog .ui-jqconfirm {padding: .4em 1em; border-width:3px;position:absolute;bottom:10px;right:10px;overflow:visible;display:none;height:80px;width:220px;text-align:center;}
.ui-jqdialog>.ui-resizable-se { bottom: -3px; right: -3px}
/* end Modal window*/
/* Form edit */
.ui-jqdialog-content .FormGrid {margin: 0;}
.ui-jqdialog-content .EditTable { width: 100%; margin-bottom:0;}
.ui-jqdialog-content .DelTable { width: 100%; margin-bottom:0;}
.EditTable td input, .EditTable td select, .EditTable td textarea {margin: 0;}
.EditTable td textarea { width:auto; height:auto;}
.ui-jqdialog-content td.EditButton {text-align: right;border-top: 0 none;border-left: 0 none;border-right: 0 none; padding-bottom:5px; padding-top:5px;}
.ui-jqdialog-content td.navButton {text-align: center; border-left: 0 none;border-top: 0 none;border-right: 0 none; padding-bottom:5px; padding-top:5px;}
.ui-jqdialog-content input.FormElement {padding:.3em}
.ui-jqdialog-content select.FormElement {padding:.3em}
.ui-jqdialog-content .data-line {padding-top:.1em;border: 0 none;}

.ui-jqdialog-content .CaptionTD {vertical-align: middle;border: 0 none; padding: 2px;white-space: nowrap;}
.ui-jqdialog-content .DataTD {padding: 2px; border: 0 none; vertical-align: top;}
.ui-jqdialog-content .form-view-data {white-space:pre}
.fm-button { display: inline-block; margin:0 4px 0 0; padding: .4em .5em; text-decoration:none !important; cursor:pointer; position: relative; text-align: center; zoom: 1; }
.fm-button-icon-left { padding-left: 1.9em; }
.fm-button-icon-right { padding-right: 1.9em; }
.fm-button-icon-left .ui-icon { right: auto; left: .2em; margin-left: 0; position: absolute; top: 50%; margin-top: -8px; }
.fm-button-icon-right .ui-icon { left: auto; right: .2em; margin-left: 0; position: absolute; top: 50%; margin-top: -8px;}
#nData, #pData { float: left; margin:3px;padding: 0; width: 15px; }
/* End Eorm edit */
/*.ui-jqgrid .edit-cell {}*/
.ui-jqgrid .selected-row, div.ui-jqgrid .selected-row td {font-style : normal;border-left: 0 none;}
/* inline edit actions button*/
.ui-inline-del.ui-state-hover span, .ui-inline-edit.ui-state-hover span,
.ui-inline-save.ui-state-hover span, .ui-inline-cancel.ui-state-hover span {
    margin: -1px;
}
/* Tree Grid */
.ui-jqgrid .tree-wrap {float: left; position: relative;height: 18px;white-space: nowrap;overflow: hidden;}
.ui-jqgrid .tree-minus {position: absolute; height: 18px; width: 18px; overflow: hidden;}
.ui-jqgrid .tree-plus {position: absolute;	height: 18px; width: 18px;	overflow: hidden;}
.ui-jqgrid .tree-leaf {position: absolute;	height: 18px; width: 18px;overflow: hidden;}
.ui-jqgrid .treeclick {cursor: pointer;}
/* moda dialog */
* iframe.jqm {position:absolute;top:0;left:0;z-index:-1;width: expression(this.parentNode.offsetWidth+'px');height: expression(this.parentNode.offsetHeight+'px');}
.ui-jqgrid-dnd tr td {border-right-width: 1px; border-right-color: inherit; border-right-style: solid; height:20px}
/* RTL Support */
.ui-jqgrid .ui-jqgrid-title-rtl {float:right;margin: .1em 0 .2em; }
.ui-jqgrid .ui-jqgrid-hbox-rtl {float: right; padding-left: 20px;}
.ui-jqgrid .ui-jqgrid-resize-ltr {float: right;margin: -2px -2px -2px 0;}
.ui-jqgrid .ui-jqgrid-resize-rtl {float: left;margin: -2px 0 -1px -3px;}
.ui-jqgrid .ui-sort-rtl {left:0;}
.ui-jqgrid .tree-wrap-ltr {float: left;}
.ui-jqgrid .tree-wrap-rtl {float: right;}
.ui-jqgrid .ui-ellipsis {text-overflow:ellipsis;}

/* Toolbar Search Menu */
.ui-search-menu { position: absolute; padding: 2px 5px;}
.ui-jqgrid .ui-search-table { padding: 0px 0px; border: 0px none; height:20px; width:100%;}
.ui-jqgrid .ui-search-table .ui-search-oper { width:20px; }
a.g-menu-item, a.soptclass { cursor: pointer; }

/* Multiselect
----------------------------------*/

.ui-multiselect { border: solid 1px; font-size: 0.8em; }
.ui-multiselect ul { -moz-user-select: none; }
.ui-multiselect li { margin: 0; padding: 0; cursor: default; line-height: 20px; height: 20px; font-size: 11px; list-style: none; }
.ui-multiselect li a { color: #999; text-decoration: none; padding: 0; display: block; float: left; cursor: pointer;}
.ui-multiselect li.ui-draggable-dragging { padding-left: 10px; }

.ui-multiselect div.selected { position: relative; padding: 0; margin: 0; border: 0; float:left; }
.ui-multiselect ul.selected { position: relative; padding: 0; overflow: auto; overflow-x: hidden; background: #fff; margin: 0; list-style: none; border: 0; position: relative; width: 100%; }
.ui-multiselect ul.selected li { }

.ui-multiselect div.available { position: relative; padding: 0; margin: 0; border: 0; float:left; border-left: 1px solid; }
.ui-multiselect ul.available { position: relative; padding: 0; overflow: auto; overflow-x: hidden; background: #fff; margin: 0; list-style: none; border: 0; width: 100%; }
.ui-multiselect ul.available li { padding-left: 10px; }
 
.ui-multiselect .ui-state-default { border: none; margin-bottom: 1px; position: relative; padding-left: 20px;}
.ui-multiselect .ui-state-hover { border: none; }
.ui-multiselect .ui-widget-header {border: none; font-size: 11px; margin-bottom: 1px;}
 
.ui-multiselect .add-all { float: right; padding: 7px;}
.ui-multiselect .remove-all { float: right; padding: 7px;}
.ui-multiselect .search { float: left; padding: 4px;}
.ui-multiselect .count { float: left; padding: 7px;}

.ui-multiselect li span.ui-icon-arrowthick-2-n-s { position: absolute; left: 2px; }
.ui-multiselect li a.action { position: absolute; right: 2px; top: 2px; }
 
.ui-multiselect input.search { height: 14px; padding: 1px; opacity: 0.5; margin: 4px; width: 100px; }

html,body {
margin: 10; /* Remove body margin/padding */
padding: 0;
font-size: 75%;
}
 
.ui-tabs-nav li {
position: relative;
}
 
.ui-tabs-selected a span {
padding-right: 10px;
}
 
.ui-tabs-close {
display: none;
position: absolute;
top: 3px;
right: 0px;
z-index: 800;
width: 16px;
height: 14px;
font-size: 10px;
font-style: normal;
cursor: pointer;
}
 
.ui-tabs-selected .ui-tabs-close {
display: block;
}
 
.ui-layout-west .ui-jqgrid tr.jqgrow td {
border-bottom: 0px none;
}
 
.ui-datepicker {
z-index: 1200;
}

	</style>


 </head>
 <body>
 	    <div id="loading" style="position:fixed !important;position:absolute;top:0;left:0;height:100%; width:100%; z-index:999; background:#000 url(http://interjc.googlecode.com/svn/trunk/waterfall/img/load.gif) no-repeat center center; opacity:0.6; filter:alpha(opacity=60);font-size:14px;line-height:20px;display:none" >  
    <p id="loading-one" style="color:#fff;position:absolute; top:50%; left:50%; margin:20px 0 0 -50px; padding:3px 10px;" onclick="javascript:turnoff('loading')">页面载入中..</p>  
</div>  

 


<jsp:include    page="navi.jsp"    flush="true">   
             <jsp:param    name="currpage"    value="siteoverlap"    />   
</jsp:include>
<h2>重合度/<a style="color:#551890" href="./crowd.jsp">人群覆盖</a>/<a style="color:#551890" href="./effect.jsp">流量效果</a></h2>

 
 
 
 
<table  border="0" cellspacing="0" cellpadding="0"> 
<tr>
<td>
	开始日期：
 <input type="text" name="thedateStart" id="thedateStart" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  />
 结束：
 <input type="text" name="thedateEnd" id="thedateEnd" onFocus="WdatePicker({dateFmt:'yyyyMMdd'})"  />	</td>
<td>
	 <font color=red size=3>注：重合度分析每周六计算之前一周的数据。</font>
</td>
</tr>
</table>



<table  border="0" cellspacing="0" cellpadding="0" > 

<tr>
<td>
	目标网站：
	<input type="text" name="a_domain" id="a_domain"  value="taobao.com" />
	对比：
 <input type="text" name="b_domain" id="b_domain"  value="*sina*" />
</td>

  		
  	<td><input type="button" onclick="searchDatatbl()"  value="查看" />  
  		
  		
  		</td>
</tr>


</table>

<table  border="0" cellspacing="0" cellpadding="0" > 
<tr>
 	<td>
 排序：
 <select name="sort_col" id="sort_col" >
      <option value="sum(overlap_uv)"  selected="selected">重合UV</option>
      <option value="sum(a_uv)">目标_uv</option>
      <option value="sum(b_uv)">对比_uv</option>
      <option value="a_domain">目标网站</option>
      <option value="b_domain">对比网站</option>
      <option value="thedate">日期</option>
  </select>
  
   <select name="sort_asc" id="sort_asc" >
      <option value="desc"  selected="selected">逆序</option>
      <option value="asc">升序</option>
  </select>
  
  返回记录数
   <select name="maxrows" id="maxrows" >
      <option value="1000"  selected="selected">1000条</option>
      <option value="2000" >2000条</option>
      <option value="5000" >5000条</option>
            <option value="8000" >8000条</option>

  </select>
 
  		</td>
  		</tr>


</table>
 	
	  <div id="matchstr">
  	
  </div>	
 	
 	<form action="./download.jsp" method="post" target="_blank" >
<input type="hidden" name="data" id="data" />

<input type="submit" name="xx" value="下载当前表格的数据" />
</form>
   <div id="tablelist_id">
<table id="list4"  dir="ltr" style="width: 2000px;"></table>
<div id="pager2"></div>


</div>


<br>
<br>
<br>
<br>
 </body>
</html>

<script>





var today=new Date();
jQuery("#thedateStart").val(parseDay(new Date(today.getTime()-1000*24*3600*10)));
jQuery("#thedateEnd").val(parseDay(new Date(today.getTime()-1000*24*3600*1)));


var g_result_table={};
var g_result_table_pid={};

var g_site_filter="a_uv,b_uv,overlap_uv";
g_coltype_kv={};
g_coltype_kv["a_uv"]="long";
g_coltype_kv["b_uv"]="long";
g_coltype_kv["overlap_uv"]="long";

g_colname_kv={};
g_colname_kv["overlap_uv"]="重合UV";



	function setupfilter()
	{
		
		$("#matchstr").empty();	
	var filter_arr=g_site_filter.split(",");

		
		for(var i=0;i<filter_arr.length;i++)
		{
			  var colname=filter_arr[i];
			  
			  var coldisplayname=g_colname_kv[colname]?g_colname_kv[colname]:colname;
			  if(!g_coltype_kv[colname]||g_coltype_kv[colname]=="string")
					{
						
						$("#matchstr").append(coldisplayname+"：<input type=text value='*' id='filter_"+colname+"' /> <br>");
					}else{
						$("#matchstr").append(coldisplayname+"：<input type=text value='*' size=5 id='filter_"+colname+"_start' />到<input type=text value='*' id='filter_"+colname+"_end' size=5 />  <br>");
					}
		}
	}


setupfilter();
function searchDatatbl()
{
	$("#tablelist_id").empty();
$("#tablelist_id").html('<table id="list4"  dir="ltr" style="width: 2000px;"></table><div id="pager2"></div>');
	var thedatestart=$("#thedateStart").val();
		var thedateend=$("#thedateEnd").val();

var diff=dayToTimestamp2(thedateend.substring(0,4)+"-"+thedateend.substring(4,6)+"-"+thedateend.substring(6))-dayToTimestamp2(thedatestart.substring(0,4)+"-"+thedatestart.substring(4,6)+"-"+thedatestart.substring(6));
if(diff>1000*3600*24*32)
{
	alert("单次查询范围不得超过7天");
	return ;
}
	showload();

		var newq=[];
		
			var filter_arr=g_site_filter.split(",");
		
		for(var i=0;i<filter_arr.length;i++)
		{
			  var colname=filter_arr[i];
			  
			  if(!g_coltype_kv[colname]||g_coltype_kv[colname]=="string")
					{
										var strval=$("#filter_"+colname+"").val();
										if(strval!="*")
										{
																						var fqitem={};
																						fqitem[colname]={"operate":1,"value":[strval]};

												 newq.push(fqitem);

										}
					}else{
						var strvalstart=$("#filter_"+colname+"_start").val();
						var strvalend=$("#filter_"+colname+"_end").val();
										if(strvalstart!="*"&&strvalend!="*"&&strvalstart!=""&&strvalend!="")
										{
											var fqitem={};
											fqitem[colname]={"operate":9,"value":[strvalstart,strvalend]};
												 newq.push(fqitem);

										}
					}
		}
		
	  newq.push({"thedate":{"operate":9,"value":[thedatestart,thedateend]}});
	  newq.push({"a_domain":{"operate":1,"value":[$("#a_domain").val()]}});
	newq.push({"b_domain":{"operate":1,"value":[$("#b_domain").val()]}});
	  	var requestparams={};
			requestparams.start=0;
			requestparams.rows=$("#maxrows").val();
			requestparams.project="rpt_adpmp_site_overlap_7d";
			requestparams.order=$("#sort_asc").val();
			requestparams.sort=$("#sort_col").val();
			requestparams.groupby="thedate,a_domain,b_domain";
			requestparams.fl="thedate,a_domain,b_domain,sum(a_uv),sum(b_uv),sum(overlap_uv)";
		  requestparams.q=JSON.stringify(newq);
		 
			$.post("/result.jsp",requestparams,
					function (data, textStatus){	
						
						if(data.code!="1")
						{
								alert("服务器异常，请稍后再试");
								failfn();
								return ;
						}
					
						var returnresult=[];
						var listtmp=data.data.docs;
						var sumall={};
						for(var i=0;i<listtmp.length;i++)
						{
							var item=listtmp[i];
							
							item["a_uv"]=(parseFloat(item["sum(a_uv)"])).toFixed(2);
						  item["b_uv"]=(parseFloat(item["sum(b_uv)"])).toFixed(2);
						  item["overlap_uv"]=(parseFloat(item["sum(overlap_uv)"])).toFixed(2);
							item["overlap_uv_a"]=(parseFloat(item["overlap_uv"]*100/item["a_uv"])).toFixed(2);
				      item["overlap_uv_b"]=(parseFloat(item["overlap_uv"]*100/item["b_uv"])).toFixed(2);


							returnresult.push(item);
						}
						
						showresultable(returnresult);
			
				}, "json");


}


var g_downloadlines=[];



function showresultable(resultlist)
{	
	
				hideload();
				$("#download_button").show();
				var resultliststr=JSON.stringify(resultlist);
				
				
				var paramsdata={ datatype: "jsonstring",
	datastr:resultliststr,
	 height: 600,
	 colNames:['日期','目标网站','对比网站','目标网站_uv','对比网站_uv','重合UV','占目标百分比（％）','占对比百分比（％）' ], 
	 colModel:[ 
	  {name:'thedate',index:'thedate', width:80,searchoptions:{"clearSearch":false,sopt:['eq']}}, 
	  {name:'a_domain',index:'a_domain', width:100,searchoptions:{"clearSearch":false,sopt:['eq']}}, 
	  {name:'b_domain',index:'b_domain', width:100,searchoptions:{"clearSearch":false,sopt:['eq']}}, 
	  {name:'a_uv',index:'a_uv',  width:100, align:"right",sorttype:"float",searchoptions:{clearSearch:false,sopt:['eq','ne','le','lt','gt','ge']}}, 
	  {name:'b_uv',index:'b_uv',  width:100, align:"right",sorttype:"float",searchoptions:{clearSearch:false,sopt:['eq','ne','le','lt','gt','ge']}}, 
	  {name:'overlap_uv',index:'overlap_uv', width:100, align:"right",sorttype:"float",searchoptions:{clearSearch:false,sopt:['eq','ne','le','lt','gt','ge']}}, 
	  {name:'overlap_uv_a',index:'overlap_uv_a', width:120, align:"right",sorttype:"float",searchoptions:{clearSearch:false,sopt:['eq','ne','le','lt','gt','ge']}}, 
	  {name:'overlap_uv_b',index:'overlap_uv_b', width:120, align:"right",sorttype:"float",searchoptions:{clearSearch:false,sopt:['eq','ne','le','lt','gt','ge']}}, 
	 
	 ], 
	  pager: '#pager2',
	  rowNum:200, 
	  rowList:[50,100,200,500],
	  sortname: '',
    viewrecords: true,
    sortorder: "desc",
    caption:"38"
};
								g_downloadlines=[];

		g_downloadlines.push(paramsdata["colNames"].join(","));
				for(var i=0;i<resultlist.length;i++)
				{
					var item=resultlist[i];
					var itemList=[];
					for(var j=0;j<paramsdata.colModel.length;j++)
					{
						var keyname=paramsdata["colModel"][j]["name"];
						itemList.push(item[keyname]);
						
					}
					g_downloadlines.push(itemList.join(","));
				}
				
						 $("#data").val(g_downloadlines.join("\r\n"));

				
				
jQuery("#list4").jqGrid(paramsdata);	
	
//jQuery("#list4").jqGrid('filterToolbar',{searchOperators : true});	


}




function parseFloatData(a)
{
		if(!a)
		{
			return 0;
		}
		
		if(a=="-")
		{
			return 0;
		}
		
		try{
			
			var rtn= parseFloat(a);
			
			if(isNaN(rtn))
			{
				return 0;
			}
			return rtn.toFixed(2);
		}catch(e){}
		return 0;
}










searchDatatbl();




function parseDate(date)
{
    var month = date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1);   
    var day = date.getDate() <= 9 ? "0" + (date.getDate()) : (date.getDate());   
    var hour = date.getHours() <= 9 ? "0" + (date.getHours()) : (date.getHours());
    var miniute = date.getMinutes() <= 9 ? "0" + (date.getMinutes()) : (date.getMinutes());
        var secs = date.getSeconds() <= 9 ? "0" + (date.getSeconds()) : (date.getSeconds());

    var yyyymmdd= (date.getFullYear() + "" + month + "" + day+hour+miniute+secs);         
    return yyyymmdd;
}

function jsonpcall()
{
	
}


$(function(){
var logparams={};
logparams["date"]=parseDate(new Date());
logparams["bizdate"]=parseDay(new Date())+"-"+parseDay(new Date());
logparams["nick"]=log_nickname.replace(/\(.*\)/ig,"");
logparams["email"]=log_userid+"@taobao.com";
logparams["set"]="全景监控重合度";
logparams["dimvalue"]=location.href;
logparams["filter"]="";
logparams["r"]=new Date().getTime();
logparams["callback"]="jsonpcall";


$.ajax({
url:"http://adhoc.etao.com:9999/querylog.jsp",
data:logparams,
dataType:"jsonp",
jsonp:"jsonpcall",
success:jsonpcall});

});

	</script>	
</script>

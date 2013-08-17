package com.alimama.mdrill.adhoc;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringMatchGet  {
    private static HashMap<String, Pattern> match=new HashMap<String, Pattern>();
    public String evaluate(final String d, String regEx, int index, String parseType) {
        if (d == null) {
            return new String("-");
        }
        Pattern pat=match.get(regEx);
        if(pat==null)
        {
            pat = Pattern.compile(regEx);
            match.put(regEx, pat);
        }

        Matcher mat = pat.matcher(d);
        while (mat.find()) {
            String rtn = mat.group(index);
            if(rtn==null)
            {
                return new String("-");
            }
            
            if (parseType.equals("String")) {
                return new String(rtn);
            }
            if (parseType.equals("Integer")) {
                try {
                    Integer parse = Integer.parseInt(rtn);
                    return new String(String.valueOf(parse));
                } catch (NumberFormatException e) {}
            }

            if (parseType.equals("Double")) {
                try {
                    Double parse = Double.parseDouble(rtn);
                    return new String(String.valueOf(parse));
                } catch (NumberFormatException e) {}
            }

            if (parseType.equals("Long")) {
                try {
                    Long parse = Long.parseLong(rtn);
                    return new String(String.valueOf(parse));
                } catch (NumberFormatException e) {}
            }
        }
        return new String("-");
    }
    
    public static void main(String[] args) {
    	StringMatchGet m=new StringMatchGet();
    	System.out.println(m.evaluate("Launching Job 1 out of 1",".*Launching.*Job.*out.*of(.*$)", 1, "Integer"));
	}


}
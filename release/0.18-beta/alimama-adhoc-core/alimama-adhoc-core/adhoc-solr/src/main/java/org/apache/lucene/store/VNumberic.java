package org.apache.lucene.store;

public class VNumberic {

    public static void main(String[] args) {
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(0)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(0l)));
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(1)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(1)));
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(100)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(100)));
	
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(10000)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(10000)));
	
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(1000000)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(1000000)));
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(999999999)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(999999999)));
	
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(1000000000)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(1000000000)));
	
	
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(10000000000000l)));
	
	
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(-1)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(-1)));
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(-100)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(-100)));
	
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(-10000)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(-10000)));
	
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(-1000000)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(-1000000)));
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(-999999999)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(-999999999)));
	
	
	System.out.println(VNumberic.unVInt(VNumberic.enVInt(-1000000000)));
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(-1000000000)));
	
	System.out.println(VNumberic.unVLong(VNumberic.enVLong(-10000000000000l)));
	
    }
    
    public static long enVLong(long i) {
	if (i < 0) {
	    return ((-1 * i) << 1) ^ 1;

	} else {
	    return i << 1;
	}
    }
    
    public static long unVLong(long i)
    {
	long lastbit=i&1;
	long rtn=i>>>1;
	if(lastbit>0)
	{
	    rtn*=-1;
	}
	return rtn;
    }
    
    public static int enVInt(int i) {
	if (i < 0) {
	    return ((-1 * i) << 1) ^ 1;

	} else {
	    return i << 1;
	}
    }
    
    public static int unVInt(int i)
    {
	int lastbit=i&1;
	int rtn=i>>>1;
	if(lastbit>0)
	{
	    rtn*=-1;
	}
	return rtn;
    }

}

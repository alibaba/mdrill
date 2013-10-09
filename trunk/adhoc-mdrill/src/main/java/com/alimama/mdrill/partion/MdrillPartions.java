package com.alimama.mdrill.partion;

import com.alimama.mdrill.partion.dirtory.DirtoryPartions;
import com.alimama.mdrill.partion.single.SinglePartions;
import com.alimama.mdrill.partion.thedate.ThedatePartions;

public class MdrillPartions {
	public static String PARTION_VERSION="201301008";
	public static MdrillPartionsInterface INSTANCE(String parttype)
	{
		MdrillPartionsInterface rtn=null;
		if(parttype.startsWith("single"))
		{
			rtn= new SinglePartions();

		}else if(parttype.startsWith("dir")){
			rtn= new DirtoryPartions();
		}else{
			rtn= new ThedatePartions();
		}
		rtn.setPartionType(parttype);
		return rtn;
	}
}

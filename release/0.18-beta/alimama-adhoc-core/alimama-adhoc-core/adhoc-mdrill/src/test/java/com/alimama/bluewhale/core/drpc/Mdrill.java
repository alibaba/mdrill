package com.alimama.bluewhale.core.drpc;

import java.util.Arrays;

import com.alimama.mdrill.topology.MdrillMain;


public class Mdrill {
	public static void main(String[] args) throws Exception {
		System.out.println("higo execute "+Arrays.toString(args) );
		MdrillMain.main(args);
	}
}

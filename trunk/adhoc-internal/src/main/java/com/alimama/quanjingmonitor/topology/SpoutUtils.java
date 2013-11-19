package com.alimama.quanjingmonitor.topology;

import java.util.zip.CRC32;

public class SpoutUtils {
	public static Long uuid()
	{
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(java.util.UUID.randomUUID().toString()).getBytes());
		return crc32.getValue();
	}
}

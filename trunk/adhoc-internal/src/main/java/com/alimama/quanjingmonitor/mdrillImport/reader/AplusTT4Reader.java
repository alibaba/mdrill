package com.alimama.quanjingmonitor.mdrillImport.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import com.alibaba.tt.log.impl.TTLogBlock;
import com.alibaba.tt.queue.impl.MessageKey;
import com.taobao.timetunnel.client.parser.MessageParser;


public class AplusTT4Reader extends TT4Reader {

	@Override
	public List<Object> read() throws IOException {

		TTLogBlock block = this.readBlock();
		if (block == null) {
			return null;
		}
		
		// ack
		MessageKey key = block.getKey();
		key.ack();
		
		byte[] data=block.getBuffer();
		
		List<byte[]> messages = MessageParser.parseProtoBufsFromBytes(data);
		List<Object> list = new ArrayList<Object>(1);
		for (byte[] d:messages) {
			list.add(d);
		}

		return list;

	}


}

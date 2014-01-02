package com.alimama.mdrill.editlog.defined;


import java.util.Map;
import java.util.HashMap;


public enum FSEditLogOpCodes {
  OP_INVALID                    ((byte) -1),
  OP_ADD                        ((byte)  0);
  private byte opCode;

  FSEditLogOpCodes(byte opCode) {
    this.opCode = opCode;
  }

  public byte getOpCode() {
    return opCode;
  }

  private static final Map<Byte, FSEditLogOpCodes> byteToEnum =
    new HashMap<Byte, FSEditLogOpCodes>();

  static {
    // initialize byte to enum map
    for(FSEditLogOpCodes opCode : values())
      byteToEnum.put(opCode.getOpCode(), opCode);
  }

  public static FSEditLogOpCodes fromByte(byte opCode) {
	  if(byteToEnum.containsKey(opCode))
	  {
		  return byteToEnum.get(opCode);
	  }else{
		  return FSEditLogOpCodes.OP_INVALID;
	  }
  }
  

}

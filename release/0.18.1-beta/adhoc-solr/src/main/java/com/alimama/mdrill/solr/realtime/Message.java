package com.alimama.mdrill.solr.realtime;

import java.io.Serializable;
import java.util.Arrays;



public class Message  implements IMessage,Serializable {
    static final long serialVersionUID = -1L;
    private byte[] data;


    public Message(final byte[] data) {
        this.data = data;
    }
    
    public Message() {
    }

    /**
     * ������Ϣpayload
     * 
     * @param data
     */
    public void setData(final byte[] data) {
        this.data = data;
    }

    /**
     * ������Ϣpayload
     * 
     * @return
     */
    public byte[] getData() {
        return this.data;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.data);
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Message other = (Message) obj;

        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
 
        return true;
    }


}

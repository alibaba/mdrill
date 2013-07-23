package com.alimama.mdrill.adhoc;

public class Base64Encode
{


    private final static byte BYTE_TABLE[] =
    {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',

        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',

        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',

        'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',
    };

    public static String encode( byte[] src, int srcOffset, int srcLength )
    {
        //if( src == null || srcOffset < 0 || srcLength < 0 || srcOffset + 1 > src.length || srcOffset + srcLength > src.length ){ return null; }

        if( src == null || srcOffset < 0 || srcLength < 1 || srcOffset + srcLength > src.length ){ return null; }

        byte[] buffer = new byte[( ( srcLength + 2 ) / 3 ) * 4];

        int round = srcLength / 3, remain = srcLength % 3;

        int srcIndex = srcOffset, destIndex = 0, b1, b2, b3;

        for( int j = 0; j < round; j ++ )
        {
            b1 = src[srcIndex ++] & 0xFF;

            b2 = src[srcIndex ++] & 0xFF;

            b3 = src[srcIndex ++] & 0xFF;

            buffer[destIndex ++] = BYTE_TABLE[b1 >> 2];

            buffer[destIndex ++] = BYTE_TABLE[b1 << 4 & 0x3F | b2 >> 4];

            buffer[destIndex ++] = BYTE_TABLE[b2 << 2 & 0x3F | b3 >> 6];

            buffer[destIndex ++] = BYTE_TABLE[b3 & 0x3F];
        }

        if( remain > 0 )
        {
            b1 = src[srcIndex ++] & 0xFF;

            buffer[destIndex ++] = BYTE_TABLE[b1 >> 2];

            if( remain == 1 )
            {
                buffer[destIndex ++] = BYTE_TABLE[b1 << 4 & 0x3F];

                buffer[destIndex ++] = '=';

                buffer[destIndex ++] = '=';
            }
            else  //remain == 2
            {
                b2 = src[srcIndex ++] & 0xFF;

                buffer[destIndex ++] = BYTE_TABLE[b1 << 4 & 0x3F | b2 >> 4];

                buffer[destIndex ++] = BYTE_TABLE[b2 << 2 & 0x3F];

                buffer[destIndex ++] = '=';
            }
        }

        return new String( buffer, 0, destIndex );
    }

    public static int encode( byte[] src, int srcOffset, int srcLength, byte[] dest, int destOffset )
    {
        //if( src == null || srcOffset < 0 || srcLength < 0 || srcOffset + 1 > src.length || srcOffset + srcLength > src.length ){ return -1; }

        if( src == null || dest == null || srcOffset < 0 || destOffset < 0 || srcLength < 1 || srcOffset + srcLength > src.length ){ return -1; }

        int destLength = ( srcLength + 2 ) / 3 * 4;  //destLength always large then zero

        if( /*destLength < 1 ||*/ destOffset + destLength > dest.length ){ return -1; }

        int round = srcLength / 3, remain = srcLength % 3;

        int srcIndex = srcOffset, destIndex = destOffset, b1, b2, b3;

        for( int j = 0; j < round; j ++ )
        {
            b1 = src[srcIndex ++] & 0xFF;

            b2 = src[srcIndex ++] & 0xFF;

            b3 = src[srcIndex ++] & 0xFF;

            dest[destIndex ++] = BYTE_TABLE[b1 >> 2];

            dest[destIndex ++] = BYTE_TABLE[b1 << 4 & 0x3F | b2 >> 4];

            dest[destIndex ++] = BYTE_TABLE[b2 << 2 & 0x3F | b3 >> 6];

            dest[destIndex ++] = BYTE_TABLE[b3 & 0x3F];
        }

        if( remain > 0 )
        {
            b1 = src[srcIndex ++] & 0xFF;

            dest[destIndex ++] = BYTE_TABLE[b1 >> 2];

            if( remain == 1 )
            {
                dest[destIndex ++] = BYTE_TABLE[b1 << 4 & 0x3F];

                dest[destIndex ++] = '=';

                dest[destIndex ++] = '=';
            }
            else  //remain == 2
            {
                b2 = src[srcIndex ++] & 0xFF;

                dest[destIndex ++] = BYTE_TABLE[b1 << 4 & 0x3F | b2 >> 4];

                dest[destIndex ++] = BYTE_TABLE[b2 << 2 & 0x3F];

                dest[destIndex ++] = '=';
            }
        }

        return destIndex;
    }
}

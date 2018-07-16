/**
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *                                                              
 * http://www.apache.org/licenses/LICENSE-2.0
 *                                                            
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.yahoo.ycsb;
import java.util.Random;
/**
 *  A ByteIterator that generates a random sequence of bytes.
 */
public class RandomByteIterator extends ByteIterator {
  private long len;
  private long off;
  private int bufOff;
  private byte[] buf;
  
  @Override
  public boolean hasNext() {
    return (off + bufOff) < len;
  }

  private void fillBytesImpl(byte[] recBuf, int base) {
    /* Original
    int bytes = Utils.random().nextInt();
    try {
      buffer[base+0] = (byte)(((bytes) & 31) + ' ');
      buffer[base+1] = (byte)(((bytes >> 5) & 63) + ' ');
      buffer[base+2] = (byte)(((bytes >> 10) & 95) + ' ');
      buffer[base+3] = (byte)(((bytes >> 15) & 31) + ' ');
      buffer[base+4] = (byte)(((bytes >> 20) & 63) + ' ');
      buffer[base+5] = (byte)(((bytes >> 25) & 95) + ' ');

    } catch (ArrayIndexOutOfBoundsException e) { }
    */
    int k = 0;
    int round = this.buf.length / 100;

    for (; k < round; k++) {
      int off = k * 100;
      Random ran = new Random();
      long ran0, ran1, ran2, ran3;
      ran0 = ran.nextLong();
      ran1 = ran.nextLong();
      ran2 = ran.nextLong();
      ran3 = ran.nextLong();
   
      ran1 &= 0x0000000000FFFF00;
      ran3 &= 0x00000000FFFFFFFF;
      Unsigned16 rand = new Unsigned16(ran0, ran1);
      Unsigned16 recordNumber = new Unsigned16(ran2, ran3);
      
      for(int i=0; i < 10; ++i) {
        recBuf[i+off] = rand.getByte(i);
      }
       
      /* add 2 bytes of "break" */
      recBuf[10+off] = 0x00;
      recBuf[11+off] = 0x11;
      long tmp;
      /* convert the 128-bit record number to 32 bits of ascii hexadecimal
       * as the next 32 bytes of the record.
       */
      /* 0: default --- 28%
       * 1: 25%
       * 2: 50%
       * 3: 75%
       */
      int pattern = 0;
      switch(pattern) {
        case 0:
          for (int i = 0; i < 32; i++) {
            recBuf[12 + i+off] = (byte) recordNumber.getHexDigit(i);
          }
          break;
        case 1:
          for (int i = 0; i < 16; i++) {
            recBuf[12 + i+off] = (byte) recordNumber.getHexDigit(i);
          }
          for (int i = 16; i < 32; i++) {
            recBuf[12 + i+off] = recBuf[i - 4+off];
          }
          break;
        case 3:
          for (int j = 0; j < 4; j++) {
            tmp = ran.nextLong();
            for (int i = j * 8; i < (j+1) * 8; i++) {
              tmp = tmp >> 8;
              recBuf[12 + i+off] = (byte) (tmp);
            }
          }
          break;
        default:
          for (int i = 0; i < 32; i++) {
            recBuf[12 + i+off] = (byte) recordNumber.getHexDigit(i);
          }
          break;
      }
     
      /* add 4 bytes of "break" data */
      recBuf[44+off] = (byte) 0x88;
      recBuf[45+off] = (byte) 0x99;
      recBuf[46+off] = (byte) 0xAA;
      recBuf[47+off] = (byte) 0xBB;

      /* add 48 bytes of filler based on low 48 bits of random number */
      switch(pattern) {
        case 0:
          for(int i = 0; i < 12; ++i) {
            recBuf[48+i*4+off] = recBuf[49+i*4+off] = recBuf[50+i*4+off] = recBuf[51+i*4+off] =
            (byte) rand.getHexDigit(20 + i);
          }
          break;
        case 2:
          for (int j = 0; j < 4; j++) {
            tmp = ran.nextLong();
            for (int i = j * 8; i < (j+1) * 8; i++) {
              tmp = tmp >> 8;
              recBuf[48 + i+off] = (byte) (tmp);
            }
          }
          for (int i = 32; i < 48; i++) {
            recBuf[48 + i+off] = recBuf[16 + i+off];
          }
          break;
        case 3:
          for (int j = 0; j < 4; j++) {
            if (j < 3) {
              tmp = ran.nextLong();
              for (int i = j * 8; i < (j+1) * 8; i++) {
                tmp = tmp >> 8;
                recBuf[48 + i+off] = (byte) (tmp);
              }
            } else {
                tmp = ran.nextLong();
              for (int i = 24; i < 28; i++) {
                tmp = tmp >> 8;
                recBuf[48 + i+off] = (byte) (tmp);
              }
            }
          }
          for (int i = 28; i < 48; i++) {
            recBuf[48 + i+off] = recBuf[20 + i+off];
          }
          break;
        default:
          for(int i=0; i < 12; ++i) {
            recBuf[48+i*4+off] = recBuf[49+i*4+off] = recBuf[50+i*4+off] = recBuf[51+i*4+off] =
            (byte) rand.getHexDigit(20 + i);
          }
          break;
      }

      /* add 4 bytes of "break" data */
      recBuf[96+off] = (byte) 0xCC;
      recBuf[97+off] = (byte) 0xDD;
      recBuf[98+off] = (byte) 0xEE;
      recBuf[99+off] = (byte) 0xFF;
    }

    int j = 0;
    for(int m = k*100; m < this.buf.length; m++) {
      recBuf[m] = (byte)(j++);
    }
  }

  private void fillBytes() {
    if(bufOff ==  buf.length) {
      fillBytesImpl(buf, 0);
      bufOff = 0;
      off += buf.length;
    }
  }

  public RandomByteIterator(long len) {
    this.len = len;
    this.buf = new byte[(int)len];
    this.bufOff = buf.length;
    fillBytes();
    this.off = 0;
  }

  public byte nextByte() {
    fillBytes();
    bufOff++;
    return buf[bufOff-1];
  }

  @Override
  public int nextBuf(byte[] buffer, int bufferOffset) {
    int ret;
    if(len - off < buffer.length - bufferOffset) {
      ret = (int)(len - off);
    } else {
      ret = buffer.length - bufferOffset;
    }
    int i;
    for(i = 0; i < ret; i+=this.buf.length) {
      fillBytesImpl(buffer, i + bufferOffset);
    }
    off+=ret;
    return ret + bufferOffset;
  }

  @Override
  public long bytesLeft() {
    return len - off - bufOff;
  }
}

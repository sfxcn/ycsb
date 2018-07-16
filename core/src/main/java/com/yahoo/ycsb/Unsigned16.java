/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yahoo.ycsb;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * An unsigned 16 byte integer class that supports addition, multiplication,
 * and left shifts.
 */
class Unsigned16 {
  private long hi8;
  private long lo8;

  public Unsigned16() {
    hi8 = 0;
    lo8 = 0;
  }

  public Unsigned16(long l) {
    hi8 = 0;
    lo8 = l;
  }

  public Unsigned16(long l, long m) {
    hi8 = m;
    lo8 = l;
  }

  public Unsigned16(Unsigned16 other) {
    hi8 = other.hi8;
    lo8 = other.lo8;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof Unsigned16) {
      Unsigned16 other = (Unsigned16) o;
      return other.hi8 == hi8 && other.lo8 == lo8;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (int) lo8;
  }


  /*
   * Set the number to a given long.
   * @param l the new value, which is treated as an unsigned number
   */
  public void set(long l) {
    lo8 = l;
    hi8 = 0;
  }

  /**
   * Map a hexadecimal character into a digit.
   * @param ch the character
   * @return the digit from 0 to 15
   * @throws NumberFormatException
   */
  private static int getHexDigit(char ch) throws NumberFormatException {
    if (ch >= '0' && ch <= '9') {
      return ch - '0';
    }
    if (ch >= 'a' && ch <= 'f') {
      return ch - 'a' + 10;
    }
    if (ch >= 'A' && ch <= 'F') {
      return ch - 'A' + 10;
    }
    throw new NumberFormatException(ch + " is not a valid hex digit");
  }


  /**
   * Return the number as a hex string.
   */
  public String toString() {
    if (hi8 == 0) {
      return Long.toHexString(lo8);
    } else {
      StringBuilder result = new StringBuilder();
      result.append(Long.toHexString(hi8));
      String loString = Long.toHexString(lo8);
      for(int i=loString.length(); i < 16; ++i) {
        result.append('0');
      }
      result.append(loString);
      return result.toString();
    }
  }

  /**
   * Get a given byte from the number.
   * @param b the byte to get with 0 meaning the most significant byte
   * @return the byte or 0 if b is outside of 0..15
   */
  public byte getByte(int b) {
    if (b >= 0 && b < 16) {
      if (b < 8) {
        return (byte) (hi8 >> (56 - 8*b));
      } else {
        return (byte) (lo8 >> (120 - 8*b));
      }
    }
    return 0;
  }

  /**
   * Get the hexadecimal digit at the given position.
   * @param p the digit position to get with 0 meaning the most significant
   * @return the character or '0' if p is outside of 0..31
   */
  public char getHexDigit(int p) {
    byte digit = getByte(p / 2);
    if (p % 2 == 0) {
      digit >>>= 4;
    }
    digit &= 0xf;
    if (digit < 10) {
      return (char) ('0' + digit);
    } else {
      return (char) ('A' + digit - 10);
    }
  }

  /**
   * Get the high 8 bytes as a long.
   */
  public long getHigh8() {
    return hi8;
  }
  
  /**
   * Get the low 8 bytes as a long.
   */
  public long getLow8() {
    return lo8;
  }
  
}

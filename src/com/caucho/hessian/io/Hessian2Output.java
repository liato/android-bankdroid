/*
 * Copyright (c) 2001-2008 Caucho Technology, Inc.  All rights reserved.
 *
 * The Apache Software License, Version 1.1
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Caucho Technology (http://www.caucho.com/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Burlap", "Resin", and "Caucho" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    info@caucho.com.
 *
 * 5. Products derived from this software may not be called "Resin"
 *    nor may "Resin" appear in their names without prior written
 *    permission of Caucho Technology.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Scott Ferguson
 */

package com.caucho.hessian.io;

import com.caucho.hessian.util.IdentityIntMap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Output stream for Hessian 2 requests.
 *
 * <p>Since HessianOutput does not depend on any classes other than
 * in the JDK, it can be extracted independently into a smaller package.
 *
 * <p>HessianOutput is unbuffered, so any client needs to provide
 * its own buffering.
 *
 * <pre>
 * OutputStream os = ...; // from http connection
 * Hessian2Output out = new Hessian11Output(os);
 * String value;
 *
 * out.startCall("hello");  // start hello call
 * out.writeString("arg1"); // write a string argument
 * out.completeCall();      // complete the call
 * </pre>
 */
public class Hessian2Output
  extends AbstractHessianOutput
  implements Hessian2Constants
{
  // the output stream/
  protected OutputStream _os;
  
  // map of references
  private IdentityIntMap _refs = new IdentityIntMap();

  private boolean _isCloseStreamOnClose;
  
  // map of classes
  private HashMap _classRefs;
  
  // map of types
  private HashMap _typeRefs;

  private final static int SIZE = 1024;
  
  private final byte []_buffer = new byte[SIZE];
  private int _offset;

  private boolean _isStreaming;
  
  /**
   * Creates a new Hessian output stream, initialized with an
   * underlying output stream.
   *
   * @param os the underlying output stream.
   */
  public Hessian2Output(OutputStream os)
  {
    _os = os;
  }
  
  public void setCloseStreamOnClose(boolean isClose)
  {
    _isCloseStreamOnClose = isClose;
  }
  
  public boolean isCloseStreamOnClose()
  {
    return _isCloseStreamOnClose;
  }
  

  /**
   * Writes a complete method call.
   */
  public void call(String method, Object []args)
    throws IOException
  {
    startCall(method);
    
    if (args != null) {
      for (int i = 0; i < args.length; i++)
        writeObject(args[i]);
    }
    
    completeCall();
  }
  
  /**
   * Starts the method call.  Clients would use <code>startCall</code>
   * instead of <code>call</code> if they wanted finer control over
   * writing the arguments, or needed to write headers.
   *
   * <code><pre>
   * c major minor
   * m b16 b8 method-name
   * </pre></code>
   *
   * @param method the method name to call.
   */
  public void startCall(String method)
    throws IOException
  {
    int offset = _offset;

    if (SIZE < offset + 32) {
      flush();
      offset = 0;
    }

    byte []buffer = _buffer;
    
    buffer[offset++] = (byte) 'c';
    buffer[offset++] = (byte) 2;
    buffer[offset++] = (byte) 0;

    buffer[offset++] = (byte) 'm';
    int len = method.length();
    buffer[offset++] = (byte) (len >> 8);
    buffer[offset++] = (byte) len;

    _offset = offset;
    
    printString(method, 0, len);
  }

  /**
   * Writes the call tag.  This would be followed by the
   * headers and the method tag.
   *
   * <code><pre>
   * c major minor
   * </pre></code>
   *
   * @param method the method name to call.
   */
  public void startCall()
    throws IOException
  {
    flushIfFull();
    
    int offset = _offset;
    byte []buffer = _buffer;
    
    buffer[offset++] = (byte) 'c';
    buffer[offset++] = (byte) 2;
    buffer[offset++] = (byte) 0;
  }

  /**
   * Writes the streaming call tag.  This would be followed by the
   * headers and the method tag.
   *
   * <code><pre>
   * C major minor
   * </pre></code>
   *
   * @param method the method name to call.
   */
  public void startStreamingCall()
    throws IOException
  {
    flushIfFull();
    
    int offset = _offset;
    byte []buffer = _buffer;
    
    buffer[offset++] = (byte) 'C';
    buffer[offset++] = (byte) 2;
    buffer[offset++] = (byte) 0;
  }
  
  /**
   * Starts an envelope.
   *
   * <code><pre>
   * E major minor
   * m b16 b8 method-name
   * </pre></code>
   *
   * @param method the method name to call.
   */
  public void startEnvelope(String method)
    throws IOException
  {
    int offset = _offset;

    if (SIZE < offset + 32) {
      flush();
      offset = 0;
    }

    byte []buffer = _buffer;
    
    buffer[offset++] = (byte) 'E';
    buffer[offset++] = (byte) 2;
    buffer[offset++] = (byte) 0;

    buffer[offset++] = (byte) 'm';
    int len = method.length();
    buffer[offset++] = (byte) (len >> 8);
    buffer[offset++] = (byte) len;

    _offset = offset;
    
    printString(method, 0, len);
  }

  /**
   * Completes an envelope.
   *
   * <p>A successful completion will have a single value:
   *
   * <pre>
   * z
   * </pre>
   */
  public void completeEnvelope()
    throws IOException
  {
    flushIfFull();
    
    _buffer[_offset++] = (byte) 'z';
  }

  /**
   * Writes the method tag.
   *
   * <code><pre>
   * m b16 b8 method-name
   * </pre></code>
   *
   * @param method the method name to call.
   */
  public void writeMethod(String method)
    throws IOException
  {
    flushIfFull();

    byte []buffer = _buffer;
    int offset = _offset;
    
    buffer[offset++] = (byte) 'm';
    int len = method.length();
    buffer[offset++] = (byte) (len >> 8);
    buffer[offset++] = (byte) len;

    _offset = offset;
    
    printString(method, 0, len);
  }

  /**
   * Completes.
   *
   * <code><pre>
   * z
   * </pre></code>
   */
  public void completeCall()
    throws IOException
  {
    flushIfFull();
    
    _buffer[_offset++] = (byte) 'z';
  }

  /**
   * Starts the reply
   *
   * <p>A successful completion will have a single value:
   *
   * <pre>
   * r
   * </pre>
   */
  public void startReply()
    throws IOException
  {
    flushIfFull();
    
    _buffer[_offset++] = (byte) 'r';
    _buffer[_offset++] = (byte) 2;
    _buffer[_offset++] = (byte) 0;
  }

  /**
   * Starts the streaming reply
   *
   * <p>A successful completion will have a single value:
   *
   * <pre>
   * r
   * </pre>
   */
  public void startStreamingReply()
    throws IOException
  {
    flushIfFull();
    
    _buffer[_offset++] = (byte) 'R';
    _buffer[_offset++] = (byte) 2;
    _buffer[_offset++] = (byte) 0;
  }

  /**
   * Completes reading the reply
   *
   * <p>A successful completion will have a single value:
   *
   * <pre>
   * z
   * </pre>
   */
  public void completeReply()
    throws IOException
  {
    flushIfFull();
    
    _buffer[_offset++] = (byte) 'z';
  }

  /**
   * Starts the message
   *
   * <p>A message contains several objects followed by a 'z'</p>
   *
   * <pre>
   * p x02 x00
   * </pre>
   */
  public void startMessage()
    throws IOException
  {
    flushIfFull();
    
    _buffer[_offset++] = (byte) 'p';
    _buffer[_offset++] = (byte) 2;
    _buffer[_offset++] = (byte) 0;
  }

  /**
   * Completes reading the message
   *
   * <p>A successful completion will have a single value:
   *
   * <pre>
   * z
   * </pre>
   */
  public void completeMessage()
    throws IOException
  {
    flushIfFull();
    
    _buffer[_offset++] = (byte) 'z';
  }

  /**
   * Writes a header name.  The header value must immediately follow.
   *
   * <code><pre>
   * H b16 b8 foo <em>value</em>
   * </pre></code>
   */
  public void writeHeader(String name)
    throws IOException
  {
    int len = name.length();

    flushIfFull();
    
    _buffer[_offset++] = (byte) 'H';
    _buffer[_offset++] = (byte) (len >> 8);
    _buffer[_offset++] = (byte) (len);

    printString(name);
  }

  /**
   * Writes a fault.  The fault will be written
   * as a descriptive string followed by an object:
   *
   * <code><pre>
   * f
   * &lt;string>code
   * &lt;string>the fault code
   *
   * &lt;string>message
   * &lt;string>the fault mesage
   *
   * &lt;string>detail
   * mt\x00\xnnjavax.ejb.FinderException
   *     ...
   * z
   * z
   * </pre></code>
   *
   * @param code the fault code, a three digit
   */
  public void writeFault(String code, String message, Object detail)
    throws IOException
  {
    flushIfFull();
    
    _buffer[_offset++] = (byte) 'f'
      ;
    writeString("code");
    writeString(code);

    writeString("message");
    writeString(message);

    if (detail != null) {
      writeString("detail");
      writeObject(detail);
    }

    flushIfFull();
    _buffer[_offset++] = (byte) ('z');
  }

  /**
   * Writes any object to the output stream.
   */
  public void writeObject(Object object)
    throws IOException
  {
    if (object == null) {
      writeNull();
      return;
    }

    Serializer serializer;

    serializer = findSerializerFactory().getSerializer(object.getClass());

    serializer.writeObject(object, this);
  }

  /**
   * Writes the list header to the stream.  List writers will call
   * <code>writeListBegin</code> followed by the list contents and then
   * call <code>writeListEnd</code>.
   *
   * <code><pre>
   * V
   * t b16 b8 type
   * l b32 b24 b16 b8
   * </pre></code>
   */
  public boolean writeListBegin(int length, String type)
    throws IOException
  {
    flushIfFull();

    if (_typeRefs != null) {
      Integer refV = (Integer) _typeRefs.get(type);

      if (refV != null) {
	_buffer[_offset++] = (byte) (LIST_FIXED);
	writeInt(refV.intValue());
	writeInt(length);

	return false;
      }
    }
    
    _buffer[_offset++] = (byte) 'V';

    writeType(type);

    flushIfFull();

    if (length < 0) {
    }
    else if (length < 0x100) {
      _buffer[_offset++] = (byte) (LENGTH_BYTE);
      _buffer[_offset++] = (byte) (length);
    }
    else {
      _buffer[_offset++] = (byte) ('l');
      _buffer[_offset++] = (byte) (length >> 24);
      _buffer[_offset++] = (byte) (length >> 16);
      _buffer[_offset++] = (byte) (length >> 8);
      _buffer[_offset++] = (byte) (length);
    }

    return true;
  }

  /**
   * Writes the tail of the list to the stream.
   */
  public void writeListEnd()
    throws IOException
  {
    flushIfFull();
    
    _buffer[_offset++] = (byte) 'z';
  }

  /**
   * Writes the map header to the stream.  Map writers will call
   * <code>writeMapBegin</code> followed by the map contents and then
   * call <code>writeMapEnd</code>.
   *
   * <code><pre>
   * Mt b16 b8 (<key> <value>)z
   * </pre></code>
   */
  public void writeMapBegin(String type)
    throws IOException
  {
    if (SIZE < _offset + 32)
      flush();
    
    _buffer[_offset++] = 'M';

    writeType(type);
  }

  /**
   * Writes the tail of the map to the stream.
   */
  public void writeMapEnd()
    throws IOException
  {
    if (SIZE < _offset + 32)
      flush();
    
    _buffer[_offset++] = (byte) 'z';
  }

  /**
   * Writes the object definition
   *
   * <code><pre>
   * O t b16 b8 <string>*
   * </pre></code>
   */
  public int writeObjectBegin(String type)
    throws IOException
  {
    if (_classRefs == null)
      _classRefs = new HashMap();

    Integer refV = (Integer) _classRefs.get(type);

    if (refV != null) {
      int ref = refV.intValue();
      
      if (SIZE < _offset + 32)
	flush();

      _buffer[_offset++] = (byte) 'o';
      writeInt(ref);

      return ref;
    }
    else {
      int ref = _classRefs.size();
      
      _classRefs.put(type, Integer.valueOf(ref));
      
      if (SIZE < _offset + 32)
	flush();

      _buffer[_offset++] = (byte) 'O';

      writeString(type);

      return -1;
    }
  }

  /**
   * Writes the tail of the class definition to the stream.
   */
  public void writeClassFieldLength(int len)
    throws IOException
  {
    writeInt(len);
  }

  /**
   * Writes the tail of the object definition to the stream.
   */
  public void writeObjectEnd()
    throws IOException
  {
  }

  /**
   * Writes a remote object reference to the stream.  The type is the
   * type of the remote interface.
   *
   * <code><pre>
   * 'r' 't' b16 b8 type url
   * </pre></code>
   */
  public void writeRemote(String type, String url)
    throws IOException
  {
    if (SIZE < _offset + 32)
      flush();

    _buffer[_offset++] = (byte) 'r';

    writeType(type);

    if (SIZE < _offset + 32)
      flush();

    _buffer[_offset++] = (byte) 'S';
    
    printLenString(url);
  }

  private void writeType(String type)
    throws IOException
  {
    if (type == null)
      return;

    int len = type.length();
    if (len == 0)
      return;

    if (_typeRefs == null)
      _typeRefs = new HashMap();

    Integer typeRefV = (Integer) _typeRefs.get(type);
    
    if (typeRefV != null) {
      int typeRef = typeRefV.intValue();
      
      flushIfFull();
      
      _buffer[_offset++] = (byte) TYPE_REF;
      
      writeInt(typeRef);
    }
    else {
      _typeRefs.put(type, Integer.valueOf(_typeRefs.size()));

      if (SIZE < _offset + 32)
	flush();
      
      _buffer[_offset++] = (byte) 't';
      
      printLenString(type);
    }
  }

  /**
   * Writes a boolean value to the stream.  The boolean will be written
   * with the following syntax:
   *
   * <code><pre>
   * T
   * F
   * </pre></code>
   *
   * @param value the boolean value to write.
   */
  public void writeBoolean(boolean value)
    throws IOException
  {
    if (SIZE < _offset + 16)
      flush();

    if (value)
      _buffer[_offset++] = (byte) 'T';
    else
      _buffer[_offset++] = (byte) 'F';
  }

  /**
   * Writes an integer value to the stream.  The integer will be written
   * with the following syntax:
   *
   * <code><pre>
   * I b32 b24 b16 b8
   * </pre></code>
   *
   * @param value the integer value to write.
   */
  public void writeInt(int value)
    throws IOException
  {
    int offset = _offset;
    byte []buffer = _buffer;

    if (SIZE <= offset + 16) {
      flush();
      offset = 0;
    }
    
    if (INT_DIRECT_MIN <= value && value <= INT_DIRECT_MAX)
      buffer[offset++] = (byte) (value + INT_ZERO);
    else if (INT_BYTE_MIN <= value && value <= INT_BYTE_MAX) {
      buffer[offset++] = (byte) (INT_BYTE_ZERO + (value >> 8));
      buffer[offset++] = (byte) (value);
    }
    else if (INT_SHORT_MIN <= value && value <= INT_SHORT_MAX) {
      buffer[offset++] = (byte) (INT_SHORT_ZERO + (value >> 16));
      buffer[offset++] = (byte) (value >> 8);
      buffer[offset++] = (byte) (value);
    }
    else {
      buffer[offset++] = (byte) ('I');
      buffer[offset++] = (byte) (value >> 24);
      buffer[offset++] = (byte) (value >> 16);
      buffer[offset++] = (byte) (value >> 8);
      buffer[offset++] = (byte) (value);
    }

    _offset = offset;
  }

  /**
   * Writes a long value to the stream.  The long will be written
   * with the following syntax:
   *
   * <code><pre>
   * L b64 b56 b48 b40 b32 b24 b16 b8
   * </pre></code>
   *
   * @param value the long value to write.
   */
  public void writeLong(long value)
    throws IOException
  {
    int offset = _offset;
    byte []buffer = _buffer;

    if (SIZE <= offset + 16) {
      flush();
      offset = 0;
    }

    if (LONG_DIRECT_MIN <= value && value <= LONG_DIRECT_MAX) {
      buffer[offset++] = (byte) (value + LONG_ZERO);
    }
    else if (LONG_BYTE_MIN <= value && value <= LONG_BYTE_MAX) {
      buffer[offset++] = (byte) (LONG_BYTE_ZERO + (value >> 8));
      buffer[offset++] = (byte) (value);
    }
    else if (LONG_SHORT_MIN <= value && value <= LONG_SHORT_MAX) {
      buffer[offset++] = (byte) (LONG_SHORT_ZERO + (value >> 16));
      buffer[offset++] = (byte) (value >> 8);
      buffer[offset++] = (byte) (value);
    }
    else if (-0x80000000L <= value && value <= 0x7fffffffL) {
      buffer[offset + 0] = (byte) LONG_INT;
      buffer[offset + 1] = (byte) (value >> 24);
      buffer[offset + 2] = (byte) (value >> 16);
      buffer[offset + 3] = (byte) (value >> 8);
      buffer[offset + 4] = (byte) (value);

      offset += 5;
    }
    else {
      buffer[offset + 0] = (byte) 'L';
      buffer[offset + 1] = (byte) (value >> 56);
      buffer[offset + 2] = (byte) (value >> 48);
      buffer[offset + 3] = (byte) (value >> 40);
      buffer[offset + 4] = (byte) (value >> 32);
      buffer[offset + 5] = (byte) (value >> 24);
      buffer[offset + 6] = (byte) (value >> 16);
      buffer[offset + 7] = (byte) (value >> 8);
      buffer[offset + 8] = (byte) (value);

      offset += 9;
    }

    _offset = offset;
  }

  /**
   * Writes a double value to the stream.  The double will be written
   * with the following syntax:
   *
   * <code><pre>
   * D b64 b56 b48 b40 b32 b24 b16 b8
   * </pre></code>
   *
   * @param value the double value to write.
   */
  public void writeDouble(double value)
    throws IOException
  {
    int offset = _offset;
    byte []buffer = _buffer;

    if (SIZE <= offset + 16) {
      flush();
      offset = 0;
    }
    
    int intValue = (int) value;
    
    if (intValue == value) {
      if (intValue == 0) {
	buffer[offset++] = (byte) DOUBLE_ZERO;

        _offset = offset;

        return;
      }
      else if (intValue == 1) {
	buffer[offset++] = (byte) DOUBLE_ONE;

        _offset = offset;

        return;
      }
      else if (-0x80 <= intValue && intValue < 0x80) {
	buffer[offset++] = (byte) DOUBLE_BYTE;
	buffer[offset++] = (byte) intValue;

        _offset = offset;

        return;
      }
      else if (-0x8000 <= intValue && intValue < 0x8000) {
	buffer[offset + 0] = (byte) DOUBLE_SHORT;
	buffer[offset + 1] = (byte) (intValue >> 8);
	buffer[offset + 2] = (byte) intValue;

	_offset = offset + 3;
        
        return;
      }
    }

    float f = (float) value;

    if (f == value) {
      int bits = Float.floatToIntBits(f);
      
      buffer[offset + 0] = (byte) (DOUBLE_FLOAT);
      buffer[offset + 1] = (byte) (bits >> 24);
      buffer[offset + 2] = (byte) (bits >> 16);
      buffer[offset + 3] = (byte) (bits >> 8);
      buffer[offset + 4] = (byte) (bits);

      _offset = offset + 5;

      return;
    }
    
    long bits = Double.doubleToLongBits(value);
    
    buffer[offset + 0] = (byte) 'D';
    buffer[offset + 1] = (byte) (bits >> 56);
    buffer[offset + 2] = (byte) (bits >> 48);
    buffer[offset + 3] = (byte) (bits >> 40);
    buffer[offset + 4] = (byte) (bits >> 32);
    buffer[offset + 5] = (byte) (bits >> 24);
    buffer[offset + 6] = (byte) (bits >> 16);
    buffer[offset + 7] = (byte) (bits >> 8);
    buffer[offset + 8] = (byte) (bits);

    _offset = offset + 9;
  }

  /**
   * Writes a date to the stream.
   *
   * <code><pre>
   * T  b64 b56 b48 b40 b32 b24 b16 b8
   * </pre></code>
   *
   * @param time the date in milliseconds from the epoch in UTC
   */
  public void writeUTCDate(long time)
    throws IOException
  {
    if (SIZE < _offset + 32)
      flush();

    int offset = _offset;
    byte []buffer = _buffer;
    
    buffer[offset++] = (byte) ('d');
    buffer[offset++] = ((byte) (time >> 56));
    buffer[offset++] = ((byte) (time >> 48));
    buffer[offset++] = ((byte) (time >> 40));
    buffer[offset++] = ((byte) (time >> 32));
    buffer[offset++] = ((byte) (time >> 24));
    buffer[offset++] = ((byte) (time >> 16));
    buffer[offset++] = ((byte) (time >> 8));
    buffer[offset++] = ((byte) (time));

    _offset = offset;
  }

  /**
   * Writes a null value to the stream.
   * The null will be written with the following syntax
   *
   * <code><pre>
   * N
   * </pre></code>
   *
   * @param value the string value to write.
   */
  public void writeNull()
    throws IOException
  {
    int offset = _offset;
    byte []buffer = _buffer;

    if (SIZE <= offset + 16) {
      flush();
      offset = 0;
    }

    buffer[offset++] = 'N';

    _offset = offset;
  }

  /**
   * Writes a string value to the stream using UTF-8 encoding.
   * The string will be written with the following syntax:
   *
   * <code><pre>
   * S b16 b8 string-value
   * </pre></code>
   *
   * If the value is null, it will be written as
   *
   * <code><pre>
   * N
   * </pre></code>
   *
   * @param value the string value to write.
   */
  public void writeString(String value)
    throws IOException
  {
    int offset = _offset;
    byte []buffer = _buffer;

    if (SIZE <= offset + 16) {
      flush();
      offset = 0;
    }
    
    if (value == null) {
      buffer[offset++] = (byte) 'N';

      _offset = offset;
    }
    else {
      int length = value.length();
      int strOffset = 0;
      
      while (length > 0x8000) {
        int sublen = 0x8000;

	offset = _offset;

	if (SIZE <= offset + 16) {
	  flush();
	  offset = 0;
	}

	// chunk can't end in high surrogate
	char tail = value.charAt(strOffset + sublen - 1);

	if (0xd800 <= tail && tail <= 0xdbff)
	  sublen--;

	buffer[offset + 0] = (byte) 's';
        buffer[offset + 1] = (byte) (sublen >> 8);
        buffer[offset + 2] = (byte) (sublen);

	_offset = offset + 3;

        printString(value, strOffset, sublen);

        length -= sublen;
        strOffset += sublen;
      }

      offset = _offset;

      if (SIZE <= offset + 16) {
	flush();
	offset = 0;
      }

      if (length <= STRING_DIRECT_MAX) {
	buffer[offset++] = (byte) (STRING_DIRECT + length);
      }
      else {
	buffer[offset++] = (byte) ('S');
	buffer[offset++] = (byte) (length >> 8);
	buffer[offset++] = (byte) (length);
      }

      _offset = offset;

      printString(value, strOffset, length);
    }
  }

  /**
   * Writes a string value to the stream using UTF-8 encoding.
   * The string will be written with the following syntax:
   *
   * <code><pre>
   * S b16 b8 string-value
   * </pre></code>
   *
   * If the value is null, it will be written as
   *
   * <code><pre>
   * N
   * </pre></code>
   *
   * @param value the string value to write.
   */
  public void writeString(char []buffer, int offset, int length)
    throws IOException
  {
    if (buffer == null) {
      if (SIZE < _offset + 16)
	flush();
      
      _buffer[_offset++] = (byte) ('N');
    }
    else {
      while (length > 0x8000) {
        int sublen = 0x8000;

	if (SIZE < _offset + 16)
	  flush();

	// chunk can't end in high surrogate
	char tail = buffer[offset + sublen - 1];

	if (0xd800 <= tail && tail <= 0xdbff)
	  sublen--;
	
        _buffer[_offset++] = (byte) 's';
        _buffer[_offset++] = (byte) (sublen >> 8);
        _buffer[_offset++] = (byte) (sublen);

        printString(buffer, offset, sublen);

        length -= sublen;
        offset += sublen;
      }

      if (SIZE < _offset + 16)
	flush();
	
      if (length <= STRING_DIRECT_MAX) {
	_buffer[_offset++] = (byte) (STRING_DIRECT + length);
      }
      else {
	_buffer[_offset++] = (byte) ('S');
	_buffer[_offset++] = (byte) (length >> 8);
	_buffer[_offset++] = (byte) (length);
      }

      printString(buffer, offset, length);
    }
  }

  /**
   * Writes a byte array to the stream.
   * The array will be written with the following syntax:
   *
   * <code><pre>
   * B b16 b18 bytes
   * </pre></code>
   *
   * If the value is null, it will be written as
   *
   * <code><pre>
   * N
   * </pre></code>
   *
   * @param value the string value to write.
   */
  public void writeBytes(byte []buffer)
    throws IOException
  {
    if (buffer == null) {
      if (SIZE < _offset + 16)
	flush();

      _buffer[_offset++] = 'N';
    }
    else
      writeBytes(buffer, 0, buffer.length);
  }
  
  /**
   * Writes a byte array to the stream.
   * The array will be written with the following syntax:
   *
   * <code><pre>
   * B b16 b18 bytes
   * </pre></code>
   *
   * If the value is null, it will be written as
   *
   * <code><pre>
   * N
   * </pre></code>
   *
   * @param value the string value to write.
   */
  public void writeBytes(byte []buffer, int offset, int length)
    throws IOException
  {
    if (buffer == null) {
      if (SIZE < _offset + 16)
	flushBuffer();
      
      _buffer[_offset++] = (byte) 'N';
    }
    else {
      flush();

      while (length > SIZE - _offset - 3) {
        int sublen = SIZE - _offset - 3;

        if (sublen < 16) {
          flushBuffer();

          sublen = SIZE - _offset - 3;

          if (length < sublen)
            sublen = length;
        }

        _buffer[_offset++] = (byte) 'b';
        _buffer[_offset++] = (byte) (sublen >> 8);
        _buffer[_offset++] = (byte) sublen;

        System.arraycopy(buffer, offset, _buffer, _offset, sublen);
        _offset += sublen;

        length -= sublen;
        offset += sublen;
      }

      if (SIZE < _offset + 16)
        flushBuffer();

      if (length < 0x10) {
        _buffer[_offset++] = (byte) (BYTES_DIRECT + length);
      }
      else {
        _buffer[_offset++] = (byte) 'B';
        _buffer[_offset++] = (byte) (length >> 8);
        _buffer[_offset++] = (byte) (length);
      }

      System.arraycopy(buffer, offset, _buffer, _offset, length);

      _offset += length;
    }
  }
  
  /**
   * Writes a byte buffer to the stream.
   *
   * <code><pre>
   * </pre></code>
   */
  public void writeByteBufferStart()
    throws IOException
  {
  }
  
  /**
   * Writes a byte buffer to the stream.
   *
   * <code><pre>
   * b b16 b18 bytes
   * </pre></code>
   */
  public void writeByteBufferPart(byte []buffer, int offset, int length)
    throws IOException
  {
    while (length > 0) {
      int sublen = length;

      if (0x8000 < sublen)
	sublen = 0x8000;

      flush(); // bypass buffer
      
      _os.write('b');
      _os.write(sublen >> 8);
      _os.write(sublen);

      _os.write(buffer, offset, sublen);

      length -= sublen;
      offset += sublen;
    }
  }
  
  /**
   * Writes a byte buffer to the stream.
   *
   * <code><pre>
   * b b16 b18 bytes
   * </pre></code>
   */
  public void writeByteBufferEnd(byte []buffer, int offset, int length)
    throws IOException
  {
    writeBytes(buffer, offset, length);
  }

  /**
   * Returns an output stream to write binary data.
   */
  public OutputStream getBytesOutputStream()
    throws IOException
  {
    return new BytesOutputStream();
  }

  /**
   * Writes a reference.
   *
   * <code><pre>
   * R b32 b24 b16 b8
   * </pre></code>
   *
   * @param value the integer value to write.
   */
  public void writeRef(int value)
    throws IOException
  {
    if (SIZE < _offset + 16)
      flush();
    
    if (value < 0x100) {
      _buffer[_offset++] = (byte) (REF_BYTE);
      _buffer[_offset++] = (byte) (value);
    }
    else if (value < 0x10000) {
      _buffer[_offset++] = (byte) (REF_SHORT);
      _buffer[_offset++] = (byte) (value >> 8);
      _buffer[_offset++] = (byte) (value);
    }
    else {
      _buffer[_offset++] = (byte) ('R');
      _buffer[_offset++] = (byte) (value >> 24);
      _buffer[_offset++] = (byte) (value >> 16);
      _buffer[_offset++] = (byte) (value >> 8);
      _buffer[_offset++] = (byte) (value);
    }
  }

  /**
   * If the object has already been written, just write its ref.
   *
   * @return true if we're writing a ref.
   */
  public boolean addRef(Object object)
    throws IOException
  {
    int ref = _refs.get(object);

    if (ref >= 0) {
      writeRef(ref);
      
      return true;
    }
    else {
      _refs.put(object, _refs.size());
      
      return false;
    }
  }

  /**
   * Removes a reference.
   */
  public boolean removeRef(Object obj)
    throws IOException
  {
    if (_refs != null) {
      _refs.remove(obj);

      return true;
    }
    else
      return false;
  }

  /**
   * Replaces a reference from one object to another.
   */
  public boolean replaceRef(Object oldRef, Object newRef)
    throws IOException
  {
    Integer value = (Integer) _refs.remove(oldRef);

    if (value != null) {
      _refs.put(newRef, value);
      return true;
    }
    else
      return false;
  }

  /**
   * Resets the references for streaming.
   */
  public void resetReferences()
  {
    if (_refs != null)
      _refs.clear();
  }

  /**
   * Starts the streaming message
   *
   * <p>A streaming message starts with 'P'</p>
   *
   * <pre>
   * P x02 x00
   * </pre>
   */
  public void writeStreamingObject(Object obj)
    throws IOException
  {
    if (_refs != null)
      _refs.clear();
    
    flush();

    _isStreaming = true;
    _offset = 3;

    writeObject(obj);

    int len = _offset - 3;
    
    _buffer[0] = (byte) 'P';
    _buffer[1] = (byte) (len >> 8);
    _buffer[2] = (byte) len;

    _isStreaming = false;

    flush();
  }

  /**
   * Prints a string to the stream, encoded as UTF-8 with preceeding length
   *
   * @param v the string to print.
   */
  public void printLenString(String v)
    throws IOException
  {
    if (SIZE < _offset + 16)
      flush();
    
    if (v == null) {
      _buffer[_offset++] = (byte) (0);
      _buffer[_offset++] = (byte) (0);
    }
    else {
      int len = v.length();
      _buffer[_offset++] = (byte) (len >> 8);
      _buffer[_offset++] = (byte) (len);

      printString(v, 0, len);
    }
  }

  /**
   * Prints a string to the stream, encoded as UTF-8
   *
   * @param v the string to print.
   */
  public void printString(String v)
    throws IOException
  {
    printString(v, 0, v.length());
  }
  
  /**
   * Prints a string to the stream, encoded as UTF-8
   *
   * @param v the string to print.
   */
  public void printString(String v, int strOffset, int length)
    throws IOException
  {
    int offset = _offset;
    byte []buffer = _buffer;
    
    for (int i = 0; i < length; i++) {
      if (SIZE <= offset + 16) {
	_offset = offset;
	flush();
	offset = 0;
      }
      
      char ch = v.charAt(i + strOffset);

      if (ch < 0x80)
        buffer[offset++] = (byte) (ch);
      else if (ch < 0x800) {
        buffer[offset++] = (byte) (0xc0 + ((ch >> 6) & 0x1f));
        buffer[offset++] = (byte) (0x80 + (ch & 0x3f));
      }
      else {
        buffer[offset++] = (byte) (0xe0 + ((ch >> 12) & 0xf));
        buffer[offset++] = (byte) (0x80 + ((ch >> 6) & 0x3f));
        buffer[offset++] = (byte) (0x80 + (ch & 0x3f));
      }
    }

    _offset = offset;
  }
  
  /**
   * Prints a string to the stream, encoded as UTF-8
   *
   * @param v the string to print.
   */
  public void printString(char []v, int strOffset, int length)
    throws IOException
  {
    int offset = _offset;
    byte []buffer = _buffer;
    
    for (int i = 0; i < length; i++) {
      if (SIZE <= offset + 16) {
	_offset = offset;
	flush();
	offset = 0;
      }
      
      char ch = v[i + strOffset];

      if (ch < 0x80)
        buffer[offset++] = (byte) (ch);
      else if (ch < 0x800) {
        buffer[offset++] = (byte) (0xc0 + ((ch >> 6) & 0x1f));
        buffer[offset++] = (byte) (0x80 + (ch & 0x3f));
      }
      else {
        buffer[offset++] = (byte) (0xe0 + ((ch >> 12) & 0xf));
        buffer[offset++] = (byte) (0x80 + ((ch >> 6) & 0x3f));
        buffer[offset++] = (byte) (0x80 + (ch & 0x3f));
      }
    }

    _offset = offset;
  }
  
  private final void flushIfFull()
    throws IOException
  {
    int offset = _offset;
    
    if (SIZE < offset + 32) {
      _offset = 0;
      _os.write(_buffer, 0, offset);
    }
  }

  public final void flush()
    throws IOException
  {
    flushBuffer();

    _os.flush();
  }

  public final void flushBuffer()
    throws IOException
  {
    int offset = _offset;

    if (! _isStreaming && offset > 0) {
      _offset = 0;
      
      _os.write(_buffer, 0, offset);
    }
    else if (_isStreaming && offset > 3) {
      int len = offset - 3;
      _buffer[0] = 'p';
      _buffer[1] = (byte) (len >> 8);
      _buffer[2] = (byte) len;
      _offset = 3;
      
      _os.write(_buffer, 0, offset);
    }
  }

  public final void close()
    throws IOException
  {
    flushBuffer();
    
    OutputStream os = _os;
    _os = null;

    if (os != null) {
      if (_isCloseStreamOnClose)
	os.close();
    }
  }

  class BytesOutputStream extends OutputStream {
    private int _startOffset;
    
    BytesOutputStream()
      throws IOException
    {
      if (SIZE < _offset + 16) {
        Hessian2Output.this.flush();
      }

      _startOffset = _offset;
      _offset += 3; // skip 'b' xNN xNN
    }

    @Override
    public void write(int ch)
      throws IOException
    {
      if (SIZE <= _offset) {
        int length = (_offset - _startOffset) - 3;

        _buffer[_startOffset] = (byte) 'b';
        _buffer[_startOffset + 1] = (byte) (length >> 8);
        _buffer[_startOffset + 2] = (byte) (length);

        Hessian2Output.this.flush();

        _startOffset = _offset;
        _offset += 3;
      }

      _buffer[_offset++] = (byte) ch;
    }

    @Override
    public void write(byte []buffer, int offset, int length)
      throws IOException
    {
      while (length > 0) {
        int sublen = SIZE - _offset;

        if (length < sublen)
          sublen = length;

        if (sublen > 0) {
          System.arraycopy(buffer, offset, _buffer, _offset, sublen);
          _offset += sublen;
        }

        length -= sublen;
        offset += sublen;

        if (SIZE <= _offset) {
          int chunkLength = (_offset - _startOffset) - 3;

          _buffer[_startOffset] = (byte) 'b';
          _buffer[_startOffset + 1] = (byte) (chunkLength >> 8);
          _buffer[_startOffset + 2] = (byte) (chunkLength);

          Hessian2Output.this.flush();

          _startOffset = _offset;
          _offset += 3;
        }
      }
    }

    @Override
    public void close()
      throws IOException
    {
      int startOffset = _startOffset;
      _startOffset = -1;

      if (startOffset < 0)
        return;

      int length = (_offset - startOffset) - 3;

      _buffer[startOffset] = (byte) 'B';
      _buffer[startOffset + 1] = (byte) (length >> 8);
      _buffer[startOffset + 2] = (byte) (length);

      Hessian2Output.this.flush();
    }
  }
}

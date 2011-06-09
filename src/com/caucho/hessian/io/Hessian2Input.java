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
 * 4. The names "Hessian", "Resin", and "Caucho" must not be used to
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

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.*;

/**
 * Input stream for Hessian requests.
 *
 * <p>HessianInput is unbuffered, so any client needs to provide
 * its own buffering.
 *
 * <pre>
 * InputStream is = ...; // from http connection
 * HessianInput in = new HessianInput(is);
 * String value;
 *
 * in.startReply();         // read reply header
 * value = in.readString(); // read string value
 * in.completeReply();      // read reply footer
 * </pre>
 */
public class Hessian2Input
  extends AbstractHessianInput
  implements Hessian2Constants
{
  private static final Logger log
    = Logger.getLogger(Hessian2Input.class.getName());
  
  private static final double D_256 = 1.0 / 256.0;
  private static final int END_OF_DATA = -2;

  private static Field _detailMessageField;

  private static final int SIZE = 256;
  private static final int GAP = 16;
  
  // factory for deserializing objects in the input stream
  protected SerializerFactory _serializerFactory;

  private static boolean _isCloseStreamOnClose;
  
  protected ArrayList _refs;
  protected ArrayList _classDefs;
  protected ArrayList _types;
  
  // the underlying input stream
  private InputStream _is;
  private final byte []_buffer = new byte[SIZE];
  
  // a peek character
  private int _offset;
  private int _length;

  // true for streaming data
  private boolean _isStreaming;
  
  // the method for a call
  private String _method;

  private Reader _chunkReader;
  private InputStream _chunkInputStream;

  private Throwable _replyFault;

  private StringBuffer _sbuf = new StringBuffer();
  
  // true if this is the last chunk
  private boolean _isLastChunk;
  // the chunk length
  private int _chunkLength;
  
  /**
   * Creates a new Hessian input stream, initialized with an
   * underlying input stream.
   *
   * @param is the underlying input stream.
   */
  public Hessian2Input(InputStream is)
  {
    _is = is;
  }

  /**
   * Sets the serializer factory.
   */
  public void setSerializerFactory(SerializerFactory factory)
  {
    _serializerFactory = factory;
  }

  /**
   * Gets the serializer factory.
   */
  public SerializerFactory getSerializerFactory()
  {
    return _serializerFactory;
  }

  /**
   * Gets the serializer factory, creating a default if necessary.
   */
  public final SerializerFactory findSerializerFactory()
  {
    SerializerFactory factory = _serializerFactory;

    if (factory == null)
      _serializerFactory = factory = new SerializerFactory();
    
    return factory;
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
   * Returns the calls method
   */
  public String getMethod()
  {
    return _method;
  }

  /**
   * Returns any reply fault.
   */
  public Throwable getReplyFault()
  {
    return _replyFault;
  }

  /**
   * Starts reading the call
   *
   * <pre>
   * c major minor
   * </pre>
   */
  public int readCall()
    throws IOException
  {
    int tag = read();
    
    if (tag != 'c')
      throw error("expected hessian call ('c') at " + codeName(tag));

    int major = read();
    int minor = read();

    return (major << 16) + minor;
  }

  /**
   * Starts reading the envelope
   *
   * <pre>
   * E major minor
   * </pre>
   */
  public int readEnvelope()
    throws IOException
  {
    int tag = read();
    
    if (tag != 'E')
      throw error("expected hessian Envelope ('E') at " + codeName(tag));

    int major = read();
    int minor = read();

    return (major << 16) + minor;
  }

  /**
   * Completes reading the envelope
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
    int tag = read();
    
    if (tag != 'z')
      error("expected end of envelope at " + codeName(tag));
  }

  /**
   * Starts reading the call
   *
   * <p>A successful completion will have a single value:
   *
   * <pre>
   * m b16 b8 method
   * </pre>
   */
  public String readMethod()
    throws IOException
  {
    int tag = read();
    
    if (tag != 'm')
      throw error("expected hessian method ('m') at " + codeName(tag));
    
    int d1 = read();
    int d2 = read();

    _isLastChunk = true;
    _chunkLength = d1 * 256 + d2;
    _sbuf.setLength(0);
    int ch;
    while ((ch = parseChar()) >= 0)
      _sbuf.append((char) ch);
    
    _method = _sbuf.toString();

    return _method;
  }

  /**
   * Starts reading the call, including the headers.
   *
   * <p>The call expects the following protocol data
   *
   * <pre>
   * c major minor
   * m b16 b8 method
   * </pre>
   */
  public void startCall()
    throws IOException
  {
    readCall();

    while (readHeader() != null) {
      readObject();
    }

    readMethod();
  }

  /**
   * Completes reading the call
   *
   * <p>A successful completion will have a single value:
   *
   * <pre>
   * z
   * </pre>
   */
  public void completeCall()
    throws IOException
  {
    int tag = read();

    if (tag == 'z') {
    }
    else if (tag < 0)
      throw error("expected end of call ('z') at end of stream.");
    else
      throw error("expected end of call ('z') at " + codeName(tag) + ".  Check method arguments and ensure method overloading is enabled if necessary");
  }

  /**
   * Reads a reply as an object.
   * If the reply has a fault, throws the exception.
   */
  @Override
  public Object readReply(Class expectedClass)
    throws Throwable
  {
    int tag = read();
    
    if (tag != 'r') {
      StringBuilder sb = new StringBuilder();
      sb.append((char) tag);
      
      try {
	int ch;

	while ((ch = read()) >= 0) {
	  sb.append((char) ch);
	}
      } catch (IOException e) {
	log.log(Level.FINE, e.toString(), e);
      }
      
      throw error("expected hessian reply at " + codeName(tag) + "\n"
		  + sb);
    }

    int major = read();
    int minor = read();

    if (major > 2 || major == 2 && minor > 0)
      throw error("Cannot understand Hessian " + major + "." + minor + " response");
    tag = read();
    if (tag == 'f')
      throw prepareFault();
    else {
      if (tag >= 0)
	_offset--;
    
      Object value = readObject(expectedClass);
      
      completeValueReply();
      
      return value;
    }
  }

  /**
   * Starts reading the reply
   *
   * <p>A successful completion will have a single value:
   *
   * <pre>
   * r
   * </pre>
   */
  public void startReply()
    throws Throwable
  {
    int tag = read();
    
    if (tag != 'r') {
      StringBuilder sb = new StringBuilder();
      sb.append((char) tag);
      
      try {
	int ch;

	while ((ch = read()) >= 0) {
	  sb.append((char) ch);
	}
      } catch (IOException e) {
	log.log(Level.FINE, e.toString(), e);
      }
      
      throw error("expected hessian reply at " + codeName(tag) + "\n"
		  + sb);
    }

    int major = read();
    int minor = read();

    if (major > 2 || major == 2 && minor > 0)
      throw error("Cannot understand Hessian " + major + "." + minor + " response");
    
    tag = read();
    if (tag == 'f')
      throw prepareFault();
    else if (tag >= 0)
      _offset--;
  }

  /**
   * Prepares the fault.
   */
  private Throwable prepareFault()
    throws IOException
  {
    HashMap fault = readFault();

    Object detail = fault.get("detail");
    String message = (String) fault.get("message");

    if (detail instanceof Throwable) {
      _replyFault = (Throwable) detail;
      
      if (message != null && _detailMessageField != null) {
	try {
	  _detailMessageField.set(_replyFault, message);
	} catch (Throwable e) {
	}
      }
	
      return _replyFault;
    }

    else {
      String code = (String) fault.get("code");
        
      _replyFault = new HessianServiceException(message, code, detail);

      return _replyFault;
    }
  }

  /**
   * Completes reading the call
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
    int tag = read();
    
    if (tag != 'z')
      error("expected end of reply at " + codeName(tag));
  }

  /**
   * Completes reading the call
   *
   * <p>A successful completion will have a single value:
   *
   * <pre>
   * z
   * </pre>
   */
  public void completeValueReply()
    throws IOException
  {
    int tag = read();
    
    if (tag != 'z')
      error("expected end of reply at " + codeName(tag));
  }

  /**
   * Reads a header, returning null if there are no headers.
   *
   * <pre>
   * H b16 b8 value
   * </pre>
   */
  public String readHeader()
    throws IOException
  {
    int tag = read();

    if (tag == 'H') {
      _isLastChunk = true;
      _chunkLength = (read() << 8) + read();

      _sbuf.setLength(0);
      int ch;
      while ((ch = parseChar()) >= 0)
        _sbuf.append((char) ch);

      return _sbuf.toString();
    }

    if (tag >= 0)
      _offset--;

    return null;
  }

  /**
   * Starts reading the message
   *
   * <pre>
   * p major minor
   * </pre>
   */
  public int startMessage()
    throws IOException
  {
    int tag = read();

    if (tag == 'p')
      _isStreaming = false;
    else if (tag == 'P')
      _isStreaming = true;
    else
      throw error("expected Hessian message ('p') at " + codeName(tag));

    int major = read();
    int minor = read();

    return (major << 16) + minor;
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
    int tag = read();
    
    if (tag != 'z')
      error("expected end of message at " + codeName(tag));
  }

  /**
   * Reads a null
   *
   * <pre>
   * N
   * </pre>
   */
  public void readNull()
    throws IOException
  {
    int tag = read();

    switch (tag) {
    case 'N': return;
      
    default:
      throw expect("null", tag);
    }
  }

  /**
   * Reads a boolean
   *
   * <pre>
   * T
   * F
   * </pre>
   */
  public boolean readBoolean()
    throws IOException
  {
    int tag = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

    switch (tag) {
    case 'T': return true;
    case 'F': return false;

      // direct integer
    case 0x80: case 0x81: case 0x82: case 0x83:
    case 0x84: case 0x85: case 0x86: case 0x87:
    case 0x88: case 0x89: case 0x8a: case 0x8b:
    case 0x8c: case 0x8d: case 0x8e: case 0x8f:
      
    case 0x90: case 0x91: case 0x92: case 0x93:
    case 0x94: case 0x95: case 0x96: case 0x97:
    case 0x98: case 0x99: case 0x9a: case 0x9b:
    case 0x9c: case 0x9d: case 0x9e: case 0x9f:
      
    case 0xa0: case 0xa1: case 0xa2: case 0xa3:
    case 0xa4: case 0xa5: case 0xa6: case 0xa7:
    case 0xa8: case 0xa9: case 0xaa: case 0xab:
    case 0xac: case 0xad: case 0xae: case 0xaf:

    case 0xb0: case 0xb1: case 0xb2: case 0xb3:
    case 0xb4: case 0xb5: case 0xb6: case 0xb7:
    case 0xb8: case 0xb9: case 0xba: case 0xbb:
    case 0xbc: case 0xbd: case 0xbe: case 0xbf:
      return tag != INT_ZERO;

      // INT_BYTE = 0
    case 0xc8: 
      return read() != 0;
      
      // INT_BYTE != 0
    case 0xc0: case 0xc1: case 0xc2: case 0xc3:
    case 0xc4: case 0xc5: case 0xc6: case 0xc7:
    case 0xc9: case 0xca: case 0xcb:
    case 0xcc: case 0xcd: case 0xce: case 0xcf:
      read();
      return true;

      // INT_SHORT = 0
    case 0xd4: 
      return (256 * read() + read()) != 0;
      
      // INT_SHORT != 0
    case 0xd0: case 0xd1: case 0xd2: case 0xd3:
    case 0xd5: case 0xd6: case 0xd7:
      read();
      read();
      return true;
      
    case 'I': return
	parseInt() != 0;
      
    case 0xd8: case 0xd9: case 0xda: case 0xdb:
    case 0xdc: case 0xdd: case 0xde: case 0xdf:
      
    case 0xe0: case 0xe1: case 0xe2: case 0xe3:
    case 0xe4: case 0xe5: case 0xe6: case 0xe7:
    case 0xe8: case 0xe9: case 0xea: case 0xeb:
    case 0xec: case 0xed: case 0xee: case 0xef:
      return tag != LONG_ZERO;

      // LONG_BYTE = 0
    case 0xf8: 
      return read() != 0;
      
      // LONG_BYTE != 0
    case 0xf0: case 0xf1: case 0xf2: case 0xf3:
    case 0xf4: case 0xf5: case 0xf6: case 0xf7:
    case 0xf9: case 0xfa: case 0xfb:
    case 0xfc: case 0xfd: case 0xfe: case 0xff:
      read();
      return true;

      // INT_SHORT = 0
    case 0x3c: 
      return (256 * read() + read()) != 0;
      
      // INT_SHORT != 0
    case 0x38: case 0x39: case 0x3a: case 0x3b:
    case 0x3d: case 0x3e: case 0x3f:
      read();
      read();
      return true;

    case LONG_INT:
      return (0x1000000L * read()
	      + 0x10000L * read()
	      + 0x100 * read()
	      + read()) != 0;
      
    case 'L':
      return parseLong() != 0;

    case DOUBLE_ZERO:
      return false;
      
    case DOUBLE_ONE:
      return true;
      
    case DOUBLE_BYTE:
      return read() != 0;
      
    case DOUBLE_SHORT:
      return (0x100 * read() + read()) != 0;
      
    case DOUBLE_FLOAT:
      {
	int f = parseInt();

	return Float.intBitsToFloat(f) != 0;
      }
      
    case 'D':
      return parseDouble() != 0.0;
      
    case 'N':
      return false;
      
    default:
      throw expect("boolean", tag);
    }
  }

  /**
   * Reads a short
   *
   * <pre>
   * I b32 b24 b16 b8
   * </pre>
   */
  public short readShort()
    throws IOException
  {
    return (short) readInt();
  }

  /**
   * Reads an integer
   *
   * <pre>
   * I b32 b24 b16 b8
   * </pre>
   */
  public final int readInt()
    throws IOException
  {
    //int tag = _offset < _length ? (_buffer[_offset++] & 0xff) : read();
    int tag = read();

    switch (tag) {
    case 'N':
      return 0;
      
    case 'F':
      return 0;
      
    case 'T':
      return 1;

      // direct integer
    case 0x80: case 0x81: case 0x82: case 0x83:
    case 0x84: case 0x85: case 0x86: case 0x87:
    case 0x88: case 0x89: case 0x8a: case 0x8b:
    case 0x8c: case 0x8d: case 0x8e: case 0x8f:
      
    case 0x90: case 0x91: case 0x92: case 0x93:
    case 0x94: case 0x95: case 0x96: case 0x97:
    case 0x98: case 0x99: case 0x9a: case 0x9b:
    case 0x9c: case 0x9d: case 0x9e: case 0x9f:
      
    case 0xa0: case 0xa1: case 0xa2: case 0xa3:
    case 0xa4: case 0xa5: case 0xa6: case 0xa7:
    case 0xa8: case 0xa9: case 0xaa: case 0xab:
    case 0xac: case 0xad: case 0xae: case 0xaf:
      
    case 0xb0: case 0xb1: case 0xb2: case 0xb3:
    case 0xb4: case 0xb5: case 0xb6: case 0xb7:
    case 0xb8: case 0xb9: case 0xba: case 0xbb:
    case 0xbc: case 0xbd: case 0xbe: case 0xbf:
      return tag - INT_ZERO;

      /* byte int */
    case 0xc0: case 0xc1: case 0xc2: case 0xc3:
    case 0xc4: case 0xc5: case 0xc6: case 0xc7:
    case 0xc8: case 0xc9: case 0xca: case 0xcb:
    case 0xcc: case 0xcd: case 0xce: case 0xcf:
      return ((tag - INT_BYTE_ZERO) << 8) + read();
      
      /* short int */
    case 0xd0: case 0xd1: case 0xd2: case 0xd3:
    case 0xd4: case 0xd5: case 0xd6: case 0xd7:
      return ((tag - INT_SHORT_ZERO) << 16) + 256 * read() + read();

    case 'I':
    case LONG_INT:
      return ((read() << 24)
	      + (read() << 16)
	      + (read() << 8)
	      + read());

      // direct long
    case 0xd8: case 0xd9: case 0xda: case 0xdb:
    case 0xdc: case 0xdd: case 0xde: case 0xdf:
      
    case 0xe0: case 0xe1: case 0xe2: case 0xe3:
    case 0xe4: case 0xe5: case 0xe6: case 0xe7:
    case 0xe8: case 0xe9: case 0xea: case 0xeb:
    case 0xec: case 0xed: case 0xee: case 0xef:
      return tag - LONG_ZERO;

      /* byte long */
    case 0xf0: case 0xf1: case 0xf2: case 0xf3:
    case 0xf4: case 0xf5: case 0xf6: case 0xf7:
    case 0xf8: case 0xf9: case 0xfa: case 0xfb:
    case 0xfc: case 0xfd: case 0xfe: case 0xff:
      return ((tag - LONG_BYTE_ZERO) << 8) + read();
      
      /* short long */
    case 0x38: case 0x39: case 0x3a: case 0x3b:
    case 0x3c: case 0x3d: case 0x3e: case 0x3f:
      return ((tag - LONG_SHORT_ZERO) << 16) + 256 * read() + read();

    case 'L':
      return (int) parseLong();

    case DOUBLE_ZERO:
      return 0;

    case DOUBLE_ONE:
      return 1;

      //case LONG_BYTE:
    case DOUBLE_BYTE:
      return (byte) (_offset < _length ? _buffer[_offset++] : read());

      //case INT_SHORT:
      //case LONG_SHORT:
    case DOUBLE_SHORT:
      return (short) (256 * read() + read());

    case DOUBLE_FLOAT:
      {
	int f = parseInt();

	return (int) Float.intBitsToFloat(f);
      }

    case 'D':
      return (int) parseDouble();
      
    default:
      throw expect("integer", tag);
    }
  }

  /**
   * Reads a long
   *
   * <pre>
   * L b64 b56 b48 b40 b32 b24 b16 b8
   * </pre>
   */
  public long readLong()
    throws IOException
  {
    int tag = read();

    switch (tag) {
    case 'N':
      return 0;
      
    case 'F':
      return 0;
      
    case 'T':
      return 1;

      // direct integer
    case 0x80: case 0x81: case 0x82: case 0x83:
    case 0x84: case 0x85: case 0x86: case 0x87:
    case 0x88: case 0x89: case 0x8a: case 0x8b:
    case 0x8c: case 0x8d: case 0x8e: case 0x8f:
      
    case 0x90: case 0x91: case 0x92: case 0x93:
    case 0x94: case 0x95: case 0x96: case 0x97:
    case 0x98: case 0x99: case 0x9a: case 0x9b:
    case 0x9c: case 0x9d: case 0x9e: case 0x9f:
      
    case 0xa0: case 0xa1: case 0xa2: case 0xa3:
    case 0xa4: case 0xa5: case 0xa6: case 0xa7:
    case 0xa8: case 0xa9: case 0xaa: case 0xab:
    case 0xac: case 0xad: case 0xae: case 0xaf:
      
    case 0xb0: case 0xb1: case 0xb2: case 0xb3:
    case 0xb4: case 0xb5: case 0xb6: case 0xb7:
    case 0xb8: case 0xb9: case 0xba: case 0xbb:
    case 0xbc: case 0xbd: case 0xbe: case 0xbf:
      return tag - INT_ZERO;

      /* byte int */
    case 0xc0: case 0xc1: case 0xc2: case 0xc3:
    case 0xc4: case 0xc5: case 0xc6: case 0xc7:
    case 0xc8: case 0xc9: case 0xca: case 0xcb:
    case 0xcc: case 0xcd: case 0xce: case 0xcf:
      return ((tag - INT_BYTE_ZERO) << 8) + read();
      
      /* short int */
    case 0xd0: case 0xd1: case 0xd2: case 0xd3:
    case 0xd4: case 0xd5: case 0xd6: case 0xd7:
      return ((tag - INT_SHORT_ZERO) << 16) + 256 * read() + read();

      //case LONG_BYTE:
    case DOUBLE_BYTE:
      return (byte) (_offset < _length ? _buffer[_offset++] : read());

      //case INT_SHORT:
      //case LONG_SHORT:
    case DOUBLE_SHORT:
      return (short) (256 * read() + read());

    case 'I':
    case LONG_INT:
      return parseInt();

      // direct long
    case 0xd8: case 0xd9: case 0xda: case 0xdb:
    case 0xdc: case 0xdd: case 0xde: case 0xdf:
      
    case 0xe0: case 0xe1: case 0xe2: case 0xe3:
    case 0xe4: case 0xe5: case 0xe6: case 0xe7:
    case 0xe8: case 0xe9: case 0xea: case 0xeb:
    case 0xec: case 0xed: case 0xee: case 0xef:
      return tag - LONG_ZERO;

      /* byte long */
    case 0xf0: case 0xf1: case 0xf2: case 0xf3:
    case 0xf4: case 0xf5: case 0xf6: case 0xf7:
    case 0xf8: case 0xf9: case 0xfa: case 0xfb:
    case 0xfc: case 0xfd: case 0xfe: case 0xff:
      return ((tag - LONG_BYTE_ZERO) << 8) + read();
      
      /* short long */
    case 0x38: case 0x39: case 0x3a: case 0x3b:
    case 0x3c: case 0x3d: case 0x3e: case 0x3f:
      return ((tag - LONG_SHORT_ZERO) << 16) + 256 * read() + read();

    case 'L':
      return parseLong();

    case DOUBLE_ZERO:
      return 0;

    case DOUBLE_ONE:
      return 1;

    case DOUBLE_FLOAT:
      {
	int f = parseInt();

	return (long) Float.intBitsToFloat(f);
      }

    case 'D':
      return (long) parseDouble();
      
    default:
      throw expect("long", tag);
    }
  }

  /**
   * Reads a float
   *
   * <pre>
   * D b64 b56 b48 b40 b32 b24 b16 b8
   * </pre>
   */
  public float readFloat()
    throws IOException
  {
    return (float) readDouble();
  }

  /**
   * Reads a double
   *
   * <pre>
   * D b64 b56 b48 b40 b32 b24 b16 b8
   * </pre>
   */
  public double readDouble()
    throws IOException
  {
    int tag = read();

    switch (tag) {
    case 'N':
      return 0;
      
    case 'F':
      return 0;
      
    case 'T':
      return 1;

      // direct integer
    case 0x80: case 0x81: case 0x82: case 0x83:
    case 0x84: case 0x85: case 0x86: case 0x87:
    case 0x88: case 0x89: case 0x8a: case 0x8b:
    case 0x8c: case 0x8d: case 0x8e: case 0x8f:
      
    case 0x90: case 0x91: case 0x92: case 0x93:
    case 0x94: case 0x95: case 0x96: case 0x97:
    case 0x98: case 0x99: case 0x9a: case 0x9b:
    case 0x9c: case 0x9d: case 0x9e: case 0x9f:
      
    case 0xa0: case 0xa1: case 0xa2: case 0xa3:
    case 0xa4: case 0xa5: case 0xa6: case 0xa7:
    case 0xa8: case 0xa9: case 0xaa: case 0xab:
    case 0xac: case 0xad: case 0xae: case 0xaf:
      
    case 0xb0: case 0xb1: case 0xb2: case 0xb3:
    case 0xb4: case 0xb5: case 0xb6: case 0xb7:
    case 0xb8: case 0xb9: case 0xba: case 0xbb:
    case 0xbc: case 0xbd: case 0xbe: case 0xbf:
      return tag - 0x90;

      /* byte int */
    case 0xc0: case 0xc1: case 0xc2: case 0xc3:
    case 0xc4: case 0xc5: case 0xc6: case 0xc7:
    case 0xc8: case 0xc9: case 0xca: case 0xcb:
    case 0xcc: case 0xcd: case 0xce: case 0xcf:
      return ((tag - INT_BYTE_ZERO) << 8) + read();
      
      /* short int */
    case 0xd0: case 0xd1: case 0xd2: case 0xd3:
    case 0xd4: case 0xd5: case 0xd6: case 0xd7:
      return ((tag - INT_SHORT_ZERO) << 16) + 256 * read() + read();

    case 'I':
    case LONG_INT:
      return parseInt();

      // direct long
    case 0xd8: case 0xd9: case 0xda: case 0xdb:
    case 0xdc: case 0xdd: case 0xde: case 0xdf:
      
    case 0xe0: case 0xe1: case 0xe2: case 0xe3:
    case 0xe4: case 0xe5: case 0xe6: case 0xe7:
    case 0xe8: case 0xe9: case 0xea: case 0xeb:
    case 0xec: case 0xed: case 0xee: case 0xef:
      return tag - LONG_ZERO;

      /* byte long */
    case 0xf0: case 0xf1: case 0xf2: case 0xf3:
    case 0xf4: case 0xf5: case 0xf6: case 0xf7:
    case 0xf8: case 0xf9: case 0xfa: case 0xfb:
    case 0xfc: case 0xfd: case 0xfe: case 0xff:
      return ((tag - LONG_BYTE_ZERO) << 8) + read();
      
      /* short long */
    case 0x38: case 0x39: case 0x3a: case 0x3b:
    case 0x3c: case 0x3d: case 0x3e: case 0x3f:
      return ((tag - LONG_SHORT_ZERO) << 16) + 256 * read() + read();

    case 'L':
      return (double) parseLong();

    case DOUBLE_ZERO:
      return 0;

    case DOUBLE_ONE:
      return 1;

    case DOUBLE_BYTE:
      return (byte) (_offset < _length ? _buffer[_offset++] : read());

    case DOUBLE_SHORT:
      return (short) (256 * read() + read());

    case DOUBLE_FLOAT:
      {
	int f = parseInt();

	return Float.intBitsToFloat(f);
      }
      
    case 'D':
      return parseDouble();
      
    default:
      throw expect("double", tag);
    }
  }

  /**
   * Reads a date.
   *
   * <pre>
   * T b64 b56 b48 b40 b32 b24 b16 b8
   * </pre>
   */
  public long readUTCDate()
    throws IOException
  {
    int tag = read();

    if (tag != 'd')
      throw expect("date", tag);

    long b64 = read();
    long b56 = read();
    long b48 = read();
    long b40 = read();
    long b32 = read();
    long b24 = read();
    long b16 = read();
    long b8 = read();

    return ((b64 << 56)
	    + (b56 << 48)
	    + (b48 << 40)
	    + (b40 << 32)
	    + (b32 << 24)
	    + (b24 << 16)
	    + (b16 << 8)
	    + b8);
  }

  /**
   * Reads a byte from the stream.
   */
  public int readChar()
    throws IOException
  {
    if (_chunkLength > 0) {
      _chunkLength--;
      if (_chunkLength == 0 && _isLastChunk)
        _chunkLength = END_OF_DATA;

      int ch = parseUTF8Char();
      return ch;
    }
    else if (_chunkLength == END_OF_DATA) {
      _chunkLength = 0;
      return -1;
    }
    
    int tag = read();

    switch (tag) {
    case 'N':
      return -1;

    case 'S':
    case 's':
    case 'X':
    case 'x':
      _isLastChunk = tag == 'S' || tag == 'X';
      _chunkLength = (read() << 8) + read();

      _chunkLength--;
      int value = parseUTF8Char();

      // special code so successive read byte won't
      // be read as a single object.
      if (_chunkLength == 0 && _isLastChunk)
        _chunkLength = END_OF_DATA;

      return value;
      
    default:
      throw expect("char", tag);
    }
  }

  /**
   * Reads a byte array from the stream.
   */
  public int readString(char []buffer, int offset, int length)
    throws IOException
  {
    int readLength = 0;

    if (_chunkLength == END_OF_DATA) {
      _chunkLength = 0;
      return -1;
    }
    else if (_chunkLength == 0) {
      int tag = read();

      switch (tag) {
      case 'N':
        return -1;
      
      case 'S':
      case 's':
      case 'X':
      case 'x':
        _isLastChunk = tag == 'S' || tag == 'X';
        _chunkLength = (read() << 8) + read();
        break;

      case 0x00: case 0x01: case 0x02: case 0x03:
      case 0x04: case 0x05: case 0x06: case 0x07:
      case 0x08: case 0x09: case 0x0a: case 0x0b:
      case 0x0c: case 0x0d: case 0x0e: case 0x0f:

      case 0x10: case 0x11: case 0x12: case 0x13:
      case 0x14: case 0x15: case 0x16: case 0x17:
      case 0x18: case 0x19: case 0x1a: case 0x1b:
      case 0x1c: case 0x1d: case 0x1e: case 0x1f:
	_isLastChunk = true;
	_chunkLength = tag - 0x00;
	break;

      default:
        throw expect("string", tag);
      }
    }

    while (length > 0) {
      if (_chunkLength > 0) {
        buffer[offset++] = (char) parseUTF8Char();
        _chunkLength--;
        length--;
        readLength++;
      }
      else if (_isLastChunk) {
        if (readLength == 0)
          return -1;
        else {
          _chunkLength = END_OF_DATA;
          return readLength;
        }
      }
      else {
        int tag = read();

        switch (tag) {
        case 'S':
        case 's':
        case 'X':
        case 'x':
          _isLastChunk = tag == 'S' || tag == 'X';
          _chunkLength = (read() << 8) + read();
          break;
      
        default:
          throw expect("string", tag);
        }
      }
    }
    
    if (readLength == 0)
      return -1;
    else if (_chunkLength > 0 || ! _isLastChunk)
      return readLength;
    else {
      _chunkLength = END_OF_DATA;
      return readLength;
    }
  }

  /**
   * Reads a string
   *
   * <pre>
   * S b16 b8 string value
   * </pre>
   */
  public String readString()
    throws IOException
  {
    int tag = read();

    switch (tag) {
    case 'N':
      return null;
    case 'T':
      return "true";
    case 'F':
      return "false";

      // direct integer
    case 0x80: case 0x81: case 0x82: case 0x83:
    case 0x84: case 0x85: case 0x86: case 0x87:
    case 0x88: case 0x89: case 0x8a: case 0x8b:
    case 0x8c: case 0x8d: case 0x8e: case 0x8f:
      
    case 0x90: case 0x91: case 0x92: case 0x93:
    case 0x94: case 0x95: case 0x96: case 0x97:
    case 0x98: case 0x99: case 0x9a: case 0x9b:
    case 0x9c: case 0x9d: case 0x9e: case 0x9f:
      
    case 0xa0: case 0xa1: case 0xa2: case 0xa3:
    case 0xa4: case 0xa5: case 0xa6: case 0xa7:
    case 0xa8: case 0xa9: case 0xaa: case 0xab:
    case 0xac: case 0xad: case 0xae: case 0xaf:
      
    case 0xb0: case 0xb1: case 0xb2: case 0xb3:
    case 0xb4: case 0xb5: case 0xb6: case 0xb7:
    case 0xb8: case 0xb9: case 0xba: case 0xbb:
    case 0xbc: case 0xbd: case 0xbe: case 0xbf:
      return String.valueOf((tag - 0x90));

      /* byte int */
    case 0xc0: case 0xc1: case 0xc2: case 0xc3:
    case 0xc4: case 0xc5: case 0xc6: case 0xc7:
    case 0xc8: case 0xc9: case 0xca: case 0xcb:
    case 0xcc: case 0xcd: case 0xce: case 0xcf:
      return String.valueOf(((tag - INT_BYTE_ZERO) << 8) + read());
      
      /* short int */
    case 0xd0: case 0xd1: case 0xd2: case 0xd3:
    case 0xd4: case 0xd5: case 0xd6: case 0xd7:
      return String.valueOf(((tag - INT_SHORT_ZERO) << 16)
			    + 256 * read() + read());

    case 'I':
    case LONG_INT:
      return String.valueOf(parseInt());

      // direct long
    case 0xd8: case 0xd9: case 0xda: case 0xdb:
    case 0xdc: case 0xdd: case 0xde: case 0xdf:
      
    case 0xe0: case 0xe1: case 0xe2: case 0xe3:
    case 0xe4: case 0xe5: case 0xe6: case 0xe7:
    case 0xe8: case 0xe9: case 0xea: case 0xeb:
    case 0xec: case 0xed: case 0xee: case 0xef:
      return String.valueOf(tag - LONG_ZERO);

      /* byte long */
    case 0xf0: case 0xf1: case 0xf2: case 0xf3:
    case 0xf4: case 0xf5: case 0xf6: case 0xf7:
    case 0xf8: case 0xf9: case 0xfa: case 0xfb:
    case 0xfc: case 0xfd: case 0xfe: case 0xff:
      return String.valueOf(((tag - LONG_BYTE_ZERO) << 8) + read());
      
      /* short long */
    case 0x38: case 0x39: case 0x3a: case 0x3b:
    case 0x3c: case 0x3d: case 0x3e: case 0x3f:
      return String.valueOf(((tag - LONG_SHORT_ZERO) << 16)
			    + 256 * read() + read());

    case 'L':
      return String.valueOf(parseLong());

    case DOUBLE_ZERO:
      return "0.0";

    case DOUBLE_ONE:
      return "1.0";

    case DOUBLE_BYTE:
      return String.valueOf((byte) (_offset < _length
				    ? _buffer[_offset++]
				    : read()));

    case DOUBLE_SHORT:
      return String.valueOf(((short) (256 * read() + read())));

    case DOUBLE_FLOAT:
      {
	int f = parseInt();

	return String.valueOf(Float.intBitsToFloat(f));
      }
      
    case 'D':
      return String.valueOf(parseDouble());

    case 'S':
    case 's':
    case 'X':
    case 'x':
      _isLastChunk = tag == 'S' || tag == 'X';
      _chunkLength = (read() << 8) + read();

      _sbuf.setLength(0);
      int ch;

      while ((ch = parseChar()) >= 0)
        _sbuf.append((char) ch);

      return _sbuf.toString();

      // 0-byte string
    case 0x00: case 0x01: case 0x02: case 0x03:
    case 0x04: case 0x05: case 0x06: case 0x07:
    case 0x08: case 0x09: case 0x0a: case 0x0b:
    case 0x0c: case 0x0d: case 0x0e: case 0x0f:

    case 0x10: case 0x11: case 0x12: case 0x13:
    case 0x14: case 0x15: case 0x16: case 0x17:
    case 0x18: case 0x19: case 0x1a: case 0x1b:
    case 0x1c: case 0x1d: case 0x1e: case 0x1f:
      _isLastChunk = true;
      _chunkLength = tag - 0x00;

      _sbuf.setLength(0);

      while ((ch = parseChar()) >= 0)
        _sbuf.append((char) ch);

      return _sbuf.toString();

    default:
      throw expect("string", tag);
    }
  }

  /**
   * Reads an XML node.
   *
   * <pre>
   * S b16 b8 string value
   * </pre>
   */
  public org.w3c.dom.Node readNode()
    throws IOException
  {
    int tag = read();

    switch (tag) {
    case 'N':
      return null;

    case 'S':
    case 's':
    case 'X':
    case 'x':
      _isLastChunk = tag == 'S' || tag == 'X';
      _chunkLength = (read() << 8) + read();

      throw error("XML is not supported");

    case 0x00: case 0x01: case 0x02: case 0x03:
    case 0x04: case 0x05: case 0x06: case 0x07:
    case 0x08: case 0x09: case 0x0a: case 0x0b:
    case 0x0c: case 0x0d: case 0x0e: case 0x0f:

    case 0x10: case 0x11: case 0x12: case 0x13:
    case 0x14: case 0x15: case 0x16: case 0x17:
    case 0x18: case 0x19: case 0x1a: case 0x1b:
    case 0x1c: case 0x1d: case 0x1e: case 0x1f:
      _isLastChunk = true;
      _chunkLength = tag - 0x00;

      throw error("XML is not supported");

    default:
      throw expect("string", tag);
    }
  }

  /**
   * Reads a byte array
   *
   * <pre>
   * B b16 b8 data value
   * </pre>
   */
  public byte []readBytes()
    throws IOException
  {
    int tag = read();

    switch (tag) {
    case 'N':
      return null;

    case 'B':
    case 'b':
      _isLastChunk = tag == 'B';
      _chunkLength = (read() << 8) + read();

      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      int data;
      while ((data = parseByte()) >= 0)
        bos.write(data);

      return bos.toByteArray();

    case 0x20: case 0x21: case 0x22: case 0x23:
    case 0x24: case 0x25: case 0x26: case 0x27:
    case 0x28: case 0x29: case 0x2a: case 0x2b:
    case 0x2c: case 0x2d: case 0x2e: case 0x2f:
      _isLastChunk = true;
      _chunkLength = tag - 0x20;

      bos = new ByteArrayOutputStream();

      while ((data = parseByte()) >= 0)
        bos.write(data);

      return bos.toByteArray();
      
    default:
      throw expect("bytes", tag);
    }
  }

  /**
   * Reads a byte from the stream.
   */
  public int readByte()
    throws IOException
  {
    if (_chunkLength > 0) {
      _chunkLength--;
      if (_chunkLength == 0 && _isLastChunk)
        _chunkLength = END_OF_DATA;

      return read();
    }
    else if (_chunkLength == END_OF_DATA) {
      _chunkLength = 0;
      return -1;
    }
    
    int tag = read();

    switch (tag) {
    case 'N':
      return -1;

    case 'B':
    case 'b':
      _isLastChunk = tag == 'B';
      _chunkLength = (read() << 8) + read();

      int value = parseByte();

      // special code so successive read byte won't
      // be read as a single object.
      if (_chunkLength == 0 && _isLastChunk)
        _chunkLength = END_OF_DATA;

      return value;
      
    default:
      throw expect("binary", tag);
    }
  }

  /**
   * Reads a byte array from the stream.
   */
  public int readBytes(byte []buffer, int offset, int length)
    throws IOException
  {
    int readLength = 0;

    if (_chunkLength == END_OF_DATA) {
      _chunkLength = 0;
      return -1;
    }
    else if (_chunkLength == 0) {
      int tag = read();

      switch (tag) {
      case 'N':
        return -1;
      
      case 'B':
      case 'b':
        _isLastChunk = tag == 'B';
        _chunkLength = (read() << 8) + read();
        break;
      
      default:
        throw expect("binary", tag);
      }
    }

    while (length > 0) {
      if (_chunkLength > 0) {
        buffer[offset++] = (byte) read();
        _chunkLength--;
        length--;
        readLength++;
      }
      else if (_isLastChunk) {
        if (readLength == 0)
          return -1;
        else {
          _chunkLength = END_OF_DATA;
          return readLength;
        }
      }
      else {
        int tag = read();

        switch (tag) {
        case 'B':
        case 'b':
          _isLastChunk = tag == 'B';
          _chunkLength = (read() << 8) + read();
          break;
      
        default:
          throw expect("binary", tag);
        }
      }
    }
    
    if (readLength == 0)
      return -1;
    else if (_chunkLength > 0 || ! _isLastChunk)
      return readLength;
    else {
      _chunkLength = END_OF_DATA;
      return readLength;
    }
  }

  /**
   * Reads a fault.
   */
  private HashMap readFault()
    throws IOException
  {
    HashMap map = new HashMap();

    int code = read();
    for (; code > 0 && code != 'z'; code = read()) {
      _offset--;
      
      Object key = readObject();
      Object value = readObject();

      if (key != null && value != null)
        map.put(key, value);
    }

    if (code != 'z')
      throw expect("fault", code);

    return map;
  }

  /**
   * Reads an object from the input stream with an expected type.
   */
  public Object readObject(Class cl)
    throws IOException
  {
    if (cl == null || cl == Object.class)
      return readObject();
    
    int tag = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

    switch (tag) {
    case 'N':
      return null;

    case 'M':
    {
      String type = readType();

      // hessian/3bb3
      if ("".equals(type)) {
	Deserializer reader;
	reader = findSerializerFactory().getDeserializer(cl);

	return reader.readMap(this);
      }
      else {
	Deserializer reader;
	reader = findSerializerFactory().getObjectDeserializer(type, cl);

	return reader.readMap(this);
      }
    }

    case 'O':
    {
      readObjectDefinition(cl);

      return readObject(cl);
    }

    case 'o':
    {
      int ref = readInt();
      int size = _classDefs.size();

      if (ref < 0 || size <= ref)
	throw new HessianProtocolException("'" + ref + "' is an unknown class definition");

      ObjectDefinition def = (ObjectDefinition) _classDefs.get(ref);

      return readObjectInstance(cl, def);
    }

    case 'V':
    {
      String type = readType();
      int length = readLength();
      
      Deserializer reader;
      reader = findSerializerFactory().getListDeserializer(type, cl);

      Object v = reader.readList(this, length);

      return v;
    }

    case 'v':
    {
      int ref = readInt();
      String type = (String) _types.get(ref);
      int length = readInt();
      
      Deserializer reader;
      reader = findSerializerFactory().getListDeserializer(type, cl);

      Object v = reader.readLengthList(this, length);

      return v;
    }

    case 'R':
    {
      int ref = parseInt();

      return _refs.get(ref);
    }

    case 'r':
    {
      String type = readType();
      String url = readString();

      return resolveRemote(type, url);
    }

    case REF_BYTE: {
      int ref = read();

      return _refs.get(ref);
    }

    case REF_SHORT: {
      int ref = 256 * read() + read();

      return _refs.get(ref);
    }
    }

    if (tag >= 0)
      _offset--;

    // hessian/3b2i vs hessian/3406
    // return readObject();

    Object value = findSerializerFactory().getDeserializer(cl).readObject(this);
    return value;
  }
  
  /**
   * Reads an arbitrary object from the input stream when the type
   * is unknown.
   */
  public Object readObject()
    throws IOException
  {
    int tag = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

    switch (tag) {
    case 'N':
      return null;
      
    case 'T':
      return Boolean.valueOf(true);
      
    case 'F':
      return Boolean.valueOf(false);

      // direct integer
    case 0x80: case 0x81: case 0x82: case 0x83:
    case 0x84: case 0x85: case 0x86: case 0x87:
    case 0x88: case 0x89: case 0x8a: case 0x8b:
    case 0x8c: case 0x8d: case 0x8e: case 0x8f:
      
    case 0x90: case 0x91: case 0x92: case 0x93:
    case 0x94: case 0x95: case 0x96: case 0x97:
    case 0x98: case 0x99: case 0x9a: case 0x9b:
    case 0x9c: case 0x9d: case 0x9e: case 0x9f:
      
    case 0xa0: case 0xa1: case 0xa2: case 0xa3:
    case 0xa4: case 0xa5: case 0xa6: case 0xa7:
    case 0xa8: case 0xa9: case 0xaa: case 0xab:
    case 0xac: case 0xad: case 0xae: case 0xaf:
      
    case 0xb0: case 0xb1: case 0xb2: case 0xb3:
    case 0xb4: case 0xb5: case 0xb6: case 0xb7:
    case 0xb8: case 0xb9: case 0xba: case 0xbb:
    case 0xbc: case 0xbd: case 0xbe: case 0xbf:
      return Integer.valueOf(tag - INT_ZERO);

      /* byte int */
    case 0xc0: case 0xc1: case 0xc2: case 0xc3:
    case 0xc4: case 0xc5: case 0xc6: case 0xc7:
    case 0xc8: case 0xc9: case 0xca: case 0xcb:
    case 0xcc: case 0xcd: case 0xce: case 0xcf:
      return Integer.valueOf(((tag - INT_BYTE_ZERO) << 8) + read());
      
      /* short int */
    case 0xd0: case 0xd1: case 0xd2: case 0xd3:
    case 0xd4: case 0xd5: case 0xd6: case 0xd7:
      return Integer.valueOf(((tag - INT_SHORT_ZERO) << 16) + 256 * read() + read());
      
    case 'I':
      return Integer.valueOf(parseInt());

      // direct long
    case 0xd8: case 0xd9: case 0xda: case 0xdb:
    case 0xdc: case 0xdd: case 0xde: case 0xdf:
      
    case 0xe0: case 0xe1: case 0xe2: case 0xe3:
    case 0xe4: case 0xe5: case 0xe6: case 0xe7:
    case 0xe8: case 0xe9: case 0xea: case 0xeb:
    case 0xec: case 0xed: case 0xee: case 0xef:
      return Long.valueOf(tag - LONG_ZERO);

      /* byte long */
    case 0xf0: case 0xf1: case 0xf2: case 0xf3:
    case 0xf4: case 0xf5: case 0xf6: case 0xf7:
    case 0xf8: case 0xf9: case 0xfa: case 0xfb:
    case 0xfc: case 0xfd: case 0xfe: case 0xff:
      return Long.valueOf(((tag - LONG_BYTE_ZERO) << 8) + read());
      
      /* short long */
    case 0x38: case 0x39: case 0x3a: case 0x3b:
    case 0x3c: case 0x3d: case 0x3e: case 0x3f:
      return Long.valueOf(((tag - LONG_SHORT_ZERO) << 16) + 256 * read() + read());
      
    case LONG_INT:
      return Long.valueOf(parseInt());
    
    case 'L':
      return Long.valueOf(parseLong());

    case DOUBLE_ZERO:
      return Double.valueOf(0);

    case DOUBLE_ONE:
      return Double.valueOf(1);

    case DOUBLE_BYTE:
      return Double.valueOf((byte) read());

    case DOUBLE_SHORT:
      return Double.valueOf((short) (256 * read() + read()));
      
    case DOUBLE_FLOAT:
      {
	int f = parseInt();

	return Double.valueOf(Float.intBitsToFloat(f));
      }

    case 'D':
      return Double.valueOf(parseDouble());
    
    case 'd':
      return new Date(parseLong());
    
    case 'x':
    case 'X': {
      _isLastChunk = tag == 'X';
      _chunkLength = (read() << 8) + read();

      return parseXML();
    }

    case 's':
    case 'S': {
      _isLastChunk = tag == 'S';
      _chunkLength = (read() << 8) + read();

      int data;
      _sbuf.setLength(0);
      
      while ((data = parseChar()) >= 0)
        _sbuf.append((char) data);

      return _sbuf.toString();
    }

    case 0x00: case 0x01: case 0x02: case 0x03:
    case 0x04: case 0x05: case 0x06: case 0x07:
    case 0x08: case 0x09: case 0x0a: case 0x0b:
    case 0x0c: case 0x0d: case 0x0e: case 0x0f:

    case 0x10: case 0x11: case 0x12: case 0x13:
    case 0x14: case 0x15: case 0x16: case 0x17:
    case 0x18: case 0x19: case 0x1a: case 0x1b:
    case 0x1c: case 0x1d: case 0x1e: case 0x1f:
      {
	_isLastChunk = true;
	_chunkLength = tag - 0x00;

	int data;
	_sbuf.setLength(0);
      
	while ((data = parseChar()) >= 0)
	  _sbuf.append((char) data);

	return _sbuf.toString();
      }

    case 'b':
    case 'B': {
      _isLastChunk = tag == 'B';
      _chunkLength = (read() << 8) + read();

      int data;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      
      while ((data = parseByte()) >= 0)
        bos.write(data);

      return bos.toByteArray();
    }

    case 0x20: case 0x21: case 0x22: case 0x23:
    case 0x24: case 0x25: case 0x26: case 0x27:
    case 0x28: case 0x29: case 0x2a: case 0x2b:
    case 0x2c: case 0x2d: case 0x2e: case 0x2f:
      {
	_isLastChunk = true;
	int len = tag - 0x20;
	_chunkLength = 0;

	byte []data = new byte[len];

	for (int i = 0; i < len; i++)
	  data[i] = (byte) read();

	return data;
      }

    case 'V': {
      String type = readType();
      int length = readLength();

      return findSerializerFactory().readList(this, length, type);
    }

      // direct lists
    case 'v': {
      int ref = readInt();
      String type = (String) _types.get(ref);
      int length = readInt();
      
      Deserializer reader;
      reader = findSerializerFactory().getObjectDeserializer(type, null);
      
      return reader.readLengthList(this, length);
    }

    case 'M': {
      String type = readType();

      return findSerializerFactory().readMap(this, type);
    }

    case 'O': {
      readObjectDefinition(null);

      return readObject();
    }

    case 'o': {
      int ref = readInt();

      ObjectDefinition def = (ObjectDefinition) _classDefs.get(ref);

      return readObjectInstance(null, def);
    }

    case 'R': {
      int ref = parseInt();

      return _refs.get(ref);
    }

    case REF_BYTE: {
      int ref = read();

      return _refs.get(ref);
    }

    case REF_SHORT: {
      int ref = 256 * read() + read();

      return _refs.get(ref);
    }
      
    case 'r': {
      String type = readType();
      String url = readString();

      return resolveRemote(type, url);
    }

    default:
      if (tag < 0)
	throw new EOFException("readObject: unexpected end of file");
      else
	throw error("readObject: unknown code " + codeName(tag));
    }
  }

  /**
   * Reads an object definition:
   *
   * <pre>
   * O string <int> (string)* <value>*
   * </pre>
   */
  private void readObjectDefinition(Class cl)
    throws IOException
  {
    String type = readString();
    int len = readInt();

    String []fieldNames = new String[len];
    for (int i = 0; i < len; i++)
      fieldNames[i] = readString();

    ObjectDefinition def = new ObjectDefinition(type, fieldNames);

    if (_classDefs == null)
      _classDefs = new ArrayList();

    _classDefs.add(def);
  }

  private Object readObjectInstance(Class cl, ObjectDefinition def)
    throws IOException
  {
    String type = def.getType();
    String []fieldNames = def.getFieldNames();
    
    if (cl != null) {
      Deserializer reader;
      reader = findSerializerFactory().getObjectDeserializer(type, cl);

      return reader.readObject(this, fieldNames);
    }
    else {
      return findSerializerFactory().readObject(this, type, fieldNames);
    }
  }

  private String readLenString()
    throws IOException
  {
    int len = readInt();
    
    _isLastChunk = true;
    _chunkLength = len;

    _sbuf.setLength(0);
    int ch;
    while ((ch = parseChar()) >= 0)
      _sbuf.append((char) ch);

    return _sbuf.toString();
  }

  private String readLenString(int len)
    throws IOException
  {
    _isLastChunk = true;
    _chunkLength = len;

    _sbuf.setLength(0);
    int ch;
    while ((ch = parseChar()) >= 0)
      _sbuf.append((char) ch);

    return _sbuf.toString();
  }
  
  /**
   * Reads a remote object.
   */
  public Object readRemote()
    throws IOException
  {
    String type = readType();
    String url = readString();

    return resolveRemote(type, url);
  }

  /**
   * Reads a reference.
   */
  public Object readRef()
    throws IOException
  {
    return _refs.get(parseInt());
  }

  /**
   * Reads the start of a list.
   */
  public int readListStart()
    throws IOException
  {
    return read();
  }

  /**
   * Reads the start of a list.
   */
  public int readMapStart()
    throws IOException
  {
    return read();
  }

  /**
   * Returns true if this is the end of a list or a map.
   */
  public boolean isEnd()
    throws IOException
  {
    int code;

    if (_offset < _length)
      code = (_buffer[_offset] & 0xff);
    else {
      code = read();

      if (code >= 0)
	_offset--;
    }

    return (code < 0 || code == 'z');
  }

  /**
   * Reads the end byte.
   */
  public void readEnd()
    throws IOException
  {
    int code = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

    if (code == 'z')
      return;
    else if (code < 0)
      throw error("unexpected end of file");
    else
      throw error("unknown code:" + codeName(code));
  }

  /**
   * Reads the end byte.
   */
  public void readMapEnd()
    throws IOException
  {
    int code = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

    if (code != 'z')
      throw error("expected end of map ('z') at '" + codeName(code) + "'");
  }

  /**
   * Reads the end byte.
   */
  public void readListEnd()
    throws IOException
  {
    int code = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

    if (code != 'z')
      throw error("expected end of list ('z') at '" + codeName(code) + "'");
  }

  /**
   * Adds a list/map reference.
   */
  public int addRef(Object ref)
  {
    if (_refs == null)
      _refs = new ArrayList();
    
    _refs.add(ref);

    return _refs.size() - 1;
  }

  /**
   * Adds a list/map reference.
   */
  public void setRef(int i, Object ref)
  {
    _refs.set(i, ref);
  }
  
  /**
   * Resets the references for streaming.
   */
  public void resetReferences()
  {
    if (_refs != null)
      _refs.clear();
  }

  public Object readStreamingObject()
    throws IOException
  {
    if (_refs != null)
      _refs.clear();

    return readObject();
  }

  /**
   * Resolves a remote object.
   */
  public Object resolveRemote(String type, String url)
    throws IOException
  {
    HessianRemoteResolver resolver = getRemoteResolver();

    if (resolver != null)
      return resolver.lookup(type, url);
    else
      return new HessianRemote(type, url);
  }

  /**
   * Parses a type from the stream.
   *
   * <pre>
   * t b16 b8
   * </pre>
   */
  public String readType()
    throws IOException
  {
    int code = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

    switch (code) {
    case 't':
      {
        int len = 256 * read() + read();
        String type = readLenString(len);

        if (_types == null)
          _types = new ArrayList();

        _types.add(type);

        return type;
      }

    case 'T':
      {
        int ref = readInt();

        return (String) _types.get(ref);
      }

    case TYPE_REF:
      {
        int ref = readInt();

        return (String) _types.get(ref);
      }
      
    default:
      {
        if (code >= 0)
          _offset--;
      
        return "";
      }
    }
  }

  /**
   * Parses the length for an array
   *
   * <pre>
   * l b32 b24 b16 b8
   * </pre>
   */
  public int readLength()
    throws IOException
  {
    int code = read();

    if (code == LENGTH_BYTE)
      return read();
	
    else if (code == 'l')
      return parseInt();

    else {
      if (code >= 0)
	_offset--;
      
      return -1;
    }
  }

  /**
   * Parses a 32-bit integer value from the stream.
   *
   * <pre>
   * b32 b24 b16 b8
   * </pre>
   */
  private int parseInt()
    throws IOException
  {
    int offset = _offset;
    
    if (offset + 3 < _length) {
      byte []buffer = _buffer;
      
      int b32 = buffer[offset + 0] & 0xff;
      int b24 = buffer[offset + 1] & 0xff;
      int b16 = buffer[offset + 2] & 0xff;
      int b8 = buffer[offset + 3] & 0xff;

      _offset = offset + 4;

      return (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
    }
    else {
      int b32 = read();
      int b24 = read();
      int b16 = read();
      int b8 = read();

      return (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
    }
  }

  /**
   * Parses a 64-bit long value from the stream.
   *
   * <pre>
   * b64 b56 b48 b40 b32 b24 b16 b8
   * </pre>
   */
  private long parseLong()
    throws IOException
  {
    long b64 = read();
    long b56 = read();
    long b48 = read();
    long b40 = read();
    long b32 = read();
    long b24 = read();
    long b16 = read();
    long b8 = read();

    return ((b64 << 56) +
            (b56 << 48) +
            (b48 << 40) +
            (b40 << 32) +
            (b32 << 24) +
            (b24 << 16) +
            (b16 << 8) +
            b8);
  }
  
  /**
   * Parses a 64-bit double value from the stream.
   *
   * <pre>
   * b64 b56 b48 b40 b32 b24 b16 b8
   * </pre>
   */
  private double parseDouble()
    throws IOException
  {
    long bits = parseLong();
  
    return Double.longBitsToDouble(bits);
  }

  org.w3c.dom.Node parseXML()
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Reads a character from the underlying stream.
   */
  private int parseChar()
    throws IOException
  {
    while (_chunkLength <= 0) {
      if (_isLastChunk)
        return -1;

      int code = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

      switch (code) {
      case 's':
      case 'x':
        _isLastChunk = false;

        _chunkLength = (read() << 8) + read();
        break;
        
      case 'S':
      case 'X':
        _isLastChunk = true;

        _chunkLength = (read() << 8) + read();
        break;
	
      case 0x00: case 0x01: case 0x02: case 0x03:
      case 0x04: case 0x05: case 0x06: case 0x07:
      case 0x08: case 0x09: case 0x0a: case 0x0b:
      case 0x0c: case 0x0d: case 0x0e: case 0x0f:

      case 0x10: case 0x11: case 0x12: case 0x13:
      case 0x14: case 0x15: case 0x16: case 0x17:
      case 0x18: case 0x19: case 0x1a: case 0x1b:
      case 0x1c: case 0x1d: case 0x1e: case 0x1f:
	_isLastChunk = true;
	_chunkLength = code - 0x00;
	break;

      default:
        throw expect("string", code);
      }

    }

    _chunkLength--;

    return parseUTF8Char();
  }

  /**
   * Parses a single UTF8 character.
   */
  private int parseUTF8Char()
    throws IOException
  {
    int ch = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

    if (ch < 0x80)
      return ch;
    else if ((ch & 0xe0) == 0xc0) {
      int ch1 = read();
      int v = ((ch & 0x1f) << 6) + (ch1 & 0x3f);

      return v;
    }
    else if ((ch & 0xf0) == 0xe0) {
      int ch1 = read();
      int ch2 = read();
      int v = ((ch & 0x0f) << 12) + ((ch1 & 0x3f) << 6) + (ch2 & 0x3f);

      return v;
    }
    else
      throw error("bad utf-8 encoding at " + codeName(ch));
  }
  
  /**
   * Reads a byte from the underlying stream.
   */
  private int parseByte()
    throws IOException
  {
    while (_chunkLength <= 0) {
      if (_isLastChunk) {
        return -1;
      }

      int code = read();

      switch (code) {
      case 'b':
        _isLastChunk = false;

        _chunkLength = (read() << 8) + read();
        break;
        
      case 'B':
        _isLastChunk = true;

        _chunkLength = (read() << 8) + read();
        break;

      case 0x20: case 0x21: case 0x22: case 0x23:
      case 0x24: case 0x25: case 0x26: case 0x27:
      case 0x28: case 0x29: case 0x2a: case 0x2b:
      case 0x2c: case 0x2d: case 0x2e: case 0x2f:
        _isLastChunk = true;

        _chunkLength = code - 0x20;
        break;

      default:
        throw expect("byte[]", code);
      }
    }

    _chunkLength--;

    return read();
  }

  /**
   * Reads bytes based on an input stream.
   */
  public InputStream readInputStream()
    throws IOException
  {
    int tag = read();

    switch (tag) {
    case 'N':
      return null;

    case 'B':
    case 'b':
      _isLastChunk = tag == 'B';
      _chunkLength = (read() << 8) + read();
      break;

    case 0x20: case 0x21: case 0x22: case 0x23:
    case 0x24: case 0x25: case 0x26: case 0x27:
    case 0x28: case 0x29: case 0x2a: case 0x2b:
    case 0x2c: case 0x2d: case 0x2e: case 0x2f:
      _isLastChunk = true;
      _chunkLength = tag - 0x20;
      break;
      
    default:
      throw expect("binary", tag);
    }
    
    return new ReadInputStream();
  }
  
  /**
   * Reads bytes from the underlying stream.
   */
  int read(byte []buffer, int offset, int length)
    throws IOException
  {
    int readLength = 0;

    while (length > 0) {
      while (_chunkLength <= 0) {
        if (_isLastChunk)
          return readLength == 0 ? -1 : readLength;

        int code = read();

        switch (code) {
        case 'b':
          _isLastChunk = false;

          _chunkLength = (read() << 8) + read();
          break;
        
        case 'B':
          _isLastChunk = true;

          _chunkLength = (read() << 8) + read();
          break;

	case 0x20: case 0x21: case 0x22: case 0x23:
	case 0x24: case 0x25: case 0x26: case 0x27:
	case 0x28: case 0x29: case 0x2a: case 0x2b:
	case 0x2c: case 0x2d: case 0x2e: case 0x2f:
	  _isLastChunk = true;
	  _chunkLength = code - 0x20;
	  break;

        default:
          throw expect("byte[]", code);
        }
      }

      int sublen = _chunkLength;
      if (length < sublen)
        sublen = length;

      if (_length <= _offset && ! readBuffer())
	return -1;
      
      if (_length - _offset < sublen)
	sublen = _length - _offset;

      System.arraycopy(_buffer, _offset, buffer, offset, sublen);

      _offset += sublen;
      
      offset += sublen;
      readLength += sublen;
      length -= sublen;
      _chunkLength -= sublen;
    }

    return readLength;
  }

  /**
   * Normally, shouldn't be called externally, but needed for QA, e.g.
   * ejb/3b01.
   */
  public final int read()
    throws IOException
  {
    if (_length <= _offset && ! readBuffer())
      return -1;

    return _buffer[_offset++] & 0xff;
  }

  private final boolean readBuffer()
    throws IOException
  {
    byte []buffer = _buffer;
    int offset = _offset;
    int length = _length;
    
    if (offset < length) {
      System.arraycopy(buffer, offset, buffer, 0, length - offset);
      offset = length - offset;
    }
    else
      offset = 0;
    
    int len = _is.read(buffer, offset, SIZE - offset);

    if (len <= 0) {
      _length = offset;
      _offset = 0;
      
      return offset > 0;
    }

    _length = offset + len;
    _offset = 0;

    return true;
  }

  public Reader getReader()
  {
    return null;
  }

  protected IOException expect(String expect, int ch)
    throws IOException
  {
    if (ch < 0)
      return error("expected " + expect + " at end of file");
    else {
      _offset--;

      try {
	Object obj = readObject();

	if (obj != null) {
	  return error("expected " + expect
		       + " at 0x" + Integer.toHexString(ch & 0xff)
		       + " " + obj.getClass().getName() + " (" + obj + ")");
	}
	else
	  return error("expected " + expect
		       + " at 0x" + Integer.toHexString(ch & 0xff) + " null");
      } catch (IOException e) {
	log.log(Level.FINE, e.toString(), e);
	
	return error("expected " + expect
		     + " at 0x" + Integer.toHexString(ch & 0xff));
      }
    }
  }

  protected String codeName(int ch)
  {
    if (ch < 0)
      return "end of file";
    else
      return "0x" + Integer.toHexString(ch & 0xff) + " (" + (char) + ch + ")";
  }
  
  protected IOException error(String message)
  {
    if (_method != null)
      return new HessianProtocolException(_method + ": " + message);
    else
      return new HessianProtocolException(message);
  }

  public void close()
    throws IOException
  {
    InputStream is = _is;
    _is = null;

    if (_isCloseStreamOnClose && is != null)
      is.close();
  }
  
  class ReadInputStream extends InputStream {
    boolean _isClosed = false;
	
    public int read()
      throws IOException
    {
      if (_isClosed)
	return -1;

      int ch = parseByte();
      if (ch < 0)
	_isClosed = true;

      return ch;
    }
	
    public int read(byte []buffer, int offset, int length)
      throws IOException
    {
      if (_isClosed)
	return -1;

      int len = Hessian2Input.this.read(buffer, offset, length);
      if (len < 0)
	_isClosed = true;

      return len;
    }

    public void close()
      throws IOException
    {
      while (read() >= 0) {
      }
    }
  };

  final static class ObjectDefinition {
    private final String _type;
    private final String []_fields;

    ObjectDefinition(String type, String []fields)
    {
      _type = type;
      _fields = fields;
    }

    String getType()
    {
      return _type;
    }

    String []getFieldNames()
    {
      return _fields;
    }
  }

  static {
    try {
      _detailMessageField = Throwable.class.getDeclaredField("detailMessage");
      _detailMessageField.setAccessible(true);
    } catch (Throwable e) {
    }
  }
}

/*
 * Copyright (c) 2001-2004 Caucho Technology, Inc.  All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Debugging input stream for Hessian requests.
 */
public class HessianDebugState implements Hessian2Constants
{
  private PrintWriter _dbg;

  private State _state;
  private ArrayList<State> _stateStack = new ArrayList<State>();

  private ArrayList<ObjectDef> _objectDefList
    = new ArrayList<ObjectDef>();

  private ArrayList<String> _typeDefList
    = new ArrayList<String>();

  private int _refId;
  private boolean _isNewline = true;
  private boolean _isObject = false;
  private int _column;
  
  /**
   * Creates an uninitialized Hessian input stream.
   */
  public HessianDebugState(PrintWriter dbg)
  {
    _dbg = dbg;

    _state = new InitialState();
  }

  /**
   * Reads a character.
   */
  public void next(int ch)
    throws IOException
  {
    _state = _state.next(ch);
  }

  void pushStack(State state)
  {
    _stateStack.add(state);
  }

  State popStack()
  {
    return _stateStack.remove(_stateStack.size() - 1);
  }

  void println()
  {
    if (! _isNewline) {
      _dbg.println();
      _dbg.flush();
    }

    _isNewline = true;
    _column = 0;
  }

  abstract class State {
    State _next;

    State()
    {
    }

    State(State next)
    {
      _next = next;
    }
    
    abstract State next(int ch);

    boolean isShift(Object value)
    {
      return false;
    }

    State shift(Object value)
    {
      return this;
    }

    int depth()
    {
      if (_next != null)
	return _next.depth();
      else
	return 0;
    }

    void printIndent(int depth)
    {
      if (_isNewline) {
	for (int i = _column; i < depth() + depth; i++) {
	  _dbg.print(" ");
	  _column++;
	}
      }
    }

    void print(String string)
    {
      print(0, string);
    }

    void print(int depth, String string)
    {
      printIndent(depth);
      
      _dbg.print(string);
      _isNewline = false;
      _isObject = false;

      int p = string.lastIndexOf('\n');
      if (p > 0)
	_column = string.length() - p - 1;
      else
	_column += string.length();
    }

    void println(String string)
    {
      println(0, string);
    }

    void println(int depth, String string)
    {
      printIndent(depth);

      _dbg.println(string);
      _dbg.flush();
      _isNewline = true;
      _isObject = false;
      _column = 0;
    }

    void println()
    {
      if (! _isNewline) {
	_dbg.println();
	_dbg.flush();
      }

      _isNewline = true;
      _isObject = false;
      _column = 0;
    }

    void printObject(String string)
    {
      if (_isObject)
	println();
      
      printIndent(0);

      _dbg.print(string);
      _dbg.flush();

      _column += string.length();

      _isNewline = false;
      _isObject = true;
    }
    
    protected State nextObject(int ch)
    {
      switch (ch) {
      case -1:
	println();
	return this;
	
      case 'N':
	if (isShift(null))
	  return shift(null);
	else {
	  printObject("null");
	  return this;
	}
	
      case 'T':
	if (isShift(Boolean.TRUE))
	  return shift(Boolean.TRUE);
	else {
	  printObject("true");
	  return this;
	}
	
      case 'F':
	if (isShift(Boolean.FALSE))
	  return shift(Boolean.FALSE);
	else {
	  printObject("false");
	  return this;
	}

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
	{
	  Integer value = new Integer(ch - 0x90);
	  
	  if (isShift(value))
	    return shift(value);
	  else {
	    printObject(value.toString());
	    return this;
	  }
	}

      case 0xc0: case 0xc1: case 0xc2: case 0xc3: 
      case 0xc4: case 0xc5: case 0xc6: case 0xc7: 
      case 0xc8: case 0xc9: case 0xca: case 0xcb: 
      case 0xcc: case 0xcd: case 0xce: case 0xcf:
	return new IntegerState(this, "int", ch - 0xc8, 3);

      case 0xd0: case 0xd1: case 0xd2: case 0xd3: 
      case 0xd4: case 0xd5: case 0xd6: case 0xd7: 
	return new IntegerState(this, "int", ch - 0xd4, 2);

      case 'I':
	return new IntegerState(this, "int");
	
      case 0xd8: case 0xd9: case 0xda: case 0xdb: 
      case 0xdc: case 0xdd: case 0xde: case 0xdf: 
      case 0xe0: case 0xe1: case 0xe2: case 0xe3: 
      case 0xe4: case 0xe5: case 0xe6: case 0xe7: 
      case 0xe8: case 0xe9: case 0xea: case 0xeb: 
      case 0xec: case 0xed: case 0xee: case 0xef:
	{
	  Long value = new Long(ch - 0xe0);
	  
	  if (isShift(value))
	    return shift(value);
	  else {
	    printObject(value.toString() + "L");
	    return this;
	  }
	}
	
      case 0xf0: case 0xf1: case 0xf2: case 0xf3: 
      case 0xf4: case 0xf5: case 0xf6: case 0xf7: 
      case 0xf8: case 0xf9: case 0xfa: case 0xfb: 
      case 0xfc: case 0xfd: case 0xfe: case 0xff:
	return new LongState(this, "long", ch - 0xf8, 7);
	
      case 0x38: case 0x39: case 0x3a: case 0x3b: 
      case 0x3c: case 0x3d: case 0x3e: case 0x3f:
	return new LongState(this, "long", ch - 0x3c, 6);
	
      case 0x77:
	return new LongState(this, "long", 0, 4);

      case 'L':
	return new LongState(this, "long");

      case 0x67: case 0x68:
	{
	  Double value = new Double(ch - 0x67);
	  
	  if (isShift(value))
	    return shift(value);
	  else {
	    printObject(value.toString());
	    return this;
	  }
	}

      case 0x69:
	return new DoubleIntegerState(this, 3);

      case 0x6a:
	return new DoubleIntegerState(this, 2);

      case 0x6b:
	return new FloatState(this);
	
      case 'D':
	return new DoubleState(this);

      case 0x4a:
	return new RefState(this, "Ref", 0, 3);

      case 0x4b:
	return new RefState(this, "Ref", 0, 2);

      case 'R':
	return new RefState(this, "Ref");

      case 'r':
	return new RemoteState(this);

      case 'd':
	return new DateState(this);

      case 0x00:
	{
	  String value = "\"\"";
	  
	  if (isShift(value))
	    return shift(value);
	  else {
	    printObject(value.toString());
	    return this;
	  }
	}

      case 0x01: case 0x02: case 0x03:
      case 0x04: case 0x05: case 0x06: case 0x07:
      case 0x08: case 0x09: case 0x0a: case 0x0b:
      case 0x0c: case 0x0d: case 0x0e: case 0x0f:
	
      case 0x10: case 0x11: case 0x12: case 0x13:
      case 0x14: case 0x15: case 0x16: case 0x17:
      case 0x18: case 0x19: case 0x1a: case 0x1b:
      case 0x1c: case 0x1d: case 0x1e: case 0x1f:
	return new StringState(this, 'S', ch);

      case 'S': case 'X':
	return new StringState(this, 'S', true);

      case 's': case 'x':
	return new StringState(this, 'S', false);

      case 0x20:
	{
	  String value = "binary(0)";
	  
	  if (isShift(value))
	    return shift(value);
	  else {
	    printObject(value.toString());
	    return this;
	  }
	}

      case 0x21: case 0x22: case 0x23:
      case 0x24: case 0x25: case 0x26: case 0x27:
      case 0x28: case 0x29: case 0x2a: case 0x2b:
      case 0x2c: case 0x2d: case 0x2e: case 0x2f:
	return new BinaryState(this, 'B', ch - 0x20);

      case 'B':
	return new BinaryState(this, 'B', true);

      case 'b':
	return new BinaryState(this, 'B', false);

      case 'M':
	return new MapState(this, _refId++);

      case 'V':
	return new ListState(this, _refId++);

      case 'v':
	return new CompactListState(this, _refId++);

      case 'O':
	return new ObjectDefState(this);

      case 'o':
	return new ObjectState(this, _refId++);

      case 'P':
	return new StreamingState(this, true);

      case 'p':
	return new StreamingState(this, false);
	
      default:
	return this;
      }
    }
  }
  
  class InitialState extends State {
    State next(int ch)
    {
      println();
      
      if (ch == 'r') {
	return new ReplyState(this);
      }
      else if (ch == 'c') {
	return new CallState(this);
      }
      else
	return nextObject(ch);
    }
  }
  
  class IntegerState extends State {
    String _typeCode;
    
    int _length;
    int _value;

    IntegerState(State next, String typeCode)
    {
      super(next);

      _typeCode = typeCode;
    }

    IntegerState(State next, String typeCode, int value, int length)
    {
      super(next);

      _typeCode = typeCode;

      _value = value;
      _length = length;
    }

    State next(int ch)
    {
      _value = 256 * _value + (ch & 0xff);

      if (++_length == 4) {
	Integer value = new Integer(_value);
	
	if (_next.isShift(value))
	  return _next.shift(value);
	else {
	  printObject(value.toString());
	  
	  return _next;
	}
      }
      else
	return this;
    }
  }
  
  class LongState extends State {
    String _typeCode;
    
    int _length;
    long _value;

    LongState(State next, String typeCode)
    {
      super(next);

      _typeCode = typeCode;
    }

    LongState(State next, String typeCode, long value, int length)
    {
      super(next);

      _typeCode = typeCode;

      _value = value;
      _length = length;
    }

    State next(int ch)
    {
      _value = 256 * _value + (ch & 0xff);

      if (++_length == 8) {
	Long value = new Long(_value);
	
	if (_next.isShift(value))
	  return _next.shift(value);
	else {
	  printObject(value.toString() + "L");
	  
	  return _next;
	}
      }
      else
	return this;
    }
  }
  
  class DoubleIntegerState extends State {
    int _length;
    int _value;
    boolean _isFirst = true;

    DoubleIntegerState(State next, int length)
    {
      super(next);

      _length = length;
    }

    State next(int ch)
    {
      if (_isFirst)
	_value = (byte) ch;
      else
	_value = 256 * _value + (ch & 0xff);

      _isFirst = false;

      if (++_length == 4) {
	Double value = new Double(_value);
	
	if (_next.isShift(value))
	  return _next.shift(value);
	else {
	  printObject(value.toString());
	  
	  return _next;
	}
      }
      else
	return this;
    }
  }
  
  class RefState extends State {
    String _typeCode;
    
    int _length;
    int _value;

    RefState(State next, String typeCode)
    {
      super(next);

      _typeCode = typeCode;
    }

    RefState(State next, String typeCode, int value, int length)
    {
      super(next);

      _typeCode = typeCode;

      _value = value;
      _length = length;
    }

    State next(int ch)
    {
      _value = 256 * _value + (ch & 0xff);

      if (++_length == 4) {
	Integer value = new Integer(_value);
	
	if (_next.isShift(value))
	  return _next.shift(value);
	else {
	  printObject("ref(#" + value + ")");
	  
	  return _next;
	}
      }
      else
	return this;
    }
  }
  
  class DateState extends State {
    int _length;
    long _value;

    DateState(State next)
    {
      super(next);
    }
      
    
    State next(int ch)
    {
      _value = 256 * _value + (ch & 0xff);

      if (++_length == 8) {
	java.util.Date value = new java.util.Date(_value);

	if (_next.isShift(value))
	  return _next.shift(value);
	else {
	  printObject(value.toString());
	  
	  return _next;
	}
      }
      else
	return this;
    }
  }
  
  class DoubleState extends State {
    int _length;
    long _value;

    DoubleState(State next)
    {
      super(next);
    }
    
    State next(int ch)
    {
      _value = 256 * _value + (ch & 0xff);

      if (++_length == 8) {
	Double value = Double.longBitsToDouble(_value);

	if (_next.isShift(value))
	  return _next.shift(value);
	else {
	  printObject(value.toString());
	  
	  return _next;
	}
      }
      else
	return this;
    }
  }
  
  class FloatState extends State {
    int _length;
    int _value;

    FloatState(State next)
    {
      super(next);
    }
    
    State next(int ch)
    {
      _value = 256 * _value + (ch & 0xff);

      if (++_length == 4) {
	Double value = (double) Float.intBitsToFloat(_value);

	if (_next.isShift(value))
	  return _next.shift(value);
	else {
	  printObject(value.toString() + "F");
	  
	  return _next;
	}
      }
      else
	return this;
    }
  }
  
  class StringState extends State {
    private static final int TOP = 0;
    private static final int UTF_2_1 = 1;
    private static final int UTF_3_1 = 2;
    private static final int UTF_3_2 = 3;

    char _typeCode;
    
    StringBuilder _value = new StringBuilder();
    int _lengthIndex;
    int _length;
    boolean _isLastChunk;
    
    int _utfState;
    char _ch;

    StringState(State next, char typeCode, boolean isLastChunk)
    {
      super(next);
      
      _typeCode = typeCode;
      _isLastChunk = isLastChunk;
    }

    StringState(State next, char typeCode, int length)
    {
      super(next);
      
      _typeCode = typeCode;
      _isLastChunk = true;
      _length = length;
      _lengthIndex = 2;
    }
    
    State next(int ch)
    {
      if (_lengthIndex < 2) {
	_length = 256 * _length + (ch & 0xff);
	
	if (++_lengthIndex == 2 && _length == 0 && _isLastChunk) {
	  if (_next.isShift(_value.toString()))
	    return _next.shift(_value.toString());
	  else {
	    printObject("\"" + _value + "\"");
	    return _next;
	  }
	}
	else
	  return this;
      }
      else if (_length == 0) {
	if (ch == 's' || ch == 'x') {
	  _isLastChunk = false;
	  _lengthIndex = 0;
	  return this;
	}
	else if (ch == 'S' || ch == 'X') {
	  _isLastChunk = true;
	  _lengthIndex = 0;
	  return this;
	}
	else if (ch == 0x00) {
	  if (_next.isShift(_value.toString()))
	    return _next.shift(_value.toString());
	  else {
	    printObject("\"" + _value + "\"");
	    return _next;
	  }
	}
	else if (0x00 <= ch && ch < 0x20) {
	  _isLastChunk = true;
	  _lengthIndex = 2;
	  _length = ch & 0xff;
	  return this;
	}
	else {
	  println(String.valueOf((char) ch) + ": unexpected character");
	  return _next;
	}
      }

      switch (_utfState) {
      case TOP:
	if (ch < 0x80) {
	  _length--;

	  _value.append((char) ch);
	}
	else if (ch < 0xe0) {
	  _ch = (char) ((ch & 0x1f) << 6);
	  _utfState = UTF_2_1;
	}
	else {
	  _ch = (char) ((ch & 0xf) << 12);
	  _utfState = UTF_3_1;
	}
	break;

      case UTF_2_1:
      case UTF_3_2:
	_ch += ch & 0x3f;
	_value.append(_ch);
	_length--;
	_utfState = TOP;
	break;

      case UTF_3_1:
	_ch += (char) ((ch & 0x3f) << 6);
	_utfState = UTF_3_2;
	break;
      }

      if (_length == 0 && _isLastChunk) {
	if (_next.isShift(_value.toString()))
	  return _next.shift(_value.toString());
	else {
	  printObject("\"" + _value + "\"");
	  
	  return _next;
	}
      }
      else
	return this;
    }
  }
  
  class BinaryState extends State {
    char _typeCode;
    
    int _totalLength;
    
    int _lengthIndex;
    int _length;
    boolean _isLastChunk;
    
    BinaryState(State next, char typeCode, boolean isLastChunk)
    {
      super(next);

      _typeCode = typeCode;
      _isLastChunk = isLastChunk;
    }

    BinaryState(State next, char typeCode, int length)
    {
      super(next);

      _typeCode = typeCode;
      _isLastChunk = true;
      _length = length;
      _lengthIndex = 2;
    }
    
    State next(int ch)
    {
      if (_lengthIndex < 2) {
	_length = 256 * _length + (ch & 0xff);
	
	if (++_lengthIndex == 2 && _length == 0 && _isLastChunk) {
	  String value = "binary(" + _totalLength + ")";
	  
	  if (_next.isShift(value))
	    return _next.shift(value);
	  else {
	    printObject(value);
	    return _next;
	  }
	}
	else
	  return this;
      }
      else if (_length == 0) {
	if (ch == 'b') {
	  _isLastChunk = false;
	  _lengthIndex = 0;
	  return this;
	}
	else if (ch == 'B') {
	  _isLastChunk = true;
	  _lengthIndex = 0;
	  return this;
	}
	else if (ch == 0x20) {
	  String value = "binary(" + _totalLength + ")";
	  
	  if (_next.isShift(value))
	    return _next.shift(value);
	  else {
	    printObject(value);
	    return _next;
	  }
	}
	else if (0x20 <=ch && ch < 0x30) {
	  _isLastChunk = true;
	  _lengthIndex = 2;
	  _length = (ch & 0xff) - 0x20;
	  return this;
	}
	else {
	  println(String.valueOf((char) ch) + ": unexpected character");
	  return _next;
	}
      }
      
      _length--;
      _totalLength++;

      if (_length == 0 && _isLastChunk) {
	String value = "binary(" + _totalLength + ")";
	
	if (_next.isShift(value))
	  return _next.shift(value);
	else {
	  printObject(value);
	  
	  return _next;
	}
      }
      else
	return this;
    }
  }
  
  class MapState extends State {
    private static final int TYPE = 0;
    private static final int KEY = 1;
    private static final int VALUE = 2;

    private int _refId;

    private int _state;
    private int _valueDepth;
    private boolean _hasData;

    MapState(State next, int refId)
    {
      super(next);
      
      _refId = refId;
      _state = TYPE;
    }

    @Override
    boolean isShift(Object value)
    {
      return _state == TYPE;
    }

    @Override
    State shift(Object type)
    {
      if (_state == TYPE) {
	if (type instanceof String) {
	  _typeDefList.add((String) type);
	}
	else if (type instanceof Integer) {
	  int iValue = (Integer) type;

	  if (iValue >= 0 && iValue < _typeDefList.size())
	    type = _typeDefList.get(iValue);
	}
	
	printObject("map " + type + "(#" + _refId + ")");

	_state = VALUE;
      
	return this;
      }
      else
	throw new IllegalStateException();
    }

    @Override
    int depth()
    {
      if (_state == TYPE)
	return _next.depth();
      else if (_state == KEY)
	return _next.depth() + 2;
      else
	return _valueDepth;
    }
    
    State next(int ch)
    {
      switch (_state) {
      case TYPE:
	if (ch == 't') {
	  return new StringState(this, 't', true);
	}
	else if (ch == 0x75) {
	  return new IndirectState(this);
	}
	else if (ch == 'z') {
	  printObject("map (#" + _refId + ")");
	  return _next;
	}
	else {
	  printObject("map (#" + _refId + ")");
	  
	  _state = KEY;
	  
	  return nextObject(ch);
	}
	
      case VALUE:
	if (ch == 'z') {
	  if (_hasData)
	    println();
	  
	  return _next;
	}
	else {
	  if (_hasData)
	    println();

	  _hasData = true;
	  _state = KEY;
	  
	  return nextObject(ch);
	}
	
      case KEY:
	print(" => ");
	_isObject = false;
	_valueDepth = _column;

	_state = VALUE;
	
	return nextObject(ch);

      default:
	throw new IllegalStateException();
      }
    }
  }
  
  class ObjectDefState extends State {
    private static final int TYPE = 1;
    private static final int COUNT = 2;
    private static final int FIELD = 3;
    private static final int COMPLETE = 4;

    private int _refId;

    private int _state;
    private boolean _hasData;
    private int _count;

    private String _type;
    private ArrayList<String> _fields = new ArrayList<String>();

    ObjectDefState(State next)
    {
      super(next);
      
      _state = TYPE;
    }

    @Override
    boolean isShift(Object value)
    {
      return true;
    }

    @Override
    State shift(Object object)
    {
      if (_state == TYPE) {
	_type = (String) object;

	print("/* defun " + _type + " [");

	_objectDefList.add(new ObjectDef(_type, _fields));

	_state = COUNT;
      }
      else if (_state == COUNT) {
	_count = (Integer) object;

	_state = FIELD;
      }
      else if (_state == FIELD) {
	String field = (String) object;

	_count--;

	_fields.add(field);

	if (_fields.size() == 1)
	  print(field);
	else
	  print(", " + field);
      }
      else {
	throw new UnsupportedOperationException();
      }

      return this;
    }

    @Override
    int depth()
    {
      if (_state <= TYPE)
	return _next.depth();
      else
	return _next.depth() + 2;
    }
    
    State next(int ch)
    {
      switch (_state) {
      case TYPE:
	return nextObject(ch);
	
      case COUNT:
	return nextObject(ch);
	
      case FIELD:
	if (_count == 0) {
	  println("] */");
	  _next.printIndent(0);

	  return _next.nextObject(ch);
	}
	else
	  return nextObject(ch);

      default:
	throw new IllegalStateException();
      }
    }
  }
  
  class ObjectState extends State {
    private static final int TYPE = 0;
    private static final int FIELD = 1;

    private int _refId;

    private int _state;
    private ObjectDef _def;
    private int _count;
    private int _fieldDepth;

    ObjectState(State next, int refId)
    {
      super(next);

      _refId = refId;
      _state = TYPE;
    }

    @Override
    boolean isShift(Object value)
    {
      if (_state == TYPE)
	return true;
      else
	return false;
    }

    @Override
    State shift(Object object)
    {
      if (_state == TYPE) {
	int def = (Integer) object;

	_def = _objectDefList.get(def);

	println("object " + _def.getType() + " (#" + _refId + ")");

	_state = FIELD;

	if (_def.getFields().size() == 0)
	  return _next;
      }

      return this;
    }

    @Override
    int depth()
    {
      if (_state <= TYPE)
	return _next.depth();
      else
	return _fieldDepth;
    }
    
    State next(int ch)
    {
      switch (_state) {
      case TYPE:
	return nextObject(ch);
	
      case FIELD:
	if (_def.getFields().size() <= _count)
	  return _next.next(ch);

	_fieldDepth = _next.depth() + 2;
	println();
	print(_def.getFields().get(_count++) + ": ");

	_fieldDepth = _column;

	_isObject = false;
	return nextObject(ch);

      default:
	throw new IllegalStateException();
      }
    }
  }
  
  class ListState extends State {
    private static final int TYPE = 0;
    private static final int LENGTH = 1;
    private static final int VALUE = 2;

    private int _refId;

    private int _state;
    private boolean _hasData;
    private int _count;
    private int _valueDepth;

    ListState(State next, int refId)
    {
      super(next);
      
      _refId = refId;
      _state = TYPE;
    }

    @Override
    boolean isShift(Object value)
    {
      return _state == TYPE || _state == LENGTH;
    }

    @Override
    State shift(Object object)
    {
      if (_state == TYPE) {
	Object type = object;
	
	if (type instanceof String) {
	  _typeDefList.add((String) type);
	}
	else if (object instanceof Integer) {
	  int index = (Integer) object;

	  if (index >= 0 && index < _typeDefList.size())
	    type = _typeDefList.get(index);
	}
	
	printObject("list " + type + "(#" + _refId + ")");
      
	_state = LENGTH;
      
	return this;
      }
      else if (_state == LENGTH) {
	_state = VALUE;

	return this;
      }
      else
	return this;
    }

    @Override
    int depth()
    {
      if (_state <= LENGTH)
	return _next.depth();
      else if (_state == VALUE)
	return _valueDepth;
      else
	return _next.depth() + 2;
    }
    
    State next(int ch)
    {
      switch (_state) {
      case TYPE:
	if (ch == 't') {
	  return new StringState(this, 't', true);
	}
	else if (ch == TYPE_REF) {
	  return new IndirectState(this);
	}
	else if (ch == 'l') {
	  printObject("list (#" + _refId + ")");
	  _state = LENGTH;
	  
	  return new IntegerState(this, "length");
	}
	else if (ch == LENGTH_BYTE) {
	  printObject("list (#" + _refId + ")");
	  _state = LENGTH;
	  
	  return new IntegerState(this, "length", 0, 3);
	}
	else if (ch == 'z') {
	  printObject("list (#" + _refId + ")");
	  return _next;
	}
	else {
	  printObject("list (#" + _refId + ")");
	  
	  _state = VALUE;

	  _valueDepth = _next.depth() + 2;
	  println();
	  printObject(_count++ + ": ");
	  _valueDepth = _column;
	  _isObject = false;
	  
	  return nextObject(ch);
	}
	
      case LENGTH:
	if (ch == 'z') {
	  return _next;
	}
	else if (ch == 'l') {
	  return new IntegerState(this, "length");
	}
	else if (ch == LENGTH_BYTE) {
	  return new IntegerState(this, "length", 0, 3);
	}
	else {
	  _state = VALUE;
	  
	  _valueDepth = _next.depth() + 2;
	  println();
	  printObject(_count++ + ": ");
	  _valueDepth = _column;
	  _isObject = false;
	  
	  return nextObject(ch);
	}
	
      case VALUE:
	if (ch == 'z') {
	  if (_count > 0)
	    println();
	  
	  return _next;
	}
	else {
	  _valueDepth = _next.depth() + 2;
	  println();
	  printObject(_count++ + ": ");
	  _valueDepth = _column;
	  _isObject = false;
	  
	  return nextObject(ch);
	}

      default:
	throw new IllegalStateException();
      }
    }
  }
  
  class CompactListState extends State {
    private static final int TYPE = 0;
    private static final int LENGTH = 1;
    private static final int VALUE = 2;

    private int _refId;

    private int _state;
    private boolean _hasData;
    private int _length;
    private int _count;
    private int _valueDepth;

    CompactListState(State next, int refId)
    {
      super(next);
      
      _refId = refId;
      _state = TYPE;
    }

    @Override
    boolean isShift(Object value)
    {
      return _state == TYPE || _state == LENGTH;
    }

    @Override
    State shift(Object object)
    {
      if (_state == TYPE) {
	Object type = object;
	
	if (object instanceof Integer) {
	  int index = (Integer) object;

	  if (index >= 0 && index < _typeDefList.size())
	    type = _typeDefList.get(index);
	}
	
	printObject("list " + type + "(#" + _refId + ")");
      
	_state = LENGTH;
      
	return this;
      }
      else if (_state == LENGTH) {
	_length = (Integer) object;
	
	_state = VALUE;

	if (_length == 0)
	  return _next;
	else
	  return this;
      }
      else
	return this;
    }

    @Override
    int depth()
    {
      if (_state <= LENGTH)
	return _next.depth();
      else if (_state == VALUE)
	return _valueDepth;
      else
	return _next.depth() + 2;
    }
    
    State next(int ch)
    {
      switch (_state) {
      case TYPE:
	return nextObject(ch);
	
      case LENGTH:
	return nextObject(ch);
	
      case VALUE:
	if (_length <= _count)
	  return _next.next(ch);
	else {
	  _valueDepth = _next.depth() + 2;
	  println();
	  printObject(_count++ + ": ");
	  _valueDepth = _column;
	  _isObject = false;
	  
	  return nextObject(ch);
	}

      default:
	throw new IllegalStateException();
      }
    }
  }
  
  class CallState extends State {
    private static final int MAJOR = 0;
    private static final int MINOR = 1;
    private static final int HEADER = 2;
    private static final int METHOD = 3;
    private static final int VALUE = 4;
    private static final int ARG = 5;

    private int _state;
    private int _major;
    private int _minor;

    CallState(State next)
    {
      super(next);
    }

    int depth()
    {
      return _next.depth() + 2;
    }
    
    State next(int ch)
    {
      switch (_state) {
      case MAJOR:
	_major = ch;
	_state = MINOR;
	return this;
	
      case MINOR:
	_minor = ch;
	_state = HEADER;
	println(-2, "call " + _major + "." + _minor);
	return this;
	
      case HEADER:
	if (ch == 'H') {
	  println();
	  print("header ");
	  _isObject = false;
	  _state = VALUE;
	  return new StringState(this, 'H', true);
	}
 	else if (ch == 'm') {
	  println();
	  print("method ");
	  _isObject = false;
	  _state = ARG;
	  return new StringState(this, 'm', true);
	}
	else {
	  println((char) ch + ": unexpected char");
	  return popStack();
	}
	
      case VALUE:
	print(" => ");
	_isObject = false;
	_state = HEADER;
	return nextObject(ch);
	
      case ARG:
	if (ch == 'z')
	  return _next;
	else
	  return nextObject(ch);

      default:
	throw new IllegalStateException();
      }
    }
  }
  
  class ReplyState extends State {
    private static final int MAJOR = 0;
    private static final int MINOR = 1;
    private static final int HEADER = 2;
    private static final int VALUE = 3;
    private static final int END = 4;

    private int _state;
    private int _major;
    private int _minor;

    ReplyState(State next)
    {
      _next = next;
    }

    int depth()
    {
      return _next.depth() + 2;
    }
    
    State next(int ch)
    {
      switch (_state) {
      case MAJOR:
	if (ch == 't' || ch == 'S')
	  return new RemoteState(this).next(ch);
	
	_major = ch;
	_state = MINOR;
	return this;
	
      case MINOR:
	_minor = ch;
	_state = HEADER;
	println(-2, "reply " + _major + "." + _minor);
	return this;
	
      case HEADER:
	if (ch == 'H') {
	  _state = VALUE;
	  return new StringState(this, 'H', true);
	}
	else if (ch == 'f') {
	  print("fault ");
	  _isObject = false;
	  _state = END;
	  return new MapState(this, 0);
	}
 	else {
	  _state = END;
	  return nextObject(ch);
	}
	
      case VALUE:
	_state = HEADER;
	return nextObject(ch);
	
      case END:
	println();
	if (ch == 'z') {
	  return _next;
	}
	else
	  return _next.next(ch);

      default:
	throw new IllegalStateException();
      }
    }
  }
  
  class IndirectState extends State {
    IndirectState(State next)
    {
      super(next);
    }

    boolean isShift(Object object)
    {
      return _next.isShift(object);
    }

    State shift(Object object)
    {
      return _next.shift(object);
    }
    
    State next(int ch)
    {
      return nextObject(ch);
    }
  }
  
  class RemoteState extends State {
    private static final int TYPE = 0;
    private static final int VALUE = 1;
    private static final int END = 2;

    private int _state;
    private int _major;
    private int _minor;

    RemoteState(State next)
    {
      super(next);
    }
    
    State next(int ch)
    {
      switch (_state) {
      case TYPE:
	println(-1, "remote");
	if (ch == 't') {
	  _state = VALUE;
	  return new StringState(this, 't', false);
	}
	else {
	  _state = END;
	  return nextObject(ch);
	}

      case VALUE:
	_state = END;
	return _next.nextObject(ch);

      case END:
	return _next.next(ch);

      default:
	throw new IllegalStateException();
      }
    }
  }
  
  class StreamingState extends State {
    private int _digit;
    private int _length;
    private boolean _isLast;
    private boolean _isFirst = true;

    private State _childState;

    StreamingState(State next, boolean isLast)
    {
      super(next);

      _isLast = isLast;
      _childState = new InitialState();
    }
    
    State next(int ch)
    {
      if (_digit < 2) {
	_length = 256 * _length + ch;
	_digit++;

	if (_digit == 2 && _length == 0 && _isLast) {
	  _refId = 0;
	  return _next;
	}
	else {
	  if (_digit == 2)
	    println(-1, "packet-start(" + _length + ")");
	  
	  return this;
	}
      }
      else if (_length == 0) {
	_isLast = (ch == 'P');
	_digit = 0;
	
	return this;
      }

      _childState = _childState.next(ch);

      _length--;

      if (_length == 0 && _isLast) {
	println(-1, "");
	println(-1, "packet-end");
	_refId = 0;
	return _next;
      }
      else
	return this;
    }
  }

  static class ObjectDef {
    private String _type;
    private ArrayList<String> _fields;

    ObjectDef(String type, ArrayList<String> fields)
    {
      _type = type;
      _fields = fields;
    }

    String getType()
    {
      return _type;
    }

    ArrayList<String> getFields()
    {
      return _fields;
    }
  }
}

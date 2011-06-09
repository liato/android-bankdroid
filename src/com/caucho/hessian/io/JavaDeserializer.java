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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Serializing an object for known object types.
 */
public class JavaDeserializer extends AbstractMapDeserializer {
  private static final Logger log = Logger.getLogger(JavaDeserializer.class.getName());
  
  private final static Map<Class<?>, FieldDeserializer> deserializerCache = new HashMap<Class<?>, FieldDeserializer>();
  
  private final Class<?> _type;
  private Map<String, FieldDeserializer> _fieldMap;
  private Map<String, Field> _keyFieldMap;
  private Method _readResolve;
  
  private Constructor _constructor;
  private Object []_constructorArgs;
  
  public JavaDeserializer(Class<?> type)
  {
    _type = type;
    _keyFieldMap = new HashMap<String, Field>();
    
    initFieldMap(type);

    _readResolve = getReadResolve(type);

    if (_readResolve != null) {
      _readResolve.setAccessible(true);
    }

    Constructor []constructors = type.getDeclaredConstructors();
    long bestCost = Long.MAX_VALUE;

    for (int i = 0; i < constructors.length; i++) {
      Class []param = constructors[i].getParameterTypes();
      long cost = 0;

      for (int j = 0; j < param.length; j++) {
	cost = 4 * cost;

	if (Object.class.equals(param[j]))
	  cost += 1;
	else if (String.class.equals(param[j]))
	  cost += 2;
	else if (int.class.equals(param[j]))
	  cost += 3;
	else if (long.class.equals(param[j]))
	  cost += 4;
	else if (param[j].isPrimitive())
	  cost += 5;
	else
	  cost += 6;
      }

      if (cost < 0 || cost > (1 << 48))
	cost = 1 << 48;

      cost += (long) param.length << 48;

      if (cost < bestCost) {
        _constructor = constructors[i];
        bestCost = cost;
      }
    }

    if (_constructor != null) {
      _constructor.setAccessible(true);
      Class []params = _constructor.getParameterTypes();
      _constructorArgs = new Object[params.length];
      for (int i = 0; i < params.length; i++) {
        _constructorArgs[i] = getParamArg(params[i]);
      }
    }
  }

  public Class<?> getType()
  {
    return _type;
  }
    
  public Object readMap(AbstractHessianInput in)
    throws IOException
  {
    try {
      Object obj = instantiate();
      return readMap(in, obj);
    } catch (IOException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IOExceptionWrapper(_type.getName() + ":" + e.getMessage(), e);
    }
  }
    
  public Object readObject(AbstractHessianInput in, String []fieldNames)
    throws IOException
  {
    try {
      Object obj = instantiate();

      return readObject(in, obj, fieldNames);
    } catch (IOException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IOExceptionWrapper(_type.getName() + ":" + e.getMessage(), e);
    }
  }

  private static Method getReadResolve(Class<?> cl)
  {
    try {
		return cl.getMethod("readResolve", new Class[0]);
	} catch (Exception e) {}
	return null;
  }
    
  public Object readMap(AbstractHessianInput in, Object obj)
    throws IOException
  {
    try {
      int ref = in.addRef(obj);

      while (! in.isEnd()) {
        Object key = in.readObject();
        
        final Field field = _keyFieldMap.get(key);
		final FieldDeserializer deser = (FieldDeserializer) _fieldMap.get(key);

        if (deser != null)
          deser.deserialize(in, field, obj);
        else
          in.readObject();
      }
      
      in.readMapEnd();

      Object resolve = resolve(obj);

      if (obj != resolve)
	in.setRef(ref, resolve);

      return resolve;
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOExceptionWrapper(e);
    }
  }
    
  public Object readObject(AbstractHessianInput in,
			   Object obj,
			   String []fieldNames)
    throws IOException
  {
    try {
      int ref = in.addRef(obj);

      for (int i = 0; i < fieldNames.length; i++) {
        String name = fieldNames[i];
        
        final Field field = _keyFieldMap.get(name);
		final FieldDeserializer deser = (FieldDeserializer) _fieldMap.get(name);

        if (deser != null)
        	deser.deserialize(in, _keyFieldMap.get(name), obj);
        else
          in.readObject();
      }

      Object resolve = resolve(obj);

      if (obj != resolve)
	in.setRef(ref, resolve);

      return resolve;
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOExceptionWrapper(obj.getClass().getName() + ":" + e, e);
    }
  }

  private Object resolve(Object obj)
    throws Exception
  {
    // if there's a readResolve method, call it
    try {
      if (_readResolve != null)
        return _readResolve.invoke(obj, new Object[0]);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() != null)
	throw e;
    }

    return obj;
  }

  protected <T> T instantiate()
    throws Exception
  {
    try {
      if (_constructor != null)
	return (T) _constructor.newInstance(_constructorArgs);
      else
	return (T) _type.newInstance();
    } catch (Exception e) {
      throw new HessianProtocolException("'" + _type.getName() + "' could not be instantiated", e);
    }
  }

  /**
   * Creates a map of the classes fields.
   */
  private void initFieldMap(Class<?> cl)
  {
    _fieldMap = new HashMap<String, FieldDeserializer>();
    
    while (cl != null) {
      Field []fields = cl.getDeclaredFields();
      for (int i = 0; i < fields.length; i++) {
        final Field field = fields[i];

        if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
          continue;
        else if (_fieldMap.get(field.getName()) != null)
          continue;

        try {
          field.setAccessible(true);
        } catch (Throwable e) {}

		final Class<?> type = field.getType();
		FieldDeserializer deser = null;
	
		if (!deserializerCache.containsKey(type))  {
			if (String.class.equals(type))
				deser = new StringFieldDeserializer();
			else if (byte.class.equals(type)) {
				deser = new ByteFieldDeserializer();
			}
			else if (short.class.equals(type)) {
				deser = new ShortFieldDeserializer();
			}
			else if (int.class.equals(type)) {
				deser = new IntFieldDeserializer();
			}
			else if (long.class.equals(type)) {
				deser = new LongFieldDeserializer();
			}
			else if (float.class.equals(type)) {
				deser = new FloatFieldDeserializer();
			}
			else if (double.class.equals(type)) {
				deser = new DoubleFieldDeserializer();
			}
			else if (boolean.class.equals(type)) {
				deser = new BooleanFieldDeserializer();
			}
			else if (java.sql.Date.class.equals(type)) {
				deser = new SqlDateFieldDeserializer();
			}
			else if (java.sql.Timestamp.class.equals(type)) {
				deser = new SqlTimestampFieldDeserializer();
			}
			else if (java.sql.Time.class.equals(type)) {
				deser = new SqlTimeFieldDeserializer();
			}
			else {
				deser = new ObjectFieldDeserializer();
			}	
			
			deserializerCache.put(type, deser);
		}
	
		_fieldMap.put(field.getName(), deserializerCache.get(type));
		_keyFieldMap.put(field.getName(), field);
	  }
      cl = cl.getSuperclass();
    }
  }

  /**
   * Creates a map of the classes fields.
   */
  protected static Object getParamArg(Class cl)
  {
    if (! cl.isPrimitive())
      return null;
    else if (boolean.class.equals(cl))
      return Boolean.FALSE;
    else if (byte.class.equals(cl))
      return new Byte((byte) 0);
    else if (short.class.equals(cl))
      return new Short((short) 0);
    else if (char.class.equals(cl))
      return new Character((char) 0);
    else if (int.class.equals(cl))
      return Integer.valueOf(0);
    else if (long.class.equals(cl))
      return Long.valueOf(0);
    else if (float.class.equals(cl))
      return Float.valueOf(0);
    else if (double.class.equals(cl))
      return Double.valueOf(0);
    else
      throw new UnsupportedOperationException();
  }

  abstract static class FieldDeserializer {
    abstract void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException;
  }

  static class ObjectFieldDeserializer extends FieldDeserializer {
    void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      Object value = null;
      
      try {
	value = in.readObject(field.getType());
	
	field.set(obj, value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class BooleanFieldDeserializer extends FieldDeserializer {
    void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      boolean value = false;
      
      try {
	value = in.readBoolean();
	
	field.setBoolean(obj, value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class ByteFieldDeserializer extends FieldDeserializer {
     void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      int value = 0;
      
      try {
	value = in.readInt();
	
	field.setByte(obj, (byte) value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class ShortFieldDeserializer extends FieldDeserializer {
     void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      int value = 0;
      
      try {
	value = in.readInt();
	
	field.setShort(obj, (short) value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class IntFieldDeserializer extends FieldDeserializer {
     void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      int value = 0;
      
      try {
	value = in.readInt();
	
	field.setInt(obj, value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class LongFieldDeserializer extends FieldDeserializer {
    void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      long value = 0;
      
      try {
	value = in.readLong();
	
	field.setLong(obj, value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class FloatFieldDeserializer extends FieldDeserializer {
    void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      double value = 0;
      
      try {
	value = in.readDouble();
	
	field.setFloat(obj, (float) value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class DoubleFieldDeserializer extends FieldDeserializer {
    void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      double value = 0;
      
      try {
	value = in.readDouble();
	
	field.setDouble(obj, value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class StringFieldDeserializer extends FieldDeserializer {
     void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      String value = null;
      
      try {
	value = in.readString();
	
	field.set(obj, value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class SqlDateFieldDeserializer extends FieldDeserializer {
     void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      java.sql.Date value = null;

      try {
        java.util.Date date = (java.util.Date) in.readObject();
        value = new java.sql.Date(date.getTime());

        field.set(obj, value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class SqlTimestampFieldDeserializer extends FieldDeserializer {
    void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      java.sql.Timestamp value = null;

      try {
        java.util.Date date = (java.util.Date) in.readObject();
        value = new java.sql.Timestamp(date.getTime());

        field.set(obj, value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static class SqlTimeFieldDeserializer extends FieldDeserializer {
    void deserialize(AbstractHessianInput in, Field field, Object obj)
      throws IOException
    {
      java.sql.Time value = null;

      try {
        java.util.Date date = (java.util.Date) in.readObject();
        value = new java.sql.Time(date.getTime());

        field.set(obj, value);
      } catch (Exception e) {
        logDeserializeError(field, obj, value, e);
      }
    }
  }

  static void logDeserializeError(Field field, Object obj, Object value,
                                  Throwable e)
    throws IOException
  {
    String fieldName = (field.getDeclaringClass().getName()
                        + "." + field.getName());

    if (e instanceof HessianFieldException)
      throw (HessianFieldException) e;
    else if (e instanceof IOException)
      throw new HessianFieldException(fieldName + ": " + e.getMessage(), e);
    
    if (value != null)
      throw new HessianFieldException(fieldName + ": " + value.getClass().getName() + " (" + value + ")"
					 + " cannot be assigned to '" + field.getType().getName() + "'");
    else
       throw new HessianFieldException(fieldName + ": " + field.getType().getName() + " cannot be assigned from null", e);
  }
}

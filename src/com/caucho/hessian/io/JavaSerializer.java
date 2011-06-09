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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serializing an object for known object types.
 */
public class JavaSerializer extends AbstractSerializer
{
  private static final Logger log = Logger.getLogger(JavaSerializer.class.getName());

  private static Map<Class<?>, ArrayList<Field>> fieldsCache = new HashMap<Class<?>, ArrayList<Field>>();
  private static Map<Class<?>, ArrayList<FieldSerializer>> fieldSerializerCache = new HashMap<Class<?>, ArrayList<FieldSerializer>>();
  
  private Object _writeReplaceFactory;
  private Method _writeReplace;
  
  private Class<?> _type;
  
  public JavaSerializer(Class<?> cl)
  {
	_type = cl;
	
	introspectWriteReplace(cl);
    
    if (_writeReplace != null)
      _writeReplace.setAccessible(true);

    if (!fieldsCache.containsKey(cl))  {
    	final ArrayList<Field> primitiveFields = new ArrayList<Field>();
    	final ArrayList<Field> compoundFields = new ArrayList<Field>();
    	final ArrayList<FieldSerializer> fieldSerializers = new ArrayList<FieldSerializer>();
	    
    	
    	
	    for (; cl != null; cl = cl.getSuperclass()) {
	      final Field []fields = cl.getDeclaredFields();
	      for (int i = 0; i < fields.length; i++) {
	    	Field field = fields[i];
	
			if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
			  continue;
	
			field.setAccessible(true);
		
			if (field.getType().isPrimitive() || (field.getType().getName().startsWith("java.lang.") && ! field.getType().equals(Object.class)))
			  primitiveFields.add(field);
			else
			  compoundFields.add(field);
		  }
		}
	    
	    final ArrayList<Field> result = new ArrayList<Field>();
	    result.addAll(primitiveFields);
	    result.addAll(compoundFields);
	    
	    fieldsCache.put(_type, result);
	    fieldSerializerCache.put(_type, fieldSerializers);
	    
	    for (int i = 0; i < result.size(); i++) {
	    	fieldSerializers.add(getFieldSerializer(result.get(i).getType()));
	    }
    }
  }

  private void introspectWriteReplace(Class cl)
  {
    try {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      String className = cl.getName() + "HessianSerializer";

      Class serializerClass = Class.forName(className, false, loader);

      Object serializerObject = serializerClass.newInstance();

      Method writeReplace = getWriteReplace(serializerClass, cl);

      if (writeReplace != null) {
	_writeReplaceFactory = serializerObject;
	_writeReplace = writeReplace;

	return;
      }
    } catch (ClassNotFoundException e) {
    } catch (Exception e) {
      log.log(Level.FINER, e.toString(), e);
    }
      
    _writeReplace = getWriteReplace(cl);
  }

  /**
   * Returns the writeReplace method
   */
  protected static Method getWriteReplace(Class<?> cl)
  {
    try {
		return cl.getMethod("writeReplace", new Class[0]);
	} catch (Exception e) {}
	
	return null;
  }

  /**
   * Returns the writeReplace method
   */
  protected Method getWriteReplace(Class<?> cl, Class<?> param)
  {
    try {
		return cl.getMethod("writeReplace", new Class[] { param });
	} catch (Exception e) {}
	
	return null;
  }
  
  public void writeObject(Object obj, AbstractHessianOutput out)
    throws IOException
  {
    if (out.addRef(obj)) {
      return;
    }
    
    final Class<?> cl = obj.getClass();

    try {
      if (_writeReplace != null) {
	Object repl;

	if (_writeReplaceFactory != null)
	  repl = _writeReplace.invoke(_writeReplaceFactory, obj);
	else
	  repl = _writeReplace.invoke(obj);

	out.removeRef(obj);

	out.writeObject(repl);

	out.replaceRef(repl, obj);

	return;
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      // log.log(Level.FINE, e.toString(), e);
      throw new RuntimeException(e);
    }

    int ref = out.writeObjectBegin(cl.getName());

    if (ref < -1) {
      writeObject10(obj, out);
    }
    else {
      if (ref == -1) {
	writeDefinition20(out);
	out.writeObjectBegin(cl.getName());
      }

      writeInstance(obj, out);
    }
  }
  
  private void writeObject10(Object obj, AbstractHessianOutput out)
    throws IOException
  {
  	final ArrayList<Field> fields = fieldsCache.get(_type);
  	final ArrayList<FieldSerializer> fieldSerializers = fieldSerializerCache.get(_type);
    
  	for (int i = 0; i < fields.size(); i++) {
      final Field field = fields.get(i);

      out.writeString(field.getName());
	
      fieldSerializers.get(i).serialize(out, obj, field);
    }
      
    out.writeMapEnd();
  }
  
  private void writeDefinition20(AbstractHessianOutput out)
    throws IOException
  {
	final ArrayList<Field> fields = fieldsCache.get(_type);
	  
    out.writeClassFieldLength(fields.size());
	
    for (int i = 0; i < fields.size(); i++) {
      final Field field = fields.get(i);
      
      out.writeString(field.getName());
    }
  }
  
  public void writeInstance(Object obj, AbstractHessianOutput out)
    throws IOException
  {
	final ArrayList<Field> fields = fieldsCache.get(_type);
	final ArrayList<FieldSerializer> fieldSerializers = fieldSerializerCache.get(_type);	  
	  
    for (int i = 0; i < fields.size(); i++) {
      final Field field = fields.get(i);

      fieldSerializers.get(i).serialize(out, obj, field);
    }
  }

  private static FieldSerializer getFieldSerializer(Class<?> type)
  {
    if (int.class.equals(type)
        || byte.class.equals(type)
        || short.class.equals(type)
        || int.class.equals(type)) {
      return IntFieldSerializer.SER;
    }
    else if (long.class.equals(type)) {
      return LongFieldSerializer.SER;
    }
    else if (double.class.equals(type) ||
        float.class.equals(type)) {
      return DoubleFieldSerializer.SER;
    }
    else if (boolean.class.equals(type)) {
      return BooleanFieldSerializer.SER;
    }
    else if (String.class.equals(type)) {
      return StringFieldSerializer.SER;
    }
    else if (java.util.Date.class.equals(type)
             || java.sql.Date.class.equals(type)
             || java.sql.Timestamp.class.equals(type)
             || java.sql.Time.class.equals(type)) {
      return DateFieldSerializer.SER;
    }
    else
      return FieldSerializer.SER;
  }

  static class FieldSerializer {
    static final FieldSerializer SER = new FieldSerializer();
    
    void serialize(AbstractHessianOutput out, Object obj, Field field)
      throws IOException
    {
      Object value = null;
	
      try {
	value = field.get(obj);
      } catch (IllegalAccessException e) {
	log.log(Level.FINE, e.toString(), e);
      }

      try {
	out.writeObject(value);
      } catch (RuntimeException e) {
	throw new RuntimeException(e.getMessage() + "\n Java field: " + field,
				   e);
      } catch (IOException e) {
	throw new IOExceptionWrapper(e.getMessage() + "\n Java field: " + field,
			      e);
      }
    }
  }

  static class BooleanFieldSerializer extends FieldSerializer {
    static final FieldSerializer SER = new BooleanFieldSerializer();
    
    void serialize(AbstractHessianOutput out, Object obj, Field field)
      throws IOException
    {
      boolean value = false;
	
      try {
	value = field.getBoolean(obj);
      } catch (IllegalAccessException e) {
	log.log(Level.FINE, e.toString(), e);
      }

      out.writeBoolean(value);
    }
  }

  static class IntFieldSerializer extends FieldSerializer {
    static final FieldSerializer SER = new IntFieldSerializer();
    
    void serialize(AbstractHessianOutput out, Object obj, Field field)
      throws IOException
    {
      int value = 0;
	
      try {
	value = field.getInt(obj);
      } catch (IllegalAccessException e) {
	log.log(Level.FINE, e.toString(), e);
      }

      out.writeInt(value);
    }
  }

  static class LongFieldSerializer extends FieldSerializer {
    static final FieldSerializer SER = new LongFieldSerializer();
    
    void serialize(AbstractHessianOutput out, Object obj, Field field)
      throws IOException
    {
      long value = 0;
	
      try {
	value = field.getLong(obj);
      } catch (IllegalAccessException e) {
	log.log(Level.FINE, e.toString(), e);
      }

      out.writeLong(value);
    }
  }

  static class DoubleFieldSerializer extends FieldSerializer {
    static final FieldSerializer SER = new DoubleFieldSerializer();
    
    void serialize(AbstractHessianOutput out, Object obj, Field field)
      throws IOException
    {
      double value = 0;
	
      try {
	value = field.getDouble(obj);
      } catch (IllegalAccessException e) {
	log.log(Level.FINE, e.toString(), e);
      }

      out.writeDouble(value);
    }
  }

  static class StringFieldSerializer extends FieldSerializer {
    static final FieldSerializer SER = new StringFieldSerializer();
    
    void serialize(AbstractHessianOutput out, Object obj, Field field)
      throws IOException
    {
      String value = null;
	
      try {
	value = (String) field.get(obj);
      } catch (IllegalAccessException e) {
	log.log(Level.FINE, e.toString(), e);
      }

      out.writeString(value);
    }
  }

  static class DateFieldSerializer extends FieldSerializer {
    static final FieldSerializer SER = new DateFieldSerializer();

    void serialize(AbstractHessianOutput out, Object obj, Field field)
      throws IOException
    {
      java.util.Date value = null;

      try {
        value = (java.util.Date) field.get(obj);
      } catch (IllegalAccessException e) {
        log.log(Level.FINE, e.toString(), e);
      }

      
      if (value != null) out.writeUTCDate(value.getTime()); else out.writeNull();
    }
  }
}

package com.liato.bankdroid.banking.banks.nordea.api.mapper;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class NordeaAttributeNamingStrategy extends PropertyNamingStrategy{

	private static final long serialVersionUID = 8367765855190584779L;

	    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
	      return convert(defaultName,method);
	    }

	    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
	      return convert(defaultName,method);
	    }

	    private String convert(String input,AnnotatedMethod method) {
	      //  easy: replace capital letters with underscore, lower-cases equivalent
	      StringBuilder result = new StringBuilder();
	      
	      JacksonXmlProperty element = method.getAnnotation(JacksonXmlProperty.class); 
	      
	      if(element != null && element.isAttribute()) {
	    	  result.append("@");
	      }
	      result.append(input);
	      
	      return result.toString();
	    }
}

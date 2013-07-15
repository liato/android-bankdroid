package com.liato.bankdroid.banking.banks.nordea.api.mapper;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;

public class NordeaObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 1561927175762963656L;

    public NordeaObjectMapper() {
		super();
		this.setAnnotationIntrospector(new JacksonXmlAnnotationIntrospector());
		this.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		this.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		this.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		this.setPropertyNamingStrategy(new NordeaAttributeNamingStrategy());
		this.getFactory().setCharacterEscapes(new SlashCharacterEscapes());
		
		//Replace empty strings with null during deserialization
		SimpleModule emptyStringModule = new SimpleModule("EmptyStringModule",new Version(1,0,0,null,null,null));
		emptyStringModule.addDeserializer(String.class, new EmptyStringDeserializer());
		this.registerModule(emptyStringModule);
		
		//Replace null objects with empty map during serialization
		DefaultSerializerProvider sp = new DefaultSerializerProvider.Impl();
		   sp.setNullValueSerializer(new NordeaNullSerializer());
		   this.setSerializerProvider(sp);
	}

	public <T> T readValue(String pBadgerfish, Class<T> pValueType)
			throws JsonParseException, JsonMappingException, IOException {
		
		String formatted = pBadgerfish.replaceAll("\\{\\\"\\$\\\":(.*?)\\}",
				"$1");
		formatted = formatted.replaceAll("@(.*?\\\")", "$1");
		formatted = formatted.replaceAll("\\{\\}", "\\\"\\\"");
		return super.readValue(formatted, pValueType);
	}

	public String writeValueAsString(Object pValue)
			throws JsonProcessingException {

		String unwrapped = super.writeValueAsString(pValue);
		unwrapped =  unwrapped.replaceAll(
				"\\\":(?!\\[)(?!\\{)([\\\"]?)(.*?)([\\\"]?)([,\\}])",
				"\\\":\\{\\\"\\$\\\":$1$2$3\\}$4");
		unwrapped = unwrapped.replaceAll("(\\\"\\$\\\":)\\{\\\"\\$\\\":(.*?)\\}", "$1$2");
		
		return unwrapped.replaceAll("\\{\\\"\\$\\\":\\\"\\\"\\}", "\\{\\}");

	}
}

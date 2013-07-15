package com.liato.bankdroid.banking.banks.nordea.api.mapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;

public class SlashCharacterEscapes extends CharacterEscapes {

	private static final long serialVersionUID = -116806559058328601L;

	private final int[] asciiEscapes;
	
	public SlashCharacterEscapes() {
	int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
	esc['/'] = CharacterEscapes.ESCAPE_CUSTOM;
	asciiEscapes = esc;
		
	}
	
	@Override
	 // this method gets called for character codes 0 - 127
	public int[] getEscapeCodesForAscii() {
		return asciiEscapes;
	}

	@Override
	// and this for others; we don't need anything special here
	public SerializableString getEscapeSequence(int ch) {
		if(ch == '/') {
			return new SerializableString() {

				public String getValue() {
					return "\\/";
				}

				public int charLength() {
					// TODO Auto-generated method stub
					return 2;
				}

				public char[] asQuotedChars() {
					 return new char[]{'\\','/'};
				}

				public byte[] asUnquotedUTF8() {
					return new byte[]{'\\','/'};
				}

				public byte[] asQuotedUTF8() {
					 return new byte[]{'\\','/'};
				}

				public int appendQuotedUTF8(byte[] buffer, int offset) {
					// TODO Auto-generated method stub
					return 0;
				}

				public int appendQuoted(char[] buffer, int offset) {
					// TODO Auto-generated method stub
					return 0;
				}

				public int appendUnquotedUTF8(byte[] buffer, int offset) {
					// TODO Auto-generated method stub
					return 0;
				}

				public int appendUnquoted(char[] buffer, int offset) {
					// TODO Auto-generated method stub
					return 0;
				}

				public int writeQuotedUTF8(OutputStream out) throws IOException {
					// TODO Auto-generated method stub
					return 0;
				}

				public int writeUnquotedUTF8(OutputStream out)
						throws IOException {
					// TODO Auto-generated method stub
					return 0;
				}

				public int putQuotedUTF8(ByteBuffer buffer) throws IOException {
					// TODO Auto-generated method stub
					return 0;
				}

				public int putUnquotedUTF8(ByteBuffer out) throws IOException {
					// TODO Auto-generated method stub
					return 0;
				}
				
			};
		}
		return null;
	}

}

/*
 * Copyright © 2011 Global Phasing Ltd. All rights reserved.
 * 
 * Author: Peter Keller
 * 
 * This file forms part of the GPhL StarTools library.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the
 *  distribution.
 *
 *  If the regular expression used to match STAR/CIF data in the
 *  redistribution is not identical to that in the original version,
 *  this fact must be stated wherever the copyright notice is
 *  reproduced.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package com.globalphasing.startools;

import static com.globalphasing.startools.StarTokenTypes.*;

/**
 * Class representing a token from a STAR file.
 * 
 * Normally, this class is not instantiated directly, but instances are returned
 * by successive invocations of the {@link StarTokeniser#nextToken()} method.
 * 
 * @author Peter Keller
 */

public class StarToken {

	private int m_token_type;
	private String m_token_value;
	
	/* This is the line number in the file where the matching operation
	 * that gave rise to this token started. It is not the line number
	 * where the token is located.
	 */
	private int m_line_number_start;
	private int m_line_number_end;

	/* This is the character offset from the start of this.m_line_number
	 * where the token is found.
	 */
	private int m_start;
	private int m_end;
	
	StarToken( int token_type, String token_value, 
			int line_number_start, int line_number_end, 
			int start, int end ) {
		this( token_type, token_value );
		this.m_line_number_start = line_number_start;
		this.m_line_number_end = line_number_end;
		this.m_start = start;
		this.m_end = end;
	}
	
	/**
	 * Same as {@link #StarToken(int, String, int, int, int, int)},
	 * but values of 0 are used for <code>line_number_start</code>,
	 * <code>line_number_end</code>, <code>start</code>
	 * and <code>end</code>. 
	 * 
	 * @param token_type
	 * @param token_value
	 */
	
	StarToken( int token_type, String token_value ) {
		this.m_token_type  = token_type;
		this.m_token_value = token_value;
		this.m_line_number_start = 0;
		this.m_line_number_end = 0;
		this.m_start = 0;
		this.m_end = 0;
	}
	
	/**
	 * Getter for token's numeric type.
	 * 
	 * @return Numeric type of token, as defined by one of the constants in {@link StarTokenTypes}
	 */
		
	public int getType() {
		return this.m_token_type;
	}
	
	/**
	 * Getter for token's type as a descriptive string. 
	 * 
	 * @return Descriptive name of token's type (the name of one of the constants in 
	 * {@link StarTokenTypes} with the initial <tt>TOKEN_</tt> removed).
	 */
	
	public String getTypeString() {
		return tokenTypeAsString( this.m_token_type );
	}

	/**
	 * Getter for token's value as a string.
	 * 
	 * @return Token's value.  Any enclosing quotes/semi-colons will have been removed.
	 */
	
	public String getValue() {
		return this.m_token_value;
	}
	
	/**
	 * Return <tt>true</tt> if token is a valid STAR token, but not allowed in CIF's or mmCIF's.
	 * 
	 * @return Whether or not token is only valid in a STAR context.
	 */
	
	public boolean starOnly() {
		return starOnlyToken( this.m_token_type );
	}
	
	/**
	 * Starting line number of the match operation that produced this
	 * token. This is not necessarily the line number where the token
	 * is located.
	 * 
	 * When the token was produced from character data (by using
	 * {@link StarTokeniser#startMatching(CharSequence)}) or by matching
	 * the entire file contents in a single operation (by using
	 * {@link StarTokeniser#startMatching(java.io.File, boolean)}),
	 * the match operation will always have started at the beginning
	 * of the data. Accordingly, this method will return a value of 1.
	 * 
	 * @return Starting line number of match operation.
	 */
	
	public int getLineNumberStart() {
		return this.m_line_number_start;
	}

	/**
	 * Ending line number of the match operation that produced this token.
	 * 
	 * When the token was produced by tokenising a file in line-oriented mode
	 * (i.e. by invoking {@link StarTokeniser#startMatching(java.io.File)}
	 * or {@link StarTokeniser#startMatching(java.io.File, int)} this method
	 * returns the line number of the last line of the data that was being
	 * matched when this token was produced.
	 * 
	 * When the token was produced from character data (by using
	 * {@link StarTokeniser#startMatching(CharSequence)}) or by matching
	 * the entire file contents in a single operation (by using
	 * {@link StarTokeniser#startMatching(java.io.File, boolean)}),
	 * this method returns {@link StarTokenTypes#EOF}
	 * 
	 * @return Ending line number of match operation or 
	 * {@link StarTokenTypes#EOF} 
	 */
	
	public int getLineNumberEnd() {
		return this.m_line_number_end;
	}
	
	/**
	 * Character offset from the start of the line returned by
	 * {@link #getLineNumberStart()} that corresponds to the first character
	 * of the match that produced this token.
	 * 
	 * Note that this value may not be meaningful when processing very large files
	 * using {@link StarTokeniser#startMatching(java.io.File, boolean)}
	 * 
	 * @return character offset of start of match
	 */
	
	public int getMatchStart() {
		return this.m_start;
	}
	
	/**
	 * Character offset from the start of the line returned by
	 * {@link #getLineNumberStart()} that corresponds to the character
	 * immediately after the last one of the match that produced this token.
	 * 
	 * Note that this value may not be meaningful when processing very large files
	 * using {@link StarTokeniser#startMatching(java.io.File, boolean)}
	 * 
	 * @return character offset of end of match
	 */

	public int getMatchEnd() {
		return this.m_end;
	}
	
	/**
	 * Returns a human-readable summary of the token's contents.
	 */
	
	public String toString() {
		StringBuilder retval = 
			new StringBuilder( "Type: " + this.getTypeString() + "; " );
		if ( this.m_start > -1 && this.m_end > -1 && this.m_line_number_start > 0 ) {
			retval.append( "line/start-end: " + this.m_line_number_start + "/" + this.m_start + "-" + this.m_end + "; ");
		}
		
		retval.append( "Value: >>>" + this.m_token_value + "<<<" );
		return retval.toString();
	}
}

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

/**
 * Class containing static final constants required for tokenising STAR data,
 * and static methods for interpreting those constants.
 * 
 * @author Peter Keller
 *
 */

public class StarTokenTypes {


	// Token types for match groups. These assignments must be kept consistent
	// with the capturing groups of StarRegex.REGEX
	
	/** <b>Token:</b> Multiline text.*/
	public static final int TOKEN_MULTILINE		= 1;
	/** <b>Token:</b> Comment */ 
	public static final int TOKEN_COMMENT			= 2;
	/** <b>Token:</b> STAR global block (forbidden in CIF&#39;s). */
	public static final int TOKEN_GLOBAL			= 3;
	/** <b>Token:</b> STAR save frame header or terminator (forbidden in CIF&#39;s) */
	public static final int TOKEN_SAVE_FRAME		= 4;
	/** <b>Token:</b> STAR save frame reference (forbidden in CIF&#39;s) */
	public static final int TOKEN_SAVE_FRAME_REF	= 5;
	/** <b>Token:</b> Loop terminator (forbidden in CIF&#39;s) */
	public static final int TOKEN_LOOP_STOP		= 6;
	/** <b>Token:</b> Data block header */
	public static final int TOKEN_DATA_BLOCK		= 7;
	/** <b>Token:</b> Loop initiator */
	public static final int TOKEN_LOOP			= 8;

	/** <b>Token:</b> Token that violates STAR rules on privileged constructs.
	 * <br>
	 * The STAR standard specifies that all unquoted strings that start with one of the
	 * following character sequences are privileged constructs:
	 * <ul>
	 * 	<li><tt>data_</tt></li>
	 * 	<li><tt>loop_</tt></li>
	 * 	<li><tt>save_</tt></li>
	 * 	<li><tt>global_</tt></li>
	 *  <li><tt>stop_</tt></li>
	 * </ul>
	 * <p>Of these, <tt>data_</tt> <em>must not</em> be followed by whitespace,
	 * and <tt>loop_</tt>, <tt>global_</tt> and <tt>stop_</tt> <em>must</em>
	 * be followed by whitespace. (<tt>save_</tt> may or may not be followed by whitespace.)
	 * The production of this token indicates that these rules have been violated.</p>
	 * 
	 * <p>See: Specification of the STAR file, Hall,&nbsp;S.R. and Spadaccini,&nbsp;N., Int. Tab. vol. G 
	 * &sect; 2.1.3.10 (Springer 2005)</p>
	 * 
	 * <p>On the other hand, the CIF specification implies that unquoted tokens that
	 * start with one of <tt>loop_</tt>, <tt>global_</tt> or <tt>stop_</tt> and are followed
	 * by at least one more non-whitespace character may be treated as non-quoted string values</p>
	 * 
	 * <p>See: Specification of the Crystallographic Information File (CIF), Hall,&nbsp;S.R., Westbrook,&nbsp;J.D.
	 * <em>et al.</em>, Int. Tab. vol. G &sect; 2.2.7.1.4, paragraphs (9), (10) and (11) </p>
	 * 
	 * <p>By default, an instance of StarTokeniser returns this token type for such
	 * a bad construct. Set {@link StarTokeniser#ALLOW_BAD_CONSTRUCT} in the {@link StarTokeniser#StarTokeniser(int)}
	 * constructor to return {@link #TOKEN_STRING} instead for non-quoted strings that start
	 * with <tt>loop_</tt>, <tt>global_</tt> or <tt>stop_</tt>.</p> 
	 */
	public static final int TOKEN_BAD_CONSTRUCT	= 9;
	/** <b>Token:</b> Data name */
	public static final int TOKEN_DATA_NAME		= 10;
	/** <b>Token:</b> Single-quoted string */
	public static final int TOKEN_SQUOTE_STRING	= 11;
	/** <b>Token:</b> Double-quoted string */
	public static final int TOKEN_DQUOTE_STRING	= 12;
	/** <b>Token:</b> CIF null value (unquoted <tt>.</tt> character) */
	public static final int TOKEN_NULL			= 13;
	
	/** 
	 * <b>Token:</b> CIF unknown value.
	 * 
	 * This production means that the token consists of an unquoted <tt>?</tt> character,
	 * which the CIF standard defines as representing an unknown value.
	 * It does not mean that the contents of the token couldn't be determined.
	 */ 
	public static final int TOKEN_UNKNOWN			= 14;

	/** <b>Token:</b> Token that starts with <tt>[</tt> or <tt>]</tt>.
	 *
	 * <p>These characters are reserved in the CIF specification for future
	 * use in delimiting multi-line text. It is unclear how or if the STAR
	 * specification would change if this notation was brought into use, or
	 * if the mmCIF standard would also change as a result.</p> 
	 * 
	 * <p>By default, an instance of StarTokeniser will return this token type
	 * for a token that starts with <tt>[</tt> or <tt>]</tt>.</p>
	 * <p>Set {@link StarTokeniser#ALLOW_SQUARE_BRACKET} in the {@link StarTokeniser#StarTokeniser(int)}
	 * constructor to return {@link #TOKEN_STRING} instead.</p>
	 * 
	 * <p>See: Specification of the Crystallographic Information File (CIF), Hall,&nbsp;S.R., Westbrook,&nbsp;J.D.
	 * <em>et al.</em>, Int. Tab. vol. G &sect; 2.2.7.1.4(19).</p>
	 */
	public static final int TOKEN_SQUARE_BRACKET	= 15;
	
	/** <b>Token:</b> Non-quoted string */
	public static final int TOKEN_STRING			= 16;

	/** <b>Token:</b> Catch-all token for a sequence of non-whitespace characters.
	 * 
	 * <p> If this token is returned, it means that none of the other tokens
	 * produced a match. This indicates a STAR syntax error, such as a 
	 * single/double quoted string or multi-line text token that is missing 
	 * its closing delimiter.</p>
	 * 
	 */
	public static final int TOKEN_BAD_TOKEN		= 17;

	// Indexable list of names of token types
	// Must be kept consistent with regex  and match group values.
	private static final String[] DESCRIPTIVE_TOKEN_TYPES = 
		{ "", "MULTILINE", "COMMENT", "GLOBAL", "SAVE_FRAME", "SAVE_FRAME_REF",
		"LOOP_STOP", "DATA_BLOCK", "LOOP", "BAD_CONSTRUCT", "DATA_NAME", "SQUOTE_STRING",
		"DQUOTE_STRING", "NULL", "UNKNOWN", "SQUARE_BRACKET", "STRING", "BAD_TOKEN" };
	
	
	
	/**
	 * Symbolic constant representing the last line of a file or character data. 
	 */
	public static final int EOF  = -1;
	
	/**
	 * Returns the string representation of a numerical token type
	 * 
	 * @param token_type numerical token type
	 * @return string representation of token type
	 */
	public static String tokenTypeAsString (int token_type) {
		return StarTokenTypes.DESCRIPTIVE_TOKEN_TYPES[token_type];
	}
	
	/**
	 * Returns true if <code>token_type</code> represents a valid STAR token, but not allowed in CIF's or mmCIF's.
	 * 
	 * @param token_type
	 * @return Whether or not <code>token_type</code> is only valid in a STAR context.
	 */
	
	public static boolean starOnlyToken (int token_type){
		return token_type >= StarTokenTypes.TOKEN_GLOBAL && token_type <= StarTokenTypes.TOKEN_LOOP_STOP;
	}
	
	/**
	 * Returns true if <code>token_type</code> represents one of the tokens
	 * that represents a STAR data value (as opposed to a data block header,
	 * data name, etc.)
	 * 
	 * 
	 * @param token_type
	 * @return Whether or not <code>token_type</code> is a data value
	 */
	
	public static boolean dataToken (int token_type) {
		return token_type == StarTokenTypes.TOKEN_DQUOTE_STRING 
			|| token_type == StarTokenTypes.TOKEN_MULTILINE
			|| token_type == StarTokenTypes.TOKEN_NULL
			|| token_type == StarTokenTypes.TOKEN_SQUOTE_STRING
			|| token_type == StarTokenTypes.TOKEN_STRING
			|| token_type == StarTokenTypes.TOKEN_UNKNOWN;
	}
	
	/**
	 * Returns true if <code>token_type</code> represents a token that is
	 * syntactically invalid according to the STAR standard.
	 * 
	 * @param token_type
	 * @return whether or not token violates STAR syntax
	 */
	public static boolean starErrorToken (int token_type) {
		return token_type == StarTokenTypes.TOKEN_BAD_CONSTRUCT
			|| token_type == StarTokenTypes.TOKEN_BAD_TOKEN;
	}
	
	// Hide constructor
	private StarTokenTypes() {}
	
}

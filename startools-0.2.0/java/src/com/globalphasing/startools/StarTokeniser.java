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

import java.io.IOException;
import java.lang.String;
import java.lang.CharSequence;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static com.globalphasing.startools.StarTokenTypes.*;

/**
 * Specialised STAR-oriented tokeniser analogous to 
 * {@link java.util.StringTokenizer}.
 * 
 *
 * This class should not be compared to 
 * {@link java.io.StreamTokenizer}, since
 * StarTokeniser knows nothing about numeric types, case (in)sensitivity of data
 * values etc. In the (mm)CIF world, this information is contained in the
 * (mm)CIF dictionary, so should be handled by the parser itself, not the
 * tokeniser<br>
 * 
 * <b>N.B.</b>Handling of very large files by this library has not been
 * characterised or tested in any way, and should not be relied on. Limitations
 * on handling such files in the current implementation arise from the standard
 * Java API itself, and include:
 * 
 * <ul>
 *  <li>The {@link #startMatching(java.io.File, boolean)} can memory map and 
 *  start matching a file longer than {@link java.lang.Integer#MAX_VALUE} 
 *  characters (2Gb in character sets where 1 character is 1 byte). However,
 *  the {@link java.util.regex.Matcher#start()} and
 *  {@link java.util.regex.Matcher#end()} methods that are used by this library
 *  return <code>int</code>, and the behaviour of the
 *  {@link java.util.regex.Matcher} class is unknown when matching continues
 *  beyond {@link java.lang.Integer#MAX_VALUE} characters.
 *  </li>
 *  <li>When matching is done by the {@link #startMatching(java.io.File)} or 
 *  {@link #startMatching(java.io.File, int)} methods, 
 *  {@link java.io.LineNumberReader#getLineNumber()} is used to keep track
 *  of the line number in the file. This latter method returns an <code>int</code>.
 *  The behaviour of the {@link java.io.LineNumberReader} class when handling
 *  a file with more than {@link java.lang.Integer#MAX_VALUE} lines is unknown.
 *  </li>
 * </ul>
 * 
 * @author Peter Keller<br>
 * 
 */

public class StarTokeniser {

	/**
	 * If set, {@link #nextToken()} returns a {@link StarToken} with a token
	 * type of {@link StarTokenTypes#TOKEN_STRING} 
	 * instead of {@link StarTokenTypes#TOKEN_SQUARE_BRACKET}
	 */
	public static final int ALLOW_SQUARE_BRACKET = 1;

	/**
	 * If set, {@link #nextToken()} returns a {@link StarToken} with a token
	 * type of {@link StarTokenTypes#TOKEN_STRING} 
	 * instead of {@link StarTokenTypes#TOKEN_BAD_CONSTRUCT}
	 */
	public static final int ALLOW_BAD_CONSTRUCT = 2;

	private int m_flags;

	private int m_debug_level = 0;

	private static Pattern m_pattern = Pattern.compile(StarRegex.REGEX, Pattern.UNICODE_CASE);
	private Matcher m_matcher;

	private java.io.File m_file = null;

	// When, and only when, the file is being read in line-oriented mode, these
	// fields store the file reader, current chunk, and starting and ending
	// line numbers of the chunk.
	private java.io.LineNumberReader m_data_reader = null;
	// m_chunk has to be a StringBuilder, because we call append on it.
	private java.lang.StringBuilder m_chunk = null;

	// This is the minimum number of lines in a chunk
	// The actual number of lines in a given chunk may be more,
	// since a chunk won't end in the middle of a multi-line text string
	private int m_chunk_size;

	// The lines in the file being handled that correspond to the
	// first and last lines in this.m_chunk.
	// When the data is not being read in chunks, m_chunk_start is set to 1,
	// and m_chunk_end to -1
	private int m_chunk_start, m_chunk_end;
	
	// If we are memory mapping or reading bytes directly from the file,
	// we use a CharSequence to point to the data
	private java.lang.CharSequence m_data = null;
	
	// Keep next StarToken instance to be returned by the nextToken() method
	private StarToken m_curr_token = null;
	
	/**
	 * Constructor for StarTokeniser class.
	 */

	public StarTokeniser() {
		this.m_flags = 0;
	}

	/**
	 * Constructor for StarTokeniser class.
	 * 
	 * @param flags
	 *            OR-ed list of flags to control the operation of the tokeniser.
	 *            Available flags are listed in the Field Summary.
	 * 
	 */

	public StarTokeniser(int flags) {
		this.m_flags = flags;
	}

	/**
	 * Get debug level for this tokeniser.
	 * 
	 * @return debug level
	 */

	public int getDebugLevel() {
		return this.m_debug_level;
	}

	/**
	 * Set debug level for this tokeniser.
	 * 
	 * @param level
	 *            0 means no debugging.
	 */
	public void setDebugLevel(int level) {
		this.m_debug_level = level;
	}

	/**
	 * Prepare StarTokeniser instance to tokenise a string.
	 * 
	 * This may be called on a StarTokeniser instance that has already been used 
	 * to match other data: the previous state will be lost and the object will reset
	 * with the new data.
	 * 
	 * @param data
	 *            {@link java.lang.CharSequence} containing STAR/CIF/mmCIF data
	 *            to be tokenised
	 */

	public void startMatching(CharSequence data) {
		this.m_file = null;
		this.m_data_reader = null;
		this.m_chunk_start = 1;
		this.m_chunk_end = StarTokenTypes.EOF;
		this._startMatching(data);
		this._setNextToken();
	}

	/**
	 * Prepare StarTokeniser instance to tokenise the contents of a file.
	 * 
	 * This may be called on a StarTokeniser instance that has already been used 
	 * to match other data: the previous state will be lost and the object will reset
	 * with the new data.
	 * 
	 * Using this method will cause the entire file contents to be matched
	 * against {@link StarRegex#REGEX} in a single operation.
	 * 
	 * @param file
	 *            Instance of {@link java.io.File} containing STAR data
	 * @param map
	 *            <code>true</code> to use memory mapping/direct buffer.
	 *            <code>false</code> to read contents of file into a buffer
	 *            and parse that. (Files longer than {@link java.lang.Integer#MAX_VALUE}
	 *            bytes (2Gb) will always be memory mapped.)
	 */
	public void startMatching(java.io.File file, boolean map) {

		java.nio.ByteBuffer buffer;

		// Allow garbage collector to reclaim any memory referred to
		// by these fields.
		this.m_data_reader = null;
		this.m_chunk = null;
		
		this.m_chunk_start = 1;
		this.m_chunk_end = StarTokenTypes.EOF;

		java.io.FileInputStream stream = null;
		try {
			stream = new java.io.FileInputStream(file);

			if (map || file.length() > java.lang.Integer.MAX_VALUE) {
				// Create ByteBuffer by mapping file. The buffer will be direct.
				// On *n*x, this should use mmap(2)
				buffer = stream.getChannel().map(
						java.nio.channels.FileChannel.MapMode.READ_ONLY, 0,
						file.length());
			} else {
				// Create ByteBuffer by allocating space and reading data from
				// file into it.
				// The buffer will be non-direct. Could use
				// ByteBuffer.allocateDirect, but
				// would there be any point? If it is really needed, the mapping
				// method
				// used above would probably be just as good
				buffer = java.nio.ByteBuffer.allocate((int) file.length());
				stream.read(buffer.array());
			}

			this.m_data = java.nio.charset.Charset.defaultCharset()
					.decode(buffer);
			if (this.m_matcher == null) {
				this.m_matcher = StarTokeniser.m_pattern.matcher(this.m_data);
			} else {
				this.m_matcher.reset(this.m_data);
			}

		} catch (java.io.FileNotFoundException e) {
			StarTokeniser._abort("Cannot open file " + file.toString(), e);
		} catch (java.nio.channels.NonReadableChannelException e) {
			StarTokeniser._abort("File " + file.toString()
					+ "was not opened for reading", e);
		} catch (java.io.IOException e) {
			StarTokeniser._abort(
					"Output error when mapping " + file.toString(), e);
		} finally {
			if ( stream != null )
				try {
					stream.close();
				} catch (IOException e) {
					StarTokeniser._abort("Exception when trying to close " + file, e);
				}
		}

		this.m_file = file;
		this._setNextToken();
		

	}

	/**
	 * Prepare StarTokeniser instance to tokenise the contents of a file.
	 * 
	 * This may be called on a StarTokeniser instance that has already been used
	 * to match other data: the previous state will be lost and the object will
	 * reset with the new data.<br>
	 * 
	 * This method invokes
	 * {@link StarTokeniser#startMatching(java.io.File, int)} with a chunk size
	 * of 1.<br>
	 * 
	 * Normally, this is the most appropriate method to use to tokenise the
	 * contents of a file, because of all the <code>startMatching</code>
	 * methods, this one provides the most accurate line numbers for the
	 * {@link StarToken} instances that are produced. Consider using one of the
	 * others if the I/O involved in reading STAR files becomes limiting.
	 * 
	 * @param file
	 *            Instance of {@link java.io.File} containing STAR data
	 */

	public void startMatching(java.io.File file) {
		this.startMatching(file, 1);
	}

	/**
	 * Prepare StarTokeniser instance to tokenise the contents of a file.
	 * 
	 * This may be called on a StarTokeniser instance that has already been used
	 * to match other data: the previous state will be lost and the object will reset
	 * with the new data.
	 * 
	 * Using this method will cause lines to be read from the file and matched
	 * against {@link StarRegex#REGEX} in chunks.
	 * 
	 * @param file
	 *            Instance of {@link java.io.File} containing STAR data
	 * @param chunkSize
	 *            Minimum number of lines in a chunk. The actual number of lines
	 *            may be greater than this, because a chunk will never end in
	 *            the middle of multi-line text, instead reading to the end of
	 *            the multiline text token.
	 * 
	 * This method should not be used with a <code>chunkSize</code> parameter
	 * of {@link java.lang.Integer#MAX_VALUE} in an attempt to process a large
	 * file in a single operation, since a multi-line text token may cause an
	 * attempt to read more than this number of lines. Use
	 * {@link #startMatching(java.io.File, boolean)} to process the whole file
	 * at once.
	 */

	public void startMatching(java.io.File file, int chunkSize) {

		// Allow garbage collector to reclaim any memory referred to
		// by this field
		this.m_data = null;

		try {
			this.m_data_reader = new java.io.LineNumberReader(
					new java.io.FileReader(file));
		} catch (java.io.FileNotFoundException e) {
			StarTokeniser._abort("Cannot open file " + file, e);
		}

		// Most people think of the first line of a file as line number 1,
		// even seasoned non-Fortran developers whose minds work in 
		// a zero-based way for everything else :-)
		this.m_data_reader.setLineNumber(1);
		this.m_file = file;
		this.m_chunk_size = chunkSize;
		this._setNextChunk();
		this._startMatching(this.m_chunk);
		this._setNextToken();

	}

	/**
	 * Get next STAR token.
	 * 
	 * @return Returns null if no more tokens found
	 */

	public StarToken nextToken() {

		StarToken retval = this.m_curr_token;
		this._setNextToken();
		return retval;
	}

	/**
	 * Tests if there are more tokens available from this tokeniser. If this
	 * method returns true, then a subsequent call to {@link #nextToken()} with
	 * no argument will successfully return a token. <code>true</code> if and
	 * only if there is at least one token after the current position;
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if and only if there is at least one token in
	 *         the string after the current position; <code>false</code>
	 *         otherwise.
	 */
	
	public boolean hasMoreTokens() {
		return this.m_curr_token != null;
	}
	
	
	/**
     * Checks whether the {@code data} parameter is a valid CIF value token.
     * It is returned if so, otherwise it attempts to turn it into one
     * first by double-quoting, then single-quoting, then 
     * if {@code semicolon} is set, by adding a {@code ';'} at the start and
     * {@code "\n;"} at the end.
     * It is the responsibility of the caller
     * to decide whether or not the returned value should be output in the first
     * column of a new line if it starts with a {@code ;} character.
     * <p>This method calls {@link #_startMatching(CharSequence)}, so it resets
     * the state of the tokeniser.</p>
     * <p>This method is provided to support the writing rather than the reading
     * of mmCIF data. It is convenient to implement it using the Tokeniser though,
     * which is why it is here and not in some other class.</p>
     *
	 * @param data character sequence to be checked
	 * @param semicolon whether or not to try semi-colon quoting
	 * @return {@code data}, quoted if necessary to form a valid CIF token
	 * @throws IllegalArgumentException if {@code data} cannot be turned into
	 * a STAR data value token.
	 */
	public String quoteDataValue( CharSequence data, boolean semicolon )
	   throws IllegalArgumentException {
	    
	    StarToken tok;
	    
	    this.startMatching(data);
	    tok = this.nextToken();
	    
	    // Check to see if we have a valid non-quoted data value
	    if ( tok != null && 
	            ! this.hasMoreTokens() && 
	            tok.getType() == TOKEN_STRING && 
	            tok.getValue().length() == data.length() )
	        return data.toString();
	            
	    String myData = '"' + data.toString() + '"';
	    
	    this.startMatching(myData);
	    tok = this.nextToken();
	    if ( tok != null && ! this.hasMoreTokens() && 
	            tok.getType() == TOKEN_DQUOTE_STRING )
	        return myData;
	    
	    myData = '\'' + data.toString() + '\'';
	    this.startMatching(myData);
	    tok = this.nextToken();
	    if ( tok != null && ! this.hasMoreTokens() &&
	            tok.getType() == TOKEN_SQUOTE_STRING )
	        return myData;
	    
	    if ( semicolon ) {
	        myData = ';' + data.toString() + "\n;";
	        this.startMatching(myData);
	        tok = this.nextToken();
	        if ( tok != null && ! this.hasMoreTokens() && 
	                tok.getType() == TOKEN_MULTILINE )
	            return myData;
	    }
	    
	    throw new IllegalArgumentException("Input data cannot be turned into a valid STAR value: " +
	           ( data.length() < 50 ? data : data.subSequence(0, 50) + "...." )
	           );
	    
	}
	
	
	/***************************************************************************
	 * 
	 * Private methods start here
	 * 
	 **************************************************************************/

	/**
	 * Private method to get next match and set m_curr_token field to new token instance
	 * 
	 * We are always one token ahead, so that we can implement hasMoreTokens() easily
	 */
	
	private void _setNextToken() {

		// If there is no next match, check to see if we are in line-oriented
		// mode and get the next chunk of lines.
		while (!this.m_matcher.find()) {

			if (this.m_data_reader == null) {
				// If we are not in line-oriented mode, we have got to the end
				// of the data
				this.m_curr_token = null;
				return;
			} else {
				// Otherwise, we get the next chunk of data and reset the
				// matcher
				this._setNextChunk();
				if (this.m_chunk == null || this.m_chunk.length() == 0) {
					this.m_curr_token = null;
					return;
				}
				this._startMatching(this.m_chunk);
			}
		}

		// Having got our next token, figure out what type it is and apply any
		// transformations implied by the settings of the flags.
		int tokenType;

		for (int i = 1; i <= this.m_matcher.groupCount(); i++) {

			if (this.m_matcher.group(i) != null) {

				tokenType = i;

				// System.out.println(" Match group " + i + "/" +
				// this.m_matcher.groupCount() + ": '" + this.m_matcher.group(i)
				// + "'");

				if (i == StarTokenTypes.TOKEN_SQUARE_BRACKET
						&& (this.m_flags & StarTokeniser.ALLOW_SQUARE_BRACKET) != 0) {
					tokenType = StarTokenTypes.TOKEN_STRING;
				}

				if (i == StarTokenTypes.TOKEN_BAD_CONSTRUCT
						&& (this.m_flags & StarTokeniser.ALLOW_BAD_CONSTRUCT) != 0
						&& !this.m_matcher.group(i).equalsIgnoreCase("data_") ) {
					tokenType = StarTokenTypes.TOKEN_STRING;
				}
				this.m_curr_token = new StarToken(tokenType, this.m_matcher.group(i),
						this.m_chunk_start, this.m_chunk_end, 
						this.m_matcher.start(), this.m_matcher.end());
				return;
			}
		}

		// Should never end up here
		this.m_curr_token = null;
		return;
	}

	
	
	/*
	 * Method to set <code>this.m_chunk</code> to a chunk of data from a file
	 * that we are reading using a LineNumberReader.
	 * 
	 * This method appends lines to the chunk until the time to return is right,
	 * specifically when both of the following conditions are satisfied:
	 * 
	 * We are not in the middle of multi-line text.
	 * 
	 * We have collected at least as many lines as <code>this.m_chunk_size</code>
	 */

	private void _setNextChunk() {

		this.m_chunk = new java.lang.StringBuilder();
		try {
			boolean multiLine = false;

			String line;
			int linesRead = 0;
			this.m_chunk_start = this.m_data_reader.getLineNumber();

			while ((line = this.m_data_reader.readLine()) != null) {
				linesRead++;
				this.m_chunk.append(line + "\n");

				if (line.matches("^;.*")) {
					// Switch in or out of multi-line context
					multiLine = !multiLine;
				}

				// In multi-line context, or while we haven't reached the
				// minimum chunk length yet, we continue around the while
				// loop appending input lines to the StringBuilder
				// that we are going to tokenise.
				//
				// Otherwise, we set the chunk end and return
				if (!multiLine && linesRead >= this.m_chunk_size) {
					this.m_chunk_end = this.m_data_reader.getLineNumber();
					return;
				}
			}
		}

		catch (java.io.IOException e) {
			StarTokeniser._abort("Could not read from " + this.m_file, e);
		}

		this.m_chunk_end = this.m_data_reader.getLineNumber();
		return;

	}

	// Private matcher resetting method that does not reset
	// the StarTokeniser's state info about where the data
	// is really coming from.
	// We can use this to reset the matcher with a new chunk
	// of data when we are in line-oriented mode.

	private void _startMatching(CharSequence data) {

		// Special-case null data, so that instantiating/resetting Matcher
		// doesn't crash.
		// if ( data == null ) {
		// data = "";
		// }

		if (this.m_matcher == null) {
			this.m_matcher = StarTokeniser.m_pattern.matcher(data);
		} else {
			this.m_matcher.reset(data);
		}
	}

	private static void _abort(CharSequence message, Throwable e) {
		System.err.println(message);
		System.err.print(e);
		e.printStackTrace();
		System.exit(1);
	}

}

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
 *  notice, this list of conditions, the special conditions below
 *  relating to the use of the regular expression, and the disclaimer
 *  below.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions, the special conditions below
 *  relating to the use of the regular expression, and the disclaimer
 *  below in the documentation and/or other materials provided with the
 *  distribution.
 *
 *  If the regular expression used to match STAR/CIF data in the
 *  redistribution is not identical to that in the original version,
 *  this fact must be stated wherever the copyright notice is
 *  reproduced.
 *
 * This file contains the "GPhL StarTools regular expression",
 * which is used by the GPhL StarTools library to tokenise STAR data.
 * 
 * Use of the GPhL StarTools regular expression separately from
 * other parts of the GPhL StarTools library, with or without
 * modification, in other libraries and applications, in any
 * programming language, is permitted provided that the following
 * conditions are met (which replace the general conditions
 * for use of the GPhL StarTools library in such a case):
 *
 *  Distributions of libraries or applications in source code
 *  form that use the GPhL StarTools regular expression must
 *  retain the above copyright notice, this list of conditions
 *  and the following disclaimer, and associate them with the
 *  StarTools regular expression where it occurs in the source
 *  code.
 *
 *  Distributions of libraries or applications in binary form
 *  that use the GPhL StarTools regular expression must reproduce
 *  the above copyright notice, this list of conditions and the
 *  following disclaimer in the documentation and/or other
 *  materials provided with the distribution, and state that they
 *  apply to the use of the GPhL StarTools regular expression by
 *  the library or application.
 *
 *  If the GPhL StarTools regular expression has been modified
 *  from its original form in the library or application this
 *  fact must be stated wherever the copyright notice is
 *  reproduced.
 *
 *  These conditions, the copyright notice, and the included
 *  disclaimer apply only to the StarTools regular expression
 *  itself, not to any other code with which the regular
 *  expression is associated.
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
 * Class providing a regular expression to match and tokenise STAR data. 
 * 
 * @author pkeller
 *
 */
public class StarRegex {

	/**
	 * Regular expression that is matched by STAR data (The "StarTools regular expression").
	 * This regular expression may be used independently of the StarTools library
	 * provided that certain conditions are met.
	 * See <a href="{@docRoot}/StarToolsRegexLicence.txt">StarToolsRegexLicence.txt</a>
	 */
	
	public static final String REGEX = 

		// We start with STAR tokens that need special handling wrt whitespace:
		
		"(?xmi) # $Revision: 1.9 $  # No 'u' flag for perl 5.8.8/RHEL5 compatibility\n" +
		"^;([\\S\\s]*?)(?:\\r\\n|\\s)^;(?:(?=\\s)|$)  # Multi-line string\n"
		                            /*-  Multi-line text string.
										N.B. (i) the closing semicolon must be followed by whitespace
										or end of input, otherwise we have a syntax error
										Minimal match on [\\S\\s]*?, so that the group ends on the next
										newline-semicolon. If this newline-semicolon is not followed
										by whitespace or end of input, the group does not match, leading
										to an eventual BAD_TOKEN
										N.B. (ii) the final newline is not part of the data. The non-
										capturing group (?:\\r\\n|\\s) ensures that it is not included in the
										returned token (allowing for the two-character DOS line ending).
										N.B. (iii) Python compatibility considerations prevent us using
										(?s)/(?-s)/(?s:...): Python regex's only allow the use of this flag
										for the whole pattern, not parts of it. [\\S\\s] lets us match any character
										including line terminators, without having to enable the DOTALL option.
										Using [..] rather than ..|.. speeds up tokenising by a factor of
										about 6.
	 								*/

		// Comment (terminated by newline/end of input, but not other whitespace)
		+ "|(?:^|(?<=\\s))(\\#.*?)\\r?$              # Comment\n"  

		// Now, tokens that behave (almost) uniformly with respect to enclosing whitespace
		// "Almost", because we need to special-case ^;  - see comment for non-quoted string token
		+ "|(?:^|(?<=\\s))(?:\n"	// Initial start-of-input or whitespace + opening bracket of non-capturing group for whole token
		
		// STAR global block                      => CIF syntax error (STAR token not allowed in CIF's)
		+ "  (global_)                            # STAR global block\n"			
		// STAR save frame header or terminator   => CIF syntax error (STAR token not allowed in CIF's)
		+ "  |(save_\\S*)                          # STAR save frame header or terminator\n"		
        // STAR save frame reference              => CIF syntax error (STAR token not allowed in CIF's)
		+ "  |(\\$\\S+)                             # STAR save frame reference\n"			
        // STAR nested loop terminator            => CIF syntax error (STAR token not allowed in CIF's)
		+ "  |(stop_)                             # STAR nested loop terminator\n"			
		+ "  |(data_\\S+)                          # Data block header\n"		
		+ "  |(loop_)                             # Loop header\n"

		// Misuse of privileged STAR construct => STAR syntax error
		+ "  |((?:global_\\S+)|(?:stop_\\S+)|(?:data_)|(?:loop_\\S+))  # Invalid privileged construct\n"

		+ "  |(_\\S+)                              # Data name\n"
		+ "  |'(.*?)'                             # Single-quoted string\n"
		+ "  |\"(.*?)\"                             # Double-quoted string\n"
		+ "  |(\\.)                                # CIF null\n"
		+ "  |(\\?)                                # CIF unknown/missing\n"
		+ "  |([\\[\\]]\\S*)                         # Square bracketed constructs (reserved)\n"
		+ "  |((?:[^'\";_$\\s]|(?<!^);)\\S*)         # Non-quoted string\n" 
		                                     /*- Non-quoted string
		                                        First character of token must be either:
		                                        
		                                           Non-whitespace character other than ', " or ;

		                                           ; not preceded by newline, so that we don't get a
		                                           match for the last line of this type of syntax error:

											         ; Some multi-line
											          text
											         ;"badly placed quoted string"
										    */

        // Catch-all match => STAR syntax error, e.g. missing closing quote
		+ "  |(\\S+)                               # Catch-all bad token\n" 
		+ ")\n"				// Close non-capturing group for whole token
		+ "(?:(?=\\s)|$)"   // Final end-of-input or whitespace terminates token
		;

	// No public constructors
	private StarRegex() {}
	
}

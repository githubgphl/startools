package com.globalphasing.startools;

import java.util.regex.Pattern;


public class DumpRegexInfo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		Pattern pattern = Pattern
		.compile(StarRegex.REGEX);
		
		System.out.println( pattern.pattern() + "\n");
		System.out.println( "Pattern compile flags: " + String.format("%x", pattern.flags()));
		
	}

}

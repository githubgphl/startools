package com.globalphasing.startools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.globalphasing.startools.StarTokeniser;
import com.globalphasing.startools.StarToken;

public class StarTokeniserExample {

	/**
	 * @param args
	 */

	private enum OutputType {
		QUIET("-q"), TABLE("-t"), ALL("-a");
		private String arg;
		
		OutputType(String arg) {
			this.arg = arg;
		}
		
		public static OutputType getByArg(String arg) {
			for ( OutputType v: OutputType.values() )
				if ( v.arg.compareTo(arg) == 0 )
					return v;
			
			return null;			
		}
		
	}

	
	public static void main (String[] arrArgs) {
		
		StarTokeniser tokeniser = new StarTokeniser();
		
		
		if ( arrArgs.length == 0 || arrArgs[0].compareTo("-h") == 0 ||
				arrArgs[0].compareTo("--help") == 0 ) {
			System.err.print( 
					"Call with one of the following options:\n" +
					"   -m <filename>  // Tokenise file, using memory mapping\n" +
					"   -f <filename>  // Read file into buffer and tokenise\n" +
					"   -s <string>    // Tokenise string argument\n" +
					"   -l <no.> <filename>  // Tokenise file by reading specified number of lines\n" +
					"                           at a time\n\n" +
					"optionally preceded with one of the following:\n" +
					"   -q  // Quiet: just output timings and number of tokens\n" +
					"   -t  // Output summary table of numbers of each token type\n" +
					"   -a  // The default: output all tokens and summary\n\n"
			);
			System.exit(0);
		}
		
		List<String> args = new ArrayList<String>( Arrays.asList(arrArgs) );
		
		// See if the first argument is one that specifies the output type.
		OutputType output = OutputType.getByArg(args.get(0));
		if ( output == null )
			output = OutputType.ALL;
		else
			args.remove(0);
		
		if ( args.get(0).compareTo("-f") == 0 ) {
			// Read whole contents of file args.get(1) into a buffer, and tokenise
			tokeniser.startMatching( new java.io.File(args.get(1)), false); 
		}
		
		else if ( args.get(0).compareTo("-m") == 0 ) {
			// Map file args.get(1) to a buffer, and tokenise
			tokeniser.startMatching( new java.io.File(args.get(1)), true); 
		}
		
		else if ( args.get(0).compareTo("-l") == 0 ) {
			// Handle file args.get(1) in line-oriented mode
			tokeniser.startMatching( new java.io.File(args.get(2)), 
									java.lang.Integer.decode(args.get(1)) );
						
		}
		
		else if ( args.get(0).compareTo("-s") == 0 ) {
			// args.get(1) is a string to be tokenised
			tokeniser.startMatching( args.get(1) );
		}
		else {
			System.err.println("Invalid arguments");
			System.exit(1);
		}
		
		StarToken tok;
		
		System.out.println("Starting tokenising...");
		long stime = System.currentTimeMillis();
		
		
		int ntokens = 0;
		int[] typeTable = output == OutputType.QUIET ? null : new int[18];
		
		// Three separate branches: evaluating the if inside the loop 
		// is pointless.....
		
		switch ( output ) {

		case QUIET :
			while ( tokeniser.hasMoreTokens() ) {
				tok = tokeniser.nextToken();
				ntokens++;
			}
		
		case TABLE:
			while ( tokeniser.hasMoreTokens() ) {
				tok = tokeniser.nextToken();
				typeTable[tok.getType()]++;
			}
			
		case ALL:
			while ( tokeniser.hasMoreTokens() ) {
				tok = tokeniser.nextToken();
				System.out.println(tok.toString());
				typeTable[tok.getType()]++;
			}
		}
		
		System.out.println("Finished: took " + ( System.currentTimeMillis() - stime )/1000.0 + "s");

		if ( typeTable != null ) {
			System.out.println("\nSummary of token types:");
			for ( int i = 1; i < 18; i++ ) {
				System.out.println( typeTable[i] + " " + StarTokenTypes.tokenTypeAsString(i));
				ntokens += typeTable[i];
			}
		}
		
		System.out.println("\n" + ntokens + " tokens in total");
		
	}
		

}

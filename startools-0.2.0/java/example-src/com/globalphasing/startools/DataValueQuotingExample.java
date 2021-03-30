package com.globalphasing.startools;
import com.globalphasing.startools.StarTokeniser;


public class DataValueQuotingExample {

    static public final String[] values = {
        "DataValue1", "Data Value 2", "  Data Value 3  ",
        "_data_value_4", "'Data Value 5'", "Data'Value'6",
        "Data' Value 7", "DataValue8'", "\"DataValue9",
        "Data' Value\" 10", ";DataValue11", ";Data Value\n12\n;",
        " ;Data Value\n13\n;", " ;Data Value\n14\n ;", ";Data Value\n15\n ;"
        
    };
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {

        StarTokeniser tokeniser = new StarTokeniser();
        String newValue;
        for ( String v: values ) {
            
            try {
                newValue = tokeniser.quoteDataValue(v, true);
                System.out.println( String.format("Quoted >>>%s<<< as >>>%s<<<", v, newValue));
            }
            catch ( IllegalArgumentException e ) {
                System.out.println("Cannot quote value: " + v );
            }

        }

    }

}

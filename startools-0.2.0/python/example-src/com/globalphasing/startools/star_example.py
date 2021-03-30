'''
Created on 25 Nov 2013

@author: pkeller
'''

import sys
import time
from com.globalphasing.startools import StarTokeniser, star_token_types

if __name__ == "__main__":

    print sys.argv

    if sys.argv[1][0] == "-":
        verbosity = sys.argv[1][1] # q => quiet, t => summary table only, a => all tokens
        cif = sys.argv[2]
    else:
        verbosity = "a"
        cif = sys.argv[1]

    tokeniser = StarTokeniser()
    tokeniser.start_matching(cif)

    type_table = None if verbosity == "q" else [0] * 18
    ntokens = 0

    stime = time.time()
    print "Starting...."
    
    # Make each of these branches independent. Bad idea to check an invariant
    # on every loop iteration like this:
    #
    # for t in tokeniser.next():
    #     if verbosity == "q":
    #         ....
    #
    # if we are interested in performance and we are going to have a huge number of tokens
    
    if verbosity == "t":
        for t in tokeniser:
            type_table[t.type] += 1
    elif verbosity == "q":
        for t in tokeniser:
            ntokens += 1
    else:        
        for t in tokeniser:
            print t
            type_table[t.type] += 1
    
    print "Done. Took " + str(time.time() - stime) + " seconds"
    
    if type_table is not None:
        print "\nSummary of token types:"
        for i in range(1,len(type_table)):
            print type_table[i], star_token_types._token_type_as_string(i)
            ntokens += type_table[i]
            
    print "\n", str(ntokens), " tokens in total" 
    
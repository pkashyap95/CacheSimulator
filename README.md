# CacheSimulator
Tetx File for input, first character is 'r|w' followed by hex address. 
If the address is not 8 hex digits, it will be padded with leading 0s.

To run use the provided make to build the application followed by:
java sim_cache [BLOCKSIZE] [L1_SIZE] [L1_ASSOC] [NO OF VC BLOCKS] [L2_SIZE] [L2_ASSSOC] [trace_file]

If you aren't using VC or L2 set the appropriate parameters to 0.

import java.io.*;
import static java.lang.Math.log;

/* args hold the command line arguments

    Example:-
    sim_cache 32 8192 4 7 262144 8 gcc_trace.txt
    args[0] = "32"
    args[1] = "8192"
    args[2] = "4"
    ... and so on
*/
public class sim_cache
{
    public static void main(String[] args) {
        cache_params params = new cache_params();       // check cache_params.java class for the definition of class cache_params 
        String trace_file;                              // Variable that holds trace file name
        char rw;                                        // Variable holds read/write type read from input file
        long addr;                                      // Variable holds the address read from input file

        if (args.length !=7 )       // Checks if correct number of inputs have been given. Throw error and exit if wrong
        {
            System.out.println("Error: Expected inputs:7 Given inputs:" + args.length);
            System.exit(0);
        }

        // Long.parseLong() converts string to long
        params.block_size = Long.parseLong(args[0]);
        params.l1_size = Long.parseLong(args[1]);
        params.l1_assoc = Long.parseLong(args[2]);
        params.vc_num_blocks = Long.parseLong(args[3]);
        params.l2_size = Long.parseLong(args[4]);
        params.l2_assoc = Long.parseLong(args[5]);
        trace_file = args[6];

        // Print params
        System.out.printf("  ===== Simulator configuration =====%n"+
                "  BLOCKSIZE:                        %d%n"+
                "  L1_SIZE:                          %d%n"+
                "  L1_ASSOC:                         %d%n"+
                "  VC_NUM_BLOCKS:                    %d%n"+
                "  L2_SIZE:                          %d%n"+
                "  L2_ASSOC:                         %d%n"+
                "  trace_file:                       %s%n%n", params.block_size, params.l1_size, params.l1_assoc, params.vc_num_blocks, params.l2_size, params.l2_assoc, trace_file);

        Memory memHeir=new Memory(params);
        int block_offset =(int) (log(params.block_size)/log(2));
        try (BufferedReader br = new BufferedReader(new FileReader(trace_file)))
        {
            String line;
            while ((line = br.readLine()) != null) {
                rw = line.charAt(0);                                // gets r/w char from String line
                addr = Long.parseLong(line.substring(2), 16);       // gets address from String line and converts to long. parseLong uses base 16
                String binaryAddr=Long.toBinaryString(addr);
                String prepend="";
                if(binaryAddr.length()<32){
                    prepend= "0";
                    for(int x=1; x<32-binaryAddr.length();x++){
                        prepend+="0";
                    } 
                }
                binaryAddr=prepend+binaryAddr;
                binaryAddr=binaryAddr.substring(0,(binaryAddr.length()-block_offset));
                if (rw == 'r') {
                    if(params.vc_num_blocks == 0){
                        if(params.l2_size == 0){
                            memHeir.readL1(binaryAddr);
                        }
                        else{
                            memHeir.readL1L2(binaryAddr);
                        }
                    }
                    else{
                        if(params.l2_size == 0){
                            memHeir.readL1(binaryAddr);
                        }
                        else{
                            memHeir.readL1L2(binaryAddr);
                        }
                    }
                }
                else if(rw == 'w') {
                    if(params.vc_num_blocks == 0){
                        if(params.l2_size == 0){
                            memHeir.writeL1(binaryAddr);
                        }
                        else{
                            memHeir.writeL1L2(binaryAddr);
                        }
                    }
                    else{
                        if(params.l2_size == 0){
                            memHeir.writeL1(binaryAddr);
                        }
                        else{
                            memHeir.writeL1L2(binaryAddr);
                        }
                    }
                }
            }
        }
        catch (IOException x)                                       // Throw error if file I/O fails
        {
            System.err.format("IOException: %s%n", x);
        }
        memHeir.printStats();
    }
}


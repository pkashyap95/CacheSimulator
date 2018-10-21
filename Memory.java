/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Priyank Kashyap
 */
import java.math.RoundingMode;
import java.text.DecimalFormat;
public class Memory {
    
    Cache L1;
    Cache L2;
    Cache VC;
    int memRef;
    int memWriteBack;
    
    public Memory(cache_params params){
        memWriteBack=0;
        memRef=0;
        L1=new Cache("L1",params.block_size, params.l1_size, params.l1_assoc,params.vc_num_blocks);
        if(params.l2_size != 0){
            L2=new Cache("L2",params.block_size, params.l2_size, params.l2_assoc,0);
        }
        //if(params.vc_num_blocks !=0)
    }
    void readL1L2(String address){
        returnVal returnStatL1, returnStatL2;
        returnStatL1 = L1.read(address);  // Print and test if file is read correctly 
        if(returnStatL1.flag ==1){
            returnStatL2=L2.read(address);
            if(returnStatL2.flag == 1){
                memRef++;
            }
            else if(returnStatL2.flag == 2){
                memRef++;
                memWriteBack++;
            }
        }
        else if(returnStatL1.flag == 2){
            returnStatL2=L2.write(returnStatL1.writeBackAddr);

            if(returnStatL2.flag == 1){
                memRef++;
            }
            else if(returnStatL2.flag == 2){
                memWriteBack++;
                memRef++;
            }
            returnStatL2=L2.read(address);

            if(returnStatL2.flag == 1){
                memRef++;
            }
            else if(returnStatL2.flag == 2){
                memWriteBack++;
                memRef++;
            }
        }
            
    }
    void writeL1L2(String address){
        returnVal returnStatL1, returnStatL2;
        returnStatL1 = L1.write(address);    // Print and test if file is read correctly 
                   
                   if(returnStatL1.flag ==1){
                       
                       returnStatL2=L2.read(address);
                       
                       if(returnStatL2.flag == 1){
                           memRef++;
                       }
                       
                       else if(returnStatL2.flag == 2){
                           memRef++;
                           memWriteBack++;
                       }
                   }
                   
                   else if(returnStatL1.flag == 2){
                       returnStatL2=L2.write(returnStatL1.writeBackAddr);
                  
                       if(returnStatL2.flag == 1){
                           memRef++;
                       }
                       else if(returnStatL2.flag == 2){
                           memWriteBack++;
                           memRef++;
                       }
                       
                       returnStatL2=L2.read(address);
                       
                       if(returnStatL2.flag == 1){
                           memRef++;
                       }
                       
                       else if(returnStatL2.flag == 2){
                           memWriteBack++;
                           memRef++;
                       }
                   }
    }
    void readL1(String address){
        returnVal returnStatL1=L1.read(address);
        if(returnStatL1.flag==1){
            memRef++;
        }
        else if(returnStatL1.flag==2){
            memRef++;
            memWriteBack++;
        }
    }
    void writeL1(String address){
        returnVal returnStatL1=L1.write(address);
        if(returnStatL1.flag==1){
            memRef++;
        }
        else if(returnStatL1.flag==2){
            memRef++;
            memWriteBack++;
        }
    }    
    
    void printStats(){
        L1.displayCacheContents();
        if(L2 !=null) L2.displayCacheContents();
        System.out.println("===== Simulation results =====");
        String [] message=new String[16];
        message[0]  = "  a. number of L1 reads:";
        message[1]  = "  b. number of L1 read misses:";
        message[2]  = "  c. number of L1 writes:";
        message[3]  = "  d. number of L1 write misses:";
        message[4]  = "  e. number of swap requests:   ";
        message[5]  = "  f. swap request rate:";
        message[6]  = "  g. number of swaps:";
        message[7]  = "  h. combined L1+VC miss rate:";
        message[8]  = "  i. number of writebacks from L1/VC:";
        message[9] = "  j. number of L2 reads:";
        message[10] = "  k. number of L2 read misses:";
        message[11] = "  l. number of L2 writes:";
        message[12] = "  m. number of L2 write misses:";
        message[13] = "  n. L2 miss rate:";
        message[14] = "  o. number of writebacks from L2:";
        message[15] = "  p. total memory traffic:";
        
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        double x=0.000000;
        String data[]=new String[16];
        data[0]  = String.valueOf(L1.getReadReq());
        data[1]  = String.valueOf(L1.getReadMisses());
        data[2]  = String.valueOf(L1.getWriteReq());
        data[3]  = String.valueOf(L1.getWriteMisses());
        data[4]  = String.valueOf(L1.getSwapsReq());
        data[5]  = df.format(L1.getSwapReqRate());
        data[6]  = String.valueOf(L1.getSwaps());
        data[7]  = df.format(L1.combinedMissRate());
        data[8]  = String.valueOf(L1.getWritebacks());
        if(L2!=null){
                data[9] = String.valueOf(L2.getReadReq());
                data[10] = String.valueOf(L2.getReadMisses());
                data[11] = String.valueOf(L2.getWriteReq());
                data[12] = String.valueOf(L2.getWriteMisses());
                data[13] = df.format(L2.getFinalLevel());
                data[14] = String.valueOf(L2.getWritebacks());
        }
        else{
                data[9] = String.valueOf(0);
                data[10] = String.valueOf(0);
                data[11] = String.valueOf(0);
                data[12] = String.valueOf(0);
                data[13] = String.valueOf(0.00000);
                data[14] = String.valueOf(0);
        }
        data[15] = String.valueOf(memRef+memWriteBack);

        int length=52;
        for(int i=0; i<=15; i++){
            String spaces=" ";
            int numOfSpaces= 52-message[i].length()-data[i].length();
            for(int j=0; j<numOfSpaces; j++){
                spaces=spaces+" ";
            }
            System.out.println(message[i]+spaces+data[i]);
        }
    }
}

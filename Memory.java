/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Priyank Kashyap
 */
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
        String [] message=new String[15];
        System.out.println("  a. number of L1 reads:                       "+L1.getReadReq());
        System.out.println("  b. number of L1 read misses:                 "+L1.getReadMisses());
        System.out.println("  c. number of L1 writes:                      "+L1.getWriteReq());
        System.out.println("  d. number of L1 write misses:                "+L1.getWriteMisses());
        System.out.println("  e. number of swap requests:                  "+L1.getSwapsReq());
        System.out.println("  f. swap request rate:                        "+L1.getSwapReqRate());
        System.out.println("  g. number of swaps:                          "+L1.getSwaps());
        System.out.println("  h. combined L1+VC miss rate:                 "+L1.combinedMissRate());
        System.out.println("  i. number of writebacks from L1/VC:          "+L1.getWritebacks());
        
        if(L2!= null){
            System.out.println("  j. number of L2 reads:                   "+L2.getReadReq());
            System.out.println("  k. number of L2 read misses:             "+L2.getReadMisses());
            System.out.println("  l. number of L2 writes:                  "+L2.getWriteReq());
            System.out.println("  m. number of L2 write misses:            "+L2.getWriteMisses());
            System.out.println("  n. L2 miss rate:                         "+L2.getFinalLevel());
            System.out.println("  o. number of writebacks from L2:         "+L2.getWritebacks());
        }
        
        else{
            System.out.println("  j. number of L2 reads:                       "+0);
            System.out.println("  k. number of L2 read misses:                 "+0);
            System.out.println("  l. number of L2 writes:                      "+0);
            System.out.println("  m. number of L2 write misses:                "+0);
            System.out.println("  n. L2 miss rate:                             "+0.00);
            System.out.println("  o. number of writebacks from L2:             "+0);
        }
        System.out.println("  p. total memory traffic                      "+ (memRef+memWriteBack));
    }
}

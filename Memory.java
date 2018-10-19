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
        //if(L2 !=null) L2.displayCacheContents();
        L1.print();
        if(L2 != null) L2.print();
        if(VC != null) VC.print();
        System.out.println("Toatl memory references "+ (memRef+memWriteBack));
    }
}

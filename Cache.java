import static java.lang.Math.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 * 
 * © by Priyank Kashyap
 */
public class Cache {
    private long mBlockSize;
    private long mAssoc;
    private long mCacheSize;
    private long mNumOfSets;
    private long mVCBlocks;
    private double indexBits;
    String name;
    Map mCache;
    ArrayList<block_params>mVC;
    //Stats for the cache
    private double readReqs;
    private double readMiss;
    private double writeReqs;
    private double writeMiss;
    private double writeBack;
    private double swapsVC;
    private double mSwapReq;
    //initialize the cache & vc
    public Cache(String inName, long blockSize, long cacheSize, long associativity, long vc_blocks){
        mCache=new HashMap();
        mBlockSize = blockSize;
        mCacheSize = cacheSize;
        mAssoc     = associativity;
        mVCBlocks  = vc_blocks;
        mSwapReq   = 0;
        mNumOfSets = mCacheSize/(mAssoc*mBlockSize); // calculate number of sets
        indexBits = log(mNumOfSets)/log(2);
        readReqs=0;
        readMiss=0;
        writeReqs=0;
        writeMiss=0;    
        writeBack=0;
        swapsVC=0;
        name=inName;
        //define  a default block
        for(long i = 0; i <mNumOfSets; ++i){
            String binAddr= Long.toBinaryString(i);
            String prepend="";
            if(binAddr.length()<indexBits){
                prepend= "0";
                for(int x=1; x<indexBits-binAddr.length();x++){
                    prepend+="0";
                }
            }
            binAddr=prepend+ binAddr;
            ArrayList<block_params> block_row = new ArrayList<block_params>();
            for(long j=0; j<mAssoc; j++){
                block_params default_block = new block_params();
                default_block.dirty_bit=0;
                default_block.valid_bit=0;
                default_block.tag="";
                default_block.lru_counter=j;
                //System.out.println("Key "+ default_block.tag+" LRU Counters "+ default_block.lru_counter);
                block_row.add(default_block);
           }
            mCache.put(binAddr,block_row);
        }
        if(mVCBlocks !=0){
            mVC = new ArrayList<block_params>();
            for (long j=0; j<mVCBlocks; j++){
                block_params default_block = new block_params();
                default_block.dirty_bit=0;
                default_block.valid_bit=0;
                default_block.tag="";
                default_block.lru_counter=j;
                mVC.add(default_block);
            }
        }
    }
    
    //////////////Regular Cache Methods/////////////////
    returnVal read(String addr){
        returnVal nextLevel = new returnVal();
        String reqTag   = addr.substring(0, addr.length()-(int) indexBits);
        String reqIndex = addr.substring(addr.length()-(int)indexBits);
        readReqs=readReqs+1;
        //System.out.println(name+ " READ REQ TAG " +reqTag + " INDEX " +reqIndex);
        boolean hit_miss= getBlock(reqIndex, reqTag,0); //find the block to read
        if(!hit_miss){ //cache miss
            readMiss= readMiss+1;            
            if(mVC == null){
                String blockToBeEvicted = getLRUEvict(reqIndex);
                //Dirty block to be evicted
                if (blockToBeEvicted.equals("")){
                    //No writeback only read request to next to next level
                    nextLevel.flag= 1;
                    nextLevel.readReqAddr= addr;
                    nextLevel.writeBackAddr= "";
                    setLRU(reqIndex, reqTag, 0);
                    return nextLevel;                       
                }
                //Clean block to be evicted
                else{
                    //read request and write request to next level
                    writeBack=writeBack+1;
                    nextLevel.flag= 2;
                    nextLevel.readReqAddr= addr;
                    nextLevel.writeBackAddr= blockToBeEvicted+reqIndex;
                    setLRU(reqIndex, reqTag, 0);
                    return nextLevel;               
                }
            }
            else{
                //check if VC has invalid if so req to next level
                //otherwise go inc swap req
                boolean cacheValidWay = areThereInvalidWays(reqIndex);
                if(!cacheValidWay){
                    long doesVCHaveIt = checkVC(addr);
                    if(doesVCHaveIt !=-1){                    
                        //System.out.println("SHOULD BE SWAPPING");
                        swap(addr, reqIndex, doesVCHaveIt, 0);
                        nextLevel.flag= 0;
                        nextLevel.readReqAddr= "";
                        nextLevel.writeBackAddr= "";
                        return nextLevel;
                    }
                    else{
                        String writeNextLevel= setLRUVC(addr, 0);
                        if(writeNextLevel.equals("")){
                            //read request to next level
                            nextLevel.flag= 1;
                            nextLevel.readReqAddr= addr;
                            nextLevel.writeBackAddr= "";
                            //setLRU(reqIndex, reqTag, 1);
                            return nextLevel;  
                        }
                        else{
                            writeBack++;
                            nextLevel.flag= 2;
                            nextLevel.readReqAddr= addr;
                            nextLevel.writeBackAddr=writeNextLevel;
                            //setLRU(reqIndex, reqTag, 2);
                            return nextLevel;  
                        }
                    }
                }
                else{
                    String writeNextLevel= setLRUVC(addr, 0);
                    nextLevel.flag= 1;
                    nextLevel.readReqAddr= addr;
                    nextLevel.writeBackAddr= "";
                    return nextLevel; 
                }
            }
        }
        //cache hit
        else{
            //System.out.println(name+ " READ HIT");
            nextLevel.flag= 0;
            nextLevel.readReqAddr= "";
            nextLevel.writeBackAddr= "";
            return nextLevel;
        }
    }        
    returnVal write(String addr){
        returnVal nextLevel = new returnVal();
        String reqTag   = addr.substring(0, addr.length()-(int) indexBits);
        String reqIndex = addr.substring(addr.length()-(int)indexBits);
        writeReqs=writeReqs+1;
        //System.out.println(name+ " WRITE REQ TAG " +reqTag+ " INDEX " +reqIndex);
        boolean hit_miss= getBlock(reqIndex, reqTag,1); //find the block to read
        //System.out.println(" HIT MISS " + hit_miss);
        if(!hit_miss){ //cache miss
            writeMiss= writeMiss+1;
            if(mVC == null){
                //System.out.println(name+ " WRITE MISS");
                String blockToBeEvicted = getLRUEvict(reqIndex);
                //Dirty block to be evicted
                if (blockToBeEvicted.equals("")){
                    //No writeback only read request to next to next level
                    nextLevel.flag= 1;
                    nextLevel.readReqAddr= addr;
                    nextLevel.writeBackAddr= "";
                    setLRU(reqIndex, reqTag, 1);
                    return nextLevel;                       
                }
                //Clean block to be evicted
                else{
                    //read request and write request to next level
                    //System.out.println(name+ " WRITE BACK");
                    writeBack=writeBack+1;
                    nextLevel.flag= 2;
                    nextLevel.readReqAddr= addr;
                    nextLevel.writeBackAddr= blockToBeEvicted+reqIndex;
                    setLRU(reqIndex, reqTag, 1);
                    return nextLevel;               
                }
            }
            else{
                //check if VC has invalid if so req to next level
                //otherwise go inc swap req
                boolean cacheValidWay = areThereInvalidWays(reqIndex);
                if(!cacheValidWay){
                    long doesVCHaveIt = checkVC(addr);
                    if(doesVCHaveIt !=-1){                    
                        //System.out.println("SHOULD BE SWAPPING");
                        swap(addr, reqIndex, doesVCHaveIt, 1);
                        nextLevel.flag= 0;
                        nextLevel.readReqAddr= "";
                        nextLevel.writeBackAddr= "";
                        return nextLevel;
                    }
                    else{
                        String writeNextLevel= setLRUVC(addr, 1);
                        if(writeNextLevel.equals("")){
                            //read request to next level
                            nextLevel.flag= 1;
                            nextLevel.readReqAddr= addr;
                            nextLevel.writeBackAddr= "";
                            //setLRU(reqIndex, reqTag, 1);
                            return nextLevel;  
                        }
                        else{
                            writeBack++;
                            nextLevel.flag= 2;
                            nextLevel.readReqAddr= addr;
                            nextLevel.writeBackAddr=writeNextLevel;
                            //setLRU(reqIndex, reqTag, 2);
                            return nextLevel;  
                        }
                    }
                }
                else{
                    String writeNextLevel= setLRUVC(addr, 1);
                    nextLevel.flag= 1;
                    nextLevel.readReqAddr= addr;
                    nextLevel.writeBackAddr= "";
                    return nextLevel; 
                }
            }
        }
        //cache hit
        else{
            //System.out.println(name+ " WRITE HIT");
            nextLevel.flag= 0;
            nextLevel.readReqAddr= "";
            nextLevel.writeBackAddr= "";
            return nextLevel;               
        }
    }            
    
    ///////////////////////////////////////////////////////
    boolean getBlock(String index, String reqTag, int flag){
        ArrayList<block_params> block_row = (ArrayList<block_params>) mCache.get(index);
        for(block_params e:block_row){
            if(e.tag.equalsIgnoreCase(reqTag) && e.valid_bit == 1) { //block is valid and address is in cache
                updateHitLRU(index, e.lru_counter); //update the LRU for hits
                e.lru_counter= 0; //update LRU counter to MRU
                if(flag==1) {
                    e.dirty_bit=1;
                }
                return true;
            }
        }
        return false;
    }
    void updateHitLRU(String index, long current_count){
        ArrayList<block_params> block_row = (ArrayList<block_params>) mCache.get(index);
        for(block_params e:block_row){
            if(e.lru_counter < current_count){
                e.lru_counter=e.lru_counter+1;
            }
        }
    }
    String getLRUEvict(String index){
        String dirtyBlockToEvict="";
        ArrayList<block_params> block_row = (ArrayList<block_params>) mCache.get(index);
        for(block_params e: block_row){
            if(e.lru_counter == mAssoc-1) { //block is valid and address is in cache
                if(e.dirty_bit == 1){
                        dirtyBlockToEvict=e.tag;
                }
            }
        }
        return dirtyBlockToEvict;
    }
    void setLRU(String index, String tag, int flag){
        ArrayList<block_params> block_row = (ArrayList<block_params>) mCache.get(index);
        for(block_params e: block_row){
            if(e.lru_counter == mAssoc-1){
                if(flag==0){
                    e.tag=tag;
                    e.dirty_bit=0;
                    e.valid_bit=1;
                    e.lru_counter=0;
                }
                else{
                    e.tag=tag;
                    e.dirty_bit=1;
                    e.valid_bit=1;
                    e.lru_counter=0;
                }
            }
            else{
                e.lru_counter=e.lru_counter+1;
            }
        }
    }

    
    /////////////////////VC Methods//////////////
    long checkVC(String address){
        mSwapReq++;
        for (block_params e: mVC){
            if(e.tag.equalsIgnoreCase(address) && e.valid_bit== 1) return e.lru_counter;
        }
        return -1;
    }
    void swap(String address, String index,long indexVCLoc, int flag){ //address
        ArrayList<block_params> block_row = (ArrayList<block_params>) mCache.get(index);
        for(block_params e: block_row){
            //System.out.println("SWAP");
            if(e.lru_counter==mAssoc-1){//THE LRU BLOCK IN MAIN CACHE
                for(block_params x:mVC){
                    if(x.lru_counter < indexVCLoc) x.lru_counter=x.lru_counter+1;
                    else if(x.lru_counter==indexVCLoc){
                        String tempAddr=x.tag;
                        long tempDirtyBlock= x.dirty_bit;
                        x.lru_counter = 0;
                        x.tag = e.tag+index;
                        x.dirty_bit= e.dirty_bit;                   
                        e.tag = tempAddr.substring(0, address.length()-(int)indexBits);
                        e.dirty_bit=tempDirtyBlock; //update the dirty bit
                        if(flag==1) e.dirty_bit=1;
                        e.lru_counter=0;//update main cache lru
                        swapsVC++;
                    }
                }
            }
            else{
                e.lru_counter=e.lru_counter+1; //increment the count of everything in cache by 1
            }
        }
    }
    String setLRUVC(String address, int flag){
        int decimal = Integer.parseInt(address,2);
        String hexStr = Integer.toString(decimal,16);
        //System.out.println("ADDRESS " + hexStr);
        String writeBack="";
        String reqTag   = address.substring(0, address.length()-(int) indexBits);
        String reqIndex = address.substring(address.length()-(int)indexBits);
        ArrayList<block_params> block_row = (ArrayList<block_params>) mCache.get(reqIndex);
        for(block_params e: block_row){
            if(e.lru_counter==mAssoc-1){                                            //THE LRU BLOCK IN MAIN CACHE THAT WILL BE EVICTED IF VALID
                if(e.valid_bit==1){                                                 //valid block from cache is making a request                     
                    for(block_params x:mVC){                                        //move the valid block to LRU in cache
                        if(x.lru_counter == mVCBlocks-1){
                            if(x.dirty_bit==1) {                                    //if the LRU in VC is dirty initaite writeback
                                writeBack=x.tag;
                            }                               
                            x.lru_counter=0;                                        //Move the block from Cache to VC
                            x.valid_bit=1;
                            x.tag=e.tag+reqIndex;
                            x.dirty_bit=e.dirty_bit;
                            if(flag ==1) {e.dirty_bit=1;}
                            else {e.dirty_bit=0;}
                            e.lru_counter=0;                                        //Update the cache contents for the actual request
                            e.tag=reqTag;
                            e.valid_bit=1;
                            if(flag==1) e.dirty_bit=1;
                            else e.dirty_bit=0;
                            }
                        else{                                                       //for every other block in VC update the lru by incrementing it by if less than the current
                            x.lru_counter=x.lru_counter+1;
                        }
                    }
                }
                //if the LRU is invalid update the LRU block 
                else{
                    if(flag == 1) e.dirty_bit=1;
                    else e.dirty_bit=0;
                    e.lru_counter=0;
                    e.tag=reqTag;
                    e.valid_bit=1;
                }
            }
            else{
                e.lru_counter=e.lru_counter+1;
            }
        }
        return writeBack;
    }
    
    boolean areThereInvalidWays(String index){
        ArrayList<block_params> block_row = (ArrayList<block_params>) mCache.get(index);
        for(block_params e:block_row){
            if(e.valid_bit==0){
                return true;
            }
        }
        return false;
    }
    ////////////////////Display Methods/////////////
    void displayCacheContents(){
        System.out.println("===== "+name+" contents =====");
        for(long i=0; i<mNumOfSets; i++){
            String binAddr= Long.toBinaryString(i);
            String prepend="";
            if(binAddr.length()<indexBits){
                prepend= "0";
                for(int x=1; x<indexBits-binAddr.length();x++){
                    prepend+="0";
                }
            }
            binAddr=prepend+ binAddr;
            int decimal = Integer.parseInt(binAddr,2);
            
            block_params []toDisplay = new block_params[(int)mAssoc];                                   //Array of fixed size store LRU elements in order
            ArrayList<block_params> block_row = (ArrayList<block_params>) mCache.get(binAddr);
            if(decimal<10) System.out.print("  set   "+ decimal+":   ");                                //Display shifts
            else if(decimal >= 10 && decimal<100) System.out.print("  set  "+ decimal+":   ");
            else if(decimal >= 100 && decimal<1000) System.out.print("  set "+ decimal+":   ");
            for(block_params e: block_row){
                toDisplay[(int)e.lru_counter]=e;
            }
            for(int k=0; k < (int)mAssoc; k++){
                block_params temp= toDisplay[k];
                String hexStr=temp.tag;
                String dirty=" ";
                if(temp.dirty_bit==1) dirty="D";
                if(!temp.tag.equals(" ")){
                    decimal = Integer.parseInt(temp.tag,2);
                    hexStr = Integer.toString(decimal,16);
                    System.out.print(hexStr+ " "+ dirty+"  ");
                }
                else{
                    System.out.print("      "+ " "+ dirty+"  ");
                }
            }
            System.out.print("\n");
        }

        if(mVC!=null){
            System.out.println("");
            block_params []vcDisplay = new block_params[mVC.size()];   
            for(block_params e: mVC){
                vcDisplay[(int)e.lru_counter]=e;
            }
            System.out.println("===== VC contents =====");
            System.out.print("  set   0:  ");
            for(int j=0; j<mVC.size(); j++){
                block_params temp= vcDisplay[j];
                String hexStr=temp.tag;
                String dirty=" ";
                if(temp.dirty_bit==1) dirty="D";
                if(!temp.tag.equals("")){
                    int decimal = Integer.parseInt(temp.tag,2);
                    hexStr = Integer.toString(decimal,16);
                    System.out.print(hexStr+ " "+ dirty+" ");
                }
                else{
                    System.out.print("        "+ " "+ dirty+" ");
                }
            }
        }
        System.out.print("\n\n");
    }
 
    int getReadReq(){
        return (int)readReqs;
    }
    int getReadMisses(){
        return (int)readMiss;
    }
    int getWriteReq(){
        return (int)writeReqs;
    }
    int getWriteMisses(){
        return (int)writeMiss;
    }
    int getSwaps(){
        return (int)swapsVC;
    }
    int getWritebacks(){
        return (int)writeBack;
    }
    double MissRate(){
        return 0.00;
    }
    int getSwapsReq(){
        return (int)mSwapReq;
    }
    double getSwapReqRate(){
        return (mSwapReq/(readReqs+writeReqs));
    }
    double combinedMissRate(){
        return (readMiss+writeMiss-swapsVC)/(writeReqs+readReqs);
    }
    double getFinalLevel(){
        return (readMiss)/(readReqs);
    }
}

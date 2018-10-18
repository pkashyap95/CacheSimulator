
import static java.lang.Math.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Priyank Kashyap
 */
public class Cache {
    private long mBlockSize;
    private long mAssoc;
    private long mCacheSize;
    private long mNumOfSets;
    private double indexBits;
    String name;
    Map mCache;
    ArrayList<block_params>VC;
    //Stats for the cache
    private int readReqs;
    private int readMiss;
    private int writeReqs;
    private int writeMiss;
    private int writeBack;

    //initialize the cache
    public Cache(String inName, long blockSize, long cacheSize, long associativity, long vc_blocks){
        mCache=new HashMap();
        mBlockSize = blockSize;
        mCacheSize = cacheSize;
        mAssoc     = associativity;
        mNumOfSets = mCacheSize/(mAssoc*mBlockSize); // calculate number of sets
        indexBits = log(mNumOfSets)/log(2);
        readReqs=0;
        readMiss=0;
        writeReqs=0;
        writeMiss=0;	
        writeBack=0;
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
        if(vc_blocks !=0){
            VC = new ArrayList<block_params>();
            for (long j=0; j<vc_blocks; j++){
                block_params default_block = new block_params();
                default_block.dirty_bit=0;
                default_block.valid_bit=0;
                default_block.tag="";
                default_block.lru_counter=j;
                VC.add(default_block);
            }
        }
    }
    returnVal read(String addr){
        returnVal nextLevel = new returnVal();
        String reqTag   = addr.substring(0, addr.length()-(int) indexBits);
        String reqIndex = addr.substring(addr.length()-(int)indexBits);
        readReqs=readReqs+1;
        //System.out.println(name+ " READ REQ TAG " +reqTag + " INDEX " +reqIndex);
        boolean hit_miss= getBlock(reqIndex, reqTag,0); //find the block to read
        if(!hit_miss){ //cache miss
            readMiss= readMiss+1;
            //System.out.println(name+ " READ MISS");
            String blockToBeEvicted = getLRUEvict(reqIndex);
            //Dirty block to be evicted
            if (blockToBeEvicted==""){
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
                //System.out.println(name+ " WRITEBACK");
                writeBack=writeBack+1;
                nextLevel.flag= 2;
                nextLevel.readReqAddr= addr;
                nextLevel.writeBackAddr= blockToBeEvicted+reqIndex;
                setLRU(reqIndex, reqTag, 0);
                return nextLevel;				
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
                //System.out.println(name+ " WRITE MISS");
                String blockToBeEvicted = getLRUEvict(reqIndex);
                //Dirty block to be evicted
                if (blockToBeEvicted==""){
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
        //cache hit
        else{
            //System.out.println(name+ " WRITE HIT");
            nextLevel.flag= 0;
            nextLevel.readReqAddr= "";
            nextLevel.writeBackAddr= "";
            return nextLevel;				
        }
    }                
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
    void swap(){};
    void print(){
        System.out.println(name +" READ REQS "+ readReqs);
        System.out.println(name +" READ MISSES "+ readMiss);
        System.out.println(name +" WRITE REQS  " + writeReqs);
        System.out.println(name +" WRITE MISSES "+ writeMiss);
        System.out.println(name +" WRITEBACK TO NEXT LEVEL "+ writeBack);
    }
}

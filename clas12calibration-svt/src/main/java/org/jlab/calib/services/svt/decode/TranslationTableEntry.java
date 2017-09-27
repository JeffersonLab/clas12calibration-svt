package org.jlab.calib.services.svt.decode;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author gavalian
 */
public class TranslationTableEntry {
    
    private final DetectorDescriptor desc = new DetectorDescriptor();
    public TranslationTableEntry(String _det_){
        this.desc.setType(DetectorType.getType(_det_));
    }
    
    public TranslationTableEntry(String _det_,
            int _sec_, int _lay_, int _comp_,
            int _cr_ , int _sl_ , int _ch_){
        this.desc.setType(DetectorType.getType(_det_));
        this.desc.setCrateSlotChannel(_cr_, _sl_, _ch_);
        this.desc.setSectorLayerComponent(_sec_, _lay_, _comp_);
    }
    
    public final void setDetector(int s, int l, int c){
        this.desc.setSectorLayerComponent(s, l, c);
    }
    
    public final void setCreate(int c, int s, int ch){
        this.desc.setCrateSlotChannel(c, s, ch);
    }
    
    public static Integer getHashCreate(int _create_, int _slot_, int _ch_){
        Integer hash_c = _create_*256*256*256 + _slot_*256*256 + _ch_;
        return hash_c;
    }
    
    public DetectorDescriptor descriptor(){return this.desc;}
    
    public Integer getHashCreate(){
        return TranslationTableEntry.getHashCreate(
                this.desc.getCrate(),
                this.desc.getSlot(), 
                this.desc.getChannel());
    }
    
    public void parse(String format){
        String[] tokens = format.split("\\s+");
        if(tokens.length<7){
            System.err.println("[ERROR] error parsing the string : " + format);
            return;
        }
        
        
        this.desc.setType(DetectorType.getType(tokens[0]));
        this.desc.setCrateSlotChannel(
                Integer.parseInt(tokens[1]),
                Integer.parseInt(tokens[2]),
                Integer.parseInt(tokens[3]));
        
        this.desc.setSectorLayerComponent(
                Integer.parseInt(tokens[4]),
                Integer.parseInt(tokens[5]),
                Integer.parseInt(tokens[6]));        
    
        if(tokens.length>7){
            this.desc.setOrder(Integer.parseInt(tokens[7]));
        }
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(this.desc.toString());
        return str.toString();
    }
}

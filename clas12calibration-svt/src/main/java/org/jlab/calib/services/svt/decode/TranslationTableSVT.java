package org.jlab.calib.services.svt.decode;

import java.util.ArrayList;

/**
 *
 * @author gavalian
 */
public class TranslationTableSVT extends TranslationTable {
    
    public TranslationTableSVT(){
        
    }
    
    @Override
    public void translateEntries(ArrayList<RawDataEntry>  rawdata){
        SVTDataRecord  record = new SVTDataRecord();
        for(RawDataEntry entry : rawdata){
            TranslationTableEntry conversion = this.getEntry(entry.getCrate(),
                    entry.getSlot(),entry.getSVTHalf());
            if(conversion!=null){
                record.init(conversion.descriptor().getSector(), 
                        conversion.descriptor().getLayer(), entry.getSVTHalf(), 
                        entry.getSVTChipID(), entry.getSVTChannel());
                entry.setSectorLayerComponent(record.SECTOR, record.LAYER,
                        record.STRIP);
            } else {
                System.err.println(" ERROR : cant find entry " + entry.getCrate()
                        + "  " + 
                    entry.getSlot() + "  " + entry.getSVTHalf());
            }
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.calib.services.svt.decode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.detector.base.DetectorType;


/**
 *
 * @author gavalian
 */
public class AbsDetectorTranslationTable implements IDetectorTranslationTable {
    
    private String tableName = "UNKNOWN";
    private TreeMap<Integer,TranslationTableEntry>  tableEntries = new 
            TreeMap<Integer,TranslationTableEntry>();
    private Boolean  supressErrors     = true;
    private Integer  bankTagNumber     = 100;
    private DetectorType   type = DetectorType.UNDEFINED;
    
    public AbsDetectorTranslationTable(){
        
    }
    
    public AbsDetectorTranslationTable(String name){
        this.tableName = name;
    }
    
    public AbsDetectorTranslationTable(String name, Integer tag){
        this.tableName = name;
        this.bankTagNumber = tag;
    }
    
    public String getName() {
        return this.tableName;
    }
    
    public void setName(String name) {
        this.tableName = name;
    }
    
    public void setBankTag(int tag){
        this.bankTagNumber = tag;
    }
    
    public int getBankTag(){
        return this.bankTagNumber;
    }
    
    public Integer getSector(int crate, int slot, int channel) {
        Integer key = TranslationTableEntry.getHashCreate(crate, slot, channel);
        if(this.tableEntries.containsKey(key)==true){
            return this.tableEntries.get(key).descriptor().getSector();
        }
        
       this.printNotFoundError(crate, slot, channel);
       return -1;
    }
    
    public DetectorType  getDetectorType(){ return this.type; }
    public void setDetectorType(DetectorType t){
        this.type = t;
    }
    
    public  Boolean hasEntry(int crate, int slot, int channel){
        Integer key = TranslationTableEntry.getHashCreate(crate, slot, channel);
        return this.tableEntries.containsKey(key);
    }
    
    public TranslationTableEntry getEntry(int crate, int slot, int channel){
        Integer key = TranslationTableEntry.getHashCreate(crate, slot, channel);
        return this.tableEntries.get(key);
    }
            
    private void printNotFoundError(int crate, int slot, int channel){
        if(this.supressErrors==false){
            System.out.println(String.format("ERROR:: [%12s] no entry for CRATE/SLOT/CHANNEL [%8d %8d %8d]",
                    this.getName(),crate,slot,channel));
        }
    }
    
    public Integer getLayer(int crate, int slot, int channel) {
        Integer key = TranslationTableEntry.getHashCreate(crate, slot, channel);
        if(this.tableEntries.containsKey(key)==true){
            return this.tableEntries.get(key).descriptor().getLayer();
        }
        this.printNotFoundError(crate, slot, channel);
        return -1;
    }
    
    public Integer getComponent(int crate, int slot, int channel) {
        Integer key = TranslationTableEntry.getHashCreate(crate, slot, channel);
        if(this.tableEntries.containsKey(key)==true){
            return this.tableEntries.get(key).descriptor().getComponent();
        }
        return -1;
    }
    
    public Integer getOrder(int crate, int slot, int channel) {
        Integer key = TranslationTableEntry.getHashCreate(crate, slot, channel);
        if(this.tableEntries.containsKey(key)==true){
            return this.tableEntries.get(key).descriptor().getOrder();
        }
        return -1;
    }
    
    
    public void addEntry(int crate, int slot, int channel, int sector,int layer, int component){
        TranslationTableEntry entry = new TranslationTableEntry(this.tableName);
        entry.setCreate(crate, slot, channel);
        entry.setDetector(sector, layer, component);
        if(this.tableEntries.containsKey(entry.getHashCreate())==true){
            System.out.println("ERROR : dublicate entry is not being added : " + entry.toString());
        } else {
            this.tableEntries.put(entry.getHashCreate(), entry);
        }
    }
    
    public void addEntry(String entryLine){
        TranslationTableEntry entry = new TranslationTableEntry("empty");
        entry.parse(entryLine);
        if(entry.descriptor().getType()==DetectorType.UNDEFINED){
            System.err.println("----> error : detector type unknown");
            return;
        } else {
            this.type = entry.descriptor().getType();
            this.tableName = this.type.getName();
        }
        
        if(this.tableEntries.size()==0){
            this.type = entry.descriptor().getType();            
        } else {
            if(entry.descriptor().getType()!=this.type){
                System.err.println("----> error : entry # " +
                this.tableEntries.size() + " has wrong detector type");
                return;
            }
        }
        
        
        if(this.tableEntries.containsKey(entry.getHashCreate())==true){
            System.err.println("[Translation Map] (ERROR) ==> "
            + " Duplicate entry detected. Not added.\n" + "\t" + entryLine);
        } else {
            this.tableEntries.put(entry.getHashCreate(), entry);
        }
    }
    
    public void readFile(String filename){
        BufferedReader in = null;        
        try {
            in = new BufferedReader(new FileReader(filename));
            while (in.ready()) {
                String s = in.readLine();
                if(s.startsWith("#")==false){
                    this.addEntry(s);
                }
            }               
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TranslationTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TranslationTable.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(TranslationTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void readResourceFile(String filename){
        
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("TRANSLATION TABLE  %s  ENTRIES =  %d",
                this.getName(),
                this.tableEntries.size()));
        int counter = 0;
        for(Map.Entry<Integer,TranslationTableEntry> entry : this.tableEntries.entrySet()){
            str.append(String.format("%8s : ", counter));
            str.append(String.format("%-12d : %s\n", entry.getKey(),entry.getValue().toString()));
            counter++;
        }
        str.append(String.format("END TRANSLATION TABLE  %s  ENTRIES =  %d",
                this.getName(),
                this.tableEntries.size()));
        return str.toString();
    }
}

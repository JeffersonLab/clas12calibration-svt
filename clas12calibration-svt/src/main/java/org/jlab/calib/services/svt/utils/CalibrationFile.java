package org.jlab.calib.services.svt.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gavalian
 */
public class CalibrationFile {

    Map<Integer,String> mapFile = new HashMap<Integer,String>();
    List<CalibrationData>  calibData = new ArrayList<CalibrationData>();
    
    public CalibrationFile(){
        
    }
    
    public List<CalibrationData> getDataList(){ return this.calibData;}
    public int  getShift(String str){
        String[] tokens = str.split(",");
        return Integer.parseInt(tokens[1].trim());
    }
    
    public int  getAmplitude(String str){
        String[] tokens = str.split(",");
        return Integer.parseInt(tokens[0].trim());
    }

    public double[]  getArray(String str){
        String[] tokens = str.split(",");
        double[] array = new double[tokens.length-2];
        for(int loop = 0; loop < array.length; loop++){
            array[loop] = Double.parseDouble(tokens[loop+2].trim());
        }
        return array;
    }
    
    public void read(String file){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while(line!=null){

                String[] tokens = line.split("\\s+");
//                System.out.println("line " + tokens.length + " : [" + line + "]");
                if(tokens.length==1){
                    if(tokens[0].length()>0){
                        int channel = Integer.parseInt(tokens[0]);
                        String s1 = br.readLine();
                        String s2 = br.readLine();
                        String s3 = br.readLine();
                        CalibrationData data = new CalibrationData(0,0,channel);
                        
                        data.addGraph(this.getAmplitude(s1), this.getShift(s1), this.getArray(s1));
                        data.addGraph(this.getAmplitude(s2), this.getShift(s2), this.getArray(s2));
                        data.addGraph(this.getAmplitude(s3), this.getShift(s3), this.getArray(s3));
                        
                        this.calibData.add(data);
                    }
                }
                
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CalibrationFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CalibrationFile.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }
}

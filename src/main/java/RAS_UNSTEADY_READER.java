/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rma.client.Browser;
import com.rma.model.Project;
import hec2.wat.client.WatFrame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Q0HECWPL
 */
public class RAS_UNSTEADY_READER {
    private final String _filePath;
    private final RASReservoir[] _RASReservoirArray;
    public RAS_UNSTEADY_READER(String path, RASReservoir[] RASReservoirArrayIn){
        _filePath = path;
        _RASReservoirArray = RASReservoirArrayIn;
    }
    public boolean updateFile(){
            WatFrame fr = null;
            fr = hec2.wat.WAT.getWatFrame();
            //read in Unsteady Flow File
            BufferedReader brp = null;
            File pf = new File(_filePath);
            String uFileLine = "";
            String newUFile = "";
            if(pf.exists()){
                try {
                    brp = new BufferedReader(new FileReader(_filePath));
                    String[] tmp = null;
                    while ((uFileLine = brp.readLine()) != null) {
                        //newUFile += uFileLine;
                        tmp = uFileLine.split("=");
                        if(tmp.length==0){
                            newUFile += uFileLine + "\r\n";
                            continue;
                        }
                        if(tmp[0].equals("Initial Flow Loc")){
                            String[] vals = tmp[1].split(",");
                            //If River, Reach, and XS Match...
                            for(RASReservoir reservoir: _RASReservoirArray) {
                                if (vals[0].equals(reservoir.get_RASname().get_river()) &&
                                        vals[1].equals(reservoir.get_RASname().get_reach()) &&
                                        vals[2].equals(reservoir.get_RASname().get_XS())) {
                                    String newLocLine = tmp[0] + "=" + vals[0];
                                    for (int i = 1; i < 3; i++) {
                                        newLocLine += "," + vals[i];
                                        newLocLine += "," + reservoir.get_initialFlow();
                                        newUFile += newLocLine + "\r\n";
                                    }
                                }
                            }
                        }
                        else if(tmp[0].equals("Initial Storage Elev")) {
                            // set the new starting pool
                            String[] vals = tmp[1].split(",");
                            for (RASReservoir reservoir : _RASReservoirArray) {
                                if (vals[0].equals(reservoir.get_RASname().get_ReservoirName())) {
                                    String newElevLine = tmp[0] + "=" + vals[0];
                                    newElevLine += "," + reservoir.get_initialPool();
                                    newUFile += newElevLine + "\r\n";
                                }
                            }
                        }
                        else if( tmp[0].equals("Initial RRR Elev")){
                            String[] vals = tmp[1].split(",");
                            for (RASReservoir reservoir : _RASReservoirArray) {
                                if ( vals[0].equals(reservoir.get_RASname().get_river()) &&
                                vals[1].equals(reservoir.get_RASname().get_ReservoirName())){
                                    String newElevLine = tmp[0] + "=" + vals[0] + "," + vals[1];
                                    newElevLine += "," + reservoir.get_initialPool();
                                    newUFile += newElevLine + "\r\n";
                                }
                            }

                        }
                        else{
                            newUFile += uFileLine + "\r\n";
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (brp != null) {
                        try {
                            brp.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        //write out newUFile.
        BufferedWriter bw = null;
        boolean ret = true;
        try {
            bw = new BufferedWriter(new FileWriter(_filePath));
            bw.write(newUFile);
            bw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ret = false;
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    ret = false;
                }
            }

        }
        return ret;
    }
}

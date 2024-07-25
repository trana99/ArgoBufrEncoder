/*
 * Created on 21-Jun-06 To change the template for this generated file go
 * to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package dfo.isdm.WriteArgoBufr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import org.apache.log4j.Logger;

import dfo.isdm.BufrUtility.BufrTable;
import dfo.isdm.BufrUtility.CreateBufrFile;
import dfo.isdm.BufrUtility.Sec3NprofMapDTO;


/**
 * the WriteArgoBufr class
 * @author Tran 11-Aug-06
 */
public class WriteMedsArgoBufr {

    /**
     * the logger instance for this class
     */
    private static Logger log = Logger.getLogger(WriteMedsArgoBufr.class);
    private static BufrTable bufrtable;
    private static int dataset;
    private static String bulletinHeader;
    private static String bulletinOrigin;
    private static String originatorCenterId;
    private static String originatorSubCenterId;
    private static List<Sec3NprofMapDTO>sec3Sequences = new ArrayList<Sec3NprofMapDTO>();
    private static Properties props;
    private static List<String> fileToBufr;
    private static String outFile;
    private static boolean sendAuxData = true;
    private static String argoTemplate;

    /***
     * 
     * @param _inputProfileNetcdf
     * @throws IOException 
     */
    public WriteMedsArgoBufr(List <String> _inputProfileNetcdf, String _outFile, 
    		String _argoTemplate, String _bulletinHeader,
    		String _bulletinOrigin, String _originatorCenterId,
    		String _originatorSubCenterId, boolean _sendAuxData, Logger _log ) 
    				throws IOException{
    	fileToBufr = new ArrayList<String>();
    	fileToBufr = _inputProfileNetcdf;
    	argoTemplate = _argoTemplate;
    	outFile = _outFile;
    	bulletinHeader = _bulletinHeader;
    	bulletinOrigin = _bulletinOrigin;
    	originatorCenterId = _originatorCenterId;
    	originatorSubCenterId = _originatorSubCenterId;
    	sendAuxData = _sendAuxData;
    	log = _log;
    	write();
    }

    /**
     * @param args ArgoBufrTemplate.txt ncfilelist.txt bufr.bin
     * @throws IOException if file not found
     */
    private void write () throws IOException {
        boolean auxData = true;
        if (!sendAuxData){
        	auxData= false;
        }
        bufrtable = new BufrTable(argoTemplate, log);
        String netcdfFile = "";
        String bncFile="";
        
        int fileId = 0;
        CreateBufrFile writeDataToFile = new CreateBufrFile(outFile, bulletinHeader,bulletinOrigin,
        		originatorCenterId, originatorSubCenterId, log);
         
        // convert netcdf file format to BUFR format
        
        for (int i =0; i < fileToBufr.size(); i++) {
        	dataset = 0;
        	File f1 = new File (fileToBufr.get(i));
        	String currentInputdir = f1.getParent();
        	String bFile = currentInputdir + "\\B"+ f1.getName();
            if (f1.getName().startsWith("R")) {
                netcdfFile = fileToBufr.get(i);
                if (fileToBufr.contains(bFile)){
                	bncFile = bFile;
                } else {
                	bncFile = null;
                }
                sec3Sequences = new ArrayList<Sec3NprofMapDTO>();
                //System.out.println("working on: " + netcdfFile);
                log.info("Start write BUFR message for " + netcdfFile );
                dataset = dataset + 1;
                ArgoBufrSection4 sec4 = new ArgoBufrSection4(netcdfFile, bncFile,auxData, bufrtable);
                StringBuffer sbuf4 = new StringBuffer();
                sbuf4 = sec4.getSbuf4();
                log.info("total length of section 4 " + sbuf4.length()/8 + " octets");
                //double sec4len = 4.0 + (sbuf4.length()/8.0);
                sec3Sequences = sec4.getSec3Sequences();
                // there is a limit of 15000 octet per file. Create new
                // file.  The total octet need for sec 0,1,3, 4 is 46 octets
                fileId = fileId + 1;                
                SetBufrSection3 section3 = new SetBufrSection3 (dataset, sec3Sequences);                    	 
                writeDataToFile.create(fileId, sbuf4, section3.getSec3());
                log.info("Write out to Bufr file " + outFile + fileId );
            }
        }
    }
    // End of class

}

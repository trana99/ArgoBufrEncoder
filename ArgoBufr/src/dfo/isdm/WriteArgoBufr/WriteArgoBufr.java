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
public class WriteArgoBufr {

    /**
     * the logger instance for this class
     */
    private static Logger log = Logger.getLogger(WriteArgoBufr.class);
    private static BufrTable bufrtable;
    private static int dataset;
    private static String bulletinHeader;
    private static String bulletinOrigin;
    private static String originatorCenterId;
    private static String originatorSubCenterId;
    private static List<Sec3NprofMapDTO>sec3Sequences = new ArrayList<Sec3NprofMapDTO>();
    private static Properties props;
    private static List<String> fileToBufr;

    /**
     * Default constructor which can 't instantiate
     */
    public WriteArgoBufr() {

    }

    /**
     * @param args ArgoBufrTemplate.txt ncfilelist.txt bufr.bin
     * @throws IOException if file not found
     */
    public static void main(String[] args) throws IOException {
        props = new Properties();
        props.load(new FileInputStream(args[0]));
        String argoTemplate = props.getProperty("ArgoBufr.BufrTemplate");
        String curentInputdir = props.getProperty("ArgoBufr.inputFolder"); 
        String outFile = props.getProperty("ArgoBufr.outputBufr");
        bulletinHeader = props.getProperty("ArgoBufr.bulletinHeader");
        bulletinOrigin = props.getProperty("ArgoBufr.bulletinOrigin");
        originatorCenterId = props.getProperty("ArgoBufr.originatorCenterId");
        originatorSubCenterId = props.getProperty("ArgoBufr.originatorSubCenterId");
        String failedFtpFile = props.getProperty("ArgoBufr.failedFtpFile");
        String sendAuxData = props.getProperty("ArgoBufr.sendAuxData");
        boolean auxData = true;
        if (sendAuxData.trim().compareToIgnoreCase("false")==0){
        	auxData= false;
        	System.out.println(sendAuxData + " " + auxData);

        }
        GetListofInputFile fileListing = new GetListofInputFile(curentInputdir, ".nc", log);
        List<String> fileToBufr = fileListing.getListOfFilesForBUFR();
        System.out.println(argoTemplate);
        bufrtable = new BufrTable(argoTemplate, log);
        if (new File (failedFtpFile).exists()){
        	 BufferedReader in = new BufferedReader(new FileReader(failedFtpFile));
        	 String str;
        	 str = in.readLine();
        	 while ((str = in.readLine()) != null) {
        		 if (fileToBufr.contains(str)){
        			 fileToBufr.remove(str);
        			 log.info("Ignore conversion to BUFR for " + str);
        		 }
        	 }
        	 in.close();        	 
        }
        String netcdfFile = "";
        String bncFile="";
        
        int fileId = 0;
        CreateBufrFile writeDataToFile = new CreateBufrFile(outFile, bulletinHeader,bulletinOrigin,
        		originatorCenterId, originatorSubCenterId, log);
         
        // convert netcdf file format to BUFR format
        
        for (int i =0; i < fileToBufr.size(); i++) {
        	dataset = 0;
            if (fileToBufr.get(i).startsWith("R")) {
                netcdfFile = curentInputdir + fileToBufr.get(i);
                if (fileToBufr.contains("B".concat(fileToBufr.get(i)))){
                	bncFile = curentInputdir + "B" + fileToBufr.get(i);
                } else {
                	bncFile = null;
                }
                sec3Sequences = new ArrayList<Sec3NprofMapDTO>();
                System.out.println("working on: " + netcdfFile + " and " +bncFile);
                log.info("Start write BUFR message for " + netcdfFile + " and " + bncFile);
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

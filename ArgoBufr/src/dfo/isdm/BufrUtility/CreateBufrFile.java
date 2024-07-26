package dfo.isdm.BufrUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.apache.logging.log4j.*;



public final class CreateBufrFile {
	private static Logger log;
	private static StringBuffer bufrMsg;
	private static StringBuffer bufrMsgSection3;
	private static String fileName;
	private static String bulletinHeader;
	private static String bulletinOrigin;
    private static String originatorCenterId;
    private static String originatorSubCenterId;

	private static String outputFile;
	public CreateBufrFile (String outfile, String _bulletinHeader, 
			String _bulletinOrigin, String _originatorCenterId, 
			String _originatorSubCenterId, Logger _log){
		log = _log;
		bulletinHeader = _bulletinHeader;
		bulletinOrigin = _bulletinOrigin;
		originatorCenterId = _originatorCenterId;
		originatorSubCenterId = _originatorSubCenterId;
		outputFile = outfile;
		
	}
	public void create(int fileId, StringBuffer msg,
			StringBuffer _bufrMsgSection3) {
		bufrMsg = msg;
		bufrMsgSection3 = _bufrMsgSection3;
		NumberFormat formatter = new DecimalFormat("00");
		String s = formatter.format(fileId);  
		fileName = outputFile + s;
		while (new File(fileName).exists()){
			fileId = fileId +1;
			fileName = outputFile + formatter.format(fileId);
		}
		log.info("Write file " + fileName);        
		try {
			FileOutputStream bufrbin = (new FileOutputStream(fileName));
	        //pad zero to the right for section 4
	        StringBuffer totalsec4 = new StringBuffer("");
	        totalsec4.append(BufrUtility.completeSection(bufrMsg, log));
	        
	        log.info("Complete Section 4 of BUFR with # octets " 
	        		+(totalsec4.length()/8));

	        // write to bufr file
	        Header header = new Header(bulletinHeader, bulletinOrigin);
	        StringBuffer headerstr = header.encodeGtsHeader();
	        Section1 sec1 = new Section1(log);
	        StringBuffer buf1 = sec1.encodeSection1(originatorCenterId, originatorSubCenterId);
	        log.info("Complete Section 1 of BUFR with " + (buf1.length()/8) + " octets");
	        log.info("Complete Section 3 of BUFR with " + (bufrMsgSection3.length()/8) + " octets");
	        int totaloctet = 8 + (buf1.length() + bufrMsgSection3.length() + totalsec4
	            .length()) / 8 + 4;
	        StringBuffer buf0 = new StringBuffer("");
	        Section0 section0 = new Section0(totaloctet);
	        buf0 = section0.getSection0();
	        StringBuffer buf5 = new StringBuffer("");
	        buf5.append(BufrUtility.putChar("7777", 32));

	        if (totaloctet < 500000) {
	            BufrUtility.writeoutput(bufrbin, headerstr, log);
	            BufrUtility.writeoutput(bufrbin, buf0, log);
	            BufrUtility.writeoutput(bufrbin, buf1, log);
	            BufrUtility.writeoutput(bufrbin, bufrMsgSection3, log);
	            BufrUtility.writeoutput(bufrbin, totalsec4, log);
	            BufrUtility.writeoutput(bufrbin, buf5, log);
	            bufrbin.close();
	            log.info("Complete write BUFR message into " + fileName
	                + ".  Total of # octet written:  " + totaloctet);
	        } else {
	            log.info("Total Bufr length is more than 500000 octets "
	                + totaloctet);
	            log.info("BUFR not created !!!!!");
	        }


		} catch (FileNotFoundException e) {
			log.info("Can't not create file " + fileName + e);
		} catch (IOException e) {
			log.info("Can't close the file " + fileName + e);
		}

		
	}

}

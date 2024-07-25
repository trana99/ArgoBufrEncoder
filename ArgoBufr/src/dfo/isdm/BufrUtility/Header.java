package dfo.isdm.BufrUtility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;



/**
 * the Header class contained gts header and source id, month day hour minute
 * @author Tran 27-Jul-06
 */
public class Header {
	/**
	 * gts header
	 */
	private String gtsHeader = "IOZX02";
	/**
	 * Source id
	 */
	private String sourceId = "KARS";
	/**
	 * gts header buffer string for Bufr message
	 */
	private StringBuffer gtsHeaderSbf;
	/**
	 * The date and time that the message was created
	 */
	private String gtsDayTime;
	private static Logger log;

	public Header (Logger _log){
		log = _log;
	}
	/**
	 * Default constructor for SetHeader class extends Header
	 * @param header gtsHeader
	 * @param source Source ID
	 */
	public Header(String header, String source) {
	        gtsHeader = header;
	        sourceId = source;
	}

	/**
	 * Default constructor for SetHeader extends Header Class
	 * @throws IOException 
	 */
	
	public Header(String gtsHeaderFile) {
	    try {
			BufferedReader gtsFile = new BufferedReader(new FileReader(
			        gtsHeaderFile));
			gtsHeader = gtsFile.readLine();
			sourceId = gtsFile.readLine();
			gtsFile.close();
		} catch (FileNotFoundException e) {
			log.error("No gts header file found" + e);
		} catch (IOException e) {
			log.error("Error in reading the gtsHeader file " + e);
		}
	}

	public String getGtsHeader() {
		return gtsHeader;
	}
	public void setGtsHeader(String gtsHeader) {
		this.gtsHeader = gtsHeader;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public StringBuffer getGtsHeaderSbf() {
		return gtsHeaderSbf;
	}
	public void setGtsHeaderSbf(StringBuffer gtsHeaderSbf) {
		this.gtsHeaderSbf = gtsHeaderSbf;
	}
	public String getGtsDateTime() {
		return gtsDayTime;
	}
	public void setGtsDateTime(String gtsDateTime) {
		this.gtsDayTime = gtsDateTime;
	}

	/**
	 * @return GTS Bufr header
	 */
	public StringBuffer encodeGtsHeader() {
	    gtsHeaderSbf = new StringBuffer("");
	  //  gtsHeaderSbf.append("00000001");
	    gtsHeaderSbf.append(BufrUtility.putChar(gtsHeader, (gtsHeader.length() * 8)));
	    gtsHeaderSbf.append(BufrUtility.putChar(" ", 8));
	    gtsHeaderSbf.append(BufrUtility.putChar(sourceId, (sourceId.length() * 8)));
	    gtsHeaderSbf.append(BufrUtility.putChar(" ", 8));
	    Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	    //int month = cal.get(Calendar.MONTH);
	    int day = cal.get(Calendar.DAY_OF_MONTH);
	    int hour = cal.get(Calendar.HOUR_OF_DAY);
	    int min = cal.get(Calendar.MINUTE);
	    NumberFormat formatter = new DecimalFormat("00");
	    String time =
	        new String(
	            formatter.format(day)
	                + formatter.format(hour)
	                + formatter.format(min));
	    gtsHeaderSbf.append(BufrUtility.putChar(time, 4));
	    // encode CR
	    gtsHeaderSbf.append("00001101");
	    gtsHeaderSbf.append("00001101");
		// encoded the line feed before the general BUFR message attach

	    gtsHeaderSbf.append("00001010");
	    return gtsHeaderSbf;
	}
	/**
	 * This method read byte array of 28 byte and decode it into gtsHeader, source Id and date time
	 * @param b
	 */
	public void decodeGtsHeader(Byte[]b) {
		String msgHeader = new String ();
		for (int i = 1; i < b.length ; i++){
			msgHeader = msgHeader + (char)b[i].byteValue();
		}
		gtsHeader = msgHeader.substring(0, 7).trim();
		sourceId = msgHeader.substring(7, 12).trim();
		gtsDayTime = msgHeader.substring(12).trim();		
		log.info("Read in msg header=" + msgHeader );
		log.info("bulletin header=" + gtsHeader);
		log.info("sourceId="+sourceId);
		log.info("GTSDayTime="+gtsDayTime);
		
		
		
		
	}
}

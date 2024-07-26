/*
 * Created on 11-Jul-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package dfo.isdm.BufrUtility;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.logging.log4j.*;




/**
 * @author Tran
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Section1 {
    private static Logger log;
    private static int msgLength;
    private static int bufrMasterTable;
    private static int originator;
    private static int subOriginator;
    private static int seqNo;
    private static boolean optionalSection;
    private static int dataCategory;
    private static int dataSubCategory;
    private static int LocalSubCategory;
    private static int bufrTableVersionNo;
    private static int bufrLocalTableVersion;
    private static String dateTime;
    private static int reserved;
    private static List<BufrDescriptorDto> data = new ArrayList<BufrDescriptorDto>();
	/**
	 * constructor class for encode section 1
	 */
	public Section1(Logger _log) {
		log = _log;
	    }
	public int getMsgLength() {
		return msgLength;
	}
	public static void setMsgLength(int msgLength) {		
		Section1.msgLength = msgLength;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.SEC1_LENGTH");
		dto.setDecodedValue(Integer.toString(msgLength));
		log.info("Section 1 length =" + dto.getDecodedValue());
		data.add(dto);
	}
	public  int getBufrMasterTable() {
		return bufrMasterTable;
	}
	public static void setBufrMasterTable(int bufrMasterTable) {
		Section1.bufrMasterTable = bufrMasterTable;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.BUFR_MASTER_TABLE");
		dto.setDecodedValue(Integer.toString(bufrMasterTable));
		log.info("Bufr master table =" + dto.getDecodedValue());
		data.add(dto);

	}
	public int getOriginator() {
		return originator;
	}
	public static void setOriginator(int originator) {
		Section1.originator = originator;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.ORIGINATOR_CENTER");
		dto.setDecodedValue(Integer.toString(originator));
		log.info("Originator center = " + dto.getDecodedValue());
		data.add(dto);
	}
	public int getSubOriginator() {
		return subOriginator;
	}
	public static void setSubOriginator(int subOriginator) {
		Section1.subOriginator = subOriginator;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.ORIGINATOR_SUB_CENTER");
		dto.setDecodedValue(Integer.toString(subOriginator));
		log.info("Originator sub-center = " + dto.getDecodedValue());
		data.add(dto);

	}
	public  int getSeqNo() {
		return seqNo;
	}
	public static void setSeqNo(int seqNo) {
		Section1.seqNo = seqNo;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.SEQ_NO");
		dto.setDecodedValue(Integer.toString(seqNo));
		log.info("Sequence number = " + dto.getDecodedValue());
		data.add(dto);
	}
	public boolean isOptionalSection() {
		return optionalSection;
	}
	public static void setOptionalSection(boolean optionalSection) {
		Section1.optionalSection = optionalSection;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.OPTIONAL_SECTION");
		if (optionalSection){
			dto.setDecodedValue("Y");
		} else{
			dto.setDecodedValue("N");
		}
		log.info("Optional section = " + dto.getDecodedValue());
		data.add(dto);

	}
	public int getDataCategory() {
		return dataCategory;
	}
	public static void setDataCategory(int dataCategory) {
		Section1.dataCategory = dataCategory;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.DATA_CATEGORY");
		dto.setDecodedValue(Integer.toString(dataCategory));
		log.info("Data category (table A) = " + dto.getDecodedValue());
		data.add(dto);
	}
	public int getDataSubCategory() {
		return dataSubCategory;
	}
	public static void setDataSubCategory(int dataSubCategory) {
		Section1.dataSubCategory = dataSubCategory;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.INT_SUB_CATEGORY");
		dto.setDecodedValue(Integer.toString(dataSubCategory));
		log.info("Internation data sub-category = " + dto.getDecodedValue());
		data.add(dto);
	}
	public int getLocalSubCategory() {
		return LocalSubCategory;
	}
	public static void setLocalSubCategory(int localSubCategory) {
		LocalSubCategory = localSubCategory;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.LOCAL_SUB_CATEGORY");
		dto.setDecodedValue(Integer.toString(localSubCategory));
		log.info("Local sub-category =" + dto.getDecodedValue());
		data.add(dto);

	}
	public int getBufrTableVersionNo() {
		return bufrTableVersionNo;
	}
	public static void setBufrTableVersionNo(int bufrTableVersionNo) {
		Section1.bufrTableVersionNo = bufrTableVersionNo;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.BUFR_MASTER_TABLE_VER");
		dto.setDecodedValue(Integer.toString(bufrTableVersionNo));
		log.info("Version of master table = " + dto.getDecodedValue());
		data.add(dto);

	}
	public int getBufrLocalTableVersion() {
		return bufrLocalTableVersion;
	}
	public static void setBufrLocalTableVersion(int bufrLocalTableVersion) {
		Section1.bufrLocalTableVersion = bufrLocalTableVersion;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.LOCAL_TABLE_VER");
		dto.setDecodedValue(Integer.toString(bufrLocalTableVersion));
		log.info("Version of local table = " + dto.getDecodedValue());
		data.add(dto);

	}
	public String getDateTime() {
		return dateTime;
	}
	public static void setDateTime(String dateTime) {
		Section1.dateTime = dateTime;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.SEC1_CREATION_TIME");
		dto.setDecodedValue(dateTime);
		log.info("Message creation time = "+ dto.getDecodedValue());
		data.add(dto);

	}
	public int getReserved() {
		return reserved;
	}
	public static void setReserved(int reserved) {
		Section1.reserved = reserved;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.ADP_RESERVED");
		dto.setDecodedValue(Integer.toString(reserved));
		log.info("Reserved for local used= " + dto.getDecodedValue());
		data.add(dto);

	}
	public List<BufrDescriptorDto> getDecodedData() {
		return data;
	}	

	/**
	 * @return StringBuffer of section1
	 */
	public StringBuffer encodeSection1(String originatorCenterId, String originatorSubCenterId) {
	    StringBuffer sec1 = new StringBuffer("");
	    sec1.append(BufrUtility.integerToBinary("24", 24,log)); // length of section
	    sec1.append(BufrUtility.integerToBinary("0", 8,log)); // bufr master table
	//  need to get id for data center. info gets from table C-11
	    sec1.append(BufrUtility.integerToBinary(originatorCenterId, 16,log));
	// sub-center Id table c-12
	    sec1.append(BufrUtility.integerToBinary(originatorSubCenterId, 16,log));
	    sec1.append(BufrUtility.integerToBinary("0", 8,log)); //update sequence number
	// bit flag
	    sec1.append(BufrUtility.integerToBinary("0", 1,log)); // 0 -no option section
	//  set to zero to reserved
	    sec1.append(BufrUtility.integerToBinary("0", 7,log));
	//Data category, 31: oceanographic
	    sec1.append(BufrUtility.integerToBinary("31", 8,log));
	//International data sub-categeroy 4:float
	    sec1.append(BufrUtility.integerToBinary("4", 8,log));
	// local data sub-category
	    sec1.append(BufrUtility.integerToBinary("0", 8,log));
	// Master table
	    sec1.append(BufrUtility.integerToBinary("25", 8,log));
	// version of local table
	    sec1.append(BufrUtility.integerToBinary("0", 8,log));
	    // GMT date
	    String date = getCurrentdate();
	//  year of century
	    sec1.append(BufrUtility.integerToBinary(date.substring(0, 4), 16,log));
	    sec1.append(BufrUtility.integerToBinary(date.substring(4, 6), 8,log)); //month
	    sec1.append(BufrUtility.integerToBinary(date.substring(6, 8), 8,log)); //day
	    sec1.append(BufrUtility.integerToBinary(date.substring(8, 10), 8,log)); //hour
	    sec1.append(BufrUtility.integerToBinary(date.substring(10, 12), 8,log)); //min
	    sec1.append(BufrUtility.integerToBinary(date.substring(12, 14), 8,log)); //sec
	    sec1.append(BufrUtility.integerToBinary("0", 16,log)); //local use
	    log.info(
	       "Complete encoded Setion 1 and total bits = " + sec1.length() + " bits");
	    return sec1;
	}
	/**
	 * @return the current gmt date and time in format of yyMMddHHmm
	 */
	public String getCurrentdate() {
	    String curentdate = new String("");
	    Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	    int month = cal.get(Calendar.MONTH) + 1;
	    int day = cal.get(Calendar.DAY_OF_MONTH);
	    int hour = cal.get(Calendar.HOUR_OF_DAY);
	    int min = cal.get(Calendar.MINUTE);
	    int sec = cal.get(Calendar.SECOND);
	    NumberFormat formatter = new DecimalFormat("00");
	    SimpleDateFormat yrformatter = new SimpleDateFormat("yyyy");
	    Date date = new Date();
	    curentdate =
	        yrformatter.format(date)
	            + formatter.format(month)
	            + formatter.format(day)
	            + formatter.format(hour)
	            + formatter.format(min)
	            + formatter.format(sec);
	    return curentdate;
	}
	public int decodeSection1(byte[]buffer, int bytePosition){
		data = new ArrayList<BufrDescriptorDto>();
		log.info("decode section 1 at:" + bytePosition);
		int pos = bytePosition;
		setMsgLength(BufrUtility.getIntValue(buffer, pos, 3));
		pos = pos + 3;
		setBufrMasterTable(BufrUtility.getIntValue(buffer, pos, 1));
		pos = pos + 1;
		setOriginator(BufrUtility.getIntValue(buffer, pos, 2));
		pos = pos +2;
		setSubOriginator(BufrUtility.getIntValue(buffer, pos,2));
		pos = pos + 2;
		setSeqNo(BufrUtility.getIntValue(buffer, pos, 1));
		pos = pos + 1;
		String optional = Integer.toBinaryString(BufrUtility.getIntValue(buffer, pos, 1));
		pos = pos + 1;
		if (optional.substring(0, 1).equals("1")){
			setOptionalSection(true);
		} else {
			setOptionalSection(false);
		}
		setDataCategory(BufrUtility.getIntValue(buffer, pos, 1));
		pos = pos +1;
		setDataSubCategory(BufrUtility.getIntValue(buffer, pos, 1));
		pos = pos + 1;
		setLocalSubCategory(BufrUtility.getIntValue(buffer, pos, 1));
		pos = pos + 1;
		setBufrTableVersionNo(BufrUtility.getIntValue(buffer, pos, 1));
		pos = pos + 1;
		setBufrLocalTableVersion(BufrUtility.getIntValue(buffer, pos, 1));
		pos = pos + 1;		
		int year = BufrUtility.getIntValue(buffer, pos, 2);		
		pos = pos + 2;
		int month = BufrUtility.getIntValue(buffer, pos, 1);
		pos = pos + 1;
		int day = BufrUtility.getIntValue(buffer, pos, 1);
		pos = pos + 1;
		int hour = BufrUtility.getIntValue(buffer, pos, 1);
		pos = pos + 1;
		int minute = BufrUtility.getIntValue(buffer, pos, 1);
		pos = pos +1;
		int second = BufrUtility.getIntValue(buffer, pos, 1);
		dateTime = BufrUtility.customFormat("0000", year)
				+ BufrUtility.customFormat("00", month)
				+ BufrUtility.customFormat("00", day)
				+ BufrUtility.customFormat("00", hour)
				+ BufrUtility.customFormat("00", minute)
				+ BufrUtility.customFormat("00", second);
		setDateTime(dateTime);
		pos = pos + 1;
		setReserved(BufrUtility.getIntValue(buffer, pos, 1));
		pos = pos + 2;
		log.info("End decode section 1 at byte position " + pos);
		if (pos != (bytePosition +msgLength)){
			pos = bytePosition + msgLength ;
		}
		return pos;
	}
		

}

/*
 * Created on 13-Jul-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package dfo.isdm.BufrUtility;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;



/** This class prepare the information for Section 0
 * @author Tran
 * Created on 13-Jul-06
  */
public class Section0 {
	/**
	 * total length of BUFR message
	 */
	private int totallength;
	/**
	 * BUFR edition number
	 */
	private int edition  = 4;
	/**
	 * Binary of section 0 string
	 */
	private StringBuffer section0 = new StringBuffer("");
	private static Logger log;
    private static List<BufrDescriptorDto> data = new ArrayList<BufrDescriptorDto>();

	/**
	 * Constructor 
	 */
	public Section0(Logger _log) {
		log = _log;
	}
	/**
	 * Default constructor for SetSection0 class
	 * @param totaloctet length of the BUFR message
	 * @param editionnumber BUFR edition number
	 */
	public Section0(int totaloctet, int editionnumber) {
	    totallength = totaloctet;
	    edition = editionnumber;
	}
	/**
	 * Constructor for SetSection0
	 * @param totaloctet lenth of BUFR message, octet 5-7
	 */
	public Section0(int totaloctet) {
	    totallength = totaloctet;
	}
	public int getTotallength() {
		return totallength;
	}
	public void setTotallength(int totallength) {
		this.totallength = totallength;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.SEC0_LENGTH");
		dto.setDecodedValue(Integer.toString(totallength));
		data.add(dto);
		log.info("Section 0 length = " +dto.getDecodedValue());
	}
	public int getEdition() {
		return edition;
	}
	public void setEdition(int edition) {
		this.edition = edition;
		BufrDescriptorDto dto = new BufrDescriptorDto();
		dto.setMeds_pcode("PARM.BUFR_EDITION");
		dto.setDecodedValue(Integer.toString(edition));
		log.info("Section 0 Bufr edition= " + dto.getDecodedValue());
		data.add(dto);
	}
	public void setSection0(StringBuffer section0) {
		this.section0 = section0;
	}
	/**
	 * @return binary string of Section 0
	 */
	public StringBuffer getSection0() {
	    section0 = encodeSection0();
	    return section0;
	}
	public List<BufrDescriptorDto> getDecodedData() {
		return data;
	}	
	
	/** this method encode setion 0 into a binary string
	 * @return StringBuffer
	 */
	private StringBuffer encodeSection0() {
	    StringBuffer sbf = new StringBuffer("");
	    sbf.append(BufrUtility.putChar("BUFR", 32));
	    sbf.append(
	        BufrUtility.integerToBinary(Integer.toString(totallength), 24,log));
	    sbf.append(BufrUtility.integerToBinary(Integer.toString(edition), 8,log));
	    return sbf;
	}
	public int decodeSection0(byte[]buffer, int bytePointer){
		data = new ArrayList<BufrDescriptorDto>();
		String s = new String();
		for (int i = bytePointer; i < bytePointer + 4; i ++){
			s = s + (char) buffer[i];
		}
		bytePointer = bytePointer + 4;
		totallength = BufrUtility.getIntValue(buffer, bytePointer, 3);
		setTotallength(totallength);
		bytePointer = bytePointer + 3;
		edition = BufrUtility.getIntValue(buffer, bytePointer, 1);
		setEdition(edition);
		bytePointer = bytePointer + 1;
		log.info("Completed decode section0 of edition " + edition + "with total length= " + totallength);
		return bytePointer;
	}
}

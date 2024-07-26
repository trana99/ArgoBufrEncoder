/*
 * Created on 5-Jul-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package dfo.isdm.WriteArgoBufr;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.*;

import dfo.isdm.BufrUtility.BufrUtility;
import dfo.isdm.BufrUtility.Sec3NprofMapDTO;



/**
 * the SetBufrSection3 class contain all of the information for Section 3
 * @author Anh Tran 25-Jul-06
 * Jan 2022, Tran modified the sec3Sequences from the list of String to the list of Sec3NprofMapDTO so that it's consistent with ArgoBufrSection4 class
 */
public class SetBufrSection3  {
    private static Logger log = LogManager.getLogger(WriteArgoBufr.class);
    private static List<Sec3NprofMapDTO> sec3Sequences = new ArrayList <Sec3NprofMapDTO>();
    private static StringBuffer sec3;
    private static int noDataSet;

/**
 * constructor for ArgoBufrSection3()
 */
public SetBufrSection3( int _noDataSet, List<Sec3NprofMapDTO> _descriptor) {
	noDataSet = _noDataSet;
	sec3Sequences = _descriptor;
	sec3 = new StringBuffer();
	encode();
	
}
public  StringBuffer getSec3() {
	return sec3;
}
/**
 * @param bufrtable Table contained BUFR format of the message
 * @param noDataSet number of DataSset
 * @return StringBuffer of Section 3
 */
private void encode() {
//  octet 5-6
	log.info("no dataset=" + noDataSet +"=" + BufrUtility.integerToBinary(Integer.toString(noDataSet), 16, log));
    sec3.append(BufrUtility.integerToBinary(Integer.toString(noDataSet), 16, log));
    sec3.append(BufrUtility.integerToBinary("1", 1, log)); //  for observed data
    sec3.append(BufrUtility.integerToBinary("0", 1, log)); // for non compressed data
    sec3.append(BufrUtility.integerToBinary("0", 6, log)); //  for reserved
    for (int i = 0; i < sec3Sequences.size(); i++) {
    		sec3.append(setFXY(sec3Sequences.get(i).getDescriptorId().toString()));
            log.info("desc for section 3 = "  + sec3Sequences.get(i).getDescriptorId().toString() + "="
                    + setFXY(sec3Sequences.get(i).getDescriptorId().toString()));
        
    }
//  complete sec 3 with even octet number and encode octet 1 to 4
    StringBuffer totalsec3 = new StringBuffer("");
    totalsec3.append(BufrUtility.completeSection(sec3,log));
    sec3 = new StringBuffer();
    sec3 = totalsec3;
    log.info(totalsec3);
    log.info(
        "Complete encoded Section 3 of Bufr Message and total length = "
            + totalsec3.length()/8
            + " octets");
    
}
/**
 * @param desc descriptor consisted of 3 part F X and Y
 * @return StringBuffer of binary
 */
private StringBuffer setFXY(String desc) {
    StringBuffer fxy = new StringBuffer("");
    fxy.append(BufrUtility.integerToBinary(desc.substring(0, 1), 2, log));
    fxy.append(BufrUtility.integerToBinary(desc.substring(1, 3), 6, log));
    fxy.append(BufrUtility.integerToBinary(desc.substring(3, 6), 8, log));
    return fxy;
}
// end of ArgoBufrSection 3
}

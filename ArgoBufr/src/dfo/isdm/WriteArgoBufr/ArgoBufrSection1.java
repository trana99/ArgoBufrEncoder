/*
 * Created on 11-Jul-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package dfo.isdm.WriteArgoBufr;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.logging.log4j.*;

import dfo.isdm.BufrUtility.BufrUtility;


/**
 * @author Tran
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ArgoBufrSection1 {
    private static Logger log = LogManager.getLogger(WriteArgoBufr.class);

/**
 * constructor class for encode section 1
 */
public ArgoBufrSection1() {
    }
/**
 * @return StringBuffer of section1
 */
public StringBuffer encodeSection1() {
    StringBuffer sec1 = new StringBuffer("");
    sec1.append(BufrUtility.integerToBinary("24", 24, log)); // length of section
    sec1.append(BufrUtility.integerToBinary("0", 8, log)); // bufr master table
//  need to get id for data center. info gets from table C-11
    sec1.append(BufrUtility.integerToBinary("174", 16, log));
// sub-center Id table c-12
    sec1.append(BufrUtility.integerToBinary("0", 16, log));
    sec1.append(BufrUtility.integerToBinary("0", 8, log)); //update sequence number
// bit flag
    sec1.append(BufrUtility.integerToBinary("0", 1, log)); // 0 -no option section
//  set to zero to reserved
    sec1.append(BufrUtility.integerToBinary("0", 7, log));
//Data category, 31: oceanographic
    sec1.append(BufrUtility.integerToBinary("31", 8, log));
//International data sub-categeroy 4:float
    sec1.append(BufrUtility.integerToBinary("4", 8, log));
// local data sub-category
    sec1.append(BufrUtility.integerToBinary("0", 8, log));
// Master table
    sec1.append(BufrUtility.integerToBinary("25", 8, log));
// version of local table
    sec1.append(BufrUtility.integerToBinary("0", 8, log));
    // GMT date
    String date = getCurrentdate();
//  year of century
    sec1.append(BufrUtility.integerToBinary(date.substring(0, 4), 16, log));
    sec1.append(BufrUtility.integerToBinary(date.substring(4, 6), 8, log)); //month
    sec1.append(BufrUtility.integerToBinary(date.substring(6, 8), 8, log)); //day
    sec1.append(BufrUtility.integerToBinary(date.substring(8, 10), 8, log)); //hour
    sec1.append(BufrUtility.integerToBinary(date.substring(10, 12), 8, log)); //min
    sec1.append(BufrUtility.integerToBinary(date.substring(12, 14), 8, log)); //sec
    sec1.append(BufrUtility.integerToBinary("0", 16, log)); //local use
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
}

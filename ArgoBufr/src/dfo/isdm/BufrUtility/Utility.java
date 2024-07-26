/*
 * Created on 30-Jan-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package dfo.isdm.BufrUtility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.logging.log4j.*;
import org.jfree.util.Log;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.ncml.NcMLReader;


/**
 * @author Tran
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public final class Utility {
/**
 * @param julian julian date in double
 * @param referencedate the reference date that use to calculate julian day
 * @return calendar date in formate YYYYMMDDHHMMss
 */

public static String convertJulianToDate (double julian, String referencedate) {
        String obsvdate = new String();
        String observationDate = new String();
        String refdate = new String (referencedate);
        int syear = Integer.parseInt(refdate.substring(0, 4));
        int smonth = Integer.parseInt(refdate.substring(4, 6));
        int sday = Integer.parseInt(refdate.substring(6, 8));
        int shr = Integer.parseInt(refdate.substring(8, 10));
        int smin = Integer.parseInt(refdate.substring(10, 12));
   //     int ssecond = Integer.parseInt(refdate.substring(12, 14));
        double reftthour =
            gtcnv(syear, smonth, sday, shr) + smin / 60.0 + smin / 3600;
        double julianhr = (julian + 5.0 * 1.0E-9) * 24 + reftthour;
        obsvdate = tgcnv((int) julianhr);
        double julianhr1 =
            gtcnv(
                Integer.parseInt(obsvdate.substring(0, 4)),
                Integer.parseInt(obsvdate.substring(4, 6)),
                Integer.parseInt(obsvdate.substring(6, 8)),
                Integer.parseInt(obsvdate.substring(8, 10)));
        double imin = julian * 24 * 60.0 - ((julianhr1 - reftthour) * 60.0);
        double isec = (imin - (int)imin)*60.0;
        NumberFormat formater = new DecimalFormat("00");
        if (Integer.parseInt(formater.format(isec))/60.0 == 1){
            imin = imin + 1;
            isec = 0;
           // isec = isec - (Integer.parseInt(formater.format(isec))/60.0)*60.0;
        }        
        observationDate = obsvdate + formater.format((int)imin);
        observationDate =observationDate.concat(formater.format(isec));
        return observationDate;
    }
/**
 * @param referencedate reference date in YYYYMMDDHHmm or YYYYMMDDHHmmss
 * @param obsdate observation date in YYYYMMDDHHmm
 * @return double julian day
 */
public static double calculateJulianDay(
            String referencedate,
            String obsdate) {
            double caljul;
            int isecond = 0;
          Calendar refdate = new GregorianCalendar(
                              TimeZone.getTimeZone("GMT"));
          Calendar inputdate = new GregorianCalendar(
                              TimeZone.getTimeZone("GMT"));
            int refyr = Integer.parseInt(referencedate.substring(0, 4));
            int refmn = Integer.parseInt(referencedate.substring(4, 6))-1;
            int refdy = Integer.parseInt(referencedate.substring(6, 8));
            int refhh = Integer.parseInt(referencedate.substring(8, 10));
            int inyr = Integer.parseInt(obsdate.substring(0, 4));
            int inmn = Integer.parseInt(obsdate.substring(4, 6))-1;
            int indy = Integer.parseInt(obsdate.substring(6, 8));
            int inhh = Integer.parseInt(obsdate.substring(8, 10));
            int inmin = Integer.parseInt(obsdate.substring(10, 12));
            if (obsdate.length()>12){
                isecond = Integer.parseInt(obsdate.substring(12,14));                
            }
            
            inputdate.set(inyr, inmn, indy, inhh, inmin, isecond);
            refdate.set(refyr, refmn, refdy, refhh, 0, 0);
            caljul =  
                ((inputdate.getTimeInMillis()  - refdate.getTimeInMillis())
                    /(24.0 * 60.0 * 60.0 * 1000.0)) ;
            return caljul;
}
/**
     * @param year4 in format YYYY
     * @param mm in format MM
     * @param dd in format DD
     * @param hh in format HH
     * @return theulian hour
     */
    public static double gtcnv(int year4, int mm, int dd, int hh) {
    int thhour;
    int k, m, y, yy;
    int nday;
    if (year4 >= 1900) {
        yy = year4 - 1900;
    } else {
        System.out.println(
            "Error.  using year less than 1900, 2 digit. stop processing");
        thhour = -1;
        return thhour;
    }
    nday = dd;
    if (mm > 2) {
        m = mm - 3;
        y = yy;
        k = (1461 * y / 4) + (153 * m + 2) / 5 + nday;
        thhour = (k - 1) * 24 + hh;
        return thhour;
    }
    m = mm + 9;
    y = yy - 1;
    if (mm != 2 || dd != 29) {
        k = (1461 * y / 4) + (153 * m + 2) / 5 + nday;
        thhour = (k - 1) * 24 + hh;
        return thhour;
    }
    if ((yy / 4) * 4 != yy) {
        nday = 28;
    }
    k = (1461 * y / 4) + (153 * m + 2) / 5 + nday;
    thhour = (k - 1) * 24 + hh;
    return thhour;
    }
/**
 * @param thhour theulian hour
 * @return string Observation Date in format of YYYYMMDDHH
 * This routin convert theulian hour to Gregorian date
 * Thhour = 0 for Mar 01, 1900, 00:00 Hours
 * Thhour = 875159 for Dec 31, 1999, 23:00 hours
 * Method dependable for dates mar 01, 1900 to Feb 28, 2100 *
 */
    public static String tgcnv(int thhour) {
    String obsdate = new String();
    int k = thhour / 24 + 1;
    int hh = thhour - (k - 1) * 24;
    int yy = (4 * k - 1) / 1461;
    int dd = 4 * k - 1 - 1461 * yy;
    dd = (dd + 4) / 4;
    int mm = (5 * dd - 3) / 153;
    dd = 5 * dd - 3 - 153 * mm;
    dd = (dd + 5) / 5;
    NumberFormat formater = new DecimalFormat("00");
    if (mm < 10) {
        mm = mm + 3;
        yy = yy + 1900;
        obsdate =
            Integer.toString(yy)
                + formater.format(mm)
                + formater.format(dd)
                + formater.format(hh);
        return obsdate;
    }
    mm = mm - 9;
    yy = yy + 1;
    yy = yy + 1900;
    obsdate =
        Integer.toString(yy)
            + formater.format(mm)
            + formater.format(dd)
            + formater.format(hh);
    return obsdate;
    }
/**
 * @return String current date and time in fmt YYYYMMDDHHMISS
 */
public static String getCurrentDateTime() {
    String currentDT = "";
    Calendar cal = new GregorianCalendar();
    int hour24 = cal.get(Calendar.HOUR_OF_DAY);
    int min = cal.get(Calendar.MINUTE);
    int sec = cal.get(Calendar.SECOND);
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;
    int day = cal.get(Calendar.DAY_OF_MONTH);
    NumberFormat formater = new DecimalFormat("00");
    currentDT =
        Integer.toString(year)
            + formater.format(month)
            + formater.format(day)
            + formater.format(hour24)
            + formater.format(min)
            + formater.format(sec);
    return currentDT;
}
public static String getUTCDateTime(){
    DateTime dt = new DateTime().withZone(DateTimeZone.UTC);
    DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYYMMddHHmmss");
    String dt_s = fmt.print(dt);
    return dt_s;
        
}
/**
  * @param ncprof netcdf profile
  * @param newnc output netcdf file
  * @param name parameter name
  * @param actioncopy true if the array need to copy
  * @return ArrayChar.D1
  */
public static ArrayChar.D1 copyCharD1Array(
    NetcdfFile ncprof, NetcdfFileWriter newnc,
    String name,
    boolean actioncopy , Logger log) {
    ArrayChar.D1 varnameArray = null;
    ArrayChar.D1 tempArray = null;
    try {
        Variable varname = ncprof.findVariable(name);
        Variable newvarname = newnc.findVariable(name);
        Array varnameValue =  varname.read();
        assert (varnameValue instanceof ArrayChar.D1);
        tempArray = (ArrayChar.D1) varnameValue;
        if (actioncopy) {
            Array newvarnameValue = newvarname.read();
            varnameArray = (ArrayChar.D1) newvarnameValue;
            int []shape = varnameArray.getShape();
         for (int i = 0; i < shape[0]; i++) {        	 
             if (i < tempArray.getShape()[0]) {
            	 varnameArray.set(i, tempArray.get(i));             
             }
            }

        } else {
            varnameArray = tempArray;
        }
        newnc.write(newvarname, varnameArray);

    } catch (IOException e) {
        log.error("Variable can't be found = " + name + '\n' + e);
              
    } catch (InvalidRangeException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return varnameArray;
}
public static boolean checkFloatFillArrayValue(NetcdfFile ncprof, String ncVarName) {
    boolean findAdjust = false;
    Variable ncvar = ncprof.findVariable(ncVarName);
    if (ncvar ==null){
    	Log.error("Can't find variable name:  " + ncVarName);
    	return findAdjust;
    } else {
    	Attribute fillval = ncvar.findAttribute("_FillValue");
    	ArrayFloat.D2 profileArray;
		try {
			profileArray = (ArrayFloat.D2) ncvar.read();
	        int []shape = profileArray.getShape();
	        for (int i = 0; i < shape[0]; i++){
	            for (int j = 0; j < shape[1]; j++){
	                if (profileArray.get(i, j)!= fillval.getNumericValue().floatValue()){
	                    findAdjust = true;
	                    return findAdjust;
	                }
	            }
	        }

		} catch (IOException e) {
			Log.error("Exception in reading " + ncVarName +" "+ e.getMessage());
			
		}

    }
    return findAdjust;
}

/**
 * @param ncprof netcdf profile
 * @param newnc output netcdf file
 * @param name parameter name
 * @param actioncopy true if the array need to copy
 * @return ArrayChar.D2
 */
public static ArrayChar.D2 copyCharD2Array(
    NetcdfFile ncprof, NetcdfFileWriter newnc,
    String name,
    boolean actioncopy, Logger log) {
       ArrayChar.D2 varnameArray = null;
       ArrayChar.D2 tempArray = null;
       try {
           Variable varname = ncprof.findVariable(name);
           Variable newvarname = newnc.findVariable(name);
           Array varnameValue =  varname.read();
           assert (varnameValue instanceof ArrayChar.D2);
           tempArray = (ArrayChar.D2) varnameValue;
           if (actioncopy) {               
               Array newvarnameValue = newvarname.read();
               varnameArray = (ArrayChar.D2) newvarnameValue;
               int []shape = varnameArray.getShape();
            for (int i = 0; i < shape[0]; i++) {
            	if(i < tempArray.getShape()[0]){
            		String s = tempArray.getString(i);
                	if (s.trim().length()< shape[1]){
                		s = addBlankstoEnd(tempArray.getString(i).trim(), shape[1]-s.trim().length());
                	}
                	varnameArray.setString(i, s);
            	}
               }
           } else {
               varnameArray = tempArray;
           }
           newnc.write(newvarname, varnameArray);
       } catch (IOException e) {
           log.error("Variable can't be found = " + name + '\n' +e);           
       } catch (InvalidRangeException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       return varnameArray;
}

/**
 * @param ncprof netcdf profile
 * @param newnc output netcdf file
 * @param name parameter name
 * @param actioncopy true if the array need to copy
 * @return ArrayChar.D3
 */
public static ArrayChar.D3 copyCharD3Array(
    NetcdfFile ncprof, NetcdfFileWriter newnc,
    String name,
    boolean actioncopy, Logger log) {
          ArrayChar.D3 varnameArray = null;
          ArrayChar.D3 tempArray = null;
          try {
              Variable varname = ncprof.findVariable(name);
              Variable newvarname = newnc.findVariable(name);
              Array varnameValue =  varname.read();
              assert (varnameValue instanceof ArrayChar.D3);
              tempArray = (ArrayChar.D3) varnameValue;
              if (actioncopy) {
                  Array newvarnameValue = newvarname.read();
                  varnameArray = (ArrayChar.D3) newvarnameValue;
                  int []shape = newvarnameValue.getShape();
                  for (int i = 0; i < shape[0]; i++) {
                      for (int j = 0; j < shape[1]; j++) {
                    	  StringBuffer s = new StringBuffer();                      	
                        for (int k = 0; k < shape[2]; k++) {
                            if (i < tempArray.getShape()[0]
                                && j < tempArray.getShape()[1]
                                && k < tempArray.getShape()[2]) {
                            	s.append(tempArray.get(i, j, k));
                            }
                          }
                        String data = new String();
                        data = s.toString();
                        if (s.toString().trim().length() < shape[2]){
                        	data = addBlankstoEnd(s.toString().trim(), shape[2]-s.toString().trim().length());
                        }
            	        Index ima = varnameArray.getIndex();
            	        varnameArray.setString(ima.set(i, j), data);
                      }
                      
                  }
              } else {
                  varnameArray = tempArray;
              }
              newnc.write(newvarname, varnameArray);
          } catch (IOException e) {
              log.error("Variable can't be found = " + name + '\n' + e);
              
          } catch (InvalidRangeException e) {
        	  log.error("Error in writting " + name + '\n' + e);			
		} 
          return varnameArray;
      }
/**
 * @param ncprof netcdf profile
 * @param newnc output netcdf file
 * @param name parameter name
 * @param actioncopy true if the array need to copy
 * @return ArrayChar.D4
 */
public static ArrayChar.D4 copyCharD4Array(
    NetcdfFile ncprof, NetcdfFileWriter newnc,
    String name,
    boolean actioncopy, Logger log) {
          ArrayChar.D4 varnameArray = null;
          ArrayChar.D4 tempArray = null;
        try {
            Variable varname = ncprof.findVariable(name);
            Variable newvarname = newnc.findVariable(name);

            Array varnameValue =  varname.read();
            assert (varnameValue instanceof ArrayChar.D4);
            tempArray = (ArrayChar.D4) varnameValue;
            if (actioncopy) {
                Array newvarnameValue = newvarname.read();
                varnameArray = (ArrayChar.D4) newvarnameValue;
                int []shape = newvarnameValue.getShape();
                for (int i = 0; i < shape[0]; i++) {
                    for (int j = 0; j < shape[1]; j++) {
                      for (int k = 0; k < shape[2]; k++) {
                    	  StringBuffer s = new StringBuffer();
                        for (int l = 0; l < shape[3]; l++) {
                            if (i < tempArray.getShape()[0]
                                && j < tempArray.getShape()[1]
                                && k < tempArray.getShape()[2]
                                && l < tempArray.getShape()[3]) {
                            	s.append(tempArray.get(i, j, k, l));
                                }
                        }
                        String data = new String();
                        if (s.toString().trim().length() < shape[3]){
                        	data = addBlankstoEnd(s.toString().trim(), shape[3]- s.toString().trim().length());
                        }else {
                        	data = s.toString();
                        }
                        Index ima = newvarnameValue.getIndex();
                        ima.set(i, j, k);
                        varnameArray.setString(ima, data);
                        }
                    }
                }
            } else {
                varnameArray = tempArray;
            }
            newnc.write(newvarname, varnameArray);
        } catch (IOException e) {
            log.error("Variable can't be found = " + name +'\n' + e);
            
        } catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          return varnameArray;
}

/**
 * @param ncprof netcdf profile
 * @param newnc output netcdf file
 * @param name parameter name
 * @param actioncopy true if the array need to copy
 * @return ArrayInt.D1
 */
public static ArrayInt.D1 copyIntD1Array(
    NetcdfFile oldNc, NetcdfFileWriter newnc,
    String name,
    boolean actioncopy, Logger log) {
      ArrayInt.D1 varnameArray = null;
      ArrayInt.D1 oldArray = null;
      try {
          Variable oldVar = oldNc.findVariable(name);
          Array varnameValue =  oldVar.read();
          assert (varnameValue instanceof ArrayInt.D1);
          oldArray = (ArrayInt.D1) varnameValue;
          Variable newVar = newnc.findVariable(name);
          varnameArray = new ArrayInt.D1(newVar.getShape()[0]);
          if (actioncopy && oldArray.getShape()[0] > 0) {
              int []shape = varnameArray.getShape();
             for (int i = 0; i < shape[0]; i++) {
                if (i < oldArray.getShape()[0]) {
                     int value = oldArray.get(i);
                     varnameArray.set(i, value);
                }
             }
          }
          newnc.write(newVar, varnameArray);
         } catch (IOException e) {
          log.error("Variable can't be found = " + name + '\n' +e);          
         } catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     return varnameArray;
  }

/**
 * @param ncprof netcdf profile
 * @param newnc output netcdf file
 * @param name parameter name
 * @param actioncopy true if the array need to copy
 * @return ArrayInt.D2
 */
public static ArrayInt.D2 copyIntD2Array(
    NetcdfFile ncprof, NetcdfFileWriter newnc,
    String name,
    boolean actioncopy, Logger log) {
     ArrayInt.D2 varnameArray = null;
     ArrayInt.D2 tempArray = null;
     try {
         Variable varname = ncprof.findVariable(name);
         Array varnameValue =  varname.read();
        assert (varnameValue instanceof ArrayInt.D2);
         tempArray = (ArrayInt.D2) varnameValue;
         Variable newvarname = newnc.findVariable(name);             

        if (actioncopy) {
             int []shape = newvarname.getShape();
             varnameArray = new ArrayInt.D2(shape[0], shape[1]);
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    if (i < tempArray.getShape()[0]
                        && j < tempArray.getShape()[1]) {
                    int value = tempArray.get(i, j);
                    varnameArray.set(i, j, value);
                    }
                }
            }
         } else {
             varnameArray = tempArray;
         }
        newnc.write(newvarname, varnameArray);
        } catch (IOException e) {
         log.error("Variable can't be found = " + name + '\n' +e);
         
        } catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    return varnameArray;
 }
/**
 * @param ncprof netcdf profile
 * @param newnc output netcdf file
 * @param name parameter name
 * @param actioncopy true if the array need to copy
 * @return ArrayInt.D3
 */
public static ArrayInt.D3 copyIntD3Array(
    NetcdfFile ncprof, NetcdfFileWriter newnc,
    String name,
    boolean actioncopy, Logger log) {
     ArrayInt.D3 varnameArray = null;
     ArrayInt.D3 tempArray = null;
     try {
         Variable varname = ncprof.findVariable(name);
         Array varnameValue =  varname.read();
         assert (varnameValue instanceof ArrayInt.D3);
         tempArray = (ArrayInt.D3) varnameValue;
         Variable newvarname = newnc.findVariable(name);

         if (actioncopy) {
             Array newvarValue = newvarname.read();
             varnameArray = (ArrayInt.D3) newvarValue;
             int []shape = varnameArray.getShape();
             for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        if (i < tempArray.getShape()[0]
                            && j < tempArray.getShape()[1]
                            && k < tempArray.getShape()[2]) {
                                int value = tempArray.get(i, j, k);
                                varnameArray.set(i, j, k, value);
                            }
                    }
                }
            }
         } else {
             varnameArray = tempArray;
         }
         newnc.write(newvarname, varnameArray);
        } catch (IOException e) {
         log.error("Variable can't be found = " + name + '\n' +e);
         
        } catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    return varnameArray;
 }
/**
 * @param ncprof input netcdf profile
 * @param newnc output netcdf profile
 * @param name variable name
 * @param actioncopy true for copy data array
 * @return ArrayFloat.D1
 */
public static ArrayFloat.D1 copyFloatD1Array (
      NetcdfFile ncprof, NetcdfFileWriter newnc,
      String name, boolean actioncopy , Logger log) {
        ArrayFloat.D1 varnameArray = null;
        ArrayFloat.D1 oldArray = null;
        try {
             Variable oldVar = ncprof.findVariable(name);
             Attribute paramfill = oldVar.findAttribute("_FillValue");
             Array oldVarValue =  oldVar.read();
             assert (oldVarValue instanceof ArrayFloat.D1);
             oldArray = (ArrayFloat.D1) oldVarValue;
                 Variable newVar = newnc.findVariable(name);
                 varnameArray = new ArrayFloat.D1(newVar.getShape()[0]);                 
                 int []shape = varnameArray.getShape();
                 int ithIndex = shape[0];
                 if (oldArray.getShape()[0] < ithIndex){
                	 ithIndex = oldArray.getShape()[0];
                 }
                 if (actioncopy && oldArray.getShape()[0] > 0){
                	 for (int i = 0; i < ithIndex; i++) {
                        float value = oldArray.get(i);
                        varnameArray.set(i, value);
                	 }
                  }else {
                	  for (int i = 0; i < shape[0]; i++) {                		  
                            float value = paramfill.getNumericValue().floatValue();
                            varnameArray.set(i, value);
                        }
                    }
                newnc.write(newVar, varnameArray);
           	} catch (IOException e) {
             log.error("Variable can't be found = " + name + '\n' +e);
             
            } catch (InvalidRangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       return  varnameArray;
    }

/**
 * @param ncprof input netcdf profile
 * @param newnc output netcdf profile
 * @param name variable name
 * @param actioncopy true for copy data array
 * @return ArrayFloat.D2
 */
public static ArrayFloat.D2 copyFloatD2Array(
      NetcdfFile ncprof, NetcdfFileWriter newnc,
      String name, boolean actioncopy, Logger log) {
        ArrayFloat.D2 varNameArray = null;
        ArrayFloat.D2 oldArray = null;
        try {
             Variable oldVar = ncprof.findVariable(name);
             Array oldVarnameValue =  oldVar.read();
            assert (oldVarnameValue instanceof ArrayFloat.D2);
             oldArray = (ArrayFloat.D2) oldVarnameValue;
             Variable newVar = newnc.findVariable(name);
             varNameArray = new ArrayFloat.D2(newVar.getShape()[0], newVar.getShape()[1]);
             Attribute fillValue = newVar.findAttributeIgnoreCase("_FillValue");
             int []shape = varNameArray.getShape();
             int ithIndex = shape[0];
             int jthIndex = shape[1];
             if(oldArray.getShape()[0] < ithIndex 
            		 || oldArray.getShape()[1] < jthIndex) {
         		ithIndex = oldArray.getShape()[0];
         		jthIndex = oldArray.getShape()[1];         		
         	}
            if (actioncopy && oldArray.getShape()[0]>0) {            	
                for (int i = 0; i < ithIndex; i++) {
                    for (int j = 0; j < jthIndex; j++) {
                            float value = oldArray.get(i, j);
                            varNameArray.set(i, j, value);                        
                    }
                }
             } else {
             	for (int i = 0; i < shape[0]; i++){
             		for (int j = 0; j < shape[1]; j++){
             			varNameArray.set(i, j, fillValue.getNumericValue().floatValue());
             		}
             	}
             }
            newnc.write(newVar, varNameArray);
            } catch (IOException e) {
             log.error("Variable can't be found = " + name + '\n' + e);
             
            } catch (InvalidRangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        return varNameArray;
     }/**
 * @param ncprof input netcdf profile
 * @param newnc output netcdf profile
 * @param name variable name
 * @param actioncopy true for copy data array
 * @return ArrayFloat.D3
 */
public static ArrayFloat.D3 copyFloatD3Array(
          NetcdfFile ncprof, NetcdfFileWriter newnc,
          String name, boolean actioncopy, Logger log) {
            ArrayFloat.D3 varnameArray = null;
            ArrayFloat.D3 tempArray = null;
            try {
                 Variable varname = ncprof.findVariable(name);
                 Array varnameValue =  varname.read();
                 assert (varnameValue instanceof ArrayFloat.D3);
                 tempArray = (ArrayFloat.D3) varnameValue;
                 Variable newvarname = newnc.findVariable(name);

                 if (actioncopy) {
                     Array newvarValue = newvarname.read();
                     varnameArray = (ArrayFloat.D3) newvarValue;
                     int []shape = varnameArray.getShape();
                     for (int i = 0; i < shape[0]; i++) {
                        for (int j = 0; j < shape[1]; j++) {
                            for (int k = 0; k < shape[2]; k++) {
                                if (i < tempArray.getShape()[0]
                                    && j < tempArray.getShape()[1]
                                    && k < tempArray.getShape()[2]) {
                                float value = tempArray.get(i, j, k);
                                varnameArray.set(i, j, k, value);
                                    }
                            }
                        }
                    }
                 } else {
                     varnameArray = tempArray;
                 }
                 newnc.write(newvarname, varnameArray);
                } catch (IOException e) {
                 log.error("Variable can't be found = " + name + '\n' + e);
                 
                } catch (InvalidRangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
           return  varnameArray;
        }
/**
 * 
 * @param ncfile
 * @param varname
 * @param input
 * @param log
 */
public static void writeDouble(NetcdfFileWriter ncfile,
	    String varname,
	    double input, Logger log){
		ArrayDouble.D0 ncvarValueArray=null;
	        try {
	            Variable ncvar = ncfile.findVariable(varname);
	            Array ncvarValue = ncvar.read();
	            ncvarValueArray = (ArrayDouble.D0)ncvarValue;
	            ncvarValueArray.setDouble(0, input);
	           ncfile.write(ncvar, ncvarValueArray);
	        } catch (IOException e) {
	        	log.error("Error in write " + varname + '\n' + e);;
	        } catch (InvalidRangeException e) {
	            log.error("Invalid range " + '\n' + e);
	            
	        }
	   
	}

/**
  * @param ncprof input netcdf profile
  * @param newnc output netcdf profile
  * @param name variable name
  * @param actioncopy true for copy data array
 * @return ArrayDouble.D1
 */
public static ArrayDouble.D1 copyDoubleD1Array(
    NetcdfFile ncprof, NetcdfFileWriter newnc,
    String name,
    boolean actioncopy, Logger log) {
       ArrayDouble.D1 varnameArray = null;
       ArrayDouble.D1 tempArray = null;
       try {
           Variable varname = ncprof.findVariable(name);
           Array varnameValue =  varname.read();
           tempArray = (ArrayDouble.D1) varnameValue;
           Variable newvarname = newnc.findVariable(name);
           //Array newvarValue = newvarname.read();
           varnameArray = new ArrayDouble.D1 (newvarname.getShape()[0]);
           int []shape = varnameArray.getShape();
           double fillValue = varname.findAttribute("_FillValue").getNumericValue().doubleValue();
           if (actioncopy) {
              for (int i = 0; i < shape[0]; i++) {
                  if (i < varname.getShape()[0]) {
                  double value = tempArray.get(i);
                      varnameArray.set(i, value);
                  }else{
                      varnameArray.set(i, fillValue);
                  }
              }
           } else {
               varnameArray = tempArray;
           }
           newnc.write(newvarname, varnameArray);
          } catch (IOException e) {
           log.error("Variable can't be found = " + name + '\n' + e);
           
          } catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      return varnameArray;
}
/**
  * @param ncprof input netcdf profile
  * @param newnc output netcdf profile
  * @param name variable name
  * @param actioncopy true for copy data array
 * @return ArrayDouble.D2
 */
public static ArrayDouble.D2 copyDoubleD2Array(
    NetcdfFile oldNc, NetcdfFileWriter newnc,
    String name, boolean actioncopy, Logger log) {
        ArrayDouble.D2 varNameArray = null;
        ArrayDouble.D2 oldArray = null;
        try {
            Variable oldVar = oldNc.findVariable(name);
            Array oldVarnameValue =  oldVar.read();
            assert (oldVarnameValue instanceof ArrayDouble.D2);
            oldArray = (ArrayDouble.D2) oldVarnameValue;
            Variable newVar = newnc.findVariable(name);
            varNameArray = new ArrayDouble.D2(newVar.getShape()[0], newVar.getShape()[1]);
            Attribute fillValue = newVar.findAttributeIgnoreCase("_FillValue");
            int []shape = varNameArray.getShape();                
           if (actioncopy && oldArray.getShape()[0]>0) {
               for (int i = 0; i < shape[0]; i++) {
                   for (int j = 0; j < shape[1]; j++) {
                       if (i <= oldArray.getShape()[0]
                           && j <= oldArray.getShape()[1]) {
                           double value = oldArray.get(i, j);
                           varNameArray.set(i, j, value);
                       }
                   }
               }
            } else {
            	for (int i = 0; i < shape[0]; i++){
            		for (int j = 0; j < shape[1]; j++){
            			varNameArray.set(i, j, fillValue.getNumericValue().doubleValue());
            		}
            	}
            }
           newnc.write(newVar, varNameArray);
           } catch (IOException e) {
            log.error("Variable can't be found = " + name + '\n' + e);
            
           } catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       return varNameArray;
    }
/**
  * @param ncprof input netcdf profile
  * @param newnc output netcdf profile
  * @param name variable name
  * @param actioncopy true for copy data array
  * @return ArrayDouble.D3
  */
public static ArrayDouble.D3 copyDoubleD3Array(
    NetcdfFile ncprof, NetcdfFileWriter newnc,
    String name,
    boolean actioncopy, Logger log) {
         ArrayDouble.D3 varnameArray = null;
         ArrayDouble.D3 tempArray = null;
         try {
             Variable varname = ncprof.findVariable(name);
             Array varnameValue =  varname.read();
             assert (varnameValue instanceof ArrayDouble.D3);
             tempArray = (ArrayDouble.D3) varnameValue;
             Variable newvarname = newnc.findVariable(name);

             if (actioncopy) {
                 Array newvarValue = newvarname.read();
                 varnameArray = (ArrayDouble.D3) newvarValue;
                 int []shape = varnameArray.getShape();
                 for (int i = 0; i < shape[0]; i++) {
                    for (int j = 0; j < shape[1]; j++) {
                        for (int k = 0; k < shape[2]; k++) {
                            if (i < tempArray.getShape()[0]
                                && j < tempArray.getShape()[1]
                                && k < tempArray.getShape()[2]) {
                            double value = tempArray.get(i, j, k);
                            varnameArray.set(i, j, k, value);
                                }
                        }
                    }
                }
             } else {
                 varnameArray = tempArray;
             }
             newnc.write(newvarname, varnameArray);
            } catch (IOException e) {
             log.error("Variable can't be found = " + name + '\n' + e);
             
            } catch (InvalidRangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        return varnameArray;
     }
/**
 * @param ncfile Netcdf file
 * @param varname variable name
 * @param input value
 */
public static void writeString(
    NetcdfFileWriter ncfile,
    String varname,
    String input, Logger log) {
    try {
        Variable ncvar = ncfile.findVariable(varname);
        int []shape = ncvar.getShape();
        char[]c = input.toCharArray();
        int len = shape[0];
        if (c.length < shape[0]){
                input = addBlankstoEnd (input, (len - input.length()));
        }
        ArrayChar.D1 ncvarValueArray = new ArrayChar.D1(shape[0]);
        c = input.toCharArray();
        for (int i = 0; i < len; i++){
            ncvarValueArray.set(i, c[i]);
        }
        ncfile.write(ncvar, ncvarValueArray);
    } catch (IOException e) {
        log.error("ERROR writting Achar1 for " + varname + '\n' + e);
    } catch (InvalidRangeException e) {
    	log.error("Invalid range for " + varname + '\n' + e);
        
    }
}
public static void writeChar(
    NetcdfFileWriter ncfile,
    String varname,
    String input, Logger log) {
    try {
        Variable ncvar = ncfile.findVariable(varname);
        Array ncvarValue = ncvar.read();
        ArrayChar.D0 ncvarValueArray = (ArrayChar.D0) ncvarValue;
        ncvarValueArray.set(input.toCharArray()[0]);
        ncfile.write(ncvar, ncvarValueArray);
    } catch (IOException e) {
        log.error("ERROR writting Achar1 for " + varname);
    } catch (InvalidRangeException e) {
    	log.error("Invalid range for " + varname + '\n' + e);
        
    }
}

public static void writeFloat(NetcdfFileWriter ncfile,
    String varname,
    float input, Logger log){
        try {
            Variable ncvar = ncfile.findVariable(varname);
            Array ncvarValue = ncvar.read();
            ArrayFloat.D0 ncvarValueArray = (ArrayFloat.D0) ncvarValue;
            ncvarValueArray.set(input);
            ncfile.write(ncvar, ncvarValueArray);
        } catch (IOException e) {
        	log.error("Error in write  " + varname + '\n' +e);
        } catch (InvalidRangeException e) {
            log.error("Error in write  " + varname + '\n' +e);            
        }
   
}

/**
 * @param ncfile Netcdf file
 * @param varname variable name
 * @param index index position in the array
 * @param input String
 */
public static void writeCharD1(
    NetcdfFileWriter ncfile,
    String varname,
    int index,
    String input, Logger log) {
        try {
            Variable ncvar = ncfile.findVariable(varname);
            Array ncvarValue = ncvar.read();
            ArrayChar ac2 = (ArrayChar.D1) (ncvarValue);
            Index ima = ncvarValue.getIndex();
            ac2.setChar(ima.set(index), input.toCharArray()[0]);
            //log.info("write: " + varname + "=" + input.toCharArray()[0]);
            ncfile.write(ncvar, ac2);
        } catch (IOException e) {
            log.error("ERROR writting Achar1 for " + varname + '\n' +e);
        } catch (InvalidRangeException e) {
        	log.error("ERROR writting Achar1 for " + varname + '\n' +e);
            
        }
}
public static void writeArrayCharD1(NetcdfFileWriter ncfile,
		String varname, int index , List <String>_inputList, Logger log){
    try {
        Variable ncvar = ncfile.findVariable(varname);
        Array ncvarValue = ncvar.read();
        ArrayChar ac2 = (ArrayChar.D1) (ncvarValue);
        for (int i = 0; i < _inputList.size(); i++){
        	ac2.setChar(index, _inputList.get(i).toCharArray()[0]);
        	index++;
        }        
        ncfile.write(ncvar, ac2);
    } catch (IOException e) {
        log.error("ERROR writting Achar1 for " + varname + '\n' +e);
    } catch (InvalidRangeException e) {
    	log.error("ERROR writting Achar1 for " + varname + '\n' +e);
        
    }
	
}
/**
 * @param ncfile Netcdf profile
 * @param i the 0 index of array (x,y,z)
 * @param j the 1 index of Array (x,y,z)
 * @param varname Netcdf file variable
 * @param input the String input
 */
public static void  writeCharD2(
    NetcdfFileWriter ncfile,
    String varname,
    int i,
    String input, Logger log) {
    try {
        Variable ncvar = ncfile.findVariable(varname);
        Array ncvarValue = ncvar.read();
        ArrayChar.D2 ncvarValueArray = (ArrayChar.D2) ncvarValue;
        Index ima = ncvarValueArray.getIndex();
        int len = ncvar.getShape()[1];
        if (input.trim().length() < len){
            input = addBlankstoEnd (input.trim(), (len - input.trim().length()));
        }
        ncvarValueArray.setString(ima.set(i), input);
        ncfile.write(ncvar, ncvarValueArray);
    } catch (IOException e) {
    	log.error("ERROR writing Achar2 for " + varname);
    } catch (InvalidRangeException e) {
    	log.error("invalid range " + e);
      
    }
}
/**
 * @param ncfile Netcdf profile
 * @param i the 0 index of array (x,y,z)
 * @param j the 1 index of Array (x,y,z)
 * @param varname Netcdf file variable
 * @param input the String input
 */
public static void  writeArraySingleCharD2(
    NetcdfFileWriter ncfile,
    String varname,
    int i,int j,
    List<String> input, Logger log) {
    try {
        Variable ncvar = ncfile.findVariable(varname);
        Array ncvarValue = ncvar.read();
        ArrayChar.D2 ncvarValueArray = (ArrayChar.D2) ncvarValue;
        int []shape = ncvar.getShape();
        for (int indexi = i; indexi < shape[0]; indexi++){
        	for (int indexj = j ; indexj < shape[1]; indexj ++){
        		ncvarValueArray.set(indexi, indexj, input.get(indexj).toCharArray()[0]);
        	}
        }
        
        ncfile.write(ncvar, ncvarValueArray);
    } catch (IOException e) {
    	log.error("ERROR writing for " + varname);
    } catch (InvalidRangeException e) {
    	log.error("invalid range " + e);
      
    }
}
public static void writeArrayStringD2(NetcdfFileWriter ncfile, 
		String varName, int index, List<String> inputs, Logger log){
    try {
        Variable ncvar = ncfile.findVariable(varName);
        Array ncvarValue = ncvar.read();
        ArrayChar.D2 ncvarValueArray = (ArrayChar.D2) ncvarValue;
        int len = ncvar.getShape()[1];        
        for (int i = 0; i < inputs.size(); i++){
        	String input = inputs.get(i);
            if (input.length() < len){
            	input = addBlankstoEnd (input, (len - input.length()));
            }
            ncvarValueArray.setString(index + i, input);
            //ncvarValueArray.set(index, i, input.toCharArray()[0]);
        }        
        ncfile.write(ncvar, ncvarValueArray);
    } catch (IOException e) {
    	log.error("ERROR writing Achar2 for " + varName);
    } catch (InvalidRangeException e) {
    	log.error("invalid range " + e);
      
    }
	
	
}
public static void writeArrayCharD2(NetcdfFileWriter ncfile, 
		String varName, int index, List<String> inputs, Logger log){
    try {
        Variable ncvar = ncfile.findVariable(varName);
        Array ncvarValue = ncvar.read();
        ArrayChar.D2 ncvarValueArray = (ArrayChar.D2) ncvarValue;
        for (int i = 0; i < inputs.size(); i++){
        	String input = inputs.get(i);
            ncvarValueArray.set(index, i, input.toCharArray()[0]);
        }        
        ncfile.write(ncvar, ncvarValueArray);
    } catch (IOException e) {
    	log.error("ERROR writing Achar2 for " + varName);
    } catch (InvalidRangeException e) {
    	log.error("invalid range " + e);
      
    }
	
	
}
/**
 * @param ncfile Netcdf profile
 * @param i the 0 index of array (x,y,z)
 * @param j the 1 index of Array (x,y,z)
 * @param varname Netcdf file variable
 * @param input the String input
 */
public static void  writeCharD3(
    NetcdfFileWriter ncfile,
    String varname,
    int i,
    int j,
    String input, Logger log) {
    try {
        Variable ncvar = ncfile.findVariable(varname);
        Array ncvarValue = ncvar.read();
        ArrayChar.D3 ncvarValueArray = (ArrayChar.D3) ncvarValue;
        Index ima = ncvarValueArray.getIndex();
        if (input.trim().length() < ncvar.getShape()[2]){
        	input = addBlankstoEnd (input, (ncvar.getShape()[2] - input.length()));
        }
        ncvarValueArray.setString(ima.set(i, j), input);
        ncfile.write(ncvar, ncvarValueArray);
    } catch (IOException e) {
    	log.error("ERROR writing Achar4 for " + varname);
    } catch (InvalidRangeException e) {
    	log.error ("Invalid range " + e );
      
    }
}
public static void  writeArrayCharD3(
	    NetcdfFileWriter ncfile,
	    String varname,
	    int index0,
	    int index1,
	    List<String> inputs, Logger log) {
	    try {
	        Variable ncvar = ncfile.findVariable(varname);
	        Array ncvarValue = ncvar.read();
	        ArrayChar.D3 ncvarValueArray = (ArrayChar.D3) ncvarValue;
	        Index ima = ncvarValueArray.getIndex();
	        int []shape = ncvar.getShape();
	        for (int i = 0; i < shape[0]; i++) {
	            for (int j= 0; j <inputs.size(); j++){
		        	String input = inputs.get(j).trim();
	            	if (input.length() < shape[2]){
	            		input = addBlankstoEnd (input, (shape[2] - input.length()));
	            	}
	            	ncvarValueArray.setString(ima.set(index0,index1+j), input);	            	
	            }
	        	index0++;
	        }
	        
	        ncfile.write(ncvar, ncvarValueArray);
	    } catch (IOException e) {
	    	log.error("ERROR writing Achar4 for " + varname);
	    } catch (InvalidRangeException e) {
	    	log.error ("Invalid range " + e );
	      
	    }
	}

public static void writeCharD4(
    NetcdfFileWriter ncfile,
    String varName,
    int i,
    int j,
    int k,
    String input, Logger log){
    try {
        Variable ncvar = ncfile.findVariable(varName);
        Array ncvarValue = ncvar.read();
        ArrayChar.D4 ncvarValueArray = (ArrayChar.D4) ncvarValue;        
        Index ima= ncvarValueArray.getIndex();
        
        int slen = ncvar.getDimension(3).getLength();
        if (input.length()>slen){
            log.info(" Input string is longer than allowed length" + 
                                input + varName);
        } else if (input.trim().length() <slen){
            input = addBlankstoEnd (input.trim(), (slen - input.trim().length()));
        }
        ncvarValueArray.setString(ima.set(i, j, k), input);
         ncfile.write(ncvar, ncvarValueArray);
        
    } catch (IOException e) {
    	log.error("ERROR writing Achar4 for " + varName + '\n' + e);
    } catch (InvalidRangeException e) {
    	log.error("ERROR writing Achar4 for " + varName + '\n' + e);
      
    }
    
    
}
/**
 * @param ncfile Netcdf file
 * @param i ith location in array (i,j)
 * @param j jth location in array (i, j)
 * @param varname variable name
 * @param input number
 */
public static void writeFloatD2(
        NetcdfFileWriter ncfile,
        String varname,
        int i,
        int j,
        float input, Logger log) {
        try {
            Variable ncvar = ncfile.findVariable(varname);
            Array ncvarValue = ncvar.read();
            ArrayFloat.D2 ncvarValueArray = (ArrayFloat.D2) ncvarValue;
            Index ima = ncvarValueArray.getIndex();
            ncvarValueArray.setFloat(ima.set(i, j), input);
            ncfile.write(ncvar, ncvarValueArray);
        } catch (IOException e) {
        	log.error ("ERROR writing Achar4 for " + varname + '\n' +e);
        } catch (InvalidRangeException e) {
        	log.error ("ERROR writing Achar4 for " + varname + '\n' +e);
          
        }
}
/**
 * @param ncfile NetcdfFile
 * @param index pos in the array
 * @param varname Netcdf variable name
 * @param input double
 */
public static void writeDoubleD1(
    NetcdfFileWriter ncfile,
    String varname,
    int index,
    double input, Logger log) {
        try {
            Variable ncvar = ncfile.findVariable(varname);
            Array ncvarValue = ncvar.read();
            ArrayDouble.D1 ncvarValueArray = (ArrayDouble.D1) ncvarValue;
            Index ima = ncvarValueArray.getIndex();
            ncvarValueArray.setDouble(ima.set(index), input);
            ncfile.write(ncvar, ncvarValueArray);
        } catch (IOException e) {
            log.error ("ERROR writing Double.D1 for " + varname + '\n' +e);
        } catch (InvalidRangeException e) {
        	log.error ("ERROR writing Double.D1 for " + varname + '\n' +e);
        }
}
public static void writeArrayDoubleD1(NetcdfFileWriter ncfile,
		String varname, int index,
		List <Double> _inputList, Logger log){
		try {
	    	Variable ncvar = ncfile.findVariable(varname);
	    	Array ncvarValue = ncvar.read();
            ArrayDouble.D1 ncvarArray = (ArrayDouble.D1) ncvarValue;
            for (int i = 0; i < _inputList.size(); i++){
            	ncvarArray.set(index, _inputList.get(i));
            	index ++;
            }
            ncfile.write(ncvar, ncvarArray);
			
		} catch (IOException e) {
			log.error("Error in writting " + varname + e);			
		} catch (InvalidRangeException e) {
			log.error("Invalid Range for writing " + varname + e);				
		}
	
	
}
public static void writeDoubleD2(
    NetcdfFileWriter ncfile,
    String varname,
    int index1,
    int index2,
    double input, Logger log) {
        try {
            Variable ncvar = ncfile.findVariable(varname);
            Array ncvarValue = ncvar.read();
            ArrayDouble.D2 ncvarValueArray = (ArrayDouble.D2) ncvarValue;
            ncvarValueArray.set(index1,index2, input);
            ncfile.write(ncvar, ncvarValueArray);
        } catch (IOException e) {
            log.error("ERROR writing Double.D2 for " + varname + '\n' + e);
        } catch (InvalidRangeException e) {
        	log.error("ERROR writing Double.D2 for " + varname + '\n' + e);
        }
}

/**
 * @param ncfile NetcdfFile
 * @param index pos in the array
 * @param varname Netcdf variable name
 * @param input double
 */
public static void writeIntD1(
    NetcdfFileWriter ncfile,
    String varname,
    int index,
    int input, Logger log) {
        try {
            Variable ncvar = ncfile.findVariable(varname);
            Array ncvarValue = ncvar.read();
            ArrayInt.D1 ncvarValueArray = (ArrayInt.D1) ncvarValue;
            ncvarValueArray.set(index, input);           
            ncfile.write(ncvar, ncvarValueArray);
        } catch (IOException e) {
            log.error("ERROR writing Int.D1 for " + varname + '\n' + e);
        } catch (InvalidRangeException e) {
        	log.error("ERROR writing Int.D1 for " + varname + '\n' + e);
        }
}
public static void writeArrayIntD1(NetcdfFileWriter ncfile,
		String varname, int index,
		List <Integer> _inputList, Logger log){
		try {
	    	Variable ncvar = ncfile.findVariable(varname);
	    	Array ncvarValue = ncvar.read();
            ArrayInt.D1 ncvarArray = (ArrayInt.D1) ncvarValue;
            for (int i = 0; i < _inputList.size(); i++){
            	ncvarArray.set(index, _inputList.get(i));
            	index ++;
            }
            ncfile.write(ncvar, ncvarArray);
			
		} catch (IOException e) {
			log.error("Error in writting " + varname + e);			
		} catch (InvalidRangeException e) {
			log.error("Invalid Range for writing " + varname + e);				
		}
	
	
}
public static void writeIntD2(
    NetcdfFileWriter ncfile,
    String varname,
    int i,
    int j,
    int input, Logger log) {
    try {
        Variable ncvar = ncfile.findVariable(varname);
        Array ncvarValue = ncvar.read();
        ArrayInt.D2 ncvarValueArray = (ArrayInt.D2) ncvarValue;
        Index ima = ncvarValueArray.getIndex();
        ncvarValueArray.setInt(ima.set(i, j), input);
        ncfile.write(ncvar, ncvarValueArray);
    } catch (IOException e) {
    	log.error("ERROR writing Int for " + varname + '\n' + e);
    } catch (InvalidRangeException e) {
    	log.error("ERROR writing Int for " + varname + '\n' + e);

    }
}
public static void writeFloatD1(
    NetcdfFileWriter ncfile,
    String varname,
    int index,
    float input, Logger log) {
	ArrayFloat.D1 ncvarValueArray = null;
        try {
            Variable ncvar = ncfile.findVariable(varname);
            Array ncvarValue = ncvar.read();
            assert (ncvarValue instanceof ArrayFloat.D1);
            ncvarValueArray = (ArrayFloat.D1) ncvarValue;
            ncvarValueArray.setFloat(index, input);
           // ncvarValueArray.set(index, input);
            ncfile.write(ncvar, ncvarValueArray);
        } catch (IOException e) {
            log.error("ERROR writing Float.D1 for " + varname + e);
        } catch (InvalidRangeException e) {
        	 log.error("Invalid range for " + varname + e);           
        }
}
public static void writeArrayFloatD1(NetcdfFileWriter ncfile,
		String varname, int index,
		List <Float> _inputList, Logger log){
		try {
	    	Variable ncvar = ncfile.findVariable(varname);
	    	Array ncvarValue = ncvar.read();
            ArrayFloat.D1 ncvarArray = (ArrayFloat.D1) ncvarValue;
            for (int i = 0; i < _inputList.size(); i++){
            	ncvarArray.set(index, _inputList.get(i));
            	index ++;
            }
            ncfile.write(ncvar, ncvarArray);
			
		} catch (IOException e) {
			log.error("Error in writting " + varname + e);			
		} catch (InvalidRangeException e) {
			log.error("Invalid Range for writing " + varname + e);				
		}
	
	
}
/**
 * @param ncprof Netcdf profile
 * @param name NetCdf name
 * @return ArrayCharD3
 */
public static List<String> getCharD4Array_List(NetcdfFile ncprof, String name, Logger log) {
      ArrayChar.D4 varnameArray = null;
      List<String>_sarray = new ArrayList <String>();
      try {
          Variable varname = ncprof.findVariable(name);
          ArrayChar.D4 varnameValue =  (ArrayChar.D4)varname.read();
          assert (varnameValue instanceof ArrayChar.D4);
          varnameArray = (ArrayChar.D4) varnameValue;

      } catch (IOException e) {
          log.error("Variable can't be found = " + name + '\n' + e);          
      }
      int []parashape = varnameArray.getShape();
      StringBuffer sbuf = new StringBuffer();
      for (int i = 0; i < parashape[0]; i++) {
          for (int j = 0; j < parashape[1]; j++) {
              for (int k = 0; k < parashape[2]; k++) {
                  for (int l = 0 ;l <parashape[3]; l++){
                      sbuf.append(varnameArray.get(i, j, k,l));
                  }
                  _sarray.add(i + ";" + j+ ";" + sbuf.toString());
                  sbuf = new StringBuffer();
                  

              }
          }
      }
      return _sarray;
  }
public static ArrayChar.D4 getCharD4Array(NetcdfFile ncprof, String name, Logger log) {
    ArrayChar.D4 varnameArray = null;
    try {
        Variable varname = ncprof.findVariable(name);
        ArrayChar.D4 varnameValue =  (ArrayChar.D4)varname.read();
        assert (varnameValue instanceof ArrayChar.D4);
        varnameArray = (ArrayChar.D4) varnameValue;

    } catch (IOException e) {
        log.error("Variable can't be found = " + name + '\n' + e);          
    }
    return varnameArray;
}

/**
 * @param ncprof Netcdf profile
 * @param name NetCdf name
 * @return ArrayCharD3
 */
public static List<List<String>> getCharD3Array_List(NetcdfFile ncprof, String name, Logger log) {
      ArrayChar.D3 varnameArray = null;
      List<List<String>>_sarray = new ArrayList<List<String>>();
      try {
          Variable varname = ncprof.findVariable(name);
          ArrayChar.D3 varnameValue =  (ArrayChar.D3)varname.read();
          assert (varnameValue instanceof ArrayChar.D3);
          varnameArray = (ArrayChar.D3) varnameValue;

      } catch (IOException e) {
          log.error("Variable can't be found = " + name + '\n' + e);
         
      }
      int []parashape = varnameArray.getShape();
      
      for (int i = 0; i < parashape[0]; i++) {
    	  ArrayList<String>varArray = new ArrayList<String>();
          for (int j = 0; j < parashape[1]; j++) {
        	  StringBuffer sbuf = new StringBuffer(parashape[2]);
              for (int k = 0; k < parashape[2]; k++) {
                  sbuf.append(varnameArray.get(i, j, k));
              }
              varArray.add(sbuf.toString().trim());              
          }
          _sarray.add(varArray);
      }

      return _sarray;
  }

/**
 * @param ncprof Netcdf profile
 * @param name NetCdf name
 * @return ArrayCharD3
 */
public static ArrayChar.D3 getCharD3Array(NetcdfFile ncprof, String name, Logger log) {
      ArrayChar.D3 varnameArray = null;
      try {
          Variable varname = ncprof.findVariable(name);
          ArrayChar.D3 varnameValue =  (ArrayChar.D3)varname.read();
          assert (varnameValue instanceof ArrayChar.D3);
          varnameArray = (ArrayChar.D3) varnameValue;

      } catch (IOException e) {
          log.error("Variable can't be found = " + name + '\n' + e);
          
      }
      return varnameArray;
  }
/**
 * @param ncprof NetCdf profile
 * @param name Netcdf name
 * @return ArrayFloatD2
 */
public static ArrayFloat.D2 getFloatD2Array(NetcdfFile ncprof, String name , Logger log) {
      ArrayFloat.D2 varfloatArray = null;
      try {
          Variable varname = ncprof.findVariable(name);
          Array varnameValue = varname.read();
          assert (varnameValue instanceof ArrayFloat.D2);
          varfloatArray = (ArrayFloat.D2) varnameValue;
      } catch (IOException e) {
          log.error("Can't find variable = " + name + '\n' + e);
          
      }
return varfloatArray;
}
public static ArrayDouble.D1 getDoubleD1Array(NetcdfFile ncprof, String name, Logger log) {
    ArrayDouble.D1 varfloatArray = null;
    try {
        Variable varname = ncprof.findVariable(name);
        Array varnameValue = varname.read();
        assert (varnameValue instanceof ArrayDouble.D1);
        varfloatArray = (ArrayDouble.D1) varnameValue;
    } catch (IOException e) {
        log.error("Can't find variable = " + name + '\n' + e);
        
    }
return varfloatArray;
}

/**
 * @param ncprof Netcdf file name
 * @param name Netcdf Variable name
 * @return integer value
 */
public static List<Integer> getIntD1 (NetcdfFile ncprof, String name, Logger log) {
    List<Integer> val = new ArrayList<Integer>();
    try {
        Variable varname = ncprof.findVariable(name);
        Array varnameValue = varname.read();
        int[]shape = varname.getShape();
        for (int i =0; i < shape[0]; i++){
        	val.add(new Integer(varnameValue.getInt(i)));
        }
    } catch (IOException e) {
        log.error("Can't find variable = " + name + '\n' +e);
        
    }
    return val;
}
/**
 * @param ncprof Netcdf file name
 * @param name Netcdf Variable name
 * @return integer value
 */
public static ArrayInt.D1 getIntD1Array (NetcdfFile ncprof, String name, Logger log) {
    ArrayInt.D1 varIntArray = null;
    try {
        Variable varname = ncprof.findVariable(name);
        Array varnameValue = varname.read();
        assert (varnameValue instanceof ArrayInt.D1);
        varIntArray = (ArrayInt.D1)varnameValue;
    } catch (IOException e) {
        log.error ("Can't find variable = " + name + '\n' +e);
       
    }
    return varIntArray;
}

/**
 * @param ncprof Netcdf file name
 * @param name Netcdf Variable name
 * @return double value
 */
public static List<Double> getDoubleD1 (NetcdfFile ncprof, String name, Logger log) {
    List<Double> val = new ArrayList<Double>();
    try {
        Variable varname = ncprof.findVariable(name);
        Array varNameValue = varname.read();
        int[]shape = varname.getShape();
        for (int i = 0; i < shape[0]; i++){
        	val.add(new Double(varNameValue.getDouble(i)));
        }
    } catch (IOException e) {
        log.error("Can't find variable = " + name + '\n' +e);
       
    }
    return val;
}
/**
 * @param ncprof netcdf file
 * @param name netcdf name variable
 * @return String value
 */
public static List<String> getCharD1(NetcdfFile ncprof, String name, Logger log) {
    List<String> varstr = new ArrayList<String>();
    
    try {
        Variable varname = ncprof.findVariable(name);
        Array varnameValue = varname.read();
        ArrayChar.D1 varnameArray = (ArrayChar.D1) varnameValue;
        int[]shape = varnameArray.getShape();       
        for (int i = 0; i < shape[0]; i++) {            
            varstr.add(String.valueOf(varnameArray.get(i)));
        }
        
    } catch (IOException e) {
       log.error ("Can't find variable = " + name + '\n' + e);
       
    }
    return varstr;
}
public static String getString(NetcdfFile ncprof, String name, Logger log) {
   StringBuffer varstr = new StringBuffer();
    
    try {
        Variable varname = ncprof.findVariable(name);
        Array varnameValue = varname.read();
        ArrayChar.D1 varnameArray = (ArrayChar.D1) varnameValue;
        int[]shape = varnameArray.getShape();       
        for (int i = 0; i < shape[0]; i++) {
        	varstr.append(String.valueOf(varnameArray.get(i)));
        }
        
    } catch (IOException e) {
       log.error ("Can't find variable = " + name + '\n' + e);
       
    }
    return varstr.toString();
}
/**
 * @param ncprof netcdf profile
 * @param name netcdf name variable
 * @return String value
 */
public static List<String> getString2D(NetcdfFile ncprof, String name, Logger log) {
      List<String> varstr = new ArrayList<String>();
      try {
          Variable varname = ncprof.findVariable(name);
          Array varnameValue = varname.read();
          ArrayChar.D2 varnameArray = (ArrayChar.D2) varnameValue;
          int[]shape = varnameArray.getShape();          
      for (int i = 0; i < shape[0]; i++) {
    	  StringBuffer sbf = new StringBuffer();
               for (int j = 0; j < shape[1]; j++) {
            	   sbf.append(varnameArray.get(i, j));
               }
               varstr.add(sbf.toString());
           }           
      } catch (IOException e) {
          log.error("Can't find variable = " + name + '\n' + e);
          
      }
      return varstr;
  }
/**
 * @param ncprof Netcdf profile
 * @param name NetCdf name
 * @return ArrayCharD3
 */
public static ArrayChar.D2 getCharD2Array(NetcdfFile ncprof, String name, Logger log) {
      ArrayChar.D2 varnameArray = null;
      try {
          Variable varname = ncprof.findVariable(name);
          Array varnameValue =  (ArrayChar.D2)varname.read();
          assert (varnameValue instanceof ArrayChar.D2);
          varnameArray = (ArrayChar.D2) varnameValue;

      } catch (IOException e) {
          log.error("Variable can't be found = " + name + '\n' + e);
          
      }
      return varnameArray;
  }

public static boolean isNumber (String input){
    boolean value = false;
    char []data = input.toCharArray();
    char[]number = {'0','1','2','3','4','5','6','7','8','9'};
    for (int i = 0; i < data.length; i ++){
        for (int j= 0; j < number.length; j++){
            if (data[i]== data[j])value = true;
        }
       
    }
    return value;
}
public static String addBlankstoEnd(String input, int noBlanks){
    String s = "";
    String blank = " ";
    for (int i = 0; i < noBlanks ; i++){
        s = input.concat(blank);
        input = s;
    }
    return s;
}
public static void createNcfile(String ncmlFile, String outputFileName, 
                                Logger log){
    String output = outputFileName;
    
    try {
        InputStream ncml = new FileInputStream(ncmlFile);
        NcMLReader.writeNcMLToFile(ncml, output);

    } catch (FileNotFoundException e) {
        log.error("Can't find NCML structure file" + ncmlFile + '\n' + e);
        
    } catch (IOException e) {
        log.error("Error in generate netcdf file use Ncml" + output + '\n' + e);
        
    }
}
/*
 * This process return a the new index of a non-sorted array after
 * the array is sorted ascending
 * 
 */
public static int[]getIndexofSort1DArray(double[]inputArray){
    int []index = new int[inputArray.length];
    double []inputA = inputArray;
    Arrays.sort(inputA);
    for (int i = 0 ; i < inputA.length ; i++){
            index [i] = Arrays.binarySearch(inputA, inputArray[i]);
    }
    return index;
}
public static double[] sort1DArrayUseIndex(int[] index, 
                        double[]input){
    double []data = new double[input.length]; 
    for (int i = 0 ; i < index.length; i++){
        data [index[i]] = data[i];
    }
    return data;
}
public static String calculateProfileQc(List<String> profilevalqc) {
    // flag 1,2,5, or 8 are good data
    // qc flag values of 9 are not used in computation
    // all other qc value are bad data
    double noDepth = profilevalqc.size();
    double goodflag = 0;
    double noqc = 0;
    String overalProfileQc = "";
    int noMissingQc = 0;
    for (int i = 0; i <noDepth; i++){
        if (profilevalqc.get(i).compareToIgnoreCase("1")==0||
            profilevalqc.get(i).compareToIgnoreCase("2")==0||
            profilevalqc.get(i).compareToIgnoreCase("5")==0||
            profilevalqc.get(i).compareToIgnoreCase("8")==0){
            goodflag++;
        }
        if (profilevalqc.get(i).compareToIgnoreCase("0")==0){
            noqc++;
        }
        if (profilevalqc.get(i).compareToIgnoreCase("9")==0){
        	noMissingQc++;
        }
    }
    noDepth = noDepth - noMissingQc;
    double percentNoqc = (noqc/noDepth)*100.0;
    double percentgoodlevel = (goodflag/noDepth) *100.0;
    if (percentgoodlevel == 100.0){
        overalProfileQc = "A";
    }else if (percentgoodlevel <100.0 && percentgoodlevel >=75.0){
        overalProfileQc = "B";
    }else if (percentgoodlevel < 75.0 && percentgoodlevel >=50.0){
        overalProfileQc = "C";
    }else if (percentgoodlevel < 50.0 && percentgoodlevel >= 25.0){
        overalProfileQc = "D";
    }else if (percentgoodlevel < 25.0 && percentgoodlevel > 0.0){
        overalProfileQc = "E";
    }else if (percentgoodlevel == 0.0){
        overalProfileQc = "F";
    }
    if (percentNoqc == 100){
        overalProfileQc = " ";
    }
    return overalProfileQc;
}
public static void setArrayToFillValue(NetcdfFileWriter ncfile , String varName, String data_type, Logger log){
        try {
            Variable ncvar = ncfile.findVariable(varName);
            Array ncvarValue = ncvar.read();
            Attribute paramfill = ncvar.findAttribute("_FillValue");
            int []shape = ncvar.getShape();
            if (data_type.compareToIgnoreCase("char")!= 0){
                ArrayFloat.D2 ncvarValueArray = (ArrayFloat.D2) ncvarValue;
                for (int i = 0; i < shape[0]; i++){
                	for (int j = 0; j < shape[1]; j++){
                		ncvarValueArray.set(i, j, paramfill.getNumericValue().floatValue());
                		
                	}
                }
                ncfile.write(ncvar, ncvarValueArray);

            } else {
            	ArrayChar.D2 ncvarValueArray = (ArrayChar.D2) ncvarValue;
                for (int i = 0; i < shape[0]; i++){
                	for (int j = 0; j < shape[1]; j++){
                		ncvarValueArray.set(i, j, paramfill.getStringValue().toString().charAt(0));
                		
                	}
                }
                ncfile.write(ncvar, ncvarValueArray);

            }
        } catch (IOException e) {
        	log.error ("ERROR writing Achar4 for " + varName + '\n' +e);
        } catch (InvalidRangeException e) {
        	log.error ("ERROR writing Achar4 for " + varName + '\n' +e);
          
        }
}
}

/*
 * Created on 22-Jun-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package dfo.isdm.BufrUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jfree.util.Log;


/**
 * @author Tran
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public final class BufrUtility {
/**
 * Default constructor for for utility class, but can't instantiate
 */
private  BufrUtility() {
}

/**
 * @param inputsrc - Intput string
 * @param ilength - length allocate for the byte buffer
 * @return byte buffer
 */
public static StringBuffer integerToBinary (String inputsrc, int ilength, Logger _log) {
	int ivalue =Integer.parseInt(inputsrc);;
	StringBuffer s = new StringBuffer();
	if (ivalue >= 0) {		
		BigInteger bi = new BigInteger(inputsrc);
		s = new StringBuffer(bi.toString(2));
		if (s.length() < ilength) {
		    s = BufrUtility.padzerotoleft(s, ilength);
		  }else if (s.length() > ilength){
			  s= setMissingValue(ilength);
			  _log.info("length of the result binary string is " + s.length() + " and longer than allowable length of" 
			  + ilength + ".  Hence the value was set value to missing");
		  }
	} else {		
		_log.info("Encode negative value of " + inputsrc);
		int pos_ivalue = -1 * ivalue;
		BigInteger bi = new BigInteger(Integer.toString(pos_ivalue));
		s = new StringBuffer(bi.toString(2));
		if (s.length() <= ilength) {
			s = BufrUtility.padzerotoleft(s, ilength);
			s.replace(0, 1, "1");
		}else {
			s = setMissingValue(ilength);
			_log.info("length of the result binary string is " + s.length() + " and longer than allowable length of" 
					  + ilength + ".  Hence the value was set value to missing");

		}
		
		/*
		String negbinary_str = Integer.toBinaryString(ivalue);
		s = new StringBuffer();
		if (negbinary_str.length() <= ilength) {
			s = BufrUtility.padzerotoleft(new StringBuffer(negbinary_str), ilength);
		} else {
			s = new StringBuffer(negbinary_str.substring(0, ilength));
			//s = new StringBuffer(negbinary_str.substring((negbinary_str.length()-ilength)));
			if (s.length()> ilength) {
				s = setMissingValue(ilength);
				_log.info("length of the result binary string is " + s.length() + " and longer than allowable length of" 
						  + ilength + ".  Hence the value was set value to missing");
			}			
		} */
		
		
	}
  
return s;
}
public static StringBuffer toBinary(String input, int ilength, String unit, Logger _log){
	StringBuffer sbuf = new StringBuffer();
	if (unit.trim().compareToIgnoreCase("char")==0){
		sbuf = putChar(input, ilength);
	} else {
		sbuf = integerToBinary(input, ilength,_log);
	}
	StringBuffer sbuf1= new StringBuffer();
	if (sbuf.length()> ilength){
		sbuf1 = new StringBuffer(sbuf.substring(0, ilength));
	} else {
		sbuf1 = sbuf;
	}
	return sbuf1;
}

/**
 * @param inputsrc binary string
 * @return Integer
 */
public static int getInteger(String inputsrc) {
    int value;
    StringBuffer s = new StringBuffer(inputsrc);
    /*
    if (inputsrc.startsWith("1")) {
    	if (inputsrc.length()< 32) {
    		s= padonetoleft(s, 32);
    	} 
    } else {
    	if (inputsrc.length()< 32) {
    		s= padzerotoleft(s, 32);
    	} 

    }    */
    BigInteger bi = new BigInteger(s.toString(), 2);    
    value = bi.intValue();
    return value;
} 

/**
 * @param s1 input string
 * @param length desired string length
 * @return output string with certain length and zero fill in the left if
 * if input string length is less than the desired lenght
 */
public static StringBuffer padzerotoleft(StringBuffer s1, int length) {
    StringBuffer s2 = new StringBuffer("");
    int delta = length - s1.length();
    for (int i = 0; i < delta; i++) {
        s2.append('0');
    }    
    s2.append(s1);
    return s2;
}
/**
 * @param s1 input string
 * @param length desired string length
 * @return output string with certain length and zero fill in the left if
 * if input string length is less than the desired length
 */
public static StringBuffer padonetoleft(StringBuffer s1, int length) {
    StringBuffer s2 = new StringBuffer("");
    int start = length - s1.length();
    for (int i = 0; i < length; i++) {
        s2.append('1');
    }
    s2.delete(start, length);
    s2.append(s1);
    return s2;
}

/** 
 * @param length desired string length
 * @return output string with certain length and zero fill in the left if
 * if input string length is less than the desired lenght
 */
public static StringBuffer setMissingValue(int length) {
    StringBuffer s2 = new StringBuffer("");    
    for (int i = 0; i < length; i++) {
        s2.append('1');
    }
    return s2;
}
public static boolean isMissingValue(String inputStr){
	boolean isMissing = true;
	int i=0;
	char[] ch = inputStr.toString().toCharArray();
	while(isMissing && i < ch.length){
		if (ch[i] =='0'){
			isMissing = false;
		}
		i++;
	}
	return isMissing;
}
/**
 * @param s1 input string
 * @param length desired string length
 * @return output string with certain length and zero fill in the left if
 * if input string length is less than the desired lenght
 */
public static StringBuffer padblanktoleft(StringBuffer s1, int length) {
    StringBuffer s2 = new StringBuffer("");
    int start = (length - s1.length()) / 8;
    for (int i = 0; i < start; i++) {
        s2.append(" ");
    }
    s2.append(s1);
    return s2;
}
public static StringBuffer padblanktoRight(StringBuffer s1, int length) {
    StringBuffer s2 = new StringBuffer("");
    if (length < s1.length()){
    	return s1;
    } else {
    	int start = (length - s1.length());
    	for (int i = 0; i < start; i++) {
            s2.append(" ");
        }
    	s1.append(s2);
    }
    return s1;
}

/**
 * @param inputsrc the input string
 * @param ilength data width for this class according to BUFR format
 * @return CharBuffer
 */
public static StringBuffer putChar(String inputsrc, int ilength) {
    StringBuffer s = new StringBuffer("");
    StringBuffer s2 = new StringBuffer("");
    if (inputsrc.length() < ilength / 8) {
        int nospaceadded = (ilength / 8 - inputsrc.length());
        for (int i = 0; i < nospaceadded; i++) {
            s.append(" ");
        }
    }
    s.append(inputsrc);
    char[] ch = s.toString().toCharArray();
    for (int i = 0; i < ch.length; i++) {
        byte b = (byte) ch[i];
        StringBuffer s1 = new StringBuffer(Integer.toBinaryString(b));
        if (s1.length() < 8) {
            s1 = BufrUtility.padzerotoleft(s1, 8);
        }
        s2.append(s1);
    }
    return s2;
}
/**
 * @param s1 StringBuffer
 * @param length length of the new string
 * @return string Buffer of zeros
 */
public static StringBuffer createzerostring(StringBuffer s1, int length) {
    for (int i = 0; i < length; i++) {
        s1.append('0');
    }
    return s1;
}

/**
 * @param inputsrc binary string
 * @return character string
 */
public static String getString(String inputsrc) {
    String result = new String("");
    StringBuffer sbuf = new StringBuffer("");
    BigInteger bi = new BigInteger(inputsrc, 2);
    byte[]bytes = bi.toByteArray();
    for (int i = 0; i < bytes.length; i++) {
        sbuf.append((char) bytes[i]);
    }
    result = sbuf.toString().trim();
    return result;
}

/**
 * @param input as a string
 * @return boolean true if the string is a number
 */
public static boolean isNumber(String input) {
    boolean no = false;
    boolean character = false;
    char []data = input.toCharArray();
    char[] number = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    for (int i = 0; i < data.length; i++) {
        for (int j = 0; j < number.length; j++) {
            if (data[i] == number[j]) {
                no = true;
            }
        }
        if (!no){
        	character = true;
        }
    }
    boolean value = false;
    if (!character){
    	value = true;
    }
    return value;
}
/**
 * @param parmqc gtspp quality class in Argo Netcdf format
 * @return GTSPP quality class according to BUFR format
 */
public static String put033050(String parmqc) {
    int qc033050;
    if (Integer.parseInt(parmqc.trim()) == 9) {
        qc033050 = 15;
    } else {
        qc033050 = Integer.parseInt(parmqc.trim());
    }
    String s = Integer.toString(qc033050);
    return s;
}
public static String put08080(String pcode) {
	String s = "63";
	if (pcode.contains("_DEPTH")){
		s= "13";
	} else if (pcode.contains("_PRES")){
		s= "10";
	} else if (pcode.contains("_TEMP")){
		s= "11";
	}else if (pcode.contains("_PSAL")){
		s= "12";
	}
    return s;
}
public static String setDigitCodeTo002032(char _digitCode){
	String s = "3";
	switch (_digitCode){
	case '5': case '6': case '7':
	s = "0";
	break;					
	case '1': case '2': case '8':
		s = "1";
		break;
}
	return s;
}
public static char getDigitCodeFrom002032 (String _bufrDigitCode) {
	char digitCode = '0';  // unknown
	if (_bufrDigitCode.equals("0.0")){
		digitCode = '7';
	} else if (_bufrDigitCode.equals("1.0")){
		digitCode = '8';
	}
	return digitCode;
}

/**
 * @param binarystr binary string
 * @return byte array
 */
public static byte cnvbinarystrtobyte(String binarystr) {
    byte []bytes = null;
    byte b = 0;
        char []ch = binarystr.toCharArray();
        boolean notdigit = false;
        for (int j = 0; j < ch.length; j++) {
            if (Character.toString(ch[j]).compareToIgnoreCase("0") != 0
                && Character.toString(ch[j]).compareToIgnoreCase("1") != 0) {
                notdigit = true;
        //        System.out.println(binarystr);
            }
        }
        if (notdigit) {
            if (binarystr.toString().trim().length() > 0) {
                bytes = binarystr.toString().trim().getBytes();
            } else {
                String s = " ";
                bytes = s.getBytes();
            }
        } else {
            BigInteger bi = new BigInteger(binarystr, 2);
            bytes = bi.toByteArray();
        }
        for (int i = 0; i < bytes.length; i++) {
             b = bytes[i];
        }
    return b;
}
/**
 * @param s StringBuffer of data section of Bufr message
 * @return StringBuffer of complete data section
 */
public static StringBuffer completeSection(StringBuffer s, Logger log) {
    StringBuffer result = new StringBuffer("");
    double totalOctet = (4.0 +s.length()/ 8.0);
    double remainder = StrictMath.IEEEremainder(totalOctet, 2);   
//  section 3 and 4 must end with the complete byte
       // double remainder = s.length() % 8.0;
        remainder = StrictMath.abs(remainder);
        log.info("Length of message in bits: " + s.length());
        log.info("the remainder of this section length by 8 and even number is:" +remainder);
        
        if (remainder > 0) {
          //  double zerotoright = 8 - remainder;
        	double zerotoright = (int) (2- remainder)*8;
            StringBuffer s1 = new StringBuffer("");
            s.append(BufrUtility.createzerostring(s1, (int)zerotoright));
            log.info("Appended " + zerotoright +" 0  to the right to make a complete byte ");
        }
// encode the begining of section
        int numberofoctet = (int) (4.0 + s.length() / 8.0);
        result.append(
            BufrUtility.integerToBinary(Integer.toString(numberofoctet), 24, log));
        // octet 4
        result.append(BufrUtility.integerToBinary("0", 8, log));
        result.append(s);
        return result;
}

/**
 * @param bufrbin FileOutputStream binary files
 * @param sbf StringBuffer to write in binary mode
 */
// write binary files
public static void writeoutput(
    FileOutputStream bufrbin,
    StringBuffer sbf, Logger log) {
    int numoctet = (int) (sbf.length() / 8.0);
    for (int i = 0; i < numoctet; i++) {
        String s = new String("");
        s = (sbf.toString().substring(i * 8, (i + 1) * 8));
        try {
            bufrbin.write(BufrUtility.cnvbinarystrtobyte(s));
        } catch (IOException e) {
            log.error("Error in write the file" + e);
            e.printStackTrace();
        }
    }
}
/**
 * @param file binary file
 * @return byteArrays
 * @throws IOException if file not found or error in reading
 */
public static byte[] getBytesFromFile(File file) throws IOException {
    byte[]bytes = null;
    try {
        InputStream in = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            System.out.println("File is too large to handle by the function");
            // File is too large
        }
        // Create the byte array to hold the data
        bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        try {
            while (offset < bytes.length
            && (numRead = in.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
// Close the input stream and return bytes
            in.close();
        } catch (IOException e1) {
            System.out.println("Error in reading the input stream");
            e1.printStackTrace();
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException(
                "Could not completely read file " + file.getName());
        }
    } catch (FileNotFoundException e) {
        System.out.println("File Not found" + file.getName());
        e.printStackTrace();
    }
    return bytes;

}
/**
 * Returns the string value of the given set of bytes.
 * @param numberOfBytes Number of bytes to read to get the full string.
 * Any number of bytes is fine since the length of a character is 1 byte.
 * @throws IOException If there are not enough bytes to read
 * from the stream.
 * @return The string value corresponding to the bytes read.
 */
public static  String getStringValue(InputStream _ins, int numberOfBytes)
throws IOException {
    byte[] buffer = new byte[numberOfBytes];
    char[] charBuffer = new char[numberOfBytes];
    _ins.read(buffer, 0, numberOfBytes);
    for (int i = 0; i < buffer.length; i++) {
        charBuffer[i] = (char) buffer[i];
    }
    return String.valueOf(charBuffer);
}
public static  String getStringValue(byte[] buffer) {
    char[] charBuffer = new char[buffer.length];
    for (int i = 0; i < buffer.length; i++) {
        charBuffer[i] = (char) buffer[i];
    }
    return String.valueOf(charBuffer);
}
/**
 * Returns the integer (int) value of the given set of bytes.
 * @param numberOfBytes Number of bytes to read to get
 * the full integer value. An integer has an length of 2 or 4 bytes.
 * @throws IOException If there are not enough bytes to read
 * from the stream.
 * @return The integer value corresponding to the bytes read.
 */
public static int getIntValue(InputStream _ins, int numberOfBytes)
throws IOException {
    byte[] buffer = new byte[numberOfBytes];
    _ins.read(buffer, 0, numberOfBytes);
    ByteBuffer myBuffer = ByteBuffer.wrap(buffer);
    myBuffer.order(ByteOrder.nativeOrder());
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < buffer.length; i ++){
    	int value = unsignedByteToInt(buffer[i]);
    	
    	s.append( padzerotoleft(new StringBuffer(Integer.toBinaryString(value)),8));
    }
    BigInteger bi = new BigInteger(s.toString(), 2);
    int ivalue = bi.intValue();
    return ivalue;
}
public static int getIntValue (byte[]b, int bytePointer, int numByteToRead) {
	byte[]buffer = new byte[numByteToRead];
	int pos = 0;
	for (int i = bytePointer; i < bytePointer + numByteToRead; i++){
		buffer[pos]= b[i];		
		pos = pos + 1;
	}	
    ByteBuffer myBuffer = ByteBuffer.wrap(buffer);
    myBuffer.order(ByteOrder.nativeOrder());
    
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < buffer.length; i ++){
    	int value = unsignedByteToInt(buffer[i]);    	
    	s.append(padzerotoleft(new StringBuffer(Integer.toBinaryString(value)),8));
    }   
    BigInteger bi = new BigInteger(s.toString(), 2);
    int ivalue = bi.intValue();
    
    return ivalue;
}

/**
 * 
 * @param _ins
 * @param _numberOfBytes
 * @throws IOException
 */
public static void skipBytes(InputStream _ins, int _numberOfBytes) throws IOException {
    byte[] buffer = new byte[_numberOfBytes];
    _ins.read(buffer);
    buffer = null;
}
public static String getBinaryString(InputStream _ins, int numberOfBytes)
throws IOException {
    byte[] buffer = new byte[numberOfBytes];
    _ins.read(buffer, 0, numberOfBytes);
    ByteBuffer myBuffer = ByteBuffer.wrap(buffer);
    myBuffer.order(ByteOrder.nativeOrder());
    StringBuffer s = new StringBuffer();
    for (int i = 0; i <buffer.length; i++){
    	int value = unsignedByteToInt(buffer[i]);
            s.append(padzerotoleft(new StringBuffer(Integer.toBinaryString(value)),8));
    }
    return s.toString();
}
/**
 * Convert an unsigned byte to an integer by masking the sign bit, cast to
 * an integer and process the masked bit if needed.
 * @param b Unsigned byte to convert to integer.
 * @return Return integer value of unsigned byte.
 */
public static int unsignedByteToInt(byte b) {	
  return (int) b & 0xFF;
  }
public static String decodeFXY(String binary){
	int ilen = 0;
	int lenOfRemainder = 16;
	String desc = new String();
	while ( lenOfRemainder >= 16 ){
		String s = binary.substring(ilen, ilen +16);
		int f = getInteger(s.substring(ilen, ilen +2));
		int x = getInteger(s.substring(ilen+2, ilen + 8));
		int y = getInteger(s.substring(ilen+8, ilen+16));
		NumberFormat xformat = new DecimalFormat("00");
		NumberFormat yformat = new DecimalFormat("000");
		String fxy = Integer.toString(f)
						.concat(xformat.format(x))
						.concat(yformat.format(y));
		desc = fxy;
		lenOfRemainder = binary.length()- (ilen + 16);
		ilen = ilen + 16;
	}
	return desc;
	
}
public static String customFormat(String pattern, double value){
	DecimalFormat myFormatter = new DecimalFormat(pattern);
	return myFormatter.format(value);
}
/**
 * 
 * @param inputS -input string to look for in the array
 * @param stringArray 
 * @return
 */
public static int findStringPosInArray(String inputS, String[] stringArray) {
	int pos = -1;
	for (int k = 0; k < stringArray.length; k++ ){
		if (inputS.compareToIgnoreCase(stringArray[k])== 0){
			pos = new Integer(k);		

			return pos;
		}
	}
	
	return pos;
}


// end of Utility function
}


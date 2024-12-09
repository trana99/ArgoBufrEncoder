Encode Argo Bufr sequences

This Java project will encode the Argo data into Binary Universal Form 
for the Representation of Meteorological Data (BUFR) for the Global Telecommunication System (GTS).
The program will require Argo NetCDF input files and a  BUFR template for respective data types.  
To run the program, it will require: 
- input.properties file (please see the example of input.properties) to set up your input/output directory
- log4j2.properties to output the log information when the program runs
- Java compiler version 1.8

Below is an example of how to run the program in the Command Prompt window.  However, you need to change the
directory to where your program's location is. 

%JRE_HOME%\bin\java" -jar -Dlog4j.configurationFile=file:///"C:\Users\location of your log4j2.properties\log4j2.properties" "C:\Users\location of your jar file\writeArgoBufr.jar" "C:\Users\location of your jar file\\input.properties"

To verify your BUFR message output to see if it's successfully encoded or not,  you could use this application:
https://codes.ecmwf.int/bufr/validator


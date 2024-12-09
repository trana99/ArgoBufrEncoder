Encode Argo Bufr sequences
This is a Java project that will encode the Argo data into Binary Universal Form 
for the Representation of meteorological data (BUFR) for Global Telecommunication System (GTS).
The program will required Argo Netcdf input files,  BUFR template for respected data types.  
In order to run the program, it will required: 
- input.properties file (please see the example of input.properties) to set up your input/output directory
- log4j2.properties to output the log information when the program runs
- Java compiler version 1.8

Below is an example of how to run the program in command prompt window.  However you will need to change the
directory to where your program located. 
%JRE_HOME%\bin\java" -jar -Dlog4j.configurationFile=file:///"C:\Users\location of your log4j2.properties\log4j2.properties" "C:\Users\location of your jar file\writeArgoBufr.jar" "C:\Users\location of your jar file\\input.properties"

To verify your BUFR message output to see if it's successfully encoded or not,  you could used this application:
https://codes.ecmwf.int/bufr/validator


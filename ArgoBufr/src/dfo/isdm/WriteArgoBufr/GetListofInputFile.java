/*
 * Created on 19-Jul-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package dfo.isdm.WriteArgoBufr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * the GetListofInputFile class
 * @author Tran 1-Aug-06
 */
public class GetListofInputFile {
    /**
     * curent directory
     */
    private String currentdir;

    /**
     * the file contain the list of filenames
     */
    private String outputfilename;

    private String fileExtensionToGet;
    private Logger log;

    /**
     * GetListofInputFile()
     */
    public GetListofInputFile() {
        currentdir = System.getProperty("user.dir");
        outputfilename = "ncfilelist.txt";
        fileExtensionToGet = ".nc";
    }
    public GetListofInputFile (String _directory, String _fileExtension, Logger _log){
    	currentdir = _directory;
    	fileExtensionToGet = _fileExtension;
    	log = _log;
    }

    /**
     * @param directory name
     * @param outfilename name of file contain the list of files request
     * @param fileExtension Extension of file requested to put into list (e.g. ".nc")
     * @param action Create the list of file for convert to BUFR, 
     * BUFRToOcproc, NetCDFToOcproc, OcprocToNetCDF
     * For BUFR format related:  Action = BUFR,
     * for Netcdf format related: Action = NetCDF 
     * @throws IOException
     */
    public GetListofInputFile(String directory, String outfilename,
            String fileExtension, String action) throws IOException {
        currentdir = directory;
        outputfilename = outfilename;
             BufferedWriter out = new BufferedWriter(new FileWriter(outputfilename));
        String[] fileExtensions = fileExtension.split(",");
   //     System.out.println(fileExtension + fileExtensions.length);
        for (int i = 0; i < fileExtensions.length; i++ ){         
            fileExtensionToGet = fileExtensions[i];
        try {
            if (action.compareToIgnoreCase("BUFR") == 0 ){
                getListOfFilesForBUFR();
            } else if (action.compareToIgnoreCase("NetCDF") == 0) {
                getNcfileListForNetCDF();
            }
        } catch (IOException e) {
            System.out
                    .println("Can't find files.  Error in GetListOfInputFile");
            e.printStackTrace();
        }
        }
        out.close();

    }

    /**
     * @return BufferedWriter
     * @throws IOException if file can't create
     */
    public List <String> getListOfFilesForBUFR() throws IOException {
        File dir = new File(currentdir);
        String[] children = dir.list();
        List <String> ncfiles = new ArrayList<String>();
        if (children == null) {
            log.info(" There is no file in this directory" + dir);
        } else {
            for (int i = 0; i < children.length; i++) {
                // Get filename of file or directory
                String filename = children[i];
                if (filename.indexOf(fileExtensionToGet) > 0){
                	if (filename.startsWith("R")
                			|| filename.startsWith("BR")
                			|| filename.startsWith("D")
                			|| filename.startsWith("BD")){
                		ncfiles.add(filename);
                		
                	}
                }               
            }
        }
        return ncfiles;
    }

    /**
     * @return list of netcdfile need to convert to ocproc
     * @throws IOException if file not found
     */
    public BufferedWriter getNcfileListForNetCDF() throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(outputfilename));
        File dir = new File(currentdir);
        String[] children = dir.list();
    //    System.out.println("Currently at " + dir);
        if (children == null) {
            System.out.println(" There is no file in this directory" + dir);
        } else {
            for (int i = 0; i < children.length; i++) {
                // Get filename of file or directory
                String filename = children[i];
                String[] names = filename.split("_");
                if (accept(dir, filename)) {
                    if (!isTechnical(dir, filename)) {
                        out.write("read_ocproc -u " + filename + " "
                                + names[0].substring(1)
                                + "_tech.nc PROFWORK.OUT calibwork.out" + '\n');
                    }
                }
            }
        }
        out.close();
        return out;
    }

    /**
     * @param dir current directory
     * @param name filename
     * @return boolean
     */
    public boolean accept(File dir, String name) {
        boolean include = false;
        if (name.indexOf(fileExtensionToGet)>0) {
            //System.out.println("FileExtension" + fileExtensionToGet);
            include = true;
        }
        return include;
    }

    /**
     * @param dir name
     * @param name filename
     * @return boolean true if it is a technical file
     */
    public boolean isTechnical(File dir, String name) {
        boolean tech = false;
        if (name.endsWith(".nc")) {
            String[] names = name.split("_");
            for (int i = 0; i < names.length; i++) {
                if (names[i].compareToIgnoreCase("tech") == 0) {
                    tech = true;
                }
            }
        }
        return tech;
    }
    public boolean isNetCDF(String _fileName) {
        boolean isNetCDF = false;
        if (_fileName.endsWith(".nc")){
            isNetCDF = true;
        }
        return isNetCDF;        
    }
}

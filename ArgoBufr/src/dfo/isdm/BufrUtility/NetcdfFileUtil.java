package dfo.isdm.BufrUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class NetcdfFileUtil extends NetcdfFile {
    private static Logger log;

    public NetcdfFileUtil() {
        super();
       
    }
    public NetcdfFileWriter deleteNetCdfVariable( String inputFile,List<String> delVariableName,  Logger log ) throws IOException {
        String tempfilename = new String (inputFile.substring(0, inputFile.indexOf(".")) +"_new.nc");
        NetcdfFile ncfile = NetcdfFile.open(inputFile);
        Map<Variable, Variable> varMap = new HashMap<Variable, Variable>(); // oldVar, newVar
        List <Variable> varList = new ArrayList<Variable>();

        NetcdfFileWriter newnc =
        		NetcdfFileWriter.createNew(NetcdfFileWriter.Version.valueOf("netcdf3"),tempfilename);
        newnc.setFill(true);
        for (Attribute att: ncfile.getGlobalAttributes()){
        	newnc.addGroupAttribute(null, att);
        }
        // dimension
        Map<String, Dimension> dimHash = new HashMap<String, Dimension>();
        for (Dimension oldD : ncfile.getDimensions()){
    		Dimension newD = newnc.addDimension(null, oldD.getShortName(), oldD.getLength(), true, oldD.isUnlimited(),oldD.isVariableLength());
    		dimHash.put(oldD.getShortName(), newD);
        }
        // Variables
        int anonCount = 0;
        for (Variable oldVar : ncfile.getVariables()) {
            List<Dimension> dims = new ArrayList<Dimension>();
            for (Dimension oldD : oldVar.getDimensions()) {
              if (!oldD.isShared()) { // netcdf3 dimensions must be shared
                String anonName = "anon" + anonCount;
                anonCount++;
                Dimension newD = newnc.addDimension(null, anonName, oldD.getLength());
                dims.add(newD);

              } else {
                Dimension dim = dimHash.get(oldD.getShortName());
                if (dim != null)
                  dims.add(dim);
                else
                  throw new IllegalStateException("Unknown dimension= " + oldD.getShortName());
              }
            }

            DataType newType = oldVar.getDataType();

            // convert STRING to CHAR
            if (oldVar.getDataType() == DataType.STRING) {
              Array data = oldVar.read();
              IndexIterator ii = data.getIndexIterator();
              int max_len = 0;
              while (ii.hasNext()) {
                String s = (String) ii.getObjectNext();
                max_len = Math.max(max_len, s.length());
              }

              // add last dimension
              String useName = oldVar.getShortName() + "_strlen";
              Dimension newD = newnc.addDimension(null, useName, max_len);
              dims.add(newD);

              newType = DataType.CHAR;
            }
            if (!delVariableName.contains(oldVar.getShortName())){
           // if (oldVar.getShortName().indexOf(delVariableName) < 0){
                Variable v = newnc.addVariable(null, oldVar.getShortName(), newType, dims);
                varMap.put(oldVar, v);
                varList.add(oldVar);

                // attributes
                for (Attribute att : oldVar.getAttributes()) {
                  newnc.addVariableAttribute(v, att); // atts are immutable
                }

            } else {
            	//System.out.println("Delete this variable: " + oldVar.getShortName());
            }
          }

        try {
            newnc.create();
            newnc = copydata(ncfile, newnc);
            newnc.flush();
            newnc.close();
            ncfile.close();
            File oldfile = new File (inputFile);
            File file3 = new File(tempfilename);
            copy(file3, oldfile);
            file3.delete();

        } catch (IOException e) {
            log.error("Error in delete variable" +delVariableName);
            e.printStackTrace();
        }
        
      return newnc;
    }
    
    /**
     * @param src file to dst file
     * @param dst if dst file doesn't exist, it is created
     * @throws IOException if file not found
     */
    public void copy(File src, File dst) throws IOException {
         InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dst);
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
     }

    /**
     * @param ncfile old NetCDF file
     * @param dimNameToChange dimension of the parameter needs change
     * @param newsize new NetCDF file
     * @param outfile output file name
     * @throws IOException
     */
    public void resizeDimension(
    	String inputFile,
        String dimNameToChange,
        int newsize , Logger log) {
        
    String tempfilename = new String (inputFile.substring(0, inputFile.indexOf(".")) +"_new.nc");
    //System.out.println(tempfilename);
    Map<Variable, Variable> varMap = new HashMap<Variable, Variable>(); // oldVar, newVar
    List <Variable> varList = new ArrayList<Variable>();
    
       try {
    	   NetcdfFile ncfile = NetcdfFile.open(inputFile);
            NetcdfFileWriter newnc =
                NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3,tempfilename);
            for (Attribute att : ncfile.getGlobalAttributes()){
                newnc.addGroupAttribute(null, att);
            }
            //dimension
            Map <String, Dimension> dimHash = new HashMap<String, Dimension>();
            for (Dimension oldD : ncfile.getDimensions()){
            	if (dimNameToChange.compareToIgnoreCase(oldD.getShortName())== 0){
            		//System.out.println("anh:" + dimNameToChange + " " + oldD.getShortName());
            		Dimension newD = newnc.addDimension(null,oldD.getShortName(), newsize, true, oldD.isUnlimited(), oldD.isVariableLength());
            		dimHash.put(oldD.getShortName(), newD);
            	} else {
            		Dimension newD = newnc.addDimension(null, oldD.getShortName(), oldD.getLength(), true, oldD.isUnlimited(),oldD.isVariableLength());
            		dimHash.put(oldD.getShortName(), newD);
            	}
            }
            // Variables
            int anonCount = 0;
            for (Variable oldVar : ncfile.getVariables()) {
                List<Dimension> dims = new ArrayList<Dimension>();
                for (Dimension oldD : oldVar.getDimensions()) {
                  if (!oldD.isShared()) { // netcdf3 dimensions must be shared
                    String anonName = "anon" + anonCount;
                    anonCount++;
                    Dimension newD = newnc.addDimension(null, anonName, oldD.getLength());
                    dims.add(newD);

                  } else {
                    Dimension dim = dimHash.get(oldD.getShortName());
                    if (dim != null)
                      dims.add(dim);
                    else
                      throw new IllegalStateException("Unknown dimension= " + oldD.getShortName());
                  }
                }

                DataType newType = oldVar.getDataType();

                // convert STRING to CHAR
                if (oldVar.getDataType() == DataType.STRING) {
                  Array data = oldVar.read();
                  IndexIterator ii = data.getIndexIterator();
                  int max_len = 0;
                  while (ii.hasNext()) {
                    String s = (String) ii.getObjectNext();
                    max_len = Math.max(max_len, s.length());
                  }

                  // add last dimension
                  String useName = oldVar.getShortName() + "_strlen";
                  Dimension newD = newnc.addDimension(null, useName, max_len);
                  dims.add(newD);
                  newType = DataType.CHAR;
                }

                Variable v = newnc.addVariable(null, oldVar.getShortName(), newType, dims);
                varMap.put(oldVar, v);
                varList.add(oldVar);

                // attributes
                for (Attribute att : oldVar.getAttributes()) {
                  newnc.addVariableAttribute(v, att); // atts are immutable
                }
              }
            
            //putGlobalAttr (ncfile, newnc);
            newnc.create();
            newnc = copydata(ncfile, newnc);
            newnc.flush();
            newnc.close();
            ncfile.close();            
            File oldfile = new File (inputFile);
            File file3 = new File(tempfilename);
          /*  boolean success = (new File(outfile)).delete();
            if (!success){
                log.info("can not delete file " + outfile);
            } */
            //copy (file3, oldfile);
            // can't use rename file because sometime it works,
            // sometime it won't. NO IDEA
            copy(file3,oldfile);
            
            if (file3.delete()){
                return;
            }else if (file3.exists()){
                throw new IOException ("file still even exist" + file3);
            }else if (!oldfile.canWrite()){
                throw new IOException("Has no write permission " + oldfile);
            } 
           
        } catch (IOException e) {
            log.error("Error in create file " + tempfilename);
            e.printStackTrace();
        }
//      Add variable and attribute to the new Netcdf file
//      Add global attribute.  But it seem like you don't need to add it to the new
//      ncfile.  It automatically generated
     


     }
	public void generateBArgoFile(List<String> makeBFile, String parameterArrayName, Logger log) {
		
		for (int ifile=0; ifile <makeBFile.size(); ifile++){
			String mFileName = makeBFile.get(ifile);
			log.info("generate b file for:  " + mFileName);
			File f1 = new File(mFileName);
			String root = f1.getParent();
			String fileName = f1.getName();
			String [] s = fileName.split("_");
			String bFileName ="";
			String coredFileName ="";

			boolean traj = false;
			if (fileName.contains("traj.nc")){
				bFileName = root + "\\"+ s[0] + "_B" + s[1].substring(1);
				coredFileName = root + "\\" + s[0] + "_" + s[1].substring(1);
				traj = true;				
				log.info(bFileName);
			}else {
				bFileName = root + "\\BR" + fileName.substring(2);
				coredFileName = root + "\\" + fileName.substring(1);
			}
			try {
				copy(new File(mFileName), new File(bFileName));
				copy( new File (mFileName), new File (coredFileName));
				NetcdfFileWriter ncfile = NetcdfFileWriter.openExisting(coredFileName);
				List<List<String>> n_cored_parameters = new ArrayList<List<String>>();
				List<List<String>> n_b_parameters_data_mode = new ArrayList<List<String>>();
				List<List<String>> n_b_parameters = new ArrayList<List<String>>();
				List <String>calib_parm = new ArrayList<String>();
				List <String>calib_equ = new ArrayList<String>();
				List <String> calib_coef = new ArrayList<String>();
				List<String> calib_comment = new ArrayList<String>();
				List<String> calib_date = new ArrayList<String>();
				List <String> traj_cored_parameters = new ArrayList<String>();
				List <String> traj_b_parameters = new ArrayList<String>();
				int cored_nparam = 0;
				int bphase_nparam = 0;
				if (traj){
					ArrayChar.D2 traj_parameters = Utility.getCharD2Array(ncfile.getNetcdfFile(),parameterArrayName, log);
					int [] shape = traj_parameters.getShape();
					for (int i = 0; i < shape[0]; i ++){
						String parm = traj_parameters.getString(i).trim();
						if (parm.compareToIgnoreCase("PRES")==0
								||parm.compareToIgnoreCase("TEMP")==0
								||parm.compareToIgnoreCase("PSAL")==0
								|| parm.compareToIgnoreCase("CNDC")==0){
							traj_cored_parameters.add(parm);
						} else {
							traj_b_parameters.add(parm);
						}
										
					}
					Map <String, String> variableToResize = new HashMap <String, String>();
					variableToResize.put("DATA_TYPE", "STRING16,STRING32");
					variableToResize.put("TRAJECTORY_PARAMETERS", "STRING16,STRING64");
					variableToResize.put("HISTORY_PARAMETER", "STRING16,STRING64");
					resizeVariableLength(bFileName,variableToResize, log);
					resizeDimension(coredFileName, "N_PARAM", traj_cored_parameters.size(), log);
					resizeDimension(bFileName, "N_PARAM", traj_b_parameters.size(), log);


				} else { // for profile netcdf
					List<List<String>> parameters = Utility.getCharD3Array_List(ncfile.getNetcdfFile(),parameterArrayName, log);
					calib_parm = Utility.getCharD4Array_List(ncfile.getNetcdfFile(), "PARAMETER", log);
					calib_equ = Utility.getCharD4Array_List(ncfile.getNetcdfFile(), "SCIENTIFIC_CALIB_EQUATION", log);
					calib_coef = Utility.getCharD4Array_List(ncfile.getNetcdfFile(), "SCIENTIFIC_CALIB_COEFFICIENT", log);
					calib_comment = Utility.getCharD4Array_List(ncfile.getNetcdfFile(), "SCIENTIFIC_CALIB_COMMENT", log);
					calib_date = Utility.getCharD4Array_List(ncfile.getNetcdfFile(), "SCIENTIFIC_CALIB_DATE", log);
					ArrayChar.D2 m_parameter_data_code = Utility.getCharD2Array(ncfile.getNetcdfFile(),"PARAMETER_DATA_MODE", log);
					for (int i = 0; i < parameters.size(); i ++){
						List<String> nprof_parm = parameters.get(i);
						List<String>cored_parameters = new ArrayList<String>();
						List <String> b_parameters = new ArrayList<String>();
						List <String> b_parameters_data_mode = new ArrayList<String>();
						for (int j = 0; j < nprof_parm.size(); j++){
							String parm = nprof_parm.get(j).trim();
							if (parm.compareToIgnoreCase("PRES")==0){
								cored_parameters.add(parm);
							}
							if (parm.compareToIgnoreCase("TEMP") == 0
								|| parm.compareToIgnoreCase("PSAL") ==0
								|| parm.compareToIgnoreCase("CNDC") == 0){
								cored_parameters.add(parm);	
							} else {
								b_parameters.add(parm);
								if (parm.compareToIgnoreCase("PRES")==0){
									boolean presadjusted = Utility.checkFloatFillArrayValue(ncfile.getNetcdfFile(), "PRES_ADJUSTED");
									if (presadjusted){
										b_parameters_data_mode.add("A");
									} else {
										b_parameters_data_mode.add("R");
									}
									
								} else {
									b_parameters_data_mode.add(String.valueOf(m_parameter_data_code.get(i, j)));								
								}				
							}
						}
						if(cored_parameters.size() > cored_nparam) {
							cored_nparam = cored_parameters.size();
						}
						if (b_parameters.size() > bphase_nparam){
							bphase_nparam = b_parameters.size();
						}
						n_cored_parameters.add(cored_parameters);
						n_b_parameters_data_mode.add(b_parameters_data_mode);
						n_b_parameters.add(b_parameters);
					}
				
				resizeDimension(coredFileName, "N_PARAM", cored_nparam, log);
				resizeDimension(bFileName, "N_PARAM", bphase_nparam, log);
				Map <String, String> variableToResize = new HashMap <String, String>();
				variableToResize.put("DATA_TYPE", "STRING16,STRING32");
				variableToResize.put("STATION_PARAMETERS", "STRING16,STRING64");
				variableToResize.put("TRAJECTORY_PARAMETERS", "STRING16,STRING64");
				variableToResize.put("PARAMETER", "STRING16,STRING64");
				variableToResize.put("HISTORY_PARAMETER", "STRING16,STRING64");
				resizeVariableLength(bFileName,variableToResize, log);

				}			
				List<String> parmToDelete = new ArrayList<String>();
				for (int i = 0 ; i < n_cored_parameters.size(); i++) {
					List<String> prof_param = n_cored_parameters.get(i);
					for (int j =0; j < prof_param.size();j++ ){
						String parm = prof_param.get(j);
						if (parm.compareToIgnoreCase("PRES")!=0){
							parmToDelete.add(parm);
							parmToDelete.add(parm.concat("_QC"));
							parmToDelete.add(parm.concat("_ADJUSTED"));
							parmToDelete.add(parm.concat("_ADJUSTED_QC"));
							parmToDelete.add(parm.concat("_ADJUSTED_ERROR"));
							parmToDelete.add("PROFILE_".concat(parm).concat("_QC"));
						} else {
							parmToDelete.add(parm.concat("_QC"));
							parmToDelete.add(parm.concat("_ADJUSTED"));
							parmToDelete.add(parm.concat("_ADJUSTED_QC"));
							parmToDelete.add(parm.concat("_ADJUSTED_ERROR"));
							parmToDelete.add("PROFILE_".concat(parm).concat("_QC"));
						}

					}
				}
				for (int i = 0 ; i < traj_cored_parameters.size(); i++) {
						String parm = traj_cored_parameters.get(i);
						if (parm.compareToIgnoreCase("PRES")!=0){
							if (!parmToDelete.contains(parm)){
								parmToDelete.add(parm);
								parmToDelete.add(parm.concat("_QC"));
								parmToDelete.add(parm.concat("_ADJUSTED"));
								parmToDelete.add(parm.concat("_ADJUSTED_QC"));
								parmToDelete.add(parm.concat("_ADJUSTED_ERROR"));
								parmToDelete.add("PROFILE_".concat(parm).concat("_QC"));
							}
						} else {
							if (!parmToDelete.contains(parm.concat("_QC"))){
								parmToDelete.add(parm.concat("_QC"));
								parmToDelete.add(parm.concat("_ADJUSTED"));
								parmToDelete.add(parm.concat("_ADJUSTED_QC"));
								parmToDelete.add(parm.concat("_ADJUSTED_ERROR"));
								parmToDelete.add("PROFILE_".concat(parm).concat("_QC"));

							}
						}
				}

				if (traj){
					parmToDelete.add("JULD_ADJUSTED");
					parmToDelete.add("JULD_ADJUSTED_STATUS");
					parmToDelete.add("JULD_ADJUSTED_QC");
					parmToDelete.add("CYCLE_NUMBER_ADJUSTED");
					parmToDelete.add("JULD_DESCENT_START");
					parmToDelete.add("JULD_DESCENT_START_STATUS");
					parmToDelete.add("JULD_FIRST_STABILIZATION_STATUS");
					parmToDelete.add("JULD_FIRST_STABILIZATION");
					parmToDelete.add("JULD_DESCENT_END");
					parmToDelete.add("JULD_DESCENT_END_STATUS");
					parmToDelete.add("JULD_PARK_START");
					parmToDelete.add("JULD_PARK_START_STATUS");
					parmToDelete.add("JULD_PARK_END");
					parmToDelete.add("JULD_PARK_END_STATUS");
					parmToDelete.add("JULD_DEEP_PARK_START");
					parmToDelete.add("JULD_DEEP_PARK_START_STATUS");
					parmToDelete.add("JULD_ASCENT_START");
					parmToDelete.add("JULD_ASCENT_START_STATUS");
					parmToDelete.add("JULD_DEEP_ASCENT_START");
					parmToDelete.add("JULD_DEEP_ASCENT_START_STATUS");
					parmToDelete.add("JULD_ASCENT_END");
					parmToDelete.add("JULD_ASCENT_END_STATUS");
					parmToDelete.add("JULD_DEEP_DESCENT_START");
					parmToDelete.add("JULD_DEEP_DESCENT_START_STATUS");
					parmToDelete.add("JULD_DEEP_DESCENT_END");
					parmToDelete.add("JULD_DEEP_DESCENT_END_STATUS");	
					parmToDelete.add("JULD_TRANSMISSION_START");
					parmToDelete.add("JULD_TRANSMISSION_START_STATUS");
					parmToDelete.add("JULD_FIRST_MESSAGE");
					parmToDelete.add("JULD_FIRST_MESSAGE_STATUS");
					parmToDelete.add("JULD_FIRST_LOCATION");
					parmToDelete.add("JULD_FIRST_LOCATION_STATUS");
					parmToDelete.add("JULD_LAST_LOCATION");
					parmToDelete.add("JULD_LAST_LOCATION_STATUS");
					parmToDelete.add("JULD_LAST_MESSAGE");
					parmToDelete.add("JULD_LAST_MESSAGE_STATUS");
					parmToDelete.add("JULD_TRANSMISSION_END");
					parmToDelete.add("JULD_TRANSMISSION_END_STATUS");
					parmToDelete.add("CLOCK_OFFSET");
					parmToDelete.add("GROUNDED");
					parmToDelete.add("REPRESENTATIVE_PARK_PRESSURE");
					parmToDelete.add("REPRESENTATIVE_PARK_PRESSURE_STATUS");
					parmToDelete.add("CYCLE_NUMBER_INDEX_ADJUSTED");
				}

				deleteNetCdfVariable(bFileName, parmToDelete, log);
				parmToDelete = new ArrayList<String>();
				for (int i = 0 ; i < n_b_parameters.size(); i++) {
					for (int j= 0; j < n_b_parameters.get(i).size();j++){
					String parm = n_b_parameters.get(i).get(j);
					if (parm.compareToIgnoreCase("PRES")!=0){
						parmToDelete.add(parm);
						parmToDelete.add(parm.concat("_QC"));
						parmToDelete.add(parm.concat("_ADJUSTED"));
						parmToDelete.add(parm.concat("_ADJUSTED_QC"));
						parmToDelete.add(parm.concat("_ADJUSTED_ERROR"));
						parmToDelete.add("PROFILE_".concat(parm).concat("_QC"));

					} 
					}
				}
				for (int i = 0 ; i < traj_b_parameters.size(); i++) {
					String parm = traj_b_parameters.get(i);
					if (parm.compareToIgnoreCase("PRES")!=0){
						if (!parmToDelete.contains(parm)){
							parmToDelete.add(parm);
							parmToDelete.add(parm.concat("_QC"));
							parmToDelete.add(parm.concat("_ADJUSTED"));
							parmToDelete.add(parm.concat("_ADJUSTED_QC"));
							parmToDelete.add(parm.concat("_ADJUSTED_ERROR"));
							parmToDelete.add("PROFILE_".concat(parm).concat("_QC"));
						}
					} 
			}

				parmToDelete.add("PARAMETER_DATA_MODE");
				deleteNetCdfVariable(coredFileName, parmToDelete, log);
				NetcdfFileWriter bncfile = NetcdfFileWriter.openExisting(bFileName);
				ncfile = NetcdfFileWriter.openExisting(coredFileName);
				if (traj){
					Utility.writeArrayStringD2(bncfile, parameterArrayName, 0, traj_b_parameters, log);
					Utility.writeString(bncfile, "DATA_TYPE", "B-Argo trajectory", log);
					Utility.writeArrayStringD2(ncfile, parameterArrayName, 0, traj_cored_parameters, log);
				} else {					
			        Utility.writeString(bncfile, "DATA_TYPE", "B-Argo profile", log);
			        for (int i = 0 ;i < n_cored_parameters.size(); i++){
						Utility.writeArrayCharD3(ncfile, parameterArrayName, i, 0, n_cored_parameters.get(i), log);						
			        }
					// set adjusted value to fill value
					for (int i = 0; i <n_b_parameters.size();i ++){
						Utility.writeArrayCharD3(bncfile, parameterArrayName, i,0, n_b_parameters.get(i), log);
						Utility.writeArraySingleCharD2(bncfile, "PARAMETER_DATA_MODE", i,
								0, n_b_parameters_data_mode.get(i), log);
						for (int j = 0; j < n_b_parameters.get(i).size();j++) {	
							if (n_b_parameters.get(i).get(j).compareToIgnoreCase("A")==0){
								Utility.writeCharD1(bncfile, "DATA_MODE", i, "A", log);
							}

							if (n_b_parameters.get(i).get(j).trim().compareToIgnoreCase("pres")!= 0 
									&& n_b_parameters_data_mode.get(i).get(j).compareToIgnoreCase("R")==0){
								//System.out.println("anh = " + n_b_parameters.get(i).get(j) + " =" + n_b_parameters_data_mode.get(i).get(j));

								Utility.setArrayToFillValue(bncfile,
										n_b_parameters.get(i).get(j).concat("_ADJUSTED"), "float", log);
								Utility.setArrayToFillValue(bncfile,
										n_b_parameters.get(i).get(j).concat("_ADJUSTED_ERROR"), "float", log);
								Utility.setArrayToFillValue(bncfile,
										n_b_parameters.get(i).get(j).concat("_ADJUSTED_QC"), "char", log);
							}

						}
					}
					int icore = 0;
					int bpos = 0; 
					int icalib = 0;
					for (int i = 0; i < calib_parm.size(); i++){
						String []s1 = calib_parm.get(i).split(";");
						String parm = s1[2].trim();
						if (Integer.parseInt(s1[1])!= icalib){
							icalib = Integer.parseInt(s1[1]);
							icore = 0;
							bpos = 0;
						}
						if (parm.compareToIgnoreCase("PRES")==0){
							Utility.writeCharD4(ncfile, "PARAMETER", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, s1[2], log);
							Utility.writeCharD4(ncfile, "SCIENTIFIC_CALIB_EQUATION", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, calib_equ.get(i).split(";")[2], log);
							Utility.writeCharD4(ncfile, "SCIENTIFIC_CALIB_COEFFICIENT", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, calib_coef.get(i).split(";")[2], log);
							Utility.writeCharD4(ncfile, "SCIENTIFIC_CALIB_COMMENT", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, calib_comment.get(i).split(";")[2], log);
							Utility.writeCharD4(ncfile, "SCIENTIFIC_CALIB_DATE", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, calib_date.get(i).split(";")[2], log);
							icore++;
							Utility.writeCharD4(bncfile, "PARAMETER", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, s1[2], log);
							Utility.writeCharD4(bncfile, "SCIENTIFIC_CALIB_EQUATION", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, calib_equ.get(i).split(";")[2], log);
							Utility.writeCharD4(bncfile, "SCIENTIFIC_CALIB_COEFFICIENT", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, calib_coef.get(i).split(";")[2], log);
							Utility.writeCharD4(bncfile, "SCIENTIFIC_CALIB_COMMENT", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, calib_comment.get(i).split(";")[2], log);
							Utility.writeCharD4(bncfile, "SCIENTIFIC_CALIB_DATE", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, calib_date.get(i).split(";")[2], log);
							bpos++;							
						} else if (parm.compareToIgnoreCase("TEMP")==0
								|| parm.compareToIgnoreCase("PSAL")==0
								|| parm.compareToIgnoreCase("CNDC")==0){
							Utility.writeCharD4(ncfile, "PARAMETER", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, s1[2], log);
							Utility.writeCharD4(ncfile, "SCIENTIFIC_CALIB_EQUATION", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, calib_equ.get(i).split(";")[2], log);
							Utility.writeCharD4(ncfile, "SCIENTIFIC_CALIB_COEFFICIENT", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, calib_coef.get(i).split(";")[2], log);
							Utility.writeCharD4(ncfile, "SCIENTIFIC_CALIB_COMMENT", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, calib_comment.get(i).split(";")[2], log);
							Utility.writeCharD4(ncfile, "SCIENTIFIC_CALIB_DATE", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), icore, calib_date.get(i).split(";")[2], log);
							icore++;
						} else {
							Utility.writeCharD4(bncfile, "PARAMETER", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, s1[2], log);
							Utility.writeCharD4(bncfile, "SCIENTIFIC_CALIB_EQUATION", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, calib_equ.get(i).split(";")[2], log);
							Utility.writeCharD4(bncfile, "SCIENTIFIC_CALIB_COEFFICIENT", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, calib_coef.get(i).split(";")[2], log);
							Utility.writeCharD4(bncfile, "SCIENTIFIC_CALIB_COMMENT", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, calib_comment.get(i).split(";")[2], log);
							Utility.writeCharD4(bncfile, "SCIENTIFIC_CALIB_DATE", Integer.parseInt(s1[0]),
									Integer.parseInt(s1[1]), bpos, calib_date.get(i).split(";")[2], log);
							
							bpos++;							
							
						}
					}
					
					

				}

				ncfile.close();
				bncfile.close();
				//new File(mFileName).delete();
				
			} catch (IOException e) {
				log.error("Error in generate B file " + e);
			}
			
		}
		
	}

    
    /**
     * @param ncfile old NetCDF file
     * @param dimNameToChange dimension of the parameter needs change
     * @param newsize new NetCDF file
     * @param outfile output file name
     * @throws IOException
     */
    /*
    public void resizeListDimension(
        NetcdfFileWriteable ncfile,
        List<String> ListDimNameToChange,
        List<Integer> ListNewSize,
        String outfile) {
    String tempfilename = new String (outfile +"_.nc");
       try {
            NetcdfFileWriteable newnc =
                NetcdfFileWriteable.createNew(tempfilename, true);
            List<Dimension>  dimension = ncfile.getDimensions();
//             change dimension
            for (int j = 0; j < ListDimNameToChange.size(); j++){
                String dimNameToChange = ListDimNameToChange.get(j);
                int newsize = ListNewSize.get(j);
                for (int k = 0; k < dimension.size(); k++) {
                String[]dimstr = dimension.get(k).toString().split("=");
                Dimension dimname = ncfile.findDimension(dimstr[0].trim());
                String[]dimstrvalue = dimstr[1].split(";");
                    int dimlen = dimname.getLength();
                    if (dimNameToChange.compareToIgnoreCase(dimstr[0].trim()) == 0
                        && dimstrvalue[0].trim().compareToIgnoreCase("UNLIMITED") == 0) {
                        dimname.setLength(newsize);
                        System.out.println(dimname.getName() + " " + dimname.getLength());
                        newnc.addDimension(dimstr[0].trim(), newsize, true, true, false);
                    } else if (dimNameToChange.compareToIgnoreCase(dimstr[0].trim()) != 0
                        && dimstrvalue[0].trim().compareToIgnoreCase("UNLIMITED") == 0) {
                        newnc.addDimension(dimstr[0].trim(), dimlen, true, true, false);
                        //System.out.println(dimname.getName() + " " + dimname.getLength());
                    } else {
                        if (dimNameToChange.compareToIgnoreCase(dimstr[0].trim()) == 0){
                         //   System.out.println("dimension to change " + dimNameToChange + newsize);
                            dimname.setLength(newsize);
                            newnc.addDimension(dimNameToChange, newsize, true, false,false);
                        } else {
                            newnc.addDimension(dimstr[0].trim(), dimlen, true, false, false);
                        }
                    }
                }

            newnc = putVariable (ncfile, newnc,dimNameToChange, newsize);
            }
            for (int j = 0; j < ncfile.getGlobalAttributes().size();j++){
                newnc.addGlobalAttribute(ncfile.getGlobalAttributes().get(j));
            }

            newnc.create();
            newnc = copydata(ncfile, newnc);     
            newnc.close();
            ncfile.close();
            File oldfile = new File (outfile);
            File file3 = new File(tempfilename);
            copy(file3,oldfile);
            if (file3.delete()){
                return;
            }else if (file3.exists()){
                throw new IOException ("file still even exist" + file3);
            }else if (!oldfile.canWrite()){
                throw new IOException("Anh has no write permission " + oldfile);
            }
        } catch (IOException e) {
            log.error("Error in create file " + tempfilename);
            e.printStackTrace();
        }
     }
    */
    /**
     * @param ncfile old netcdf file
     * @param newnc new netcdf file
     * @return netcdf file
     * @throws IOException
     * @throws InvalidRangeException
     */
    public NetcdfFileWriter copydata (
        NetcdfFile ncfile,
        NetcdfFileWriter newnc) {

    try {
        List <Variable> variableList = ncfile.getVariables();
        for (int k = 0; k < variableList.size(); k++) {
        	Variable ncfileVar = variableList.get(k); 
            int numdims = ncfileVar.getDimensions().size();
            DataType varatype = ncfileVar.getDataType();
            // check to make sure that the varialbe exist in the newnc file
            if (newnc.findVariable(ncfileVar.getShortName())!= null){   
            	if (numdims ==0){
                	newnc.write(newnc.findVariable(ncfileVar.getShortName()), ncfileVar.read());
                	
            	} else {
            		if (varatype.toString().compareToIgnoreCase("char") == 0) {
            			if (numdims == 1 ) {
                            ArrayChar.D1 data = Utility.copyCharD1Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
                            newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
            			} else if (numdims == 2) {
            				ArrayChar.D2 data = Utility.copyCharD2Array(
            						ncfile, newnc, ncfileVar.getShortName(), true, log);
            				newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
            			} else if (numdims == 3 ) {
                            ArrayChar.D3 data = Utility.copyCharD3Array(
                            		ncfile, newnc, ncfileVar.getShortName(),true, log);
                            newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);                 
            			} else if (numdims == 4 ) {
            				ArrayChar.D4 data =
                    			Utility.copyCharD4Array(ncfile, newnc,ncfileVar.getShortName(), true, log);
            				newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
            			}
            		}
            		if (varatype.toString().compareToIgnoreCase("int") == 0) {
            			if (numdims == 1 ) {
            				ArrayInt.D1 data =
            						Utility.copyIntD1Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
            				newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
            			} else if (numdims == 2 ) {
            				ArrayInt.D2 data =
            						Utility.copyIntD2Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
            				newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
            			} else if (numdims == 3 ) {
            				ArrayInt.D3 data =
            						Utility.copyIntD3Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
            				newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
            			}
            		}
            		if (varatype.toString().compareToIgnoreCase("double") == 0) {
            			if (numdims == 1 ) {
            				ArrayDouble.D1 data =
            						Utility.copyDoubleD1Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
            				newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
            			} else if (numdims == 2 ) {
            				ArrayDouble.D2 data = (ArrayDouble.D2)
            						Utility.copyDoubleD2Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
                            	newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
            			} else if (numdims == 3) {
            				ArrayDouble.D3 data =
            						Utility.copyDoubleD3Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
                            	newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
            			} 
            		}
            		if (varatype.toString().compareToIgnoreCase("float") == 0) {
                        if (numdims == 1) {
                            ArrayFloat.D1 data =
                         Utility.copyFloatD1Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
                                newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
                        } else if (numdims == 2 ) {
                            ArrayFloat.D2 data = 
                         Utility.copyFloatD2Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
                                newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
                        } else if (numdims == 3 ) {
                            ArrayFloat.D3 data =
                            		Utility.copyFloatD3Array(ncfile, newnc, ncfileVar.getShortName(), true, log);
                                newnc.write(newnc.findVariable(ncfileVar.getShortName()), data);
                        }
                    }
                } 
            	 
            }
            }
    } catch (IOException e) {
        log.error("Error writting file");
    } catch (InvalidRangeException e) {
    	log.error("Invalid range " + e);
        
    }

    return newnc;
    }

    /**
     * @param vara list of short variable name
     * @return HashTable of variable name and its datatype
     */
    public Map <String, String>loadVariable(List<Variable> vara) {
        Map <String, String>varaname = new LinkedHashMap<String,String>();
        String variablename;
        for (int i = 0; i < vara.size(); i++) {
            String[]varstr = vara.get(i).toString().split(":");
            int end = varstr[0].indexOf("(");
            if (end <0){
                end = varstr[0].indexOf(";");
            }
            if (varstr[0].toString().trim().startsWith("float")) {
                int start = varstr[0].indexOf("float") + "float".length() + 1;
                variablename = varstr[0].toString().substring(start, end).trim();
                varaname.put(variablename, "float");
            } else if (varstr[0].toString().trim().startsWith("double")) {
                int start = varstr[0].indexOf("double") + "double".length() + 1;
                variablename = varstr[0].toString().substring(start, end).trim();
                varaname.put(variablename, "DOUBLE");
            } else if (varstr[0].toString().trim().startsWith("char")) {
                int start = varstr[0].indexOf("char") + "char".length() + 1;
                variablename = varstr[0].toString().substring(start, end).trim();
                varaname.put(variablename, "CHAR");
            } else if (varstr[0].toString().trim().startsWith("int")) {
                int start = varstr[0].indexOf("int") + "int".length() + 1;
                variablename = varstr[0].toString().substring(start, end).trim();
                varaname.put(variablename, "INT");
            }
        }
        return varaname;
    }

    /**
     * @param att list of Attribute for that variables
     * @return Hashtable of name and attribute description
     */
    public Map<String, String> loadVariableAtt(List <Attribute> att) {
      Map<String, String> attarray = new LinkedHashMap <String, String>();
    for (int i = 0; i < att.size(); i++) {
          int istart = att.get(i).toString().indexOf("=");
        String name = new String(att.get(i).toString().substring(0, istart));
        String desc =
            new String(
                att.get(i).toString().substring(
                    istart + 1,
                    att.get(i).toString().length()));
          int start =   desc.indexOf('"');
          int end = desc.lastIndexOf('"');
          String value = new String();
        if (start > 0 && desc.substring(start, end).lastIndexOf('"') > 0) {
            end++;
          }
        if (start > 0 && end > 0) {
            value = desc.substring(start + 1, end);
          } else {
              value = desc.trim();
          }
          attarray.put(name.trim(), value);
      }
    return attarray;
    }

    /**
     * @param log 
     * @param ncfile input netcdf file
     * @param newnc the copy of ncfile
     * @return newnc with variable and Attribute
     */
    private NetcdfFileWriter resizeVariableLength(
        String inputFile,        
        Map <String, String> variableNameToChange, Logger log) {
        
    	String tempfilename = new String (inputFile.substring(0, inputFile.indexOf(".")) +"_new.nc");
        Map<Variable, Variable> varMap = new HashMap<Variable, Variable>(); // oldVar, newVar
        List <Variable> varList = new ArrayList<Variable>();
        NetcdfFileWriter newnc = null;
		try {
			NetcdfFile ncfile = NetcdfFile.open(inputFile);
	        newnc =
	        		NetcdfFileWriter.createNew(NetcdfFileWriter.Version.valueOf("netcdf3"),tempfilename);
	        newnc.setFill(true);
	        for (Attribute att: ncfile.getGlobalAttributes()){
	        	newnc.addGroupAttribute(null, att);
	        }
	        // dimension
	        Map<String, Dimension> dimHash = new HashMap<String, Dimension>();
	        for (Dimension oldD : ncfile.getDimensions()){
	    		Dimension newD = newnc.addDimension(null, oldD.getShortName(), oldD.getLength(), true, oldD.isUnlimited(),oldD.isVariableLength());
	    		dimHash.put(oldD.getShortName(), newD);
	        }
	        // Variables
	        int anonCount = 0;
	        for (Variable oldVar : ncfile.getVariables()) {
	            List<Dimension> dims = new ArrayList<Dimension>();
	            for (Dimension oldD : oldVar.getDimensions()) {
	              if (!oldD.isShared()) { // netcdf3 dimensions must be shared
	                String anonName = "anon" + anonCount;
	                anonCount++;
	                Dimension newD = newnc.addDimension(null, anonName, oldD.getLength());
	                dims.add(newD);

	              } else {
	                Dimension dim = dimHash.get(oldD.getShortName());
	                if (dim != null){
	                  dims.add(dim);
	                } else {
	                  throw new IllegalStateException("Unknown dimension= " + oldD.getShortName());
	                }
	              }
	            }

	            DataType newType = oldVar.getDataType();

	            // convert STRING to CHAR
	            if (oldVar.getDataType() == DataType.STRING) {
	              Array data = oldVar.read();
	              IndexIterator ii = data.getIndexIterator();
	              int max_len = 0;
	              while (ii.hasNext()) {
	                String s = (String) ii.getObjectNext();
	                max_len = Math.max(max_len, s.length());
	              }

	              // add last dimension
	              String useName = oldVar.getShortName() + "_strlen";
	              Dimension newD = newnc.addDimension(null, useName, max_len);
	              dims.add(newD);

	              newType = DataType.CHAR;
	            }
	            if (variableNameToChange.containsKey(oldVar.getShortName())){
	            	String[] dimToChange = variableNameToChange.get(oldVar.getShortName()).toString().trim().split(",");
	            	for (int i = 0; i < dims.size(); i++){	            		
	            		if (dims.get(i).getShortName().compareToIgnoreCase(dimToChange[0]) ==0){
	            			Dimension newDim = dimHash.get(dimToChange[1].toUpperCase());
	            			dims.remove(i);
	            			dims.add(i, newDim);
	            		}
	            	}
	            }
	            	Variable  v= newnc.addVariable(null, oldVar.getShortName(), newType, dims);
	            	varMap.put(oldVar, v);
	                varList.add(oldVar);

	                // attributes
	                for (Attribute att : oldVar.getAttributes()) {
	                  newnc.addVariableAttribute(v, att); // atts are immutable
	                }             
	        }
	        newnc.create();
            newnc = copydata(ncfile, newnc);
            newnc.flush();
            newnc.close();
            ncfile.close();
            File oldfile = new File (inputFile);
            File file3 = new File(tempfilename);
            copy(file3, oldfile);
            file3.delete();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      return newnc;
    }
    /**
     * @param ncfile input Netcdf file
     * @param newnc output Netcdf file
     * @return output netcdf file
     */
    /*
    private NetcdfFileWriter putGlobalAttr(
        NetcdfFile ncfile,
        NetcdfFileWriter newnc) {
     List<Attribute> globalatt = ncfile.getGlobalAttributes();
     List <Attribute> newglobalatt = ((NetcdfFile) newnc).getGlobalAttributes();
     Map<String, String> globalattmap = new LinkedHashMap <String, String>();
     Map <String, String>newglobalattmap = new LinkedHashMap <String, String>();
    for (int i = 0; i < globalatt.size(); i++) {
         String []tempstr = globalatt.get(i).toString().split("=");
        int start = tempstr[1].indexOf('"') + 1;
         int end = tempstr[1].lastIndexOf('"');
        String value = new String(tempstr[1].substring(start, end).trim());
         globalattmap.put(tempstr[0].trim(), value);
     }
    for (int i = 0; i < newglobalatt.size(); i++) {
         String []tempstr1 = newglobalatt.get(i).toString().split("=");
        int start = tempstr1[1].indexOf('"') + 1;
         int end = tempstr1[1].lastIndexOf('"');
        String value = new String(tempstr1[1].substring(start, end).trim());
         newglobalattmap.put(tempstr1[0].trim(), value);
     }
     Iterator <String>it = globalattmap.keySet().iterator();
     Iterator <String> it1 = globalattmap.values().iterator();
    while (it.hasNext()) {
         Object key = it.next();
         Object value = it1.next();
        if (newglobalattmap.get(key) == null) {
             newglobalattmap.put(key.toString(), value.toString());
             newnc.addGlobalAttribute(key.toString(), value.toString());
         } 
     }
        return newnc;
    } */

}

/*
 * Created on 27-Jun-06 To change the template for this generated file go
 * to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package dfo.isdm.WriteArgoBufr;

import dfo.isdm.BufrUtility.BufrDescriptorDto;
import dfo.isdm.BufrUtility.BufrTable;
import dfo.isdm.BufrUtility.BufrUtility;
import dfo.isdm.BufrUtility.Sec3NprofMapDTO;
import dfo.isdm.BufrUtility.Utility;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.mbertoli.jfep.Parser;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayChar.D2;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.ma2.ArrayFloat;

/** This class will encode the section 4 of the BUFR message and produce a list of sequences to encode in section 3 of the BUFR message
 * @author Tran 
 * Jan 2022, Tran update the getArgoBufrSequences method to include the associated n_prof to be used for a given sequence
 * For sequence 3 15 003, the program will only used n_prof equals 1.  For DOXY data, the program won't encode near surface doxy sampling.
 */
public class ArgoBufrSection4 {
    private static Logger log = Logger.getLogger(WriteArgoBufr.class);
    private  List<Sec3NprofMapDTO> sec3Sequences = new ArrayList <Sec3NprofMapDTO>();
    private static int icoredProf;
    private static String coredFileName;
    private static String BFileName;;
    private static BufrTable bufrTable;
    private static StringBuffer sbuf4;
    private List<String> emptySequences = new ArrayList<String>();
    

    /**
     * constructor for ArgoBufrSection4()
     */
    public ArgoBufrSection4(String _ncCoreFile, String _ncBFile, boolean _auxData, BufrTable _bufrTable) {
    	coredFileName = _ncCoreFile;
    	BFileName = _ncBFile;
    	bufrTable = _bufrTable;
    	sec3Sequences = new ArrayList<Sec3NprofMapDTO>();
    	getArgoBufrSequences(_auxData);
    	sbuf4 = new StringBuffer();
    	for (int i = 0; i < sec3Sequences.size(); i++){
    		if (sec3Sequences.get(i).getDescriptorId().compareToIgnoreCase("315003")==0){  	
    			sbuf4.append(encode_315003());    			
    		}else  {
    			log.info("Encode section " + sec3Sequences.get(i).getDescriptorId());
    			StringBuffer nonCoredData = encode_nonCoredData(sec3Sequences.get(i));
    			sbuf4.append(nonCoredData);
    		} 
    	}
    	if (emptySequences.size()>0){
    		sec3Sequences.removeAll(emptySequences);
    	}
    	log.info("Section 4 length without zeros padding" + sbuf4.length()/8 + " octets" + " and" + sbuf4.length() + " bits");
    }

    public List<Sec3NprofMapDTO> getSec3Sequences() {
		return sec3Sequences;
	}

	public StringBuffer getSbuf4() {
		return sbuf4;
	}

	private StringBuffer encode_nonCoredData(Sec3NprofMapDTO _parentDescriptor) {
		List<BufrDescriptorDto> childDesc = bufrTable.getChildSubset(_parentDescriptor.getDescriptorId());
		StringBuffer sbuf = new StringBuffer();
		BufrDescriptorDto dto;
		BufrDescriptorDto delayedReplicatorDto = null;
		NetcdfFile ncfile;
		NetcdfFile ncIcored = null;
		boolean bgc = false;
		int n_prof = _parentDescriptor.getNprof();
     //   DecimalFormat presFormat = new DecimalFormat("#####.##");
      //  DecimalFormat nonPresFormat = new DecimalFormat("#####.000");
        DecimalFormat bufrnumFormat = new DecimalFormat("#");
		List<String>profilesValues= new ArrayList<String>();
		int ibgcprof=-1;
		// check to see if there is any descriptor operators and change the data width and scale if applicable
		List<BufrDescriptorDto> modified_childDesc = modifiedDescriptorValue(childDesc);
		childDesc = modified_childDesc;
		try {
    		if (_parentDescriptor.getDescriptorId().compareTo("306044")== 0 
    				||_parentDescriptor.getDescriptorId().compareTo("306045")== 0
    				||_parentDescriptor.getDescriptorId().compareTo("306046")== 0
    				||_parentDescriptor.getDescriptorId().compareTo("306047")== 0
    				|| _parentDescriptor.getDescriptorId().compareToIgnoreCase("306048")==0) {
    			if (BFileName == null){
    				BFileName = coredFileName;
    			}
    			bgc = true;
    			log.info("working on file " + BFileName);
				ncfile = NetcdfFile.open(BFileName);
				ncIcored = NetcdfFile.open(coredFileName);
		        ArrayChar.D3 paramArray = Utility.getCharD3Array(ncfile,
		                "STATION_PARAMETERS", log);
		            int[] parashape = paramArray.getShape();
		            StringBuffer profName = new StringBuffer(parashape[2]);
		            List <String>paraname = new ArrayList<String>();
		            for (int i = 0; i < parashape[0]; i++) {
		                for (int j = 0; j < parashape[1]; j++) {
		                    for (int k = 0; k < parashape[2]; k++) {
		                        profName.append(paramArray.get(i, j, k));
		                    }
		                    paraname.add(profName.toString().trim());
		                    if (profName.toString().trim().compareToIgnoreCase("DOXY")==0
		                    		|| profName.toString().trim().compareToIgnoreCase("CHLA")==0
		                    		|| profName.toString().trim().compareToIgnoreCase("NITRATE")==0
		                    		|| profName.toString().trim().compareToIgnoreCase("BBP700")==0
		                    		|| profName.toString().trim().compareToIgnoreCase("PH_IN_SITU_TOTAL")==0
		                    		){
		                    	//n_prof = i;
		                    	ibgcprof = j;
		                    }		                    
		                    profName.delete(0, parashape[2]);
		                }
		            }
    		} else {
    			log.info("working on file " + coredFileName);
    			ncfile = NetcdfFile.open(coredFileName);
    			//n_prof = icoredProf + 1;    			   			
    		}
			log.info("working on n_prof= " + n_prof);
			List<String>coredDataMode = Utility.getCharD1(NetcdfFile.open(coredFileName), "DATA_MODE", log);
			List<String>datamode = Utility.getCharD1(ncfile, "DATA_MODE", log);
			boolean adjustedVariable = false;
			if (datamode.get(n_prof).compareToIgnoreCase("R")!=0){
				adjustedVariable = true;
			}
			ArrayChar.D2 paramDataMode = null;
			if (ncfile.findVariable("PARAMETER_DATA_MODE")!= null){
				paramDataMode = (ArrayChar.D2)ncfile.findVariable("PARAMETER_DATA_MODE").read();
			}
			
			
			for (int k = 0 ; k < childDesc.size(); k++) {
				dto = childDesc.get(k);
				Variable ncvar = null;
				String ncvarName;
				String eqFromNcToBufr = dto.getNetcdf_bufr_conversion_eq();
				if (dto.getDescriptor_child().compareTo("002032")==0){
					List<Integer>tsDigitization = new ArrayList<Integer>();
					tsDigitization.add(0);
					dto.setData_array(tsDigitization);
				}
				if (dto.getDescriptor_child().substring(0, 3).compareToIgnoreCase("203")==0
						&&dto.getDescriptor_child().substring(3, 6).compareToIgnoreCase("000") != 0) {
					List<Integer>reference_values = new ArrayList<Integer>();
					reference_values.add(dto.getReference());
					dto.setData_array(reference_values);
				}
				if (bufrTable.isDelayedReplicator(dto.getDescriptor_child())){
					delayedReplicatorDto = dto;
				}
				if (dto.getNetcdf_variable().trim().length() > 0){
					ncvarName = dto.getNetcdf_variable().trim();
					if (ncvarName.compareToIgnoreCase("PRES")==0 
							|| ncvarName.compareToIgnoreCase("TEMP")==0
							|| ncvarName.compareToIgnoreCase("PSAL")==0 ){
						if(coredDataMode.get(n_prof).compareToIgnoreCase("R")==0 ){
							adjustedVariable = false;
						} else {
							adjustedVariable = true;
						}
					
					} else if (ncvarName.compareToIgnoreCase("DOXY")==0 
							|| ncvarName.compareToIgnoreCase("CHLA")==0
							|| ncvarName.compareToIgnoreCase("NITRATE")==0
							|| ncvarName.compareToIgnoreCase("BBP700")==0
							|| ncvarName.compareToIgnoreCase("PH_IN_SITU_TOTAL") ==0 ) {
						ncvarName = ncvarName.concat("_ADJUSTED");
						adjustedVariable = true;
					}

					if (adjustedVariable) {
						if (ncvarName.contains("_QC")){							
							//ncvarName = dto.getNetcdf_variable().trim().split("_")[0].concat("_ADJUSTED_QC");
							ncvarName = dto.getNetcdf_variable().trim().replace("_QC", "_ADJUSTED_QC");
						} else if (!ncvarName.contains("_QC") && !ncvarName.contains("N_LEVELS")
								&& !ncvarName.contains("VERTICAL_SAMPLING_SCHEME")){
							ncvarName = dto.getNetcdf_variable().concat("_ADJUSTED");
						}
					}
					ncvar = ncfile.findVariable(ncvarName.toUpperCase());
					if (ncvar == null && bgc) {
						ncvar = ncIcored.findVariable(ncvarName.toUpperCase());
					}
					// get the profile data (pres, temp, psal, pres_qc, temp_qc, psal_qc etc)
					if (ncvar != null && ncvar.getDimensions().size()==2){						
						Array profileValue = ncvar.read();
						if (ncvar.getDataType() != DataType.CHAR){			                
			                ArrayFloat.D2 profileArray = (ArrayFloat.D2) profileValue;
			                Attribute fillValue = ncvar
			                    .findAttribute("_FillValue");
			                Number fill = fillValue.getNumericValue();
			                int[] shape = profileArray.getShape();
			                List<Integer> data = new ArrayList<Integer>();
			                int totalDepth = 0;
			                String sValue= "MISSING";
			                for (int j = 0; j < shape[1]; j++) {
			                	float value = profileArray.get(n_prof, j);
			                	if (value != fill.floatValue()){
			                		double val = value;			                		
			                		if (eqFromNcToBufr.length() > 0 ){
				                		Parser parser = new Parser(eqFromNcToBufr);
										parser.setVariable("x", (double)value );						
										val =  ((parser.getValue() 
												* java.lang.Math.pow(10, dto.getScale())) - dto.getReference());
				                	}
			                		if (val >= 0) {
				                		sValue=bufrnumFormat.format(val);
				                		data.add(Integer.parseInt(bufrnumFormat.format(val)));
				                		totalDepth++;
			                		} else {
				                		int missingValue = BufrUtility.getInteger(BufrUtility.setMissingValue(dto.getData_width()).toString());
				                		data.add(missingValue);
				                		sValue= "MISSING";

			                		}
			                		
			                	} else {
			                		int missingValue = BufrUtility.getInteger(BufrUtility.setMissingValue(dto.getData_width()).toString());
			                		data.add(missingValue);
			                		sValue= "MISSING";
			                	}
		                		if (profilesValues.size() <=j){		                			
		                			profilesValues.add(j,sValue);		                			
		                		} else {		                			
		                			String s = profilesValues.get(j).concat(",").concat(sValue);		                			
		                			profilesValues.remove(j);
		                			profilesValues.add(j, s);

		                		}

			                }
			                dto.setData_array(data);
			                List<Integer> num_depths = new ArrayList<Integer>();			                
			                num_depths.add(getNonMissingValue(profilesValues).size());	
			                if (delayedReplicatorDto != null && data.size() > 0){
			                	delayedReplicatorDto.setData_array(num_depths);
			                } 
			                
						} else if (ncvar.getDataType() == DataType.CHAR 
								&& !ncvar.getFullName().contains("VERTICAL_SAMPLING_SCHEME")) {
							ArrayChar.D2 qcArray = (ArrayChar.D2) profileValue;
			                Attribute fillValue = ncvar
				                    .findAttribute("_FillValue");
			                List<Integer> data = new ArrayList<Integer>();
			                String sValue = "MISSING";
			                for (int j = 0; j < qcArray.getShape()[1]; j++){			                	
			                	StringBuffer qc = new StringBuffer();
			                	qc.append(qcArray.get(n_prof, j));
			                	if (qc.toString().compareToIgnoreCase(fillValue.getStringValue())!=0){
			                		sValue = BufrUtility.put033050(qc.toString());
			                		data.add(Integer.parseInt(BufrUtility
                                        .put033050(qc.toString())));
			                	} else {
			                		data.add(BufrUtility.getInteger(
			                				BufrUtility.setMissingValue(dto.getData_width()).toString()));
			                		sValue = BufrUtility.setMissingValue(dto.getData_width()).toString();
			                	}
			                	if (profilesValues.size() <=j){		                			
		                			profilesValues.add(j,sValue);		                			
		                		} else {		                			
		                			String s = profilesValues.get(j).concat(",").concat(sValue);		                			
		                			profilesValues.remove(j);
		                			profilesValues.add(j, s);
		                		}
			                }
			                dto.setData_array(data);
			                
						} else if (ncvar.getFullName().contains("VERTICAL_SAMPLING_SCHEME")
								&& dto.getMeds_pcode().trim().compareToIgnoreCase("PARM.008034_TS_QUALIFIER")==0){
							List<String> verticalSamplingScheme = Utility.getString2D(ncfile, 
					                dto.getNetcdf_variable(), log);
							List<Integer>verticalSampling = new ArrayList<Integer>();
							String ncVerticalSampling = verticalSamplingScheme.get(n_prof).toLowerCase();
							if (ncVerticalSampling.startsWith("secondary sampling:")){
								if (ncVerticalSampling.indexOf("averaged")>=0){
									verticalSampling.add(0);
								} else if ((ncVerticalSampling.indexOf("discrete")>=0)){
									verticalSampling.add(1);
								} else if ((ncVerticalSampling.indexOf("mixed")>=0)){
									verticalSampling.add(2);
								}
							} else if (ncVerticalSampling.startsWith("near-surface sampling:")){
								if (ncVerticalSampling.indexOf(", pumped")>=0){
									if (ncVerticalSampling.indexOf("average")>=0){
										verticalSampling.add(3);
									} else if (ncVerticalSampling.indexOf("discrete")>=0){
										verticalSampling.add(5);
									} else if (ncVerticalSampling.indexOf("mixed")>=0){
										verticalSampling.add(7);
									}
								}
								if (ncVerticalSampling.indexOf(", un-pumped")>=0
										|| ncVerticalSampling.indexOf(", unpumped") >=0){
									if (ncVerticalSampling.indexOf("average")>=0){
										verticalSampling.add(4);
									} else if (ncVerticalSampling.indexOf("discrete")>=0){
										verticalSampling.add(6);
									} else if (ncVerticalSampling.indexOf("mixed")>=0){
										verticalSampling.add(8);
									}
								}	
							} else {
								verticalSampling.add(15);
							}
							dto.setData_array(verticalSampling);
						}
					} 
				}
			}
            

			// convert data to binary 
			//Get all data whether or not it's missing
			List<String> data = getNonMissingValue(profilesValues);
			//List<String> data = profilesValues;
			if (data.size() == 0){
            	log.info("The number of depth for " + _parentDescriptor + " is " + data.size() + ".  Remove " + _parentDescriptor + " from BUFR messages");
            	emptySequences.add(_parentDescriptor.getDescriptorId());            	
            	return sbuf;
            }
			int iDesc = 0; 
			while (iDesc < childDesc.size()){			
				dto = childDesc.get(iDesc);
				String _descriptor = dto.getDescriptor_child();
				if (!bufrTable.isSimpleReplicator(dto.getDescriptor_child())) { 
						//&& dto.getMeds_pcode().compareToIgnoreCase("IGNORE")!=0) {
					if(dto.getMeds_pcode().compareToIgnoreCase("IGNORE")!= 0) {
						if (dto.getData_array()!= null){
							int value = dto.getData_array().get(0);
							log.info("encode " + dto.getDescriptor_child() + " = " + value 
									+ "="+BufrUtility.toBinary(
											Integer.toString(value), dto.getData_width(), dto.getUnits(),log));
							sbuf.append(BufrUtility.toBinary(
									Integer.toString(value), dto.getData_width(), dto.getUnits(),log));
						} else {
							log.info("encode missing " + dto.getDescriptor_child() + "=" 
						+ BufrUtility.setMissingValue(dto.getData_width()));
							sbuf.append(BufrUtility.setMissingValue(dto.getData_width()));
						}

					}
					iDesc++;
				} else {
					int numReplicate = bufrTable.getNumDescriptorToReplicate(_descriptor);
					log.info("number of descriptor to replicate " + numReplicate);
					if (bufrTable.isDelayedReplicator(
							childDesc.get(iDesc + 1).getDescriptor_child())){
						log.info("get descriptor: " + childDesc.get(iDesc + 1).getDescriptor_child());
						int nodepths = delayedReplicatorDto.getData_array().get(0);
						
						log.info("encode " + childDesc.get(iDesc + 1).getDescriptor_child() + "=" + nodepths + " =" 
						+ BufrUtility.toBinary(Integer.toString(nodepths),
								childDesc.get(iDesc + 1).getData_width(),
								childDesc.get(iDesc+1).getUnits(),log));
						sbuf.append(BufrUtility.toBinary(Integer.toString(nodepths),
								childDesc.get(iDesc + 1).getData_width(),
								childDesc.get(iDesc+1).getUnits(),log));
						iDesc++;
						for (int idepth = 0; idepth < nodepths ; idepth ++){
							int sLoc = 0;
							for (int j = 1; j < numReplicate ; j++){
								BufrDescriptorDto profileDto = childDesc.get(iDesc + j );
								if (profileDto.getData_array() != null){
									String []s = data.get(idepth).split(",");
									if (s[sLoc].compareToIgnoreCase("MISSING")!=0) {
										log.info("encode " + profileDto.getDescriptor_child() + "="
												+ s[sLoc] +"="
												+ BufrUtility.toBinary(s[sLoc],										
														profileDto.getData_width(), profileDto.getUnits(),log) );
													
													sbuf.append(BufrUtility.toBinary(s[sLoc], 
															profileDto.getData_width(), profileDto.getUnits(),log));

									} else {
										//log.info("encode " + profileDto.getDescriptor_child() + "=" + s[sLoc]);
										sbuf.append(BufrUtility.setMissingValue(profileDto.getData_width()));
									}
									sLoc++;
								} else {
									if (profileDto.getForced_missing() == 'Y'){										
										/*log.info("encode " + profileDto.getDescriptor_id() + "="+ profileDto.getDescriptor_child() + "=" 
												+BufrUtility.toBinary(
											Integer.toString(profileDto.getForced_value()),
											profileDto.getData_width(), profileDto.getUnits(),log)
												 );	*/									
										sbuf.append(BufrUtility.toBinary(
												Integer.toString(profileDto.getForced_value()),
												profileDto.getData_width(), profileDto.getUnits(),log));
									} else {
										/*log.info("encode " + profileDto.getDescriptor_child() + "=" 
									+ BufrUtility.setMissingValue(profileDto.getData_width()) ); */
										
										sbuf.append(BufrUtility.setMissingValue(profileDto.getData_width()));
									}
								}
							}
						}
						iDesc = iDesc + numReplicate ;
					} else { // for simple replication without depths variables
						for (int j = 0; j < numReplicate ; j++){
							BufrDescriptorDto profileDto = childDesc.get(iDesc + j);
							if (profileDto.getData_array() != null){
								sbuf.append(BufrUtility.toBinary(
										Integer.toString(profileDto.getData_array().get(0)),
										profileDto.getData_width(), profileDto.getUnits(),log));
							} else {
								if (profileDto.getForced_missing() == 'Y'){
									sbuf.append(BufrUtility.toBinary(
											Integer.toString(profileDto.getForced_value()),
											profileDto.getData_width(), profileDto.getUnits(),log));
								} else {
									sbuf.append(BufrUtility.setMissingValue(profileDto.getData_width()));
								}
							}
						}
					}
				}
				
			}
			log.info("Completed encoding " + _parentDescriptor + " descriptor "
	                + " and total length = "
	                + sbuf.length() + " bits");			
		} catch (IOException e) {
			log.error("Can't find the file" + BFileName + '\r' + '\n' + e);			
		}
		
		return sbuf;
	}


	private List<BufrDescriptorDto> modifiedDescriptorValue(List<BufrDescriptorDto> childDesc) {
		List<BufrDescriptorDto>modified_desc = new ArrayList<BufrDescriptorDto>();
		int ref_value = 0;
		int delta_data_width = 0;
		int delta_scale =0;
		Hashtable<String, Integer> nreference_value = new Hashtable<String, Integer> ();
		Hashtable<String, Integer> ndelta_data_width = new Hashtable<String, Integer> ();
		Hashtable<String, Integer> ndelta_scale = new Hashtable<String, Integer> ();
		boolean startRef = false; // start to change the data reference for the subsequence sequences until stop
		boolean startcdw = false;// start to change the data width for the subsequence sequences until stop
		boolean startcs = false;// start to change the data scale for the subsequence sequences until stop
		for (int i = 0; i < childDesc.size(); i++) {
			if (childDesc.get(i).getDescriptor_child().substring(0, 3).compareToIgnoreCase("203")==0
					&& childDesc.get(i).getDescriptor_child().substring(3).compareToIgnoreCase("255")!= 0) {
				ref_value = childDesc.get(i).getReference();				
				startRef = true;
			}
			if (childDesc.get(i).getDescriptor_child().compareToIgnoreCase("203255")==0) {
				startRef = false;				
			}
			if (childDesc.get(i).getDescriptor_child().substring(0, 3).compareToIgnoreCase("201")==0
					&& childDesc.get(i).getDescriptor_child().substring(3).compareToIgnoreCase("000")!= 0) {
				delta_data_width = bufrTable.getOperandYYY(childDesc.get(i).getDescriptor_child());
				startcdw = true;
			}
			if (childDesc.get(i).getDescriptor_child().compareToIgnoreCase("201000")==0) {
				startcdw = false;				
			}
			if (childDesc.get(i).getDescriptor_child().substring(0, 3).compareToIgnoreCase("202")==0
					&& childDesc.get(i).getDescriptor_child().substring(3).compareToIgnoreCase("000")!= 0) {
				delta_scale = bufrTable.getOperandYYY(childDesc.get(i).getDescriptor_child());					
				startcs = true;
			}
			if (childDesc.get(i).getDescriptor_child().compareToIgnoreCase("202000")==0) {
				startcs = false;				
			}
			
			if (startRef && !bufrTable.isSimpleReplicator(childDesc.get(i).getDescriptor_child())
					&& !bufrTable.isDelayedReplicator(childDesc.get(i).getDescriptor_child())
					&& !bufrTable.isOperatorDescriptor(childDesc.get(i).getDescriptor_child())) {
				nreference_value.put(childDesc.get(i).getDescriptor_child(), ref_value);
			}
			if (startcdw && !bufrTable.isSimpleReplicator(childDesc.get(i).getDescriptor_child())
					&& !bufrTable.isDelayedReplicator(childDesc.get(i).getDescriptor_child())
					&& !bufrTable.isOperatorDescriptor(childDesc.get(i).getDescriptor_child())) {
				ndelta_data_width.put(childDesc.get(i).getDescriptor_child(), delta_data_width);
			}
			if (startcs && !bufrTable.isSimpleReplicator(childDesc.get(i).getDescriptor_child())
					&& !bufrTable.isDelayedReplicator(childDesc.get(i).getDescriptor_child())
					&& !bufrTable.isOperatorDescriptor(childDesc.get(i).getDescriptor_child())) {
				ndelta_scale.put(childDesc.get(i).getDescriptor_child(), delta_data_width);
			}

		}
		
		for (int i =0; i < childDesc.size(); i++) {
			BufrDescriptorDto bu_dto = childDesc.get(i);
			if (!bufrTable.isSimpleReplicator(childDesc.get(i).getDescriptor_child())
					&& !bufrTable.isDelayedReplicator(childDesc.get(i).getDescriptor_child())
					&& !bufrTable.isOperatorDescriptor(childDesc.get(i).getDescriptor_child())) {
				if (nreference_value.containsKey(childDesc.get(i).getDescriptor_child()) 
						&& childDesc.get(i).getMeds_pcode().compareToIgnoreCase("IGNORE")!= 0){
					bu_dto.setReference(nreference_value.get(childDesc.get(i).getDescriptor_child()));
				}
				if (ndelta_data_width.containsKey(childDesc.get(i).getDescriptor_child())
						&& childDesc.get(i).getMeds_pcode().compareToIgnoreCase("IGNORE")!= 0){
					bu_dto.setData_width(bu_dto.getData_width() + ndelta_data_width.get(childDesc.get(i).getDescriptor_child()));
				}
				if (ndelta_scale.containsKey(childDesc.get(i).getDescriptor_child())
						&& childDesc.get(i).getMeds_pcode().compareToIgnoreCase("IGNORE")!= 0){
					bu_dto.setScale(bu_dto.getScale() + ndelta_scale.get(childDesc.get(i).getDescriptor_child()));
				}
			}
			modified_desc.add(bu_dto);
		}
		return modified_desc;
	}

	private List<String> getNonMissingValue(List<String> profilesValues) {
		List<String> unique = new ArrayList<String>();
		for (int i = 0; i < profilesValues.size(); i++){
			if (!profilesValues.get(i).contains("MISSING")) {
				unique.add(profilesValues.get(i));
			}
			//if (profilesValues.get(i).compareToIgnoreCase("MISSING,15,MISSING,15,MISSING,15,")!= 0) {
				
			
		}
		
		return unique;
	}
	private void getArgoBufrSequences(boolean auxData) {
		try {
			NetcdfFile ncfile = NetcdfFile.open(coredFileName);
			ArrayChar.D3 paramArray = Utility.getCharD3Array(ncfile,
		            "STATION_PARAMETERS", log);
			int[] parashape = paramArray.getShape();
			List <String>paraname = new ArrayList<String>();
			List<String> presTempPsal = new ArrayList<String>();
			presTempPsal.add("PRES");
			presTempPsal.add("TEMP");
			presTempPsal.add("PSAL");
			List<String>nearPresTemp = new ArrayList<String>();
			nearPresTemp.add("PRES");
			nearPresTemp.add("TEMP");
			List <String>presDoxy = new ArrayList<String>();
			presDoxy.add("PRES");
			presDoxy.add("DOXY");			
	        for (int i = 0; i < parashape[0]; i++) {
	        	paraname = new ArrayList<String>();
	            for (int j = 0; j < parashape[1]; j++) {
	            	StringBuffer profName = new StringBuffer(parashape[2]);
	                for (int k = 0; k < parashape[2]; k++) {
	                    profName.append(paramArray.get(i, j, k));
	                }
	                paraname.add(profName.toString().trim());
	                profName.delete(0, parashape[2]);
	            }
	            if (i==0 ){
	            	if (paraname.containsAll(presTempPsal)
	            			&& isProfileDataValid(ncfile,i, "PRES") && isProfileDataValid(ncfile,i, "TEMP")
	            			|| isProfileDataValid(ncfile, i, "PSAL")){
	            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
	            		sec3_dto.setDescriptorId("315003");
	            		sec3_dto.setNprof(i);
	            		sec3Sequences.add(sec3_dto);
	            	}
	            	/*
	    	        if (BFileName != null){
	    	        	NetcdfFile bfile = NetcdfFile.open(BFileName);
	    				List<String> b_paraname = Utility.getCharD3Array_List(bfile,
	    			            "STATION_PARAMETERS", log).get(i);
		            	if (auxData && b_paraname.containsAll(presDoxy)
		            			&& isNotFillValueArray(ncfile, i, "PRES") && isNotFillValueArray(bfile, i, "DOXY")){
		            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
		            		sec3_dto.setDescriptorId("306037");
		            		sec3_dto.setNprof(i);
		            		sec3Sequences.add(sec3_dto);
		            	}
	    	        }*/
	            } else {// for the profile i > 0
	            	if (auxData){
						List<String> verticalSamplingScheme = Utility.getString2D(ncfile, 
				                "VERTICAL_SAMPLING_SCHEME", log);

	            		if (verticalSamplingScheme.get(i).trim().startsWith("Near-surface sampling:")) {
			            	if (paraname.containsAll(nearPresTemp) 
			            			&& isProfileDataValid(ncfile,i, "PRES") && isProfileDataValid(ncfile,i, "TEMP")
			            			&& !isProfileDataValid(ncfile, i, "PSAL")){
			            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
			            		sec3_dto.setDescriptorId("306017");
			            		sec3_dto.setNprof(i);
			            		sec3Sequences.add(sec3_dto);
			            	} else if  (paraname.containsAll(presTempPsal)) {
			            		if (isProfileDataValid(ncfile,i, "PRES")
			            				&& isProfileDataValid(ncfile, i, "TEMP") && isProfileDataValid(ncfile, i, "PSAL")){
				            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
				            		sec3_dto.setDescriptorId("306018");
				            		sec3_dto.setNprof(i);
				            		sec3Sequences.add(sec3_dto);
			            		} else if (isProfileDataValid(ncfile,i, "PRES") 
			            				&& isProfileDataValid(ncfile, i, "TEMP") && !isProfileDataValid(ncfile, i, "PSAL")){
				            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
				            		sec3_dto.setDescriptorId("306017");
				            		sec3_dto.setNprof(i);
				            		sec3Sequences.add(sec3_dto);
			            		}
			            	} 
	            		}
		    	        if (BFileName != null){
		    	        	NetcdfFile bfile = NetcdfFile.open(BFileName);
		    				List<String> b_paraname = Utility.getCharD3Array_List(bfile,
		    			            "STATION_PARAMETERS", log).get(i);
		    				for (int k = 0; k < b_paraname.size(); k++) {
		    					if (b_paraname.get(k).compareToIgnoreCase("DOXY")==0 && isProfileDataValid(bfile,i, "DOXY_ADJUSTED")) {
		    						//	&& isProfileDataValid(bfile,i, "PRES_ADJUSTED")) {
				            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
				            		sec3_dto.setDescriptorId("306044");
				            		sec3_dto.setNprof(i);
				            		sec3Sequences.add(sec3_dto);
		    					} else if (b_paraname.get(k).compareToIgnoreCase("CHLA")==0 && isProfileDataValid(bfile,i, "CHLA_ADJUSTED")) {
		    							//&& isNotFillValueArray(ncfile,i, "PRES_ADJUSTED")) {
				            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
				            		sec3_dto.setDescriptorId("306045");
				            		sec3_dto.setNprof(i);
				            		sec3Sequences.add(sec3_dto);

		    					} else if (b_paraname.get(k).compareToIgnoreCase("NITRATE")==0 && isProfileDataValid(bfile,i, "NITRATE_ADJUSTED")) {
		    							//&& isProfileDataValid(ncfile,i, "PRES_ADJUSTED")) {
				            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
				            		sec3_dto.setDescriptorId("306046");
				            		sec3_dto.setNprof(i);
				            		sec3Sequences.add(sec3_dto);		    						

		    					}else if (b_paraname.get(k).compareToIgnoreCase("PH_IN_SITU_TOTAL")==0 
		    							&& isProfileDataValid(bfile,i, "PH_IN_SITU_TOTAL_ADJUSTED")) {
		    							//&& isProfileDataValid(ncfile,i, "PRES_ADJUSTED")) {
				            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
				            		sec3_dto.setDescriptorId("306047");
				            		sec3_dto.setNprof(i);
				            		sec3Sequences.add(sec3_dto);
		    					} else if (b_paraname.get(k).compareToIgnoreCase("BBP700")==0 && isNotFillValueArray(bfile,i, "BBP700_ADJUSTED")) {
		    							//&& isNotFillValueArray(ncfile,i, "PRES_ADJUSTED")) {
				            		Sec3NprofMapDTO sec3_dto = new Sec3NprofMapDTO();
				            		sec3_dto.setDescriptorId("306048");
				            		sec3_dto.setNprof(i);
				            		sec3Sequences.add(sec3_dto);
		    					} 
		    				}
			            }
	            	}
	            	
	            }
	        }
	        ncfile.close();

			
		} catch (IOException e) {
			log.error("Error in open file " + coredFileName +'\r' + '\n' + e);			
		}
		
		
	}

	private boolean isNotFillValueArray(NetcdfFile ncfile, int n_prof, String profileName) {
		boolean validData = false;		
		if (ncfile.findVariable(profileName)!= null){		
			try {
				Variable dataMode = ncfile.findVariable("DATA_MODE");
				Array dataModeValue = dataMode.read();
				ArrayChar.D1 datamodeArray = (ArrayChar.D1) dataModeValue;
				if (ncfile.findVariable("PARAMETER_DATA_MODE") != null){					
					ArrayChar.D2 paramDataMode = (ArrayChar.D2) ncfile.findVariable("PARAMETER_DATA_MODE").read();
					ArrayChar.D3 stationParameters = (ArrayChar.D3)ncfile.findVariable("STATION_PARAMETERS").read();
					int []shape = stationParameters.getShape();
					for (int i = 0; i < shape[1]; i++){
						StringBuffer sbf = new StringBuffer();
						for (int j = 0; j < shape[2];j++){
							sbf.append(stationParameters.get(n_prof, i, j));
						}						
						if (sbf.toString().trim().compareToIgnoreCase(profileName)==0){
							if (paramDataMode.get(n_prof, i)=='A'||paramDataMode.get(n_prof, i)=='a'){
								profileName = profileName.concat("_ADJUSTED");
							} else if (paramDataMode.get(n_prof, i)=='R'||paramDataMode.get(n_prof, i)=='r'
									&& profileName.compareToIgnoreCase("TEMP")!= 0 && profileName.compareToIgnoreCase("PSAL")!= 0){
								profileName = profileName.concat("_ADJUSTED");
								
							}
						}
					}		
				} else {
					if (datamodeArray.get(n_prof)=='A'|| datamodeArray.get(n_prof) == 'a'){
						profileName = profileName.concat("_ADJUSTED");
					} else {
						if (profileName.trim().compareToIgnoreCase("TEMP")!= 0 
								&& profileName.trim().compareToIgnoreCase("PSAL") != 0
								&& profileName.trim().compareToIgnoreCase("PRES") != 0) {
							profileName = profileName.trim().concat("_ADJUSTED");
						}
					}
				}
				Variable ncvar = ncfile.findVariable(profileName.trim().toUpperCase());
				Array profileValue = ncvar.read();
		        ArrayFloat.D2 profileArray = (ArrayFloat.D2) profileValue;
		        Attribute fillValue = ncvar
		                .findAttribute("_FillValue");
		            Number fill = fillValue.getNumericValue();
		            int[] shape = profileArray.getShape();
		            int j = 0;
		            while (j < shape[1] && !validData) {
		            	if (profileArray.get(n_prof, j) != fill.floatValue()){
		            		validData = true;
		            	}
		            	j++;
		            }
			} catch (IOException e) {
				log.error("Error reading array:  " + profileName + '\r' + '\n' + e);
			}
			

		} 

		return validData;
	}
	private boolean isProfileDataValid(NetcdfFile ncfile, int n_prof, String profileName) {
		boolean valid = false;
		if (ncfile.findVariable(profileName)!= null){
			try {
				Variable ncvar = ncfile.findVariable(profileName.trim().toUpperCase());				 	
				 Array	profileValue = ncvar.read();
				ArrayFloat.D2 profileArray = (ArrayFloat.D2)profileValue;
				Attribute fillValue = ncvar.findAttribute("_FillValue");
				Number fill = fillValue.getNumericValue();
				int []shape = profileArray.getShape();
				int j = 0; 
				while (j < shape[1] && !valid) {
					if (profileArray.get(n_prof, j) != fill.floatValue()){
						valid = true;						
					}
					j++;
				}
			} catch (IOException e) {
				
				log.error("Error reading array:  " + profileName + '\r' + '\n' + e);
			}
	
			
		}
		return valid;
		
	}


	/**
     * @param netcdf Netcdf profile
     * @param bufrtable Bufr table
     * @return ByteBuffer
     */
    private StringBuffer encode_315003() {
        // load Argo Bufr template into table
        StringBuffer buf4 = new StringBuffer("");
        icoredProf = 0;
        DecimalFormat numFmt = new DecimalFormat("#");
        try {
            NetcdfFile ncprof = NetcdfFile.open(coredFileName);
            List<String> platform = Utility.getString2D(ncprof,
                "PLATFORM_NUMBER", log);
            Dimension nLevelsDim = (Dimension) ncprof
                .findDimension("N_LEVELS");
            int nodepth = nLevelsDim.getLength();
            // start to construct sec4 of Bufr message
            
            if (BufrUtility.isNumber(platform.get(icoredProf).trim())) {
                buf4.append(BufrUtility.integerToBinary(platform.get(0)
                    .trim(), bufrTable.getDescriptorDto("001087").getData_width(),log));
                log.info("001087 = " + platform.get(0).trim());
            }
            // fill 001085
            List <String> instruments = Utility.getString2D(ncprof,
                "PLATFORM_TYPE", log);
           // System.out.println(instrument)
            String instrument = instruments.get(icoredProf).trim();
            if (instrument.length() > 0){
                buf4.append(BufrUtility.putChar(instrument, 
                		bufrTable.getDescriptorDto("001085").getData_width()));
                        
            } else {
            	buf4.append(BufrUtility.setMissingValue(bufrTable.getDescriptorDto("001085").getData_width()));
            }
            log.info("001085 = " + instrument);

            // fill 001086
            List<String> serialNumbers = Utility.getString2D(ncprof,
                    "FLOAT_SERIAL_NO", log);
            String serialNumber = serialNumbers.get(icoredProf).trim();
            if (serialNumber.length() > 0){
                buf4.append(BufrUtility.putChar(serialNumber,
                		bufrTable.getDescriptorDto("001086").getData_width()));
            } else {
            	buf4.append(BufrUtility.setMissingValue(bufrTable.getDescriptorDto("001086").getData_width()));
            }
            log.info("001086 = " + serialNumber +  " = " + BufrUtility.putChar(serialNumber, 
            		bufrTable.getDescriptorDto("001086").getData_width()));
            // fill 002036- buoy type = 2 sub-surface float
            buf4.append(BufrUtility.integerToBinary("2", bufrTable.getDescriptorDto("002036").getData_width(),log));
            // fill value for 002148            
            List<String> positionSystems = Utility.getString2D(ncprof,
                "POSITIONING_SYSTEM", log);
            String possystem = positionSystems.get(icoredProf);
            String s2148 = "31";
           if (possystem.trim().compareToIgnoreCase("ARGOS") == 0) {
        	   s2148 = "1";
            } else if (possystem.trim().compareToIgnoreCase("GPS") == 0) {
            	s2148 = "2";
            } else if (possystem.trim().compareToIgnoreCase("GOES DCP") == 0) {
            	s2148 = "3";
            } else if (possystem.trim().compareToIgnoreCase("METEOSAT DCP") == 0) {
            	s2148 = "4";
            } else if (possystem.trim().compareToIgnoreCase("ORBCOMM") == 0) {
            	s2148 = "5";
            } else if (possystem.trim().compareToIgnoreCase("INMARSAT") == 0) {
            	s2148 = "6";
            } else if (possystem.trim().compareToIgnoreCase("IRIDIUM") == 0) {
            	s2148 = "7";
            } else if (possystem.trim().compareToIgnoreCase("IRIDIUM and GPS") == 0) {
            	s2148 = "8";
            } else if (possystem.trim().compareToIgnoreCase("ARGOS-3") == 0) {
            	s2148 = "9";
            } else if (possystem.trim().compareToIgnoreCase("ARGOS-4") == 0) {
            	s2148 = "10";
        	}
           buf4.append(BufrUtility.integerToBinary(s2148,
       			bufrTable.getDescriptorDto("002148").getData_width(),log));
           log.info("002148" + "=" + s2148 + "="+ BufrUtility.integerToBinary(s2148,
       			bufrTable.getDescriptorDto("002148").getData_width(),log));
            // 002149 - type of data buoy = 26: sub-surface Argo float
           String s2149 = "63";
           if (instrument.trim().toUpperCase().startsWith("ALACE") ){
        	   s2149 = "10";
           } else if (instrument.trim().toUpperCase().startsWith("MARVOR")){
        	   s2149 = "11";
           } else if (instrument.trim().toUpperCase().startsWith("RAFOS")){
        	   s2149 = "12";
           } else if (instrument.trim().toUpperCase().startsWith("PROVOR")){
        	   s2149 = "13";
           } else if (instrument.trim().toUpperCase().startsWith("SOLO")){
        	   s2149 = "14";
           } else if (instrument.trim().toUpperCase().startsWith("APEX")){
        	   s2149 = "15";
           } else if (instrument.trim().toUpperCase().startsWith("PALACE")){
        	   s2149 = "27";
           } else if (instrument.trim().toUpperCase().startsWith("NEMO")){
        	   s2149 = "28";
           } else if (instrument.trim().toUpperCase().startsWith("NINJA")){
        	   s2149 = "29";
           } else if (instrument.trim().toUpperCase().startsWith("Ice buoy/float (POPS or ITP)")
        		   || instrument.trim().contains("POPS")
        		   || instrument.trim().contains("ITP")){
        	   s2149 = "30";
           } else if (instrument.trim().toUpperCase().startsWith("UNSPECIFIED SUB-SURFACE FLOAT")){ 
        	   s2149 = "8";
           } else if (instrument.trim().toUpperCase().startsWith("SUB-SURFACE ARGO FLOAT")
        		   || instrument.trim().toUpperCase().startsWith("NOVA")){
        	   s2149 = "26";
           } 
           buf4.append(BufrUtility.integerToBinary(s2149, 
    			   bufrTable.getDescriptorDto("002149").getData_width(),log));
           log.info("002149" + "=" + s2149 + "="+ BufrUtility.integerToBinary(s2149,
       			bufrTable.getDescriptorDto("002149").getData_width(),log));
 
            List<Integer> cycleno = Utility.getIntD1(ncprof, "CYCLE_NUMBER", log);
            buf4.append(BufrUtility
                .integerToBinary(Integer.toString(cycleno.get(icoredProf)), 
                		bufrTable.getDescriptorDto("022055").getData_width(),log));
            log.info("022055 = "
                + Integer.toString(cycleno.get(icoredProf)) + BufrUtility
                .integerToBinary(Integer.toString(cycleno.get(icoredProf)), 
                		bufrTable.getDescriptorDto("022055").getData_width(),log));

            List<String> directions = Utility.getCharD1(ncprof, "DIRECTION", log);
            String direction = directions.get(icoredProf);
            // fill in value for 022056
            if (direction.trim().compareToIgnoreCase("A") == 0) {
                buf4.append(BufrUtility.integerToBinary("0",
                		bufrTable.getDescriptorDto("022056").getData_width(),log));

            } else if (direction.trim().compareToIgnoreCase("D") == 0) {
                buf4.append(BufrUtility.integerToBinary("1",
                		bufrTable.getDescriptorDto("022056").getData_width(),log));
            } else {
                buf4.append(BufrUtility.integerToBinary("3",
                		bufrTable.getDescriptorDto("022056").getData_width(),log));
            }
            // 022067 instrument type for water temp profile measuremnt
            // IxIxIx
            // common code table C-3 (code table 1770)
            List<String> wmoid = Utility.getString2D(ncprof, "WMO_INST_TYPE", log);
            buf4.append(BufrUtility.integerToBinary(wmoid.get(0).trim(),
            		bufrTable.getDescriptorDto("022067").getData_width(),log));

            // fill date and time in UTC section 301011 and 301012
            String referencedt = ncprof.findVariable("REFERENCE_DATE_TIME").readScalarString();
            List<Double> julday = Utility.getDoubleD1(ncprof, "JULD", log);
            String obsdate = Utility.convertJulianToDate(julday.get(icoredProf),
                referencedt);
            log.info(obsdate);
            buf4.append(BufrUtility.integerToBinary(obsdate
                .substring(0, 4).trim(), 
                bufrTable.getDescriptorDto("004001").getData_width(),log));
            buf4.append(BufrUtility.integerToBinary(obsdate
                .substring(4, 6).trim(), bufrTable.getDescriptorDto("004002").getData_width(),log));
            buf4.append(BufrUtility.integerToBinary(obsdate
                .substring(6, 8).trim(),
                bufrTable.getDescriptorDto("004003").getData_width(),log));
            log.info("301011 + 301012 = " + obsdate);
            buf4.append(BufrUtility.integerToBinary(obsdate
                .substring(8, 10).trim(),
                bufrTable.getDescriptorDto("004004").getData_width(),log));
            buf4.append(BufrUtility.integerToBinary(obsdate
                .substring(10, 12).trim(),
                bufrTable.getDescriptorDto("004005").getData_width(),log));
            // fill location and location flags- 301021-latiude and
            // longitude(high accuracy)
            // 005001 - Latiude (high accuracy, scale 5, reference =
            // -9000000)
            // 006001 - Longitude (high accuracy, scale 5)
            List <Double>latitudes = Utility.getDoubleD1(ncprof, "LATITUDE", log);
            List <Double>longitudes = Utility.getDoubleD1(ncprof, "LONGITUDE", log);
            double position_fill = ncprof.findVariable("LATITUDE")
            		.findAttribute("_FillValue").getNumericValue().doubleValue();
            if (latitudes.get(icoredProf)==position_fill){
            	buf4.append(BufrUtility.setMissingValue(bufrTable.getDescriptorDto("005001").getData_width()));
            	buf4.append(BufrUtility.setMissingValue(bufrTable.getDescriptorDto("006001").getData_width()));
                buf4.append(BufrUtility.integerToBinary("20",
                		bufrTable.getDescriptorDto("008080").getData_width(),log));
                List<String> qpos = Utility.getCharD1(ncprof, "POSITION_QC", log);
                String sqpos = BufrUtility.put033050(qpos.get(icoredProf).trim());
                buf4.append(BufrUtility.integerToBinary(sqpos, 
                		bufrTable.getDescriptorDto("033050").getData_width(),log));
            } else {
                double lat = latitudes.get(icoredProf)* 100000 - (-9000000);
                double lon = longitudes.get(icoredProf) * 100000 - (-18000000);
                buf4.append(BufrUtility.integerToBinary(Integer
                    .toString(Integer.parseInt(numFmt.format(lat))), 
                    bufrTable.getDescriptorDto("005001").getData_width(),log));
                buf4.append(BufrUtility.integerToBinary(Integer
                    .toString(Integer.parseInt(numFmt.format(lon))), 
                    bufrTable.getDescriptorDto("006001").getData_width(),log));
                // fill 008080 - qualifier for quality class, 20: position
                // 033050 - GTSPP quality class
                buf4.append(BufrUtility.integerToBinary("20",
                		bufrTable.getDescriptorDto("008080").getData_width(),log));
                List<String> qpos = Utility.getCharD1(ncprof, "POSITION_QC", log);
                String sqpos = BufrUtility.put033050(qpos.get(icoredProf).trim());
                buf4.append(BufrUtility.integerToBinary(sqpos, 
                		bufrTable.getDescriptorDto("033050").getData_width(),log));

            }
            
            List<String> datamode = Utility.getCharD1(ncprof, "DATA_MODE", log);
            putProfileLevel(ncprof, buf4, nodepth, datamode.get(icoredProf).substring(0,1));
            ncprof.close();
           
        } catch (IOException e) {
        	log.info("Error in writting Section 4 of Argo Bufr");
            e.printStackTrace();
        }
        
        log.info("Completed encoding 315003 descriptor from "
                + coredFileName + " BUFR and total length = "
                + buf4.length() + " bits");
        
        return buf4;
        // End of encodeSection 4 class
    }

    /**
     * @param ncprof Argo Netcdfprofile
     * @param buf4 Stringbuffer to write the data into
     * @param nodepth number of depths in the profile
     * @param datamode the current data status according Argo Netcdf format
     * @param bufrtable the BufrTable
     * @return StringBuffer
     */
    private StringBuffer putProfileLevel(NetcdfFile ncprof,
        StringBuffer buf4, int nodepth, String datamode ) {
        //       determine the station_parameters
        ArrayChar.D3 paramArray = Utility.getCharD3Array(ncprof,
            "STATION_PARAMETERS", log);
        int[] parashape = paramArray.getShape();
        StringBuffer profName = new StringBuffer(parashape[2]);
        List <String>paraname = new ArrayList<String>();
        DecimalFormat numFmt = new DecimalFormat("#");
        List<String>profilesValues= new ArrayList<String>();
       // for (int i = 0; i < icoredProf + 1; i++) {
            for (int j = 0; j < parashape[1]; j++) {
                for (int k = 0; k < parashape[2]; k++) {
                    profName.append(paramArray.get(0, j, k));
                }
                paraname.add(profName.toString());
                profName.delete(0, parashape[2]);
            }
       // }
        //initiate pres, temp and psal array and its qc arrays
        //if you need to add extra variable, add another array
        List<Integer>pres = new ArrayList<Integer>();
        List<Integer>temp = new ArrayList<Integer>();
        List<Integer>psal = new ArrayList<Integer>();
        List<Integer>presqc = new ArrayList<Integer>();
        List<Integer>tempqc = new ArrayList<Integer>();
        List<Integer>psalqc = new ArrayList<Integer>();
        
        for (int k = 0; k < nodepth; k++) {
        	pres.add(131071);            
            temp.add(524287); // set to missing value (19 bits)
            psal.add(131071);  // set to missing value
            presqc.add(15);
            tempqc.add(15);
            psalqc.add(15);
        }
        int nprofile = paraname.size();
        String varname = "";
        List <String>paramName = new ArrayList<String>();
        Hashtable<String, String>profilesName = new Hashtable<String, String>();        
       // Hash<String, > profileLocation = new HashMap<String, Integer>();
        for (int k = 0; k < nprofile; k++) {
        	if (paraname.get(k).toString().trim().compareTo("PRES")==0
        			|| paraname.get(k).toString().trim().compareToIgnoreCase("TEMP")==0
        			|| paraname.get(k).toString().trim().compareToIgnoreCase("PSAL")==0){
        		if (datamode.compareToIgnoreCase("D") == 0
                        || datamode.compareToIgnoreCase("A") == 0) {
                        varname = paraname.get(k).toString().trim()
                            + "_ADJUSTED";
                        profilesName.put(paraname.get(k).toString().trim(), varname);                     
                    } else {
                        varname = paraname.get(k).toString().trim();
                        profilesName.put(paraname.get(k).toString().trim(), varname);                        
                    }
        		
        	}
        }
        // sorting the paramName in the order of PRES, TEMP and PSAL as what is expected by the BUFR template
        if (profilesName.containsKey("PRES")) {
        	paramName.add(profilesName.get("PRES"));
        }
        if (profilesName.containsKey("TEMP")) {
        	paramName.add(profilesName.get("TEMP"));
        } 
        if (profilesName.containsKey("PSAL")) {
        	paramName.add(profilesName.get("PSAL"));
        }
        for (int k = 0; k < paramName.size();k++){
            log.info("variable to get = " + paramName.get(k));
            try {
                Variable profilevar = ncprof.findVariable(paramName.get(k));
                Array profilevarValue = profilevar.read();
                ArrayFloat.D2 profilevarArray = (ArrayFloat.D2) profilevarValue;
                Attribute fillValue = profilevar
                    .findAttribute("_FillValue");
                Number fill = fillValue.getNumericValue();
                // qc value
                Variable profilevarqc = ncprof
                    .findVariable(paramName.get(k).concat("_QC"));
                Array profilevarqcValue = profilevarqc.read();
                //assert (profilevarqcValue instanceof ArrayChar.D2);
                ArrayChar.D2 profilevarqcArray = (ArrayChar.D2) profilevarqcValue;
                Attribute fillqc = profilevarqc
                    .findAttribute("_FillValue");
                String fillqcst = fillqc.getStringValue();
                int[] shape = profilevarArray.getShape();
                for (int i = 0; i < 1; i++) {
                    for (int j = 0; j < shape[1]; j++) {
                        float valparam = profilevarArray.get(i, j);                        
                       // StringBuffer dpqc = new StringBuffer();
                        StringBuffer parmqc = new StringBuffer();
                        parmqc = parmqc.append(profilevarqcArray.get(
                            i, j));
                        StringBuffer sValue= new StringBuffer();
                        if (valparam != fill.floatValue()) {
                            if (paramName.get(k).toString().trim().compareToIgnoreCase("PRES") ==0
                            		|| paramName.get(k).toString().trim().compareToIgnoreCase("PRES_ADJUSTED")==0 ) {
                            	double value = valparam *10000 *0.001;
                            	pres.add(j, Integer.parseInt(numFmt.format(value)));
                            	sValue.append(numFmt.format(value)).append(",");
                            } else if (paramName.get(k).toString().trim().compareToIgnoreCase("TEMP")==0
                            		|| paramName.get(k).toString().trim().compareToIgnoreCase("TEMP_ADJUSTED")==0){
                            	double value = (valparam+ 273.150)*1000;
                            	temp.add(j,Integer.parseInt(numFmt.format(value)));
                            	sValue.append(numFmt.format(value)).append(",");
                            } else if (paramName.get(k).toString().trim().compareToIgnoreCase("PSAL")==0 
                            		|| paramName.get(k).toString().trim().compareToIgnoreCase("PSAL_ADJUSTED")==0) {
                            	double  value = valparam *1000;
                            	psal.add(j,Integer.parseInt(numFmt.format(value)));
                            	sValue.append(numFmt.format(value)).append(",");                            	
                            }
                        } else {
                        	sValue.append("MISSING,");
                        }
                        if (parmqc.toString().compareToIgnoreCase(
                            fillqcst) != 0 && pres.get(j)!= 131071) {
                            if (paramName.get(k).toString().trim().compareToIgnoreCase("PRES") ==0
                            		|| paramName.get(k).toString().trim().compareToIgnoreCase("PRES_ADJUSTED")==0) {
                                presqc.add(j,Integer
                                    .parseInt(BufrUtility
                                        .put033050(parmqc.toString())));
                                sValue.append(BufrUtility
                                        .put033050(parmqc.toString())).append(",");
                            } else if (paramName.get(k).toString().trim().compareToIgnoreCase("TEMP") ==0
                            		|| paramName.get(k).toString().trim().compareToIgnoreCase("TEMP_ADJUSTED")==0) {                            	
                                tempqc.add(j,Integer
                                    .parseInt(BufrUtility
                                        .put033050(parmqc.toString())));
                                sValue.append(BufrUtility
                                        .put033050(parmqc.toString())).append(",");
                            } else if (paramName.get(k).toString().trim().compareToIgnoreCase("PSAL")==0
                            		|| paramName.get(k).toString().trim().compareToIgnoreCase("PSAL_ADJUSTED")==0) {
                                psalqc.add(j,Integer
                                    .parseInt(BufrUtility
                                        .put033050(parmqc.toString())));                                
                                sValue.append(BufrUtility
                                        .put033050(parmqc.toString())).append(",");
                            }
                        } else {                        	
                        	sValue.append(Integer.toString(BufrUtility.getInteger(
	                				BufrUtility.setMissingValue(
	                						bufrTable.getDescriptorDto("033050").getData_width()).toString()))).append(",");
                        }
                        parmqc.delete(0, parmqc.length());
                        if (profilesValues.size() <=j){                			
                			profilesValues.add(j,sValue.toString());                			
                		} else {		                			
                			String s = profilesValues.get(j).concat(sValue.toString());                			
                			profilesValues.remove(j);
                			profilesValues.add(j, s);

                		}
                    }
                }
            } catch (IOException e) {
            	log.info("Can't find the variable name = "
                    + varname);
                e.printStackTrace();
            }
        }
        List<String> data = getNonMissingValue(profilesValues);
        //List<String> data = profilesValues;
        buf4.append(BufrUtility
                .integerToBinary(Integer.toString(data.size()),
                		bufrTable.getDescriptorDto("031002").getData_width(),log));

        //      add to the Buffer array
        for (int i = 0; i < data.size(); i++) {
        	int sloc = 0;
        	String[]s = data.get(i).split(",");
        	if (s[sloc].compareToIgnoreCase("MISSING")!= 0){
                buf4.append(BufrUtility
                        .integerToBinary(s[sloc], 
                        		bufrTable.getDescriptorDto("007065").getData_width(),log));
                log.info("007065 = " + s[sloc] + " = " + bufrTable.getDescriptorDto("007065").getData_width() + " " 
                        		+ BufrUtility.integerToBinary(s[sloc], 
                        		bufrTable.getDescriptorDto("007065").getData_width(),log));

        	} else {
        		buf4.append(BufrUtility.setMissingValue(bufrTable.getDescriptorDto("007065").getData_width()));
        	}
            sloc++;
            buf4.append(BufrUtility.integerToBinary("10",
            		bufrTable.getDescriptorDto("008080").getData_width(),log));
            buf4.append(BufrUtility.integerToBinary(s[sloc], 
                    bufrTable.getDescriptorDto("033050").getData_width(),log));
            sloc++;
            if (s[sloc].compareToIgnoreCase("MISSING")!= 0){
                buf4.append(BufrUtility
                        .integerToBinary(s[sloc], bufrTable.getDescriptorDto("022045").getData_width(),log));
                log.info("022045 = " +  s[sloc] + " = " + bufrTable.getDescriptorDto("022045").getData_width() + " " + BufrUtility
                        .integerToBinary(s[sloc], 
                        		bufrTable.getDescriptorDto("022045").getData_width(),log));


            } else{
            	buf4.append(BufrUtility.setMissingValue(bufrTable.getDescriptorDto("022045").getData_width()));
            	//log.info("022045= " + s[sloc] + " = " + BufrUtility.setMissingValue(bufrTable.getDescriptorDto("022045").getData_width()));
            }
            sloc++;
            buf4.append(BufrUtility.integerToBinary("11",
            		bufrTable.getDescriptorDto("008080").getData_width(),log));            
            buf4.append(BufrUtility.integerToBinary(s[sloc],
            		bufrTable.getDescriptorDto("033050").getData_width(),log));
            sloc++;
            if (s[sloc].compareToIgnoreCase("MISSING")!= 0){
            	buf4.append(BufrUtility
                        .integerToBinary(s[sloc],
                        		bufrTable.getDescriptorDto("022064").getData_width(),log));
               log.info("022064 = " + s[sloc] + " = " +   BufrUtility
                        .integerToBinary(s[sloc], 
                        		bufrTable.getDescriptorDto("022064").getData_width(), log));

            } else {
            	buf4.append(BufrUtility.setMissingValue(bufrTable.getDescriptorDto("022064").getData_width()));
            //	log.info("022064= " + s[sloc] + " = " +  BufrUtility.setMissingValue(bufrTable.getDescriptorDto("022064").getData_width()));

            }
            
            sloc++;
            buf4.append(BufrUtility.integerToBinary("12",
            		bufrTable.getDescriptorDto("008080").getData_width(),log));
            buf4.append(BufrUtility.integerToBinary(s[sloc], 
            		bufrTable.getDescriptorDto("033050").getData_width(),log));
            sloc++;
        }
        return buf4;
    }



    // End of ArgoBufrSec4 class
}

/*
 * Created on 21-Jun-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package dfo.isdm.BufrUtility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.*;

/**
 * BUFR Table class contain all of the information from BUFR template
 * @author Tran 25-Jul-06
 */
public class BufrTable {
    /**
     * List of descriptor to included in section 3
     */
    private ArrayList <String>section3Code = new ArrayList<String>();
    /**
     * serverName that contain the oracle database
     */
    private String serverName;

    /**
     * portNumber
     */
    private String portNumber;

    /**
     * username to access the oracle database
     */
    private String username;

    /**
     * Password for accessing
     */
    private String password;

    /**
     * sid
     */
    private String sid;

    /**
     * table name to access
     */
    private String tablename;

    /**
     * driverName
     */
    private static String driverName = "oracle.jdbc.driver.OracleDriver";    
    private static Logger log;
    private static String dataType;
    private Connection connection = null;
    private List<BufrDescriptorDto>descriptorsList = new ArrayList<BufrDescriptorDto>();
    /**
     * Constructor for Bufr table
     * @param templatefile Bufr template
     */
    public BufrTable(String templatefile, Logger _log) {
    	log = _log;
        try {
            loadTemplate(templatefile);
        } catch (IOException e) {
            log.info("LoadBufrTable.java:  Template File not found"
                    + templatefile + e);
            e.printStackTrace();
        }
    }

    /**
     * Default constructor for access oracle database contain the Argo Bufr
     * template
     * @param serverId server Name
     * @param portId port Number
     * @param userId UserName
     * @param pw Password
     * @param sno Sid
     * @param tableId name of the table to access
     * @param log 
     * @param tableName2 
     */
    public BufrTable(String serverId, String portId, String sno, String _driverName,String userId,
            String pw, String tableId, String _dataType, Logger _log) {
        serverName = serverId;
        portNumber = portId;
        username = userId;
        password = pw;
        sid = sno;
        tablename = tableId;
        driverName = _driverName;
        log = _log;
        dataType = _dataType;
        connection = connect();
        
    }

    /**
     * @return List contain all descriptors use for Section 3 from the template
     * file
     */
    public ArrayList<String> getSection3CodeTable() {
        return section3Code;
    }

    public List<BufrDescriptorDto> getDescriptorsList() {
		return descriptorsList;
	}

	public boolean isSimpleReplicator(String _descriptor) {
        boolean replicator = false;
        if (_descriptor.substring(0, 1).compareToIgnoreCase("1")== 0){
        	replicator = true;
        }
        return replicator;
    }
    /**
     * This method get how many descriptor will be replicated.
     * @param _descriptor
     * @return
     */
    public int getNumDescriptorToReplicate(String _descriptor) {
        int numReplicate=0;
        if (isSimpleReplicator(_descriptor)){
        	numReplicate = Integer.parseInt(_descriptor.substring(1,3));
        	if (_descriptor.substring(3, 6).compareToIgnoreCase("000")==0){
        		numReplicate = numReplicate + 1;
        	}
        }
        
        return numReplicate;
    }
    
    public int getReplicationFactor (String _descriptor){
    	int numIteration = 1;
    	if (isSimpleReplicator(_descriptor) ){
    		numIteration = Integer.parseInt(_descriptor.substring(3, 6));
    	} else {
    		log.info(_descriptor + " is not a replicator description");
    	}
    	if (numIteration ==0){
    		numIteration = 1;
    	}
    	return numIteration;
    }
    


    /**
     * @return boolean = true if there is replicator operator
     */
    public boolean isDelayedReplicatorInSection3() {
        boolean replicator = false;
        for (int i = 0; i < section3Code.size(); i++) {
            if (section3Code.get(i).toString().substring(0, 1)
                    .compareToIgnoreCase("1") == 0) {
                replicator = true;
            }
        }
        return replicator;
    }

    /**
     * @return List of duplicator descriptor if there is replicator operator
     */
    public List<String> duplicateDescriptor() {
        List <String>dupDescriptor = new ArrayList<String>();
        int ireplicator = 0;
        if (isDelayedReplicatorInSection3()) {
            for (int i = 0; i < section3Code.size(); i++) {
                if (isDelayedReplicator(section3Code.get(i).toString())) {
                    ireplicator++;
                }
                if (!isDelayedReplicator(section3Code.get(i).toString())
                        && ireplicator > 0) {
                    dupDescriptor.add(section3Code.get(i));
                }
            }
        } else {
            dupDescriptor = null;
        }
        return dupDescriptor;
    }

    /**
     * @param descriptor FXY
     * @return boolean true if the descriptor is a replicator factor. Descriptor
     * started with 031
     */
    public boolean isDelayedReplicator(String descriptor) {
        boolean delayedreplicator = false;
        if (descriptor.substring(0, 3).compareToIgnoreCase("031") == 0) {
            delayedreplicator = true;
        }
        return delayedreplicator;
    }
    /**
     * This method determine whether or not a descriptor is a descriptor operator
     * @param descriptor
     * @return
     */
    public boolean isOperatorDescriptor (String descriptor) {
    	boolean operatorDescriptor = false;
    	if (descriptor.substring(0,1).compareToIgnoreCase("2")==0) {
    		operatorDescriptor = true;
    	}
    	return operatorDescriptor;
    }
    public int getOperandYYY(String descriptor) {
    	int y = 0;
    	if (descriptor.substring(0, 3).compareToIgnoreCase("201")==0
    			|| descriptor.substring(0, 3).compareToIgnoreCase("202")==0 
    			&& descriptor.substring(3).compareToIgnoreCase("000")!= 0) {
    		y = Integer.parseInt(descriptor.substring(3)) - 128;
    	} else if (descriptor.substring(0, 3).compareToIgnoreCase("203")==0) {
    		y = Integer.parseInt(descriptor.substring(3));
    	}
    	return y;
    }
    
    /**
     * 
     * @param _parentDescriptor
     * @return
     */
    public List<BufrDescriptorDto> getChildSubset(String _parentDescriptor){
    	List <BufrDescriptorDto> childList = new ArrayList<BufrDescriptorDto>();
    	for (int i = 0; i < descriptorsList.size(); i++){
    		if (descriptorsList.get(i).getDescriptor().compareToIgnoreCase(_parentDescriptor)==0){
    			childList.add(descriptorsList.get(i));
    		}
    	}

    	return childList;
    }
    public BufrDescriptorDto getDescriptorDto(String _descriptor){
    	BufrDescriptorDto dto = null;
    	for (int i = 0; i < descriptorsList.size();i++){
    		if (descriptorsList.get(i).getDescriptor_child().trim()
    				.compareToIgnoreCase(_descriptor.trim())==0){
    			dto = descriptorsList.get(i);
    		}
    	}
    	return dto;
    }

    /**
     * @param templatefile Argo BUFR Template
     * @param i - integer determined which table to return i = 1 for
     * section3code, i = 2 for scale, i = 3 for reference value, i = 4 for data
     * width, i = 5 for unit, i = 6 for meds name i = 7 for child-Parent
     * descriptor
     * @return Hashtable of variable data type
     * @throws IOException if template file not found
     */
    private void loadTemplate(String templatefile)
            throws IOException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(templatefile));
            String str;
            str = in.readLine();
            while ((str = in.readLine()) != null) {
                String[] value = str.split(",");
                BufrDescriptorDto dto = new BufrDescriptorDto();
                	int index = Integer.parseInt(value[0]);
                	dto.setDescriptor_id(index);
                	dto.setDescriptor(value[1]);
                	dto.setDescriptor_child(value[2]);
                	if (value[3].trim().length()>0){
                		dto.setForced_value(Integer.parseInt(value[3]));
                	} else{
                		dto.setForced_value(java.sql.Types.NULL);
                	}
                	if (value[4].trim().length()>0){
                		dto.setForced_missing(value[4].trim().charAt(0));
                	} else {
                		dto.setForced_missing('N');
                	}
                	dto.setMeds_pcode(value[5]);
                    dto.setUnits(value[6]);
                    if (value[7].trim().length() > 0){
                    	dto.setCode_table_max(Integer.parseInt(value[7].trim()));
                    } else{
                    	dto.setCode_table_max(java.sql.Types.NULL);
                    }
                    
                    if (value[8].trim().length()>0){
                        dto.setScale(Integer.parseInt(value[8].trim()));
                    } else {
                    	dto.setScale(0);
                    }
                    if (value[9].trim().length()>0){
                    	dto.setReference(Integer.parseInt(value[9].trim()));
                    } else {
                    	dto.setReference(0);
                    }
                    if (value[10].trim().length()>0){
                    	dto.setData_width(Integer.parseInt(value[10].trim()));
                    }
                    dto.setFormat_template(value[11]);
                    dto.setSection3_fxy_seq(value[12]);
                    if (value[12].trim().length()>0){
                    	section3Code.add(value[12]);
                    }
                    dto.setBufr_meds_conversion_eq(value[13]);
                    dto.setBufr_meds_conversion_eq(value[14]);
                    dto.setNetcdf_variable(value[15]);
                    if (value[16].trim().compareToIgnoreCase("none")!= 0){
                        dto.setNetcdf_bufr_conversion_eq(value[16]);
                    } else {
                        dto.setNetcdf_bufr_conversion_eq(" ");

                    }
                    descriptorsList.add(dto);
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.err.println(e);
            e.printStackTrace();
            System.exit(1);
        }
    }
    public Connection connect(){
        Connection connectionId = null;
       /* String url = "jdbc:oracle:thin:@" + serverName + ":" + portNumber
                + ":" + sid;*/
		String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST="
				+"(ADDRESS=(PROTOCOL=TCP)"
				+"(HOST =" 
				+ serverName 
				+ ")(PORT="
				+ portNumber 
				+ ")))"
				+ " (CONNECT_DATA=(SERVICE_NAME="
				+ sid 
				+ ")))";
            try {
				Class.forName(driverName).newInstance();
				connectionId = DriverManager.getConnection(url, username, password);
				log.info("Connection id: " + connectionId);
			} catch (InstantiationException e) {
				log.error("ERROR with JDBC connection " + e);
			} catch (IllegalAccessException e) {
				log.error("Illegal access to database " + e);				
			} catch (ClassNotFoundException e) {
				log.error("Class not found " + e);
				e.printStackTrace();
			} catch (SQLException e) {
				log.error("error in connect to " + url + e);				
			} 
        
        return connectionId;
    }

    /**
     * Method to connect to database
     */
    public boolean loadTable() {
        ResultSet rs = null;
        Statement stmt = null;
        boolean success = false;
        try {
            stmt = connection.createStatement();
            // get child info
            String getchildinfo = "Select *  from "
                    + tablename                        
                    + " order by descriptor_id asc";

            if (dataType != null){
                getchildinfo = "Select *  from "
                        + tablename
                        + " where format_template = '"
                        + dataType
                        + "' order by descriptor_id asc";

            } 
            rs = stmt.executeQuery(getchildinfo);
            descriptorsList = new ArrayList<BufrDescriptorDto>();
            while (rs.next()) {
            	BufrDescriptorDto dto = new BufrDescriptorDto();
            	dto.setDescriptor_id(rs.getInt("DESCRIPTOR_ID"));
            	dto.setDescriptor(rs.getString("DESCRIPTOR"));
                dto.setDescriptor_child(rs.getString("DESCRIPTOR_CHILD"));
                dto.setForced_value(rs.getInt("FORCE_VALUE"));
                dto.setForced_missing(rs.getString("FORCED_MISSING").charAt(0));
            	if (rs.getString("MEDS_PCODE") != null){
            		dto.setMeds_pcode(rs.getString("MEDS_PCODE"));                    
            	} else {
            		dto.setMeds_pcode("");                   
            	}
            	dto.setUnits(rs.getString("UNITS"));
            	if (rs.getInt("CODE_TABLE_MAX")!= java.sql.Types.INTEGER){
            		dto.setCode_table_max(rs.getInt("CODE_TABLE_MAX"));
            	}
            	dto.setScale(rs.getInt("SCALE"));
            	dto.setReference(rs.getInt("REFERENCE"));
            	dto.setData_width(rs.getInt("DATA_WIDTH"));
            	dto.setFormat_template(rs.getString("FORMAT_TEMPLATE"));
            	dto.setSection3_fxy_seq(rs.getString("SECTION3_FXY_SEQ"));
                if (rs.getString("MEDS_BUFR_CONVERSION_EQ") != null){
                    dto.setMeds_bufr_conversion_eq(rs.getString("MEDS_BUFR_CONVERSION_EQ"));
                } else {
                	dto.setMeds_bufr_conversion_eq("");

                }
                if (rs.getString("BUFR_MEDS_CONVERSION_EQ") != null){
                	dto.setBufr_meds_conversion_eq("BUFR_MEDS_CONVERSION_EQ");
                } else {
                	dto.setBufr_meds_conversion_eq(" ");              	

                }
            	dto.setNetcdf_variable(rs.getString("NETCDF_VARIABLE"));
            	dto.setNetcdf_bufr_conversion_eq(rs.getString("NETCDF_BUFR_CONVERSION_EQ"));
            	descriptorsList.add(dto);
            }
            rs.close();
            // get Section 3 code for written bufr
            String getParentDescriptor = "Select SECTION3_FXY_SEQ from "
            		+ tablename
            		+ " ORDER BY DESCRIPTOR_ID ASC";
            if (dataType != null){
                getParentDescriptor = "Select SECTION3_FXY_SEQ from "
                		+ tablename
                		+ " where format_template = '"
                        + dataType + "' ORDER BY DESCRIPTOR_ID ASC";

            }            
                               
            rs = stmt.executeQuery(getParentDescriptor);

            // get Section 3 descriptor;
            while (rs.next()) {
            	if (rs.getString("SECTION3_FXY_SEQ")!= null){
            		section3Code.add(rs.getString("SECTION3_FXY_SEQ"));
            	}
                
            }
            rs.close();

        } catch (SQLException e) {
        	log.error(e);
        } finally {
        	try {
				stmt.close();
				rs.close();
	        	connection.close();
			} catch (SQLException e) {
				log.error("SQL exception " + e);				
			}
        	
        }
        success = true;
        return success;
    }
    public boolean getDescriptorInfo (String descriptor){
    	boolean found = false;
        ResultSet rs = null;
        Statement stmt = null;
        String sql;
        try {
        	if (connection.isClosed()){
        		connection = connect();
        	}
			stmt = connection.createStatement();
			sql = "select * from ARGO_GTS.bufrtemplate where descriptor = '"
					+ descriptor + "' order by descriptor_id asc";
			rs = stmt.executeQuery(sql);
			descriptorsList = new ArrayList<BufrDescriptorDto>();
            while (rs.next()) {
            	BufrDescriptorDto dto = new BufrDescriptorDto();
            	dto.setDescriptor_id(rs.getInt("DESCRIPTOR_ID"));
            	dto.setDescriptor(rs.getString("DESCRIPTOR"));
                dto.setDescriptor_child(rs.getString("DESCRIPTOR_CHILD"));
                dto.setForced_value(rs.getInt("FORCED_VALUE"));
                if (rs.getString("FORCED_MISSING") != null){
                    dto.setForced_missing(rs.getString("FORCED_MISSING").charAt(0));
                } else {
                    dto.setForced_missing('N');

                }
            	if (rs.getString("MEDS_PCODE") != null){
            		dto.setMeds_pcode(rs.getString("MEDS_PCODE"));                    
            	} else {
            		dto.setMeds_pcode("");                   
            	}
            	dto.setUnits(rs.getString("UNITS"));
            	if (rs.getInt("CODE_TABLE_MAX")!= java.sql.Types.INTEGER){
            		dto.setCode_table_max(rs.getInt("CODE_TABLE_MAX"));
            	}
            	dto.setScale(rs.getInt("SCALE"));
            	dto.setReference(rs.getInt("REFERENCE"));
            	dto.setData_width(rs.getInt("DATA_WIDTH"));
            	dto.setFormat_template(rs.getString("FORMAT_TEMPLATE"));
            	dto.setSection3_fxy_seq(rs.getString("SECTION3_FXY_SEQ"));
                if (rs.getString("MEDS_BUFR_CONVERSION_EQ") != null){
                    dto.setMeds_bufr_conversion_eq(rs.getString("MEDS_BUFR_CONVERSION_EQ"));
                } else {
                	dto.setMeds_bufr_conversion_eq("");

                }
                if (rs.getString("BUFR_MEDS_CONVERSION_EQ") != null){
                	dto.setBufr_meds_conversion_eq(rs.getString("BUFR_MEDS_CONVERSION_EQ"));
                } else {
                	dto.setBufr_meds_conversion_eq(" ");             	

                }
            	dto.setNetcdf_variable(rs.getString("NETCDF_VARIABLE"));
            	dto.setNetcdf_bufr_conversion_eq(rs.getString("NETCDF_BUFR_CONVERSION_EQ"));
            	descriptorsList.add(dto);
            	found = true;

            }
            rs.close();
            stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
        	try {
				stmt.close();
				rs.close();
	        	connection.close();
			} catch (SQLException e) {
				log.error("SQL exception " + e);				
			}
        	
        }
        
    	return found;
    }
    // end of class


}

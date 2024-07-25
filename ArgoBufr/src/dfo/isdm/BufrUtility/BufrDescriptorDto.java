package dfo.isdm.BufrUtility;

import java.util.List;

public class BufrDescriptorDto {
	private int descriptor_id;
	private String descriptor;
	private String descriptor_child;
	private int forced_value ;
	private char forced_missing;
	private String meds_pcode;
	private String units;
	private int code_table_max;
	private int scale;
	private int reference;
	private int data_width;
	private String format_template;
	private String section3_fxy_seq;
	private String meds_bufr_conversion_eq;
	private String bufr_meds_conversion_eq;
	private String netcdf_variable;
	private String netcdf_bufr_conversion_eq;
	private List<Integer>data_array = null;
	private String decodedValue;	
	public BufrDescriptorDto(){
		
	}

	public int getDescriptor_id() {
		return descriptor_id;
	}

	public void setDescriptor_id(int descriptor_id) {
		this.descriptor_id = descriptor_id;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getDescriptor_child() {
		return descriptor_child;
	}

	public void setDescriptor_child(String descriptor_child) {
		this.descriptor_child = descriptor_child;
	}

	public int getForced_value() {
		return forced_value;
	}

	public void setForced_value(int forced_value) {
		this.forced_value = forced_value;
	}

	public char getForced_missing() {
		return forced_missing;
	}

	public void setForced_missing(char forced_missing) {
		this.forced_missing = forced_missing;
	}

	public String getMeds_pcode() {
		return meds_pcode;
	}

	public void setMeds_pcode(String meds_pcode) {
		this.meds_pcode = meds_pcode;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public int getCode_table_max() {
		return code_table_max;
	}

	public void setCode_table_max(int code_table_max) {
		this.code_table_max = code_table_max;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getReference() {
		return reference;
	}

	public void setReference(int reference) {
		this.reference = reference;
	}

	public int getData_width() {
		return data_width;
	}

	public void setData_width(int data_width) {
		this.data_width = data_width;
	}

	public String getFormat_template() {
		return format_template;
	}

	public void setFormat_template(String format_template) {
		this.format_template = format_template;
	}

	public String getSection3_fxy_seq() {
		return section3_fxy_seq;
	}

	public void setSection3_fxy_seq(String section3_fxy_seq) {
		this.section3_fxy_seq = section3_fxy_seq;
	}

	public String getMeds_bufr_conversion_eq() {
		return meds_bufr_conversion_eq;
	}

	public void setMeds_bufr_conversion_eq(String meds_bufr_conversion_eq) {
		this.meds_bufr_conversion_eq = meds_bufr_conversion_eq;
	}

	public String getBufr_meds_conversion_eq() {
		return bufr_meds_conversion_eq;
	}

	public void setBufr_meds_conversion_eq(String bufr_meds_conversion_eq) {
		this.bufr_meds_conversion_eq = bufr_meds_conversion_eq;
	}

	public String getNetcdf_variable() {
		return netcdf_variable;
	}

	public void setNetcdf_variable(String netcdf_variable) {
		this.netcdf_variable = netcdf_variable;
	}

	public String getNetcdf_bufr_conversion_eq() {
		return netcdf_bufr_conversion_eq;
	}

	public void setNetcdf_bufr_conversion_eq(String netcdf_bufr_conversion_eq) {
		this.netcdf_bufr_conversion_eq = netcdf_bufr_conversion_eq;
	}

	public List<Integer> getData_array() {
		return data_array;
	}

	public void setData_array(List<Integer> data_array) {
		this.data_array = data_array;
	}

	public String getDecodedValue() {
		return decodedValue;
	}

	public void setDecodedValue(String decodedValue) {
		this.decodedValue = decodedValue;
	}
	

}

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
	// copy Constructor
	public BufrDescriptorDto(BufrDescriptorDto other){
		this.descriptor_id = other.descriptor_id;
		this.descriptor = other.descriptor;
		this.descriptor_child= other.descriptor_child;
		this.forced_value = other.forced_value ;
		this.forced_missing = other.forced_missing;
		this.meds_pcode = other.meds_pcode;
		this.units = other.units;
		this.code_table_max = other.code_table_max;
		this.scale = other.scale;
		this.reference = other.reference;
		this.data_width = other.data_width;
		this.format_template = other.format_template;
		this.section3_fxy_seq = other.section3_fxy_seq;
		this.meds_bufr_conversion_eq = other.meds_bufr_conversion_eq;
		this.bufr_meds_conversion_eq= other.bufr_meds_conversion_eq;
		this.netcdf_variable = other.netcdf_variable;
		this.netcdf_bufr_conversion_eq = other.netcdf_bufr_conversion_eq;
		this.data_array = other.data_array;
		this.decodedValue = other.decodedValue;

		
		
	}

	public int getDescriptor_id() {
		return descriptor_id;
	}

	public void setDescriptor_id(int _descriptor_id) {
		descriptor_id = _descriptor_id;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String _descriptor) {
		descriptor = _descriptor;
	}

	public String getDescriptor_child() {
		return descriptor_child;
	}

	public void setDescriptor_child(String _descriptor_child) {
		descriptor_child = _descriptor_child;
	}

	public int getForced_value() {
		return forced_value;
	}

	public void setForced_value(int _forced_value) {
		forced_value = _forced_value;
	}

	public char getForced_missing() {
		return forced_missing;
	}

	public void setForced_missing(char _forced_missing) {
		forced_missing = _forced_missing;
	}

	public String getMeds_pcode() {
		return meds_pcode;
	}

	public void setMeds_pcode(String _meds_pcode) {
		meds_pcode = _meds_pcode;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String _units) {
		units = _units;
	}

	public int getCode_table_max() {
		return code_table_max;
	}

	public void setCode_table_max(int _code_table_max) {
		code_table_max = _code_table_max;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int _scale) {
		scale = _scale;
	}

	public int getReference() {
		return reference;
	}

	public void setReference(int _reference) {
		reference = _reference;
	}

	public int getData_width() {
		return data_width;
	}

	public void setData_width(int _data_width) {
		data_width = _data_width;
	}

	public String getFormat_template() {
		return format_template;
	}

	public void setFormat_template(String _format_template) {
		format_template = _format_template;
	}

	public String getSection3_fxy_seq() {
		return section3_fxy_seq;
	}

	public void setSection3_fxy_seq(String _section3_fxy_seq) {
		section3_fxy_seq = _section3_fxy_seq;
	}

	public String getMeds_bufr_conversion_eq() {
		return meds_bufr_conversion_eq;
	}

	public void setMeds_bufr_conversion_eq(String _meds_bufr_conversion_eq) {
		meds_bufr_conversion_eq = _meds_bufr_conversion_eq;
	}

	public String getBufr_meds_conversion_eq() {
		return bufr_meds_conversion_eq;
	}

	public void setBufr_meds_conversion_eq(String _bufr_meds_conversion_eq) {
		bufr_meds_conversion_eq = _bufr_meds_conversion_eq;
	}

	public String getNetcdf_variable() {
		return netcdf_variable;
	}

	public void setNetcdf_variable(String _netcdf_variable) {
		netcdf_variable = _netcdf_variable;
	}

	public String getNetcdf_bufr_conversion_eq() {
		return netcdf_bufr_conversion_eq;
	}

	public void setNetcdf_bufr_conversion_eq(String _netcdf_bufr_conversion_eq) {
		netcdf_bufr_conversion_eq = _netcdf_bufr_conversion_eq;
	}

	public List<Integer> getData_array() {
		return data_array;
	}

	public void setData_array(List<Integer> _data_array) {
		data_array = _data_array;
	}

	public String getDecodedValue() {
		return decodedValue;
	}

	public void setDecodedValue(String _decodedValue) {
		decodedValue = _decodedValue;
	}
	

}

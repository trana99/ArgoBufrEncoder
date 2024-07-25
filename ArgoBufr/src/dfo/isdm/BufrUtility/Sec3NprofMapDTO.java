package dfo.isdm.BufrUtility;
/*
 * This class will provide the corresponding n_prof profile in the Netcdf files for encoding the specific BUFR sequences
 */
public class Sec3NprofMapDTO {
	private String descriptorId;  /// descriptor started with 3
	private Integer nprof; // corressponding Argo Netcdf nprof

	public Sec3NprofMapDTO() {
		// TODO Auto-generated constructor stub
	}

	public String getDescriptorId() {
		return descriptorId;
	}

	public void setDescriptorId(String _descriptorId) {
		descriptorId = _descriptorId;
	}

	public Integer getNprof() {
		return nprof;
	}

	public void setNprof(Integer _nprof) {
		nprof = _nprof;
	}

}

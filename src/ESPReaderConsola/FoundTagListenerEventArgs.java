package ESPReaderConsola;

public class FoundTagListenerEventArgs {
	private String tagId;
	private String epc;
	private int antenna;
	
	FoundTagListenerEventArgs(String tagId, String epc, int antenna){
		this.tagId = tagId;
		this.epc = epc;
		this.antenna = antenna;
	}
	
	public String getEpc() {
		return epc;
	}
	public void setEpc(String epc) {
		this.epc = epc;
	}
	public String getTagId() {
		return tagId;
	}
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
	public int getAntenna() {
		return antenna;
	}
	public void setAntenna(int antenna) {
		this.antenna = antenna;
	}
}

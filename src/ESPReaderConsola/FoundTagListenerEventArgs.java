package ESPReaderConsola;

public class FoundTagListenerEventArgs {
	private String tagId;
	private String epc;
	private int antenna;
	
	public FoundTagListenerEventArgs(String tagId, String epc, int antenna){
		this.tagId = tagId;
		this.epc = epc;	
		this.antenna = antenna;
	}
	
	public String getEpc() {
		return epc;
	}
	public int getAntenna() {
		return antenna;
	}
	public String getTagId() {
		return tagId;
	}
}

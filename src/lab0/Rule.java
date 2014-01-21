package lab0;



public class Rule {
	private String action = null;
	private String src = null;
	private String dest = null;
	private String kind = null;
	private Integer seqNum = null;
	private boolean duplicate;

	public Rule(String action) {
		this.action = action;
	}
	/*
	 * check whether a message match this rule
	 */
	public boolean match(Message m){
		if (src == null && dest == null && kind == null && seqNum == -1 && duplicate == m.getDuplicate())	//only contains action, it matches all msg
			return true;
		if (src != null && !src.equals(m.getSrc()))
			return false;
		if (dest != null && !dest.equals(m.getDest()))
			return false;
		if (kind != null && !kind.equals(m.getKind()))
			return false;
		if (seqNum != -1 && seqNum != m.getSeqNum())
			return false;
		if (duplicate != m.getDuplicate())
			return false;
		return true;
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Integer getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(Integer seqnum) {
		this.seqNum = seqnum;
	}
	public boolean getDuplicate() {
		return duplicate;
	}

	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}

	
}

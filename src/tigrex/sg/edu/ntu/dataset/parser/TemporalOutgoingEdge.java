package tigrex.sg.edu.ntu.dataset.parser;

public class TemporalOutgoingEdge implements Comparable<TemporalOutgoingEdge> {

	private int target;
	private int startTime;
	private int endTime;
	
	public TemporalOutgoingEdge(int target, int timestamp) {
		this.target = target;
		this.startTime = timestamp;
	}
	
	public int getTarget() {
		return target;
	}
	
	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	
	@Override
	public int compareTo(TemporalOutgoingEdge other) {
		return Integer.compare(this.target, other.target);
	}

}

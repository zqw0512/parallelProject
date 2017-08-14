package Project_2;

import java.io.Serializable;

public class Message implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	Sign sign;
	int pivot;
	Side side;
	Boolean getPivot;
	int count;
	
	public Message(Sign sign) {
		super();
		this.sign = sign;
	}

	public Message(Sign sign, int pivot) {
		super();
		this.sign = sign;
		this.pivot = pivot;
	}

	public Message(Sign sign, Side side) {
		super();
		this.sign = sign;
		this.side = side;
	}

	public Message(int count) {
		super();
		this.count = count;
	}

	public Message(Boolean getPivot, int pivot) {
		super();
		this.getPivot = getPivot;
		this.pivot = pivot;
	}

	public Message(Boolean getPivot) {
		super();
		this.getPivot = getPivot;
	}
}

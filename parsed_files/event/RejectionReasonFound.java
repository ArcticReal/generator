package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RejectionReasonFound implements Event{

	private List<RejectionReason> rejectionReasons;

	public RejectionReasonFound(List<RejectionReason> rejectionReasons) {
		this.setRejectionReasons(rejectionReasons);
	}

	public List<RejectionReason> getRejectionReasons()	{
		return rejectionReasons;
	}

	public void setRejectionReasons(List<RejectionReason> rejectionReasons)	{
		this.rejectionReasons = rejectionReasons;
	}
}

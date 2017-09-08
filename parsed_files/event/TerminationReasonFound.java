package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TerminationReasonFound implements Event{

	private List<TerminationReason> terminationReasons;

	public TerminationReasonFound(List<TerminationReason> terminationReasons) {
		this.setTerminationReasons(terminationReasons);
	}

	public List<TerminationReason> getTerminationReasons()	{
		return terminationReasons;
	}

	public void setTerminationReasons(List<TerminationReason> terminationReasons)	{
		this.terminationReasons = terminationReasons;
	}
}

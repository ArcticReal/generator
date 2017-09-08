package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplLeaveReasonTypeFound implements Event{

	private List<EmplLeaveReasonType> emplLeaveReasonTypes;

	public EmplLeaveReasonTypeFound(List<EmplLeaveReasonType> emplLeaveReasonTypes) {
		this.setEmplLeaveReasonTypes(emplLeaveReasonTypes);
	}

	public List<EmplLeaveReasonType> getEmplLeaveReasonTypes()	{
		return emplLeaveReasonTypes;
	}

	public void setEmplLeaveReasonTypes(List<EmplLeaveReasonType> emplLeaveReasonTypes)	{
		this.emplLeaveReasonTypes = emplLeaveReasonTypes;
	}
}

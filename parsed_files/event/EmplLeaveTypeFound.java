package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplLeaveTypeFound implements Event{

	private List<EmplLeaveType> emplLeaveTypes;

	public EmplLeaveTypeFound(List<EmplLeaveType> emplLeaveTypes) {
		this.setEmplLeaveTypes(emplLeaveTypes);
	}

	public List<EmplLeaveType> getEmplLeaveTypes()	{
		return emplLeaveTypes;
	}

	public void setEmplLeaveTypes(List<EmplLeaveType> emplLeaveTypes)	{
		this.emplLeaveTypes = emplLeaveTypes;
	}
}

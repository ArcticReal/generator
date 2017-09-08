package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplLeaveFound implements Event{

	private List<EmplLeave> emplLeaves;

	public EmplLeaveFound(List<EmplLeave> emplLeaves) {
		this.setEmplLeaves(emplLeaves);
	}

	public List<EmplLeave> getEmplLeaves()	{
		return emplLeaves;
	}

	public void setEmplLeaves(List<EmplLeave> emplLeaves)	{
		this.emplLeaves = emplLeaves;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TimeEntryFound implements Event{

	private List<TimeEntry> timeEntrys;

	public TimeEntryFound(List<TimeEntry> timeEntrys) {
		this.setTimeEntrys(timeEntrys);
	}

	public List<TimeEntry> getTimeEntrys()	{
		return timeEntrys;
	}

	public void setTimeEntrys(List<TimeEntry> timeEntrys)	{
		this.timeEntrys = timeEntrys;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TimesheetFound implements Event{

	private List<Timesheet> timesheets;

	public TimesheetFound(List<Timesheet> timesheets) {
		this.setTimesheets(timesheets);
	}

	public List<Timesheet> getTimesheets()	{
		return timesheets;
	}

	public void setTimesheets(List<Timesheet> timesheets)	{
		this.timesheets = timesheets;
	}
}

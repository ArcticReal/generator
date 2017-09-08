package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TimesheetRoleFound implements Event{

	private List<TimesheetRole> timesheetRoles;

	public TimesheetRoleFound(List<TimesheetRole> timesheetRoles) {
		this.setTimesheetRoles(timesheetRoles);
	}

	public List<TimesheetRole> getTimesheetRoles()	{
		return timesheetRoles;
	}

	public void setTimesheetRoles(List<TimesheetRole> timesheetRoles)	{
		this.timesheetRoles = timesheetRoles;
	}
}

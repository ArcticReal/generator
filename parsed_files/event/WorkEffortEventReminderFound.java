package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortEventReminderFound implements Event{

	private List<WorkEffortEventReminder> workEffortEventReminders;

	public WorkEffortEventReminderFound(List<WorkEffortEventReminder> workEffortEventReminders) {
		this.setWorkEffortEventReminders(workEffortEventReminders);
	}

	public List<WorkEffortEventReminder> getWorkEffortEventReminders()	{
		return workEffortEventReminders;
	}

	public void setWorkEffortEventReminders(List<WorkEffortEventReminder> workEffortEventReminders)	{
		this.workEffortEventReminders = workEffortEventReminders;
	}
}

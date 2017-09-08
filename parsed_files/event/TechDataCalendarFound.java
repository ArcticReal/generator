package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TechDataCalendarFound implements Event{

	private List<TechDataCalendar> techDataCalendars;

	public TechDataCalendarFound(List<TechDataCalendar> techDataCalendars) {
		this.setTechDataCalendars(techDataCalendars);
	}

	public List<TechDataCalendar> getTechDataCalendars()	{
		return techDataCalendars;
	}

	public void setTechDataCalendars(List<TechDataCalendar> techDataCalendars)	{
		this.techDataCalendars = techDataCalendars;
	}
}

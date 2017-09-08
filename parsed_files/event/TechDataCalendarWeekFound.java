package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TechDataCalendarWeekFound implements Event{

	private List<TechDataCalendarWeek> techDataCalendarWeeks;

	public TechDataCalendarWeekFound(List<TechDataCalendarWeek> techDataCalendarWeeks) {
		this.setTechDataCalendarWeeks(techDataCalendarWeeks);
	}

	public List<TechDataCalendarWeek> getTechDataCalendarWeeks()	{
		return techDataCalendarWeeks;
	}

	public void setTechDataCalendarWeeks(List<TechDataCalendarWeek> techDataCalendarWeeks)	{
		this.techDataCalendarWeeks = techDataCalendarWeeks;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TechDataCalendarExcWeekFound implements Event{

	private List<TechDataCalendarExcWeek> techDataCalendarExcWeeks;

	public TechDataCalendarExcWeekFound(List<TechDataCalendarExcWeek> techDataCalendarExcWeeks) {
		this.setTechDataCalendarExcWeeks(techDataCalendarExcWeeks);
	}

	public List<TechDataCalendarExcWeek> getTechDataCalendarExcWeeks()	{
		return techDataCalendarExcWeeks;
	}

	public void setTechDataCalendarExcWeeks(List<TechDataCalendarExcWeek> techDataCalendarExcWeeks)	{
		this.techDataCalendarExcWeeks = techDataCalendarExcWeeks;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TechDataCalendarExcDayFound implements Event{

	private List<TechDataCalendarExcDay> techDataCalendarExcDays;

	public TechDataCalendarExcDayFound(List<TechDataCalendarExcDay> techDataCalendarExcDays) {
		this.setTechDataCalendarExcDays(techDataCalendarExcDays);
	}

	public List<TechDataCalendarExcDay> getTechDataCalendarExcDays()	{
		return techDataCalendarExcDays;
	}

	public void setTechDataCalendarExcDays(List<TechDataCalendarExcDay> techDataCalendarExcDays)	{
		this.techDataCalendarExcDays = techDataCalendarExcDays;
	}
}

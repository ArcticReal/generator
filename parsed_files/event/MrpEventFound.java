package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MrpEventFound implements Event{

	private List<MrpEvent> mrpEvents;

	public MrpEventFound(List<MrpEvent> mrpEvents) {
		this.setMrpEvents(mrpEvents);
	}

	public List<MrpEvent> getMrpEvents()	{
		return mrpEvents;
	}

	public void setMrpEvents(List<MrpEvent> mrpEvents)	{
		this.mrpEvents = mrpEvents;
	}
}

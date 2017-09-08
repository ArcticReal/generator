package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MrpEventTypeFound implements Event{

	private List<MrpEventType> mrpEventTypes;

	public MrpEventTypeFound(List<MrpEventType> mrpEventTypes) {
		this.setMrpEventTypes(mrpEventTypes);
	}

	public List<MrpEventType> getMrpEventTypes()	{
		return mrpEventTypes;
	}

	public void setMrpEventTypes(List<MrpEventType> mrpEventTypes)	{
		this.mrpEventTypes = mrpEventTypes;
	}
}

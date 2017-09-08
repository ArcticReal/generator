package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplPositionTypeFound implements Event{

	private List<EmplPositionType> emplPositionTypes;

	public EmplPositionTypeFound(List<EmplPositionType> emplPositionTypes) {
		this.setEmplPositionTypes(emplPositionTypes);
	}

	public List<EmplPositionType> getEmplPositionTypes()	{
		return emplPositionTypes;
	}

	public void setEmplPositionTypes(List<EmplPositionType> emplPositionTypes)	{
		this.emplPositionTypes = emplPositionTypes;
	}
}

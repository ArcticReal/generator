package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplPositionClassTypeFound implements Event{

	private List<EmplPositionClassType> emplPositionClassTypes;

	public EmplPositionClassTypeFound(List<EmplPositionClassType> emplPositionClassTypes) {
		this.setEmplPositionClassTypes(emplPositionClassTypes);
	}

	public List<EmplPositionClassType> getEmplPositionClassTypes()	{
		return emplPositionClassTypes;
	}

	public void setEmplPositionClassTypes(List<EmplPositionClassType> emplPositionClassTypes)	{
		this.emplPositionClassTypes = emplPositionClassTypes;
	}
}

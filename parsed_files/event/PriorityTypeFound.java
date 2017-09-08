package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PriorityTypeFound implements Event{

	private List<PriorityType> priorityTypes;

	public PriorityTypeFound(List<PriorityType> priorityTypes) {
		this.setPriorityTypes(priorityTypes);
	}

	public List<PriorityType> getPriorityTypes()	{
		return priorityTypes;
	}

	public void setPriorityTypes(List<PriorityType> priorityTypes)	{
		this.priorityTypes = priorityTypes;
	}
}

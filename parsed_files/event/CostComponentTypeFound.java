package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CostComponentTypeFound implements Event{

	private List<CostComponentType> costComponentTypes;

	public CostComponentTypeFound(List<CostComponentType> costComponentTypes) {
		this.setCostComponentTypes(costComponentTypes);
	}

	public List<CostComponentType> getCostComponentTypes()	{
		return costComponentTypes;
	}

	public void setCostComponentTypes(List<CostComponentType> costComponentTypes)	{
		this.costComponentTypes = costComponentTypes;
	}
}

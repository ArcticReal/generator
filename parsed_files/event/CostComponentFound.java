package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CostComponentFound implements Event{

	private List<CostComponent> costComponents;

	public CostComponentFound(List<CostComponent> costComponents) {
		this.setCostComponents(costComponents);
	}

	public List<CostComponent> getCostComponents()	{
		return costComponents;
	}

	public void setCostComponents(List<CostComponent> costComponents)	{
		this.costComponents = costComponents;
	}
}

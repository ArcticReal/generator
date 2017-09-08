package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CostComponentAttributeFound implements Event{

	private List<CostComponentAttribute> costComponentAttributes;

	public CostComponentAttributeFound(List<CostComponentAttribute> costComponentAttributes) {
		this.setCostComponentAttributes(costComponentAttributes);
	}

	public List<CostComponentAttribute> getCostComponentAttributes()	{
		return costComponentAttributes;
	}

	public void setCostComponentAttributes(List<CostComponentAttribute> costComponentAttributes)	{
		this.costComponentAttributes = costComponentAttributes;
	}
}

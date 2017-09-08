package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuantityBreakTypeFound implements Event{

	private List<QuantityBreakType> quantityBreakTypes;

	public QuantityBreakTypeFound(List<QuantityBreakType> quantityBreakTypes) {
		this.setQuantityBreakTypes(quantityBreakTypes);
	}

	public List<QuantityBreakType> getQuantityBreakTypes()	{
		return quantityBreakTypes;
	}

	public void setQuantityBreakTypes(List<QuantityBreakType> quantityBreakTypes)	{
		this.quantityBreakTypes = quantityBreakTypes;
	}
}

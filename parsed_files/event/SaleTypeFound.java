package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SaleTypeFound implements Event{

	private List<SaleType> saleTypes;

	public SaleTypeFound(List<SaleType> saleTypes) {
		this.setSaleTypes(saleTypes);
	}

	public List<SaleType> getSaleTypes()	{
		return saleTypes;
	}

	public void setSaleTypes(List<SaleType> saleTypes)	{
		this.saleTypes = saleTypes;
	}
}

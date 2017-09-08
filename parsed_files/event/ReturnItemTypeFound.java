package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnItemTypeFound implements Event{

	private List<ReturnItemType> returnItemTypes;

	public ReturnItemTypeFound(List<ReturnItemType> returnItemTypes) {
		this.setReturnItemTypes(returnItemTypes);
	}

	public List<ReturnItemType> getReturnItemTypes()	{
		return returnItemTypes;
	}

	public void setReturnItemTypes(List<ReturnItemType> returnItemTypes)	{
		this.returnItemTypes = returnItemTypes;
	}
}

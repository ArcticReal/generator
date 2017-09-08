package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnItemFound implements Event{

	private List<ReturnItem> returnItems;

	public ReturnItemFound(List<ReturnItem> returnItems) {
		this.setReturnItems(returnItems);
	}

	public List<ReturnItem> getReturnItems()	{
		return returnItems;
	}

	public void setReturnItems(List<ReturnItem> returnItems)	{
		this.returnItems = returnItems;
	}
}

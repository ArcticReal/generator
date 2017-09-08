package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PicklistItemFound implements Event{

	private List<PicklistItem> picklistItems;

	public PicklistItemFound(List<PicklistItem> picklistItems) {
		this.setPicklistItems(picklistItems);
	}

	public List<PicklistItem> getPicklistItems()	{
		return picklistItems;
	}

	public void setPicklistItems(List<PicklistItem> picklistItems)	{
		this.picklistItems = picklistItems;
	}
}

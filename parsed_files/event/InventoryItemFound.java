package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemFound implements Event{

	private List<InventoryItem> inventoryItems;

	public InventoryItemFound(List<InventoryItem> inventoryItems) {
		this.setInventoryItems(inventoryItems);
	}

	public List<InventoryItem> getInventoryItems()	{
		return inventoryItems;
	}

	public void setInventoryItems(List<InventoryItem> inventoryItems)	{
		this.inventoryItems = inventoryItems;
	}
}

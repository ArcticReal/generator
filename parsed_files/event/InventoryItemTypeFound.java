package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemTypeFound implements Event{

	private List<InventoryItemType> inventoryItemTypes;

	public InventoryItemTypeFound(List<InventoryItemType> inventoryItemTypes) {
		this.setInventoryItemTypes(inventoryItemTypes);
	}

	public List<InventoryItemType> getInventoryItemTypes()	{
		return inventoryItemTypes;
	}

	public void setInventoryItemTypes(List<InventoryItemType> inventoryItemTypes)	{
		this.inventoryItemTypes = inventoryItemTypes;
	}
}

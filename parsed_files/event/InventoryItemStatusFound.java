package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemStatusFound implements Event{

	private List<InventoryItemStatus> inventoryItemStatuss;

	public InventoryItemStatusFound(List<InventoryItemStatus> inventoryItemStatuss) {
		this.setInventoryItemStatuss(inventoryItemStatuss);
	}

	public List<InventoryItemStatus> getInventoryItemStatuss()	{
		return inventoryItemStatuss;
	}

	public void setInventoryItemStatuss(List<InventoryItemStatus> inventoryItemStatuss)	{
		this.inventoryItemStatuss = inventoryItemStatuss;
	}
}

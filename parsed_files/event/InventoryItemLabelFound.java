package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemLabelFound implements Event{

	private List<InventoryItemLabel> inventoryItemLabels;

	public InventoryItemLabelFound(List<InventoryItemLabel> inventoryItemLabels) {
		this.setInventoryItemLabels(inventoryItemLabels);
	}

	public List<InventoryItemLabel> getInventoryItemLabels()	{
		return inventoryItemLabels;
	}

	public void setInventoryItemLabels(List<InventoryItemLabel> inventoryItemLabels)	{
		this.inventoryItemLabels = inventoryItemLabels;
	}
}

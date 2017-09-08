package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemVarianceFound implements Event{

	private List<InventoryItemVariance> inventoryItemVariances;

	public InventoryItemVarianceFound(List<InventoryItemVariance> inventoryItemVariances) {
		this.setInventoryItemVariances(inventoryItemVariances);
	}

	public List<InventoryItemVariance> getInventoryItemVariances()	{
		return inventoryItemVariances;
	}

	public void setInventoryItemVariances(List<InventoryItemVariance> inventoryItemVariances)	{
		this.inventoryItemVariances = inventoryItemVariances;
	}
}

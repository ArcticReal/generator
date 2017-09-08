package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemTempResFound implements Event{

	private List<InventoryItemTempRes> inventoryItemTempRess;

	public InventoryItemTempResFound(List<InventoryItemTempRes> inventoryItemTempRess) {
		this.setInventoryItemTempRess(inventoryItemTempRess);
	}

	public List<InventoryItemTempRes> getInventoryItemTempRess()	{
		return inventoryItemTempRess;
	}

	public void setInventoryItemTempRess(List<InventoryItemTempRes> inventoryItemTempRess)	{
		this.inventoryItemTempRess = inventoryItemTempRess;
	}
}

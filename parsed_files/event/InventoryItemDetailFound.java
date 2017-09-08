package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemDetailFound implements Event{

	private List<InventoryItemDetail> inventoryItemDetails;

	public InventoryItemDetailFound(List<InventoryItemDetail> inventoryItemDetails) {
		this.setInventoryItemDetails(inventoryItemDetails);
	}

	public List<InventoryItemDetail> getInventoryItemDetails()	{
		return inventoryItemDetails;
	}

	public void setInventoryItemDetails(List<InventoryItemDetail> inventoryItemDetails)	{
		this.inventoryItemDetails = inventoryItemDetails;
	}
}

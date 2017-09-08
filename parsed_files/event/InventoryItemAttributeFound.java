package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemAttributeFound implements Event{

	private List<InventoryItemAttribute> inventoryItemAttributes;

	public InventoryItemAttributeFound(List<InventoryItemAttribute> inventoryItemAttributes) {
		this.setInventoryItemAttributes(inventoryItemAttributes);
	}

	public List<InventoryItemAttribute> getInventoryItemAttributes()	{
		return inventoryItemAttributes;
	}

	public void setInventoryItemAttributes(List<InventoryItemAttribute> inventoryItemAttributes)	{
		this.inventoryItemAttributes = inventoryItemAttributes;
	}
}

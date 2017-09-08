package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemLabelTypeFound implements Event{

	private List<InventoryItemLabelType> inventoryItemLabelTypes;

	public InventoryItemLabelTypeFound(List<InventoryItemLabelType> inventoryItemLabelTypes) {
		this.setInventoryItemLabelTypes(inventoryItemLabelTypes);
	}

	public List<InventoryItemLabelType> getInventoryItemLabelTypes()	{
		return inventoryItemLabelTypes;
	}

	public void setInventoryItemLabelTypes(List<InventoryItemLabelType> inventoryItemLabelTypes)	{
		this.inventoryItemLabelTypes = inventoryItemLabelTypes;
	}
}

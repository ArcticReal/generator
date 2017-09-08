package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemTypeAttrFound implements Event{

	private List<InventoryItemTypeAttr> inventoryItemTypeAttrs;

	public InventoryItemTypeAttrFound(List<InventoryItemTypeAttr> inventoryItemTypeAttrs) {
		this.setInventoryItemTypeAttrs(inventoryItemTypeAttrs);
	}

	public List<InventoryItemTypeAttr> getInventoryItemTypeAttrs()	{
		return inventoryItemTypeAttrs;
	}

	public void setInventoryItemTypeAttrs(List<InventoryItemTypeAttr> inventoryItemTypeAttrs)	{
		this.inventoryItemTypeAttrs = inventoryItemTypeAttrs;
	}
}

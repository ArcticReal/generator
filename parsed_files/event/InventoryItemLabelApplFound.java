package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryItemLabelApplFound implements Event{

	private List<InventoryItemLabelAppl> inventoryItemLabelAppls;

	public InventoryItemLabelApplFound(List<InventoryItemLabelAppl> inventoryItemLabelAppls) {
		this.setInventoryItemLabelAppls(inventoryItemLabelAppls);
	}

	public List<InventoryItemLabelAppl> getInventoryItemLabelAppls()	{
		return inventoryItemLabelAppls;
	}

	public void setInventoryItemLabelAppls(List<InventoryItemLabelAppl> inventoryItemLabelAppls)	{
		this.inventoryItemLabelAppls = inventoryItemLabelAppls;
	}
}

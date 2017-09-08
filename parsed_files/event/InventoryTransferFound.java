package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InventoryTransferFound implements Event{

	private List<InventoryTransfer> inventoryTransfers;

	public InventoryTransferFound(List<InventoryTransfer> inventoryTransfers) {
		this.setInventoryTransfers(inventoryTransfers);
	}

	public List<InventoryTransfer> getInventoryTransfers()	{
		return inventoryTransfers;
	}

	public void setInventoryTransfers(List<InventoryTransfer> inventoryTransfers)	{
		this.inventoryTransfers = inventoryTransfers;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentReceiptFound implements Event{

	private List<ShipmentReceipt> shipmentReceipts;

	public ShipmentReceiptFound(List<ShipmentReceipt> shipmentReceipts) {
		this.setShipmentReceipts(shipmentReceipts);
	}

	public List<ShipmentReceipt> getShipmentReceipts()	{
		return shipmentReceipts;
	}

	public void setShipmentReceipts(List<ShipmentReceipt> shipmentReceipts)	{
		this.shipmentReceipts = shipmentReceipts;
	}
}

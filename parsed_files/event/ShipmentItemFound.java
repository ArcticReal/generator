package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentItemFound implements Event{

	private List<ShipmentItem> shipmentItems;

	public ShipmentItemFound(List<ShipmentItem> shipmentItems) {
		this.setShipmentItems(shipmentItems);
	}

	public List<ShipmentItem> getShipmentItems()	{
		return shipmentItems;
	}

	public void setShipmentItems(List<ShipmentItem> shipmentItems)	{
		this.shipmentItems = shipmentItems;
	}
}

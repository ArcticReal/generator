package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentFound implements Event{

	private List<Shipment> shipments;

	public ShipmentFound(List<Shipment> shipments) {
		this.setShipments(shipments);
	}

	public List<Shipment> getShipments()	{
		return shipments;
	}

	public void setShipments(List<Shipment> shipments)	{
		this.shipments = shipments;
	}
}

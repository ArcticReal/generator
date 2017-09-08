package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentAttributeFound implements Event{

	private List<ShipmentAttribute> shipmentAttributes;

	public ShipmentAttributeFound(List<ShipmentAttribute> shipmentAttributes) {
		this.setShipmentAttributes(shipmentAttributes);
	}

	public List<ShipmentAttribute> getShipmentAttributes()	{
		return shipmentAttributes;
	}

	public void setShipmentAttributes(List<ShipmentAttribute> shipmentAttributes)	{
		this.shipmentAttributes = shipmentAttributes;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentBoxTypeFound implements Event{

	private List<ShipmentBoxType> shipmentBoxTypes;

	public ShipmentBoxTypeFound(List<ShipmentBoxType> shipmentBoxTypes) {
		this.setShipmentBoxTypes(shipmentBoxTypes);
	}

	public List<ShipmentBoxType> getShipmentBoxTypes()	{
		return shipmentBoxTypes;
	}

	public void setShipmentBoxTypes(List<ShipmentBoxType> shipmentBoxTypes)	{
		this.shipmentBoxTypes = shipmentBoxTypes;
	}
}

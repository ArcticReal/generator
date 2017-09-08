package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentTypeFound implements Event{

	private List<ShipmentType> shipmentTypes;

	public ShipmentTypeFound(List<ShipmentType> shipmentTypes) {
		this.setShipmentTypes(shipmentTypes);
	}

	public List<ShipmentType> getShipmentTypes()	{
		return shipmentTypes;
	}

	public void setShipmentTypes(List<ShipmentType> shipmentTypes)	{
		this.shipmentTypes = shipmentTypes;
	}
}

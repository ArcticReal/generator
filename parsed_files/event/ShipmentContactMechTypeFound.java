package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentContactMechTypeFound implements Event{

	private List<ShipmentContactMechType> shipmentContactMechTypes;

	public ShipmentContactMechTypeFound(List<ShipmentContactMechType> shipmentContactMechTypes) {
		this.setShipmentContactMechTypes(shipmentContactMechTypes);
	}

	public List<ShipmentContactMechType> getShipmentContactMechTypes()	{
		return shipmentContactMechTypes;
	}

	public void setShipmentContactMechTypes(List<ShipmentContactMechType> shipmentContactMechTypes)	{
		this.shipmentContactMechTypes = shipmentContactMechTypes;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentMethodTypeFound implements Event{

	private List<ShipmentMethodType> shipmentMethodTypes;

	public ShipmentMethodTypeFound(List<ShipmentMethodType> shipmentMethodTypes) {
		this.setShipmentMethodTypes(shipmentMethodTypes);
	}

	public List<ShipmentMethodType> getShipmentMethodTypes()	{
		return shipmentMethodTypes;
	}

	public void setShipmentMethodTypes(List<ShipmentMethodType> shipmentMethodTypes)	{
		this.shipmentMethodTypes = shipmentMethodTypes;
	}
}

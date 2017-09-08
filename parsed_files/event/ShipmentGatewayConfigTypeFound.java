package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentGatewayConfigTypeFound implements Event{

	private List<ShipmentGatewayConfigType> shipmentGatewayConfigTypes;

	public ShipmentGatewayConfigTypeFound(List<ShipmentGatewayConfigType> shipmentGatewayConfigTypes) {
		this.setShipmentGatewayConfigTypes(shipmentGatewayConfigTypes);
	}

	public List<ShipmentGatewayConfigType> getShipmentGatewayConfigTypes()	{
		return shipmentGatewayConfigTypes;
	}

	public void setShipmentGatewayConfigTypes(List<ShipmentGatewayConfigType> shipmentGatewayConfigTypes)	{
		this.shipmentGatewayConfigTypes = shipmentGatewayConfigTypes;
	}
}

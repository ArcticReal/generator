package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentGatewayFedexFound implements Event{

	private List<ShipmentGatewayFedex> shipmentGatewayFedexs;

	public ShipmentGatewayFedexFound(List<ShipmentGatewayFedex> shipmentGatewayFedexs) {
		this.setShipmentGatewayFedexs(shipmentGatewayFedexs);
	}

	public List<ShipmentGatewayFedex> getShipmentGatewayFedexs()	{
		return shipmentGatewayFedexs;
	}

	public void setShipmentGatewayFedexs(List<ShipmentGatewayFedex> shipmentGatewayFedexs)	{
		this.shipmentGatewayFedexs = shipmentGatewayFedexs;
	}
}

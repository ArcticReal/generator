package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentGatewayUspsFound implements Event{

	private List<ShipmentGatewayUsps> shipmentGatewayUspss;

	public ShipmentGatewayUspsFound(List<ShipmentGatewayUsps> shipmentGatewayUspss) {
		this.setShipmentGatewayUspss(shipmentGatewayUspss);
	}

	public List<ShipmentGatewayUsps> getShipmentGatewayUspss()	{
		return shipmentGatewayUspss;
	}

	public void setShipmentGatewayUspss(List<ShipmentGatewayUsps> shipmentGatewayUspss)	{
		this.shipmentGatewayUspss = shipmentGatewayUspss;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentGatewayUpsFound implements Event{

	private List<ShipmentGatewayUps> shipmentGatewayUpss;

	public ShipmentGatewayUpsFound(List<ShipmentGatewayUps> shipmentGatewayUpss) {
		this.setShipmentGatewayUpss(shipmentGatewayUpss);
	}

	public List<ShipmentGatewayUps> getShipmentGatewayUpss()	{
		return shipmentGatewayUpss;
	}

	public void setShipmentGatewayUpss(List<ShipmentGatewayUps> shipmentGatewayUpss)	{
		this.shipmentGatewayUpss = shipmentGatewayUpss;
	}
}

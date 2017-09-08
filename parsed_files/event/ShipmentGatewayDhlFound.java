package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentGatewayDhlFound implements Event{

	private List<ShipmentGatewayDhl> shipmentGatewayDhls;

	public ShipmentGatewayDhlFound(List<ShipmentGatewayDhl> shipmentGatewayDhls) {
		this.setShipmentGatewayDhls(shipmentGatewayDhls);
	}

	public List<ShipmentGatewayDhl> getShipmentGatewayDhls()	{
		return shipmentGatewayDhls;
	}

	public void setShipmentGatewayDhls(List<ShipmentGatewayDhl> shipmentGatewayDhls)	{
		this.shipmentGatewayDhls = shipmentGatewayDhls;
	}
}

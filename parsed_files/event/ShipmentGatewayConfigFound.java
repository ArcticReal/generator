package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentGatewayConfigFound implements Event{

	private List<ShipmentGatewayConfig> shipmentGatewayConfigs;

	public ShipmentGatewayConfigFound(List<ShipmentGatewayConfig> shipmentGatewayConfigs) {
		this.setShipmentGatewayConfigs(shipmentGatewayConfigs);
	}

	public List<ShipmentGatewayConfig> getShipmentGatewayConfigs()	{
		return shipmentGatewayConfigs;
	}

	public void setShipmentGatewayConfigs(List<ShipmentGatewayConfig> shipmentGatewayConfigs)	{
		this.shipmentGatewayConfigs = shipmentGatewayConfigs;
	}
}

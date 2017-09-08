package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentPackageRouteSegFound implements Event{

	private List<ShipmentPackageRouteSeg> shipmentPackageRouteSegs;

	public ShipmentPackageRouteSegFound(List<ShipmentPackageRouteSeg> shipmentPackageRouteSegs) {
		this.setShipmentPackageRouteSegs(shipmentPackageRouteSegs);
	}

	public List<ShipmentPackageRouteSeg> getShipmentPackageRouteSegs()	{
		return shipmentPackageRouteSegs;
	}

	public void setShipmentPackageRouteSegs(List<ShipmentPackageRouteSeg> shipmentPackageRouteSegs)	{
		this.shipmentPackageRouteSegs = shipmentPackageRouteSegs;
	}
}

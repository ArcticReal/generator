package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentPackageFound implements Event{

	private List<ShipmentPackage> shipmentPackages;

	public ShipmentPackageFound(List<ShipmentPackage> shipmentPackages) {
		this.setShipmentPackages(shipmentPackages);
	}

	public List<ShipmentPackage> getShipmentPackages()	{
		return shipmentPackages;
	}

	public void setShipmentPackages(List<ShipmentPackage> shipmentPackages)	{
		this.shipmentPackages = shipmentPackages;
	}
}

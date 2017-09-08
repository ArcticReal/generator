package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentItemFeatureFound implements Event{

	private List<ShipmentItemFeature> shipmentItemFeatures;

	public ShipmentItemFeatureFound(List<ShipmentItemFeature> shipmentItemFeatures) {
		this.setShipmentItemFeatures(shipmentItemFeatures);
	}

	public List<ShipmentItemFeature> getShipmentItemFeatures()	{
		return shipmentItemFeatures;
	}

	public void setShipmentItemFeatures(List<ShipmentItemFeature> shipmentItemFeatures)	{
		this.shipmentItemFeatures = shipmentItemFeatures;
	}
}

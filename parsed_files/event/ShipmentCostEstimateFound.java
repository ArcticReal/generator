package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentCostEstimateFound implements Event{

	private List<ShipmentCostEstimate> shipmentCostEstimates;

	public ShipmentCostEstimateFound(List<ShipmentCostEstimate> shipmentCostEstimates) {
		this.setShipmentCostEstimates(shipmentCostEstimates);
	}

	public List<ShipmentCostEstimate> getShipmentCostEstimates()	{
		return shipmentCostEstimates;
	}

	public void setShipmentCostEstimates(List<ShipmentCostEstimate> shipmentCostEstimates)	{
		this.shipmentCostEstimates = shipmentCostEstimates;
	}
}

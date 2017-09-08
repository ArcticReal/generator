package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CarrierShipmentBoxTypeFound implements Event{

	private List<CarrierShipmentBoxType> carrierShipmentBoxTypes;

	public CarrierShipmentBoxTypeFound(List<CarrierShipmentBoxType> carrierShipmentBoxTypes) {
		this.setCarrierShipmentBoxTypes(carrierShipmentBoxTypes);
	}

	public List<CarrierShipmentBoxType> getCarrierShipmentBoxTypes()	{
		return carrierShipmentBoxTypes;
	}

	public void setCarrierShipmentBoxTypes(List<CarrierShipmentBoxType> carrierShipmentBoxTypes)	{
		this.carrierShipmentBoxTypes = carrierShipmentBoxTypes;
	}
}

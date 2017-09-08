package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CarrierShipmentMethodFound implements Event{

	private List<CarrierShipmentMethod> carrierShipmentMethods;

	public CarrierShipmentMethodFound(List<CarrierShipmentMethod> carrierShipmentMethods) {
		this.setCarrierShipmentMethods(carrierShipmentMethods);
	}

	public List<CarrierShipmentMethod> getCarrierShipmentMethods()	{
		return carrierShipmentMethods;
	}

	public void setCarrierShipmentMethods(List<CarrierShipmentMethod> carrierShipmentMethods)	{
		this.carrierShipmentMethods = carrierShipmentMethods;
	}
}

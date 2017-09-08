package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityCarrierShipmentFound implements Event{

	private List<FacilityCarrierShipment> facilityCarrierShipments;

	public FacilityCarrierShipmentFound(List<FacilityCarrierShipment> facilityCarrierShipments) {
		this.setFacilityCarrierShipments(facilityCarrierShipments);
	}

	public List<FacilityCarrierShipment> getFacilityCarrierShipments()	{
		return facilityCarrierShipments;
	}

	public void setFacilityCarrierShipments(List<FacilityCarrierShipment> facilityCarrierShipments)	{
		this.facilityCarrierShipments = facilityCarrierShipments;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityTypeAttrFound implements Event{

	private List<FacilityTypeAttr> facilityTypeAttrs;

	public FacilityTypeAttrFound(List<FacilityTypeAttr> facilityTypeAttrs) {
		this.setFacilityTypeAttrs(facilityTypeAttrs);
	}

	public List<FacilityTypeAttr> getFacilityTypeAttrs()	{
		return facilityTypeAttrs;
	}

	public void setFacilityTypeAttrs(List<FacilityTypeAttr> facilityTypeAttrs)	{
		this.facilityTypeAttrs = facilityTypeAttrs;
	}
}

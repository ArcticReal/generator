package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityFound implements Event{

	private List<Facility> facilitys;

	public FacilityFound(List<Facility> facilitys) {
		this.setFacilitys(facilitys);
	}

	public List<Facility> getFacilitys()	{
		return facilitys;
	}

	public void setFacilitys(List<Facility> facilitys)	{
		this.facilitys = facilitys;
	}
}

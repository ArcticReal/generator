package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AccommodationClassFound implements Event{

	private List<AccommodationClass> accommodationClasss;

	public AccommodationClassFound(List<AccommodationClass> accommodationClasss) {
		this.setAccommodationClasss(accommodationClasss);
	}

	public List<AccommodationClass> getAccommodationClasss()	{
		return accommodationClasss;
	}

	public void setAccommodationClasss(List<AccommodationClass> accommodationClasss)	{
		this.accommodationClasss = accommodationClasss;
	}
}

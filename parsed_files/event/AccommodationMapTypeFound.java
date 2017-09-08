package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AccommodationMapTypeFound implements Event{

	private List<AccommodationMapType> accommodationMapTypes;

	public AccommodationMapTypeFound(List<AccommodationMapType> accommodationMapTypes) {
		this.setAccommodationMapTypes(accommodationMapTypes);
	}

	public List<AccommodationMapType> getAccommodationMapTypes()	{
		return accommodationMapTypes;
	}

	public void setAccommodationMapTypes(List<AccommodationMapType> accommodationMapTypes)	{
		this.accommodationMapTypes = accommodationMapTypes;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AccommodationMapFound implements Event{

	private List<AccommodationMap> accommodationMaps;

	public AccommodationMapFound(List<AccommodationMap> accommodationMaps) {
		this.setAccommodationMaps(accommodationMaps);
	}

	public List<AccommodationMap> getAccommodationMaps()	{
		return accommodationMaps;
	}

	public void setAccommodationMaps(List<AccommodationMap> accommodationMaps)	{
		this.accommodationMaps = accommodationMaps;
	}
}

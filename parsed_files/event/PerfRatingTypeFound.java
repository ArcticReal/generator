package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PerfRatingTypeFound implements Event{

	private List<PerfRatingType> perfRatingTypes;

	public PerfRatingTypeFound(List<PerfRatingType> perfRatingTypes) {
		this.setPerfRatingTypes(perfRatingTypes);
	}

	public List<PerfRatingType> getPerfRatingTypes()	{
		return perfRatingTypes;
	}

	public void setPerfRatingTypes(List<PerfRatingType> perfRatingTypes)	{
		this.perfRatingTypes = perfRatingTypes;
	}
}

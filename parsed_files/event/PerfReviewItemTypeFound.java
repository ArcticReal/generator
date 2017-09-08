package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PerfReviewItemTypeFound implements Event{

	private List<PerfReviewItemType> perfReviewItemTypes;

	public PerfReviewItemTypeFound(List<PerfReviewItemType> perfReviewItemTypes) {
		this.setPerfReviewItemTypes(perfReviewItemTypes);
	}

	public List<PerfReviewItemType> getPerfReviewItemTypes()	{
		return perfReviewItemTypes;
	}

	public void setPerfReviewItemTypes(List<PerfReviewItemType> perfReviewItemTypes)	{
		this.perfReviewItemTypes = perfReviewItemTypes;
	}
}

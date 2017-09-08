package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PerfReviewFound implements Event{

	private List<PerfReview> perfReviews;

	public PerfReviewFound(List<PerfReview> perfReviews) {
		this.setPerfReviews(perfReviews);
	}

	public List<PerfReview> getPerfReviews()	{
		return perfReviews;
	}

	public void setPerfReviews(List<PerfReview> perfReviews)	{
		this.perfReviews = perfReviews;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortReviewFound implements Event{

	private List<WorkEffortReview> workEffortReviews;

	public WorkEffortReviewFound(List<WorkEffortReview> workEffortReviews) {
		this.setWorkEffortReviews(workEffortReviews);
	}

	public List<WorkEffortReview> getWorkEffortReviews()	{
		return workEffortReviews;
	}

	public void setWorkEffortReviews(List<WorkEffortReview> workEffortReviews)	{
		this.workEffortReviews = workEffortReviews;
	}
}

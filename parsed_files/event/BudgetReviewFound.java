package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetReviewFound implements Event{

	private List<BudgetReview> budgetReviews;

	public BudgetReviewFound(List<BudgetReview> budgetReviews) {
		this.setBudgetReviews(budgetReviews);
	}

	public List<BudgetReview> getBudgetReviews()	{
		return budgetReviews;
	}

	public void setBudgetReviews(List<BudgetReview> budgetReviews)	{
		this.budgetReviews = budgetReviews;
	}
}

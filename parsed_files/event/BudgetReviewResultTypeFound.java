package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetReviewResultTypeFound implements Event{

	private List<BudgetReviewResultType> budgetReviewResultTypes;

	public BudgetReviewResultTypeFound(List<BudgetReviewResultType> budgetReviewResultTypes) {
		this.setBudgetReviewResultTypes(budgetReviewResultTypes);
	}

	public List<BudgetReviewResultType> getBudgetReviewResultTypes()	{
		return budgetReviewResultTypes;
	}

	public void setBudgetReviewResultTypes(List<BudgetReviewResultType> budgetReviewResultTypes)	{
		this.budgetReviewResultTypes = budgetReviewResultTypes;
	}
}

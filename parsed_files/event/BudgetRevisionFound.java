package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetRevisionFound implements Event{

	private List<BudgetRevision> budgetRevisions;

	public BudgetRevisionFound(List<BudgetRevision> budgetRevisions) {
		this.setBudgetRevisions(budgetRevisions);
	}

	public List<BudgetRevision> getBudgetRevisions()	{
		return budgetRevisions;
	}

	public void setBudgetRevisions(List<BudgetRevision> budgetRevisions)	{
		this.budgetRevisions = budgetRevisions;
	}
}

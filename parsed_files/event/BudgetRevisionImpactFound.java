package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetRevisionImpactFound implements Event{

	private List<BudgetRevisionImpact> budgetRevisionImpacts;

	public BudgetRevisionImpactFound(List<BudgetRevisionImpact> budgetRevisionImpacts) {
		this.setBudgetRevisionImpacts(budgetRevisionImpacts);
	}

	public List<BudgetRevisionImpact> getBudgetRevisionImpacts()	{
		return budgetRevisionImpacts;
	}

	public void setBudgetRevisionImpacts(List<BudgetRevisionImpact> budgetRevisionImpacts)	{
		this.budgetRevisionImpacts = budgetRevisionImpacts;
	}
}

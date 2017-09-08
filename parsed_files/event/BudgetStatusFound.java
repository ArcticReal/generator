package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetStatusFound implements Event{

	private List<BudgetStatus> budgetStatuss;

	public BudgetStatusFound(List<BudgetStatus> budgetStatuss) {
		this.setBudgetStatuss(budgetStatuss);
	}

	public List<BudgetStatus> getBudgetStatuss()	{
		return budgetStatuss;
	}

	public void setBudgetStatuss(List<BudgetStatus> budgetStatuss)	{
		this.budgetStatuss = budgetStatuss;
	}
}

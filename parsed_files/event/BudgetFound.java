package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetFound implements Event{

	private List<Budget> budgets;

	public BudgetFound(List<Budget> budgets) {
		this.setBudgets(budgets);
	}

	public List<Budget> getBudgets()	{
		return budgets;
	}

	public void setBudgets(List<Budget> budgets)	{
		this.budgets = budgets;
	}
}

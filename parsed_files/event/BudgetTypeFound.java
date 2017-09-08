package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetTypeFound implements Event{

	private List<BudgetType> budgetTypes;

	public BudgetTypeFound(List<BudgetType> budgetTypes) {
		this.setBudgetTypes(budgetTypes);
	}

	public List<BudgetType> getBudgetTypes()	{
		return budgetTypes;
	}

	public void setBudgetTypes(List<BudgetType> budgetTypes)	{
		this.budgetTypes = budgetTypes;
	}
}

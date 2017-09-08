package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetItemTypeFound implements Event{

	private List<BudgetItemType> budgetItemTypes;

	public BudgetItemTypeFound(List<BudgetItemType> budgetItemTypes) {
		this.setBudgetItemTypes(budgetItemTypes);
	}

	public List<BudgetItemType> getBudgetItemTypes()	{
		return budgetItemTypes;
	}

	public void setBudgetItemTypes(List<BudgetItemType> budgetItemTypes)	{
		this.budgetItemTypes = budgetItemTypes;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetItemFound implements Event{

	private List<BudgetItem> budgetItems;

	public BudgetItemFound(List<BudgetItem> budgetItems) {
		this.setBudgetItems(budgetItems);
	}

	public List<BudgetItem> getBudgetItems()	{
		return budgetItems;
	}

	public void setBudgetItems(List<BudgetItem> budgetItems)	{
		this.budgetItems = budgetItems;
	}
}

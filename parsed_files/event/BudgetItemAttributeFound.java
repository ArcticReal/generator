package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetItemAttributeFound implements Event{

	private List<BudgetItemAttribute> budgetItemAttributes;

	public BudgetItemAttributeFound(List<BudgetItemAttribute> budgetItemAttributes) {
		this.setBudgetItemAttributes(budgetItemAttributes);
	}

	public List<BudgetItemAttribute> getBudgetItemAttributes()	{
		return budgetItemAttributes;
	}

	public void setBudgetItemAttributes(List<BudgetItemAttribute> budgetItemAttributes)	{
		this.budgetItemAttributes = budgetItemAttributes;
	}
}

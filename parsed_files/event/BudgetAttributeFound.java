package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetAttributeFound implements Event{

	private List<BudgetAttribute> budgetAttributes;

	public BudgetAttributeFound(List<BudgetAttribute> budgetAttributes) {
		this.setBudgetAttributes(budgetAttributes);
	}

	public List<BudgetAttribute> getBudgetAttributes()	{
		return budgetAttributes;
	}

	public void setBudgetAttributes(List<BudgetAttribute> budgetAttributes)	{
		this.budgetAttributes = budgetAttributes;
	}
}

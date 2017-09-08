package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetRoleFound implements Event{

	private List<BudgetRole> budgetRoles;

	public BudgetRoleFound(List<BudgetRole> budgetRoles) {
		this.setBudgetRoles(budgetRoles);
	}

	public List<BudgetRole> getBudgetRoles()	{
		return budgetRoles;
	}

	public void setBudgetRoles(List<BudgetRole> budgetRoles)	{
		this.budgetRoles = budgetRoles;
	}
}

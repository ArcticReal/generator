package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetScenarioApplicationFound implements Event{

	private List<BudgetScenarioApplication> budgetScenarioApplications;

	public BudgetScenarioApplicationFound(List<BudgetScenarioApplication> budgetScenarioApplications) {
		this.setBudgetScenarioApplications(budgetScenarioApplications);
	}

	public List<BudgetScenarioApplication> getBudgetScenarioApplications()	{
		return budgetScenarioApplications;
	}

	public void setBudgetScenarioApplications(List<BudgetScenarioApplication> budgetScenarioApplications)	{
		this.budgetScenarioApplications = budgetScenarioApplications;
	}
}

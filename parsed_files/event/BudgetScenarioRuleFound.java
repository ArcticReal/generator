package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetScenarioRuleFound implements Event{

	private List<BudgetScenarioRule> budgetScenarioRules;

	public BudgetScenarioRuleFound(List<BudgetScenarioRule> budgetScenarioRules) {
		this.setBudgetScenarioRules(budgetScenarioRules);
	}

	public List<BudgetScenarioRule> getBudgetScenarioRules()	{
		return budgetScenarioRules;
	}

	public void setBudgetScenarioRules(List<BudgetScenarioRule> budgetScenarioRules)	{
		this.budgetScenarioRules = budgetScenarioRules;
	}
}

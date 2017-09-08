package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetScenarioFound implements Event{

	private List<BudgetScenario> budgetScenarios;

	public BudgetScenarioFound(List<BudgetScenario> budgetScenarios) {
		this.setBudgetScenarios(budgetScenarios);
	}

	public List<BudgetScenario> getBudgetScenarios()	{
		return budgetScenarios;
	}

	public void setBudgetScenarios(List<BudgetScenario> budgetScenarios)	{
		this.budgetScenarios = budgetScenarios;
	}
}

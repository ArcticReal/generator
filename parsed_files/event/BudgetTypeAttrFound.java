package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetTypeAttrFound implements Event{

	private List<BudgetTypeAttr> budgetTypeAttrs;

	public BudgetTypeAttrFound(List<BudgetTypeAttr> budgetTypeAttrs) {
		this.setBudgetTypeAttrs(budgetTypeAttrs);
	}

	public List<BudgetTypeAttr> getBudgetTypeAttrs()	{
		return budgetTypeAttrs;
	}

	public void setBudgetTypeAttrs(List<BudgetTypeAttr> budgetTypeAttrs)	{
		this.budgetTypeAttrs = budgetTypeAttrs;
	}
}

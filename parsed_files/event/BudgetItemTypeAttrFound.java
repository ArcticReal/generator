package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BudgetItemTypeAttrFound implements Event{

	private List<BudgetItemTypeAttr> budgetItemTypeAttrs;

	public BudgetItemTypeAttrFound(List<BudgetItemTypeAttr> budgetItemTypeAttrs) {
		this.setBudgetItemTypeAttrs(budgetItemTypeAttrs);
	}

	public List<BudgetItemTypeAttr> getBudgetItemTypeAttrs()	{
		return budgetItemTypeAttrs;
	}

	public void setBudgetItemTypeAttrs(List<BudgetItemTypeAttr> budgetItemTypeAttrs)	{
		this.budgetItemTypeAttrs = budgetItemTypeAttrs;
	}
}

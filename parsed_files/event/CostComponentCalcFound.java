package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CostComponentCalcFound implements Event{

	private List<CostComponentCalc> costComponentCalcs;

	public CostComponentCalcFound(List<CostComponentCalc> costComponentCalcs) {
		this.setCostComponentCalcs(costComponentCalcs);
	}

	public List<CostComponentCalc> getCostComponentCalcs()	{
		return costComponentCalcs;
	}

	public void setCostComponentCalcs(List<CostComponentCalc> costComponentCalcs)	{
		this.costComponentCalcs = costComponentCalcs;
	}
}

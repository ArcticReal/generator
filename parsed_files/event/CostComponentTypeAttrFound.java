package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CostComponentTypeAttrFound implements Event{

	private List<CostComponentTypeAttr> costComponentTypeAttrs;

	public CostComponentTypeAttrFound(List<CostComponentTypeAttr> costComponentTypeAttrs) {
		this.setCostComponentTypeAttrs(costComponentTypeAttrs);
	}

	public List<CostComponentTypeAttr> getCostComponentTypeAttrs()	{
		return costComponentTypeAttrs;
	}

	public void setCostComponentTypeAttrs(List<CostComponentTypeAttr> costComponentTypeAttrs)	{
		this.costComponentTypeAttrs = costComponentTypeAttrs;
	}
}

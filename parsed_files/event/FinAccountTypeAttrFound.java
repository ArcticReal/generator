package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountTypeAttrFound implements Event{

	private List<FinAccountTypeAttr> finAccountTypeAttrs;

	public FinAccountTypeAttrFound(List<FinAccountTypeAttr> finAccountTypeAttrs) {
		this.setFinAccountTypeAttrs(finAccountTypeAttrs);
	}

	public List<FinAccountTypeAttr> getFinAccountTypeAttrs()	{
		return finAccountTypeAttrs;
	}

	public void setFinAccountTypeAttrs(List<FinAccountTypeAttr> finAccountTypeAttrs)	{
		this.finAccountTypeAttrs = finAccountTypeAttrs;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountTransTypeAttrFound implements Event{

	private List<FinAccountTransTypeAttr> finAccountTransTypeAttrs;

	public FinAccountTransTypeAttrFound(List<FinAccountTransTypeAttr> finAccountTransTypeAttrs) {
		this.setFinAccountTransTypeAttrs(finAccountTransTypeAttrs);
	}

	public List<FinAccountTransTypeAttr> getFinAccountTransTypeAttrs()	{
		return finAccountTransTypeAttrs;
	}

	public void setFinAccountTransTypeAttrs(List<FinAccountTransTypeAttr> finAccountTransTypeAttrs)	{
		this.finAccountTransTypeAttrs = finAccountTransTypeAttrs;
	}
}

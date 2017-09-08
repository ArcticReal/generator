package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountTransFound implements Event{

	private List<FinAccountTrans> finAccountTranss;

	public FinAccountTransFound(List<FinAccountTrans> finAccountTranss) {
		this.setFinAccountTranss(finAccountTranss);
	}

	public List<FinAccountTrans> getFinAccountTranss()	{
		return finAccountTranss;
	}

	public void setFinAccountTranss(List<FinAccountTrans> finAccountTranss)	{
		this.finAccountTranss = finAccountTranss;
	}
}

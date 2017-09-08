package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountStatusFound implements Event{

	private List<FinAccountStatus> finAccountStatuss;

	public FinAccountStatusFound(List<FinAccountStatus> finAccountStatuss) {
		this.setFinAccountStatuss(finAccountStatuss);
	}

	public List<FinAccountStatus> getFinAccountStatuss()	{
		return finAccountStatuss;
	}

	public void setFinAccountStatuss(List<FinAccountStatus> finAccountStatuss)	{
		this.finAccountStatuss = finAccountStatuss;
	}
}

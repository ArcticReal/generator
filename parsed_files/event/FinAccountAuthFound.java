package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountAuthFound implements Event{

	private List<FinAccountAuth> finAccountAuths;

	public FinAccountAuthFound(List<FinAccountAuth> finAccountAuths) {
		this.setFinAccountAuths(finAccountAuths);
	}

	public List<FinAccountAuth> getFinAccountAuths()	{
		return finAccountAuths;
	}

	public void setFinAccountAuths(List<FinAccountAuth> finAccountAuths)	{
		this.finAccountAuths = finAccountAuths;
	}
}

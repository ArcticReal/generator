package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class VarianceReasonGlAccountFound implements Event{

	private List<VarianceReasonGlAccount> varianceReasonGlAccounts;

	public VarianceReasonGlAccountFound(List<VarianceReasonGlAccount> varianceReasonGlAccounts) {
		this.setVarianceReasonGlAccounts(varianceReasonGlAccounts);
	}

	public List<VarianceReasonGlAccount> getVarianceReasonGlAccounts()	{
		return varianceReasonGlAccounts;
	}

	public void setVarianceReasonGlAccounts(List<VarianceReasonGlAccount> varianceReasonGlAccounts)	{
		this.varianceReasonGlAccounts = varianceReasonGlAccounts;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountFound implements Event{

	private List<GlAccount> glAccounts;

	public GlAccountFound(List<GlAccount> glAccounts) {
		this.setGlAccounts(glAccounts);
	}

	public List<GlAccount> getGlAccounts()	{
		return glAccounts;
	}

	public void setGlAccounts(List<GlAccount> glAccounts)	{
		this.glAccounts = glAccounts;
	}
}

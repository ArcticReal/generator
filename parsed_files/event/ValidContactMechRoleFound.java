package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ValidContactMechRoleFound implements Event{

	private List<ValidContactMechRole> validContactMechRoles;

	public ValidContactMechRoleFound(List<ValidContactMechRole> validContactMechRoles) {
		this.setValidContactMechRoles(validContactMechRoles);
	}

	public List<ValidContactMechRole> getValidContactMechRoles()	{
		return validContactMechRoles;
	}

	public void setValidContactMechRoles(List<ValidContactMechRole> validContactMechRoles)	{
		this.validContactMechRoles = validContactMechRoles;
	}
}

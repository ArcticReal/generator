package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SecurityGroupFound implements Event{

	private List<SecurityGroup> securityGroups;

	public SecurityGroupFound(List<SecurityGroup> securityGroups) {
		this.setSecurityGroups(securityGroups);
	}

	public List<SecurityGroup> getSecurityGroups()	{
		return securityGroups;
	}

	public void setSecurityGroups(List<SecurityGroup> securityGroups)	{
		this.securityGroups = securityGroups;
	}
}

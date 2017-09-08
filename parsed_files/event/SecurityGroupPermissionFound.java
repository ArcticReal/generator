package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SecurityGroupPermissionFound implements Event{

	private List<SecurityGroupPermission> securityGroupPermissions;

	public SecurityGroupPermissionFound(List<SecurityGroupPermission> securityGroupPermissions) {
		this.setSecurityGroupPermissions(securityGroupPermissions);
	}

	public List<SecurityGroupPermission> getSecurityGroupPermissions()	{
		return securityGroupPermissions;
	}

	public void setSecurityGroupPermissions(List<SecurityGroupPermission> securityGroupPermissions)	{
		this.securityGroupPermissions = securityGroupPermissions;
	}
}

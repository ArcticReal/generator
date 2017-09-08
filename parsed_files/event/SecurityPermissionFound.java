package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SecurityPermissionFound implements Event{

	private List<SecurityPermission> securityPermissions;

	public SecurityPermissionFound(List<SecurityPermission> securityPermissions) {
		this.setSecurityPermissions(securityPermissions);
	}

	public List<SecurityPermission> getSecurityPermissions()	{
		return securityPermissions;
	}

	public void setSecurityPermissions(List<SecurityPermission> securityPermissions)	{
		this.securityPermissions = securityPermissions;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RoleTypeAttrFound implements Event{

	private List<RoleTypeAttr> roleTypeAttrs;

	public RoleTypeAttrFound(List<RoleTypeAttr> roleTypeAttrs) {
		this.setRoleTypeAttrs(roleTypeAttrs);
	}

	public List<RoleTypeAttr> getRoleTypeAttrs()	{
		return roleTypeAttrs;
	}

	public void setRoleTypeAttrs(List<RoleTypeAttr> roleTypeAttrs)	{
		this.roleTypeAttrs = roleTypeAttrs;
	}
}

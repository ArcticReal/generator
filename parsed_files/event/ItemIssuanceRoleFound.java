package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ItemIssuanceRoleFound implements Event{

	private List<ItemIssuanceRole> itemIssuanceRoles;

	public ItemIssuanceRoleFound(List<ItemIssuanceRole> itemIssuanceRoles) {
		this.setItemIssuanceRoles(itemIssuanceRoles);
	}

	public List<ItemIssuanceRole> getItemIssuanceRoles()	{
		return itemIssuanceRoles;
	}

	public void setItemIssuanceRoles(List<ItemIssuanceRole> itemIssuanceRoles)	{
		this.itemIssuanceRoles = itemIssuanceRoles;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreGroupMemberFound implements Event{

	private List<ProductStoreGroupMember> productStoreGroupMembers;

	public ProductStoreGroupMemberFound(List<ProductStoreGroupMember> productStoreGroupMembers) {
		this.setProductStoreGroupMembers(productStoreGroupMembers);
	}

	public List<ProductStoreGroupMember> getProductStoreGroupMembers()	{
		return productStoreGroupMembers;
	}

	public void setProductStoreGroupMembers(List<ProductStoreGroupMember> productStoreGroupMembers)	{
		this.productStoreGroupMembers = productStoreGroupMembers;
	}
}

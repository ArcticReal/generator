package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryMemberFound implements Event{

	private List<ProductCategoryMember> productCategoryMembers;

	public ProductCategoryMemberFound(List<ProductCategoryMember> productCategoryMembers) {
		this.setProductCategoryMembers(productCategoryMembers);
	}

	public List<ProductCategoryMember> getProductCategoryMembers()	{
		return productCategoryMembers;
	}

	public void setProductCategoryMembers(List<ProductCategoryMember> productCategoryMembers)	{
		this.productCategoryMembers = productCategoryMembers;
	}
}

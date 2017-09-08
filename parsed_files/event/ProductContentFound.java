package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductContentFound implements Event{

	private List<ProductContent> productContents;

	public ProductContentFound(List<ProductContent> productContents) {
		this.setProductContents(productContents);
	}

	public List<ProductContent> getProductContents()	{
		return productContents;
	}

	public void setProductContents(List<ProductContent> productContents)	{
		this.productContents = productContents;
	}
}

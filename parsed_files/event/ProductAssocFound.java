package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductAssocFound implements Event{

	private List<ProductAssoc> productAssocs;

	public ProductAssocFound(List<ProductAssoc> productAssocs) {
		this.setProductAssocs(productAssocs);
	}

	public List<ProductAssoc> getProductAssocs()	{
		return productAssocs;
	}

	public void setProductAssocs(List<ProductAssoc> productAssocs)	{
		this.productAssocs = productAssocs;
	}
}

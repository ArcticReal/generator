package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductAssocTypeFound implements Event{

	private List<ProductAssocType> productAssocTypes;

	public ProductAssocTypeFound(List<ProductAssocType> productAssocTypes) {
		this.setProductAssocTypes(productAssocTypes);
	}

	public List<ProductAssocType> getProductAssocTypes()	{
		return productAssocTypes;
	}

	public void setProductAssocTypes(List<ProductAssocType> productAssocTypes)	{
		this.productAssocTypes = productAssocTypes;
	}
}

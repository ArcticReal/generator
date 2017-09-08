package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductSearchConstraintFound implements Event{

	private List<ProductSearchConstraint> productSearchConstraints;

	public ProductSearchConstraintFound(List<ProductSearchConstraint> productSearchConstraints) {
		this.setProductSearchConstraints(productSearchConstraints);
	}

	public List<ProductSearchConstraint> getProductSearchConstraints()	{
		return productSearchConstraints;
	}

	public void setProductSearchConstraints(List<ProductSearchConstraint> productSearchConstraints)	{
		this.productSearchConstraints = productSearchConstraints;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPaymentMethodTypeFound implements Event{

	private List<ProductPaymentMethodType> productPaymentMethodTypes;

	public ProductPaymentMethodTypeFound(List<ProductPaymentMethodType> productPaymentMethodTypes) {
		this.setProductPaymentMethodTypes(productPaymentMethodTypes);
	}

	public List<ProductPaymentMethodType> getProductPaymentMethodTypes()	{
		return productPaymentMethodTypes;
	}

	public void setProductPaymentMethodTypes(List<ProductPaymentMethodType> productPaymentMethodTypes)	{
		this.productPaymentMethodTypes = productPaymentMethodTypes;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductMeterTypeFound implements Event{

	private List<ProductMeterType> productMeterTypes;

	public ProductMeterTypeFound(List<ProductMeterType> productMeterTypes) {
		this.setProductMeterTypes(productMeterTypes);
	}

	public List<ProductMeterType> getProductMeterTypes()	{
		return productMeterTypes;
	}

	public void setProductMeterTypes(List<ProductMeterType> productMeterTypes)	{
		this.productMeterTypes = productMeterTypes;
	}
}

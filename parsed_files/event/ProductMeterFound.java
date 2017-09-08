package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductMeterFound implements Event{

	private List<ProductMeter> productMeters;

	public ProductMeterFound(List<ProductMeter> productMeters) {
		this.setProductMeters(productMeters);
	}

	public List<ProductMeter> getProductMeters()	{
		return productMeters;
	}

	public void setProductMeters(List<ProductMeter> productMeters)	{
		this.productMeters = productMeters;
	}
}

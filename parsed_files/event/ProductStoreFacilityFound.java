package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreFacilityFound implements Event{

	private List<ProductStoreFacility> productStoreFacilitys;

	public ProductStoreFacilityFound(List<ProductStoreFacility> productStoreFacilitys) {
		this.setProductStoreFacilitys(productStoreFacilitys);
	}

	public List<ProductStoreFacility> getProductStoreFacilitys()	{
		return productStoreFacilitys;
	}

	public void setProductStoreFacilitys(List<ProductStoreFacility> productStoreFacilitys)	{
		this.productStoreFacilitys = productStoreFacilitys;
	}
}

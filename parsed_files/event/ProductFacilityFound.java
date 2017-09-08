package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFacilityFound implements Event{

	private List<ProductFacility> productFacilitys;

	public ProductFacilityFound(List<ProductFacility> productFacilitys) {
		this.setProductFacilitys(productFacilitys);
	}

	public List<ProductFacility> getProductFacilitys()	{
		return productFacilitys;
	}

	public void setProductFacilitys(List<ProductFacility> productFacilitys)	{
		this.productFacilitys = productFacilitys;
	}
}

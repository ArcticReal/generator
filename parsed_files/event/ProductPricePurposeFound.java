package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPricePurposeFound implements Event{

	private List<ProductPricePurpose> productPricePurposes;

	public ProductPricePurposeFound(List<ProductPricePurpose> productPricePurposes) {
		this.setProductPricePurposes(productPricePurposes);
	}

	public List<ProductPricePurpose> getProductPricePurposes()	{
		return productPricePurposes;
	}

	public void setProductPricePurposes(List<ProductPricePurpose> productPricePurposes)	{
		this.productPricePurposes = productPricePurposes;
	}
}

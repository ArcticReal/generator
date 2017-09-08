package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureCatGrpApplFound implements Event{

	private List<ProductFeatureCatGrpAppl> productFeatureCatGrpAppls;

	public ProductFeatureCatGrpApplFound(List<ProductFeatureCatGrpAppl> productFeatureCatGrpAppls) {
		this.setProductFeatureCatGrpAppls(productFeatureCatGrpAppls);
	}

	public List<ProductFeatureCatGrpAppl> getProductFeatureCatGrpAppls()	{
		return productFeatureCatGrpAppls;
	}

	public void setProductFeatureCatGrpAppls(List<ProductFeatureCatGrpAppl> productFeatureCatGrpAppls)	{
		this.productFeatureCatGrpAppls = productFeatureCatGrpAppls;
	}
}

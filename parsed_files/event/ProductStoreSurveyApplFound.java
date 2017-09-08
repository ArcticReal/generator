package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreSurveyApplFound implements Event{

	private List<ProductStoreSurveyAppl> productStoreSurveyAppls;

	public ProductStoreSurveyApplFound(List<ProductStoreSurveyAppl> productStoreSurveyAppls) {
		this.setProductStoreSurveyAppls(productStoreSurveyAppls);
	}

	public List<ProductStoreSurveyAppl> getProductStoreSurveyAppls()	{
		return productStoreSurveyAppls;
	}

	public void setProductStoreSurveyAppls(List<ProductStoreSurveyAppl> productStoreSurveyAppls)	{
		this.productStoreSurveyAppls = productStoreSurveyAppls;
	}
}

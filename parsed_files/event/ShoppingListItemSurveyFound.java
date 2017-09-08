package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShoppingListItemSurveyFound implements Event{

	private List<ShoppingListItemSurvey> shoppingListItemSurveys;

	public ShoppingListItemSurveyFound(List<ShoppingListItemSurvey> shoppingListItemSurveys) {
		this.setShoppingListItemSurveys(shoppingListItemSurveys);
	}

	public List<ShoppingListItemSurvey> getShoppingListItemSurveys()	{
		return shoppingListItemSurveys;
	}

	public void setShoppingListItemSurveys(List<ShoppingListItemSurvey> shoppingListItemSurveys)	{
		this.shoppingListItemSurveys = shoppingListItemSurveys;
	}
}

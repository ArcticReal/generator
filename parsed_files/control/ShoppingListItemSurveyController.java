package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddShoppingListItemSurvey;
import com.skytala.eCommerce.command.DeleteShoppingListItemSurvey;
import com.skytala.eCommerce.command.UpdateShoppingListItemSurvey;
import com.skytala.eCommerce.entity.ShoppingListItemSurvey;
import com.skytala.eCommerce.entity.ShoppingListItemSurveyMapper;
import com.skytala.eCommerce.event.ShoppingListItemSurveyAdded;
import com.skytala.eCommerce.event.ShoppingListItemSurveyDeleted;
import com.skytala.eCommerce.event.ShoppingListItemSurveyFound;
import com.skytala.eCommerce.event.ShoppingListItemSurveyUpdated;
import com.skytala.eCommerce.query.FindShoppingListItemSurveysBy;

@RestController
@RequestMapping("/api/shoppingListItemSurvey")
public class ShoppingListItemSurveyController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShoppingListItemSurvey>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShoppingListItemSurveyController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShoppingListItemSurvey
	 * @return a List with the ShoppingListItemSurveys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShoppingListItemSurvey> findShoppingListItemSurveysBy(@RequestParam Map<String, String> allRequestParams) {

		FindShoppingListItemSurveysBy query = new FindShoppingListItemSurveysBy(allRequestParams);

		int usedTicketId;

		synchronized (ShoppingListItemSurveyController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListItemSurveyFound.class,
				event -> sendShoppingListItemSurveysFoundMessage(((ShoppingListItemSurveyFound) event).getShoppingListItemSurveys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShoppingListItemSurveysFoundMessage(List<ShoppingListItemSurvey> shoppingListItemSurveys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shoppingListItemSurveys);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createShoppingListItemSurvey(HttpServletRequest request) {

		ShoppingListItemSurvey shoppingListItemSurveyToBeAdded = new ShoppingListItemSurvey();
		try {
			shoppingListItemSurveyToBeAdded = ShoppingListItemSurveyMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShoppingListItemSurvey(shoppingListItemSurveyToBeAdded);

	}

	/**
	 * creates a new ShoppingListItemSurvey entry in the ofbiz database
	 * 
	 * @param shoppingListItemSurveyToBeAdded
	 *            the ShoppingListItemSurvey thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShoppingListItemSurvey(ShoppingListItemSurvey shoppingListItemSurveyToBeAdded) {

		AddShoppingListItemSurvey com = new AddShoppingListItemSurvey(shoppingListItemSurveyToBeAdded);
		int usedTicketId;

		synchronized (ShoppingListItemSurveyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListItemSurveyAdded.class,
				event -> sendShoppingListItemSurveyChangedMessage(((ShoppingListItemSurveyAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateShoppingListItemSurvey(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		ShoppingListItemSurvey shoppingListItemSurveyToBeUpdated = new ShoppingListItemSurvey();

		try {
			shoppingListItemSurveyToBeUpdated = ShoppingListItemSurveyMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShoppingListItemSurvey(shoppingListItemSurveyToBeUpdated);

	}

	/**
	 * Updates the ShoppingListItemSurvey with the specific Id
	 * 
	 * @param shoppingListItemSurveyToBeUpdated the ShoppingListItemSurvey thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShoppingListItemSurvey(ShoppingListItemSurvey shoppingListItemSurveyToBeUpdated) {

		UpdateShoppingListItemSurvey com = new UpdateShoppingListItemSurvey(shoppingListItemSurveyToBeUpdated);

		int usedTicketId;

		synchronized (ShoppingListItemSurveyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListItemSurveyUpdated.class,
				event -> sendShoppingListItemSurveyChangedMessage(((ShoppingListItemSurveyUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a ShoppingListItemSurvey from the database
	 * 
	 * @param shoppingListItemSurveyId:
	 *            the id of the ShoppingListItemSurvey thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshoppingListItemSurveyById(@RequestParam(value = "shoppingListItemSurveyId") String shoppingListItemSurveyId) {

		DeleteShoppingListItemSurvey com = new DeleteShoppingListItemSurvey(shoppingListItemSurveyId);

		int usedTicketId;

		synchronized (ShoppingListItemSurveyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListItemSurveyDeleted.class,
				event -> sendShoppingListItemSurveyChangedMessage(((ShoppingListItemSurveyDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendShoppingListItemSurveyChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shoppingListItemSurvey/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}

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
import com.skytala.eCommerce.command.AddCreditCard;
import com.skytala.eCommerce.command.DeleteCreditCard;
import com.skytala.eCommerce.command.UpdateCreditCard;
import com.skytala.eCommerce.entity.CreditCard;
import com.skytala.eCommerce.entity.CreditCardMapper;
import com.skytala.eCommerce.event.CreditCardAdded;
import com.skytala.eCommerce.event.CreditCardDeleted;
import com.skytala.eCommerce.event.CreditCardFound;
import com.skytala.eCommerce.event.CreditCardUpdated;
import com.skytala.eCommerce.query.FindCreditCardsBy;

@RestController
@RequestMapping("/api/creditCard")
public class CreditCardController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CreditCard>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CreditCardController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CreditCard
	 * @return a List with the CreditCards
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CreditCard> findCreditCardsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCreditCardsBy query = new FindCreditCardsBy(allRequestParams);

		int usedTicketId;

		synchronized (CreditCardController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CreditCardFound.class,
				event -> sendCreditCardsFoundMessage(((CreditCardFound) event).getCreditCards(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCreditCardsFoundMessage(List<CreditCard> creditCards, int usedTicketId) {
		queryReturnVal.put(usedTicketId, creditCards);
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
	public boolean createCreditCard(HttpServletRequest request) {

		CreditCard creditCardToBeAdded = new CreditCard();
		try {
			creditCardToBeAdded = CreditCardMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCreditCard(creditCardToBeAdded);

	}

	/**
	 * creates a new CreditCard entry in the ofbiz database
	 * 
	 * @param creditCardToBeAdded
	 *            the CreditCard thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCreditCard(CreditCard creditCardToBeAdded) {

		AddCreditCard com = new AddCreditCard(creditCardToBeAdded);
		int usedTicketId;

		synchronized (CreditCardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CreditCardAdded.class,
				event -> sendCreditCardChangedMessage(((CreditCardAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCreditCard(HttpServletRequest request) {

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

		CreditCard creditCardToBeUpdated = new CreditCard();

		try {
			creditCardToBeUpdated = CreditCardMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCreditCard(creditCardToBeUpdated);

	}

	/**
	 * Updates the CreditCard with the specific Id
	 * 
	 * @param creditCardToBeUpdated the CreditCard thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCreditCard(CreditCard creditCardToBeUpdated) {

		UpdateCreditCard com = new UpdateCreditCard(creditCardToBeUpdated);

		int usedTicketId;

		synchronized (CreditCardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CreditCardUpdated.class,
				event -> sendCreditCardChangedMessage(((CreditCardUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CreditCard from the database
	 * 
	 * @param creditCardId:
	 *            the id of the CreditCard thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecreditCardById(@RequestParam(value = "creditCardId") String creditCardId) {

		DeleteCreditCard com = new DeleteCreditCard(creditCardId);

		int usedTicketId;

		synchronized (CreditCardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CreditCardDeleted.class,
				event -> sendCreditCardChangedMessage(((CreditCardDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCreditCardChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/creditCard/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddSubscriptionFulfillmentPiece;
import com.skytala.eCommerce.command.DeleteSubscriptionFulfillmentPiece;
import com.skytala.eCommerce.command.UpdateSubscriptionFulfillmentPiece;
import com.skytala.eCommerce.entity.SubscriptionFulfillmentPiece;
import com.skytala.eCommerce.entity.SubscriptionFulfillmentPieceMapper;
import com.skytala.eCommerce.event.SubscriptionFulfillmentPieceAdded;
import com.skytala.eCommerce.event.SubscriptionFulfillmentPieceDeleted;
import com.skytala.eCommerce.event.SubscriptionFulfillmentPieceFound;
import com.skytala.eCommerce.event.SubscriptionFulfillmentPieceUpdated;
import com.skytala.eCommerce.query.FindSubscriptionFulfillmentPiecesBy;

@RestController
@RequestMapping("/api/subscriptionFulfillmentPiece")
public class SubscriptionFulfillmentPieceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SubscriptionFulfillmentPiece>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SubscriptionFulfillmentPieceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SubscriptionFulfillmentPiece
	 * @return a List with the SubscriptionFulfillmentPieces
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SubscriptionFulfillmentPiece> findSubscriptionFulfillmentPiecesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSubscriptionFulfillmentPiecesBy query = new FindSubscriptionFulfillmentPiecesBy(allRequestParams);

		int usedTicketId;

		synchronized (SubscriptionFulfillmentPieceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionFulfillmentPieceFound.class,
				event -> sendSubscriptionFulfillmentPiecesFoundMessage(((SubscriptionFulfillmentPieceFound) event).getSubscriptionFulfillmentPieces(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSubscriptionFulfillmentPiecesFoundMessage(List<SubscriptionFulfillmentPiece> subscriptionFulfillmentPieces, int usedTicketId) {
		queryReturnVal.put(usedTicketId, subscriptionFulfillmentPieces);
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
	public boolean createSubscriptionFulfillmentPiece(HttpServletRequest request) {

		SubscriptionFulfillmentPiece subscriptionFulfillmentPieceToBeAdded = new SubscriptionFulfillmentPiece();
		try {
			subscriptionFulfillmentPieceToBeAdded = SubscriptionFulfillmentPieceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSubscriptionFulfillmentPiece(subscriptionFulfillmentPieceToBeAdded);

	}

	/**
	 * creates a new SubscriptionFulfillmentPiece entry in the ofbiz database
	 * 
	 * @param subscriptionFulfillmentPieceToBeAdded
	 *            the SubscriptionFulfillmentPiece thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSubscriptionFulfillmentPiece(SubscriptionFulfillmentPiece subscriptionFulfillmentPieceToBeAdded) {

		AddSubscriptionFulfillmentPiece com = new AddSubscriptionFulfillmentPiece(subscriptionFulfillmentPieceToBeAdded);
		int usedTicketId;

		synchronized (SubscriptionFulfillmentPieceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionFulfillmentPieceAdded.class,
				event -> sendSubscriptionFulfillmentPieceChangedMessage(((SubscriptionFulfillmentPieceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSubscriptionFulfillmentPiece(HttpServletRequest request) {

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

		SubscriptionFulfillmentPiece subscriptionFulfillmentPieceToBeUpdated = new SubscriptionFulfillmentPiece();

		try {
			subscriptionFulfillmentPieceToBeUpdated = SubscriptionFulfillmentPieceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSubscriptionFulfillmentPiece(subscriptionFulfillmentPieceToBeUpdated);

	}

	/**
	 * Updates the SubscriptionFulfillmentPiece with the specific Id
	 * 
	 * @param subscriptionFulfillmentPieceToBeUpdated the SubscriptionFulfillmentPiece thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSubscriptionFulfillmentPiece(SubscriptionFulfillmentPiece subscriptionFulfillmentPieceToBeUpdated) {

		UpdateSubscriptionFulfillmentPiece com = new UpdateSubscriptionFulfillmentPiece(subscriptionFulfillmentPieceToBeUpdated);

		int usedTicketId;

		synchronized (SubscriptionFulfillmentPieceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionFulfillmentPieceUpdated.class,
				event -> sendSubscriptionFulfillmentPieceChangedMessage(((SubscriptionFulfillmentPieceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SubscriptionFulfillmentPiece from the database
	 * 
	 * @param subscriptionFulfillmentPieceId:
	 *            the id of the SubscriptionFulfillmentPiece thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesubscriptionFulfillmentPieceById(@RequestParam(value = "subscriptionFulfillmentPieceId") String subscriptionFulfillmentPieceId) {

		DeleteSubscriptionFulfillmentPiece com = new DeleteSubscriptionFulfillmentPiece(subscriptionFulfillmentPieceId);

		int usedTicketId;

		synchronized (SubscriptionFulfillmentPieceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionFulfillmentPieceDeleted.class,
				event -> sendSubscriptionFulfillmentPieceChangedMessage(((SubscriptionFulfillmentPieceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSubscriptionFulfillmentPieceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/subscriptionFulfillmentPiece/\" plus one of the following: "
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

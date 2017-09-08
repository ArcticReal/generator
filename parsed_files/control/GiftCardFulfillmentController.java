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
import com.skytala.eCommerce.command.AddGiftCardFulfillment;
import com.skytala.eCommerce.command.DeleteGiftCardFulfillment;
import com.skytala.eCommerce.command.UpdateGiftCardFulfillment;
import com.skytala.eCommerce.entity.GiftCardFulfillment;
import com.skytala.eCommerce.entity.GiftCardFulfillmentMapper;
import com.skytala.eCommerce.event.GiftCardFulfillmentAdded;
import com.skytala.eCommerce.event.GiftCardFulfillmentDeleted;
import com.skytala.eCommerce.event.GiftCardFulfillmentFound;
import com.skytala.eCommerce.event.GiftCardFulfillmentUpdated;
import com.skytala.eCommerce.query.FindGiftCardFulfillmentsBy;

@RestController
@RequestMapping("/api/giftCardFulfillment")
public class GiftCardFulfillmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GiftCardFulfillment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GiftCardFulfillmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GiftCardFulfillment
	 * @return a List with the GiftCardFulfillments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GiftCardFulfillment> findGiftCardFulfillmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindGiftCardFulfillmentsBy query = new FindGiftCardFulfillmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (GiftCardFulfillmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GiftCardFulfillmentFound.class,
				event -> sendGiftCardFulfillmentsFoundMessage(((GiftCardFulfillmentFound) event).getGiftCardFulfillments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGiftCardFulfillmentsFoundMessage(List<GiftCardFulfillment> giftCardFulfillments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, giftCardFulfillments);
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
	public boolean createGiftCardFulfillment(HttpServletRequest request) {

		GiftCardFulfillment giftCardFulfillmentToBeAdded = new GiftCardFulfillment();
		try {
			giftCardFulfillmentToBeAdded = GiftCardFulfillmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGiftCardFulfillment(giftCardFulfillmentToBeAdded);

	}

	/**
	 * creates a new GiftCardFulfillment entry in the ofbiz database
	 * 
	 * @param giftCardFulfillmentToBeAdded
	 *            the GiftCardFulfillment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGiftCardFulfillment(GiftCardFulfillment giftCardFulfillmentToBeAdded) {

		AddGiftCardFulfillment com = new AddGiftCardFulfillment(giftCardFulfillmentToBeAdded);
		int usedTicketId;

		synchronized (GiftCardFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GiftCardFulfillmentAdded.class,
				event -> sendGiftCardFulfillmentChangedMessage(((GiftCardFulfillmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGiftCardFulfillment(HttpServletRequest request) {

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

		GiftCardFulfillment giftCardFulfillmentToBeUpdated = new GiftCardFulfillment();

		try {
			giftCardFulfillmentToBeUpdated = GiftCardFulfillmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGiftCardFulfillment(giftCardFulfillmentToBeUpdated);

	}

	/**
	 * Updates the GiftCardFulfillment with the specific Id
	 * 
	 * @param giftCardFulfillmentToBeUpdated the GiftCardFulfillment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGiftCardFulfillment(GiftCardFulfillment giftCardFulfillmentToBeUpdated) {

		UpdateGiftCardFulfillment com = new UpdateGiftCardFulfillment(giftCardFulfillmentToBeUpdated);

		int usedTicketId;

		synchronized (GiftCardFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GiftCardFulfillmentUpdated.class,
				event -> sendGiftCardFulfillmentChangedMessage(((GiftCardFulfillmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GiftCardFulfillment from the database
	 * 
	 * @param giftCardFulfillmentId:
	 *            the id of the GiftCardFulfillment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletegiftCardFulfillmentById(@RequestParam(value = "giftCardFulfillmentId") String giftCardFulfillmentId) {

		DeleteGiftCardFulfillment com = new DeleteGiftCardFulfillment(giftCardFulfillmentId);

		int usedTicketId;

		synchronized (GiftCardFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GiftCardFulfillmentDeleted.class,
				event -> sendGiftCardFulfillmentChangedMessage(((GiftCardFulfillmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGiftCardFulfillmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/giftCardFulfillment/\" plus one of the following: "
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

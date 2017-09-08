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
import com.skytala.eCommerce.command.AddGiftCard;
import com.skytala.eCommerce.command.DeleteGiftCard;
import com.skytala.eCommerce.command.UpdateGiftCard;
import com.skytala.eCommerce.entity.GiftCard;
import com.skytala.eCommerce.entity.GiftCardMapper;
import com.skytala.eCommerce.event.GiftCardAdded;
import com.skytala.eCommerce.event.GiftCardDeleted;
import com.skytala.eCommerce.event.GiftCardFound;
import com.skytala.eCommerce.event.GiftCardUpdated;
import com.skytala.eCommerce.query.FindGiftCardsBy;

@RestController
@RequestMapping("/api/giftCard")
public class GiftCardController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GiftCard>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GiftCardController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GiftCard
	 * @return a List with the GiftCards
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GiftCard> findGiftCardsBy(@RequestParam Map<String, String> allRequestParams) {

		FindGiftCardsBy query = new FindGiftCardsBy(allRequestParams);

		int usedTicketId;

		synchronized (GiftCardController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GiftCardFound.class,
				event -> sendGiftCardsFoundMessage(((GiftCardFound) event).getGiftCards(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGiftCardsFoundMessage(List<GiftCard> giftCards, int usedTicketId) {
		queryReturnVal.put(usedTicketId, giftCards);
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
	public boolean createGiftCard(HttpServletRequest request) {

		GiftCard giftCardToBeAdded = new GiftCard();
		try {
			giftCardToBeAdded = GiftCardMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGiftCard(giftCardToBeAdded);

	}

	/**
	 * creates a new GiftCard entry in the ofbiz database
	 * 
	 * @param giftCardToBeAdded
	 *            the GiftCard thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGiftCard(GiftCard giftCardToBeAdded) {

		AddGiftCard com = new AddGiftCard(giftCardToBeAdded);
		int usedTicketId;

		synchronized (GiftCardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GiftCardAdded.class,
				event -> sendGiftCardChangedMessage(((GiftCardAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGiftCard(HttpServletRequest request) {

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

		GiftCard giftCardToBeUpdated = new GiftCard();

		try {
			giftCardToBeUpdated = GiftCardMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGiftCard(giftCardToBeUpdated);

	}

	/**
	 * Updates the GiftCard with the specific Id
	 * 
	 * @param giftCardToBeUpdated the GiftCard thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGiftCard(GiftCard giftCardToBeUpdated) {

		UpdateGiftCard com = new UpdateGiftCard(giftCardToBeUpdated);

		int usedTicketId;

		synchronized (GiftCardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GiftCardUpdated.class,
				event -> sendGiftCardChangedMessage(((GiftCardUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GiftCard from the database
	 * 
	 * @param giftCardId:
	 *            the id of the GiftCard thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletegiftCardById(@RequestParam(value = "giftCardId") String giftCardId) {

		DeleteGiftCard com = new DeleteGiftCard(giftCardId);

		int usedTicketId;

		synchronized (GiftCardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GiftCardDeleted.class,
				event -> sendGiftCardChangedMessage(((GiftCardDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGiftCardChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/giftCard/\" plus one of the following: "
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

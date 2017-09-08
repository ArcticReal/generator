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
import com.skytala.eCommerce.command.AddMarketInterest;
import com.skytala.eCommerce.command.DeleteMarketInterest;
import com.skytala.eCommerce.command.UpdateMarketInterest;
import com.skytala.eCommerce.entity.MarketInterest;
import com.skytala.eCommerce.entity.MarketInterestMapper;
import com.skytala.eCommerce.event.MarketInterestAdded;
import com.skytala.eCommerce.event.MarketInterestDeleted;
import com.skytala.eCommerce.event.MarketInterestFound;
import com.skytala.eCommerce.event.MarketInterestUpdated;
import com.skytala.eCommerce.query.FindMarketInterestsBy;

@RestController
@RequestMapping("/api/marketInterest")
public class MarketInterestController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<MarketInterest>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public MarketInterestController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a MarketInterest
	 * @return a List with the MarketInterests
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<MarketInterest> findMarketInterestsBy(@RequestParam Map<String, String> allRequestParams) {

		FindMarketInterestsBy query = new FindMarketInterestsBy(allRequestParams);

		int usedTicketId;

		synchronized (MarketInterestController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketInterestFound.class,
				event -> sendMarketInterestsFoundMessage(((MarketInterestFound) event).getMarketInterests(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendMarketInterestsFoundMessage(List<MarketInterest> marketInterests, int usedTicketId) {
		queryReturnVal.put(usedTicketId, marketInterests);
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
	public boolean createMarketInterest(HttpServletRequest request) {

		MarketInterest marketInterestToBeAdded = new MarketInterest();
		try {
			marketInterestToBeAdded = MarketInterestMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createMarketInterest(marketInterestToBeAdded);

	}

	/**
	 * creates a new MarketInterest entry in the ofbiz database
	 * 
	 * @param marketInterestToBeAdded
	 *            the MarketInterest thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createMarketInterest(MarketInterest marketInterestToBeAdded) {

		AddMarketInterest com = new AddMarketInterest(marketInterestToBeAdded);
		int usedTicketId;

		synchronized (MarketInterestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketInterestAdded.class,
				event -> sendMarketInterestChangedMessage(((MarketInterestAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateMarketInterest(HttpServletRequest request) {

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

		MarketInterest marketInterestToBeUpdated = new MarketInterest();

		try {
			marketInterestToBeUpdated = MarketInterestMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateMarketInterest(marketInterestToBeUpdated);

	}

	/**
	 * Updates the MarketInterest with the specific Id
	 * 
	 * @param marketInterestToBeUpdated the MarketInterest thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateMarketInterest(MarketInterest marketInterestToBeUpdated) {

		UpdateMarketInterest com = new UpdateMarketInterest(marketInterestToBeUpdated);

		int usedTicketId;

		synchronized (MarketInterestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketInterestUpdated.class,
				event -> sendMarketInterestChangedMessage(((MarketInterestUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a MarketInterest from the database
	 * 
	 * @param marketInterestId:
	 *            the id of the MarketInterest thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletemarketInterestById(@RequestParam(value = "marketInterestId") String marketInterestId) {

		DeleteMarketInterest com = new DeleteMarketInterest(marketInterestId);

		int usedTicketId;

		synchronized (MarketInterestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketInterestDeleted.class,
				event -> sendMarketInterestChangedMessage(((MarketInterestDeleted) event).isSuccess(), usedTicketId));

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

	public void sendMarketInterestChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/marketInterest/\" plus one of the following: "
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

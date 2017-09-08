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
import com.skytala.eCommerce.command.AddMarketingCampaignPromo;
import com.skytala.eCommerce.command.DeleteMarketingCampaignPromo;
import com.skytala.eCommerce.command.UpdateMarketingCampaignPromo;
import com.skytala.eCommerce.entity.MarketingCampaignPromo;
import com.skytala.eCommerce.entity.MarketingCampaignPromoMapper;
import com.skytala.eCommerce.event.MarketingCampaignPromoAdded;
import com.skytala.eCommerce.event.MarketingCampaignPromoDeleted;
import com.skytala.eCommerce.event.MarketingCampaignPromoFound;
import com.skytala.eCommerce.event.MarketingCampaignPromoUpdated;
import com.skytala.eCommerce.query.FindMarketingCampaignPromosBy;

@RestController
@RequestMapping("/api/marketingCampaignPromo")
public class MarketingCampaignPromoController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<MarketingCampaignPromo>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public MarketingCampaignPromoController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a MarketingCampaignPromo
	 * @return a List with the MarketingCampaignPromos
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<MarketingCampaignPromo> findMarketingCampaignPromosBy(@RequestParam Map<String, String> allRequestParams) {

		FindMarketingCampaignPromosBy query = new FindMarketingCampaignPromosBy(allRequestParams);

		int usedTicketId;

		synchronized (MarketingCampaignPromoController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignPromoFound.class,
				event -> sendMarketingCampaignPromosFoundMessage(((MarketingCampaignPromoFound) event).getMarketingCampaignPromos(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendMarketingCampaignPromosFoundMessage(List<MarketingCampaignPromo> marketingCampaignPromos, int usedTicketId) {
		queryReturnVal.put(usedTicketId, marketingCampaignPromos);
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
	public boolean createMarketingCampaignPromo(HttpServletRequest request) {

		MarketingCampaignPromo marketingCampaignPromoToBeAdded = new MarketingCampaignPromo();
		try {
			marketingCampaignPromoToBeAdded = MarketingCampaignPromoMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createMarketingCampaignPromo(marketingCampaignPromoToBeAdded);

	}

	/**
	 * creates a new MarketingCampaignPromo entry in the ofbiz database
	 * 
	 * @param marketingCampaignPromoToBeAdded
	 *            the MarketingCampaignPromo thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createMarketingCampaignPromo(MarketingCampaignPromo marketingCampaignPromoToBeAdded) {

		AddMarketingCampaignPromo com = new AddMarketingCampaignPromo(marketingCampaignPromoToBeAdded);
		int usedTicketId;

		synchronized (MarketingCampaignPromoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignPromoAdded.class,
				event -> sendMarketingCampaignPromoChangedMessage(((MarketingCampaignPromoAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateMarketingCampaignPromo(HttpServletRequest request) {

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

		MarketingCampaignPromo marketingCampaignPromoToBeUpdated = new MarketingCampaignPromo();

		try {
			marketingCampaignPromoToBeUpdated = MarketingCampaignPromoMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateMarketingCampaignPromo(marketingCampaignPromoToBeUpdated);

	}

	/**
	 * Updates the MarketingCampaignPromo with the specific Id
	 * 
	 * @param marketingCampaignPromoToBeUpdated the MarketingCampaignPromo thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateMarketingCampaignPromo(MarketingCampaignPromo marketingCampaignPromoToBeUpdated) {

		UpdateMarketingCampaignPromo com = new UpdateMarketingCampaignPromo(marketingCampaignPromoToBeUpdated);

		int usedTicketId;

		synchronized (MarketingCampaignPromoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignPromoUpdated.class,
				event -> sendMarketingCampaignPromoChangedMessage(((MarketingCampaignPromoUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a MarketingCampaignPromo from the database
	 * 
	 * @param marketingCampaignPromoId:
	 *            the id of the MarketingCampaignPromo thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletemarketingCampaignPromoById(@RequestParam(value = "marketingCampaignPromoId") String marketingCampaignPromoId) {

		DeleteMarketingCampaignPromo com = new DeleteMarketingCampaignPromo(marketingCampaignPromoId);

		int usedTicketId;

		synchronized (MarketingCampaignPromoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignPromoDeleted.class,
				event -> sendMarketingCampaignPromoChangedMessage(((MarketingCampaignPromoDeleted) event).isSuccess(), usedTicketId));

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

	public void sendMarketingCampaignPromoChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/marketingCampaignPromo/\" plus one of the following: "
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

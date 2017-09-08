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
import com.skytala.eCommerce.command.AddAffiliate;
import com.skytala.eCommerce.command.DeleteAffiliate;
import com.skytala.eCommerce.command.UpdateAffiliate;
import com.skytala.eCommerce.entity.Affiliate;
import com.skytala.eCommerce.entity.AffiliateMapper;
import com.skytala.eCommerce.event.AffiliateAdded;
import com.skytala.eCommerce.event.AffiliateDeleted;
import com.skytala.eCommerce.event.AffiliateFound;
import com.skytala.eCommerce.event.AffiliateUpdated;
import com.skytala.eCommerce.query.FindAffiliatesBy;

@RestController
@RequestMapping("/api/affiliate")
public class AffiliateController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Affiliate>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AffiliateController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Affiliate
	 * @return a List with the Affiliates
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Affiliate> findAffiliatesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAffiliatesBy query = new FindAffiliatesBy(allRequestParams);

		int usedTicketId;

		synchronized (AffiliateController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AffiliateFound.class,
				event -> sendAffiliatesFoundMessage(((AffiliateFound) event).getAffiliates(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAffiliatesFoundMessage(List<Affiliate> affiliates, int usedTicketId) {
		queryReturnVal.put(usedTicketId, affiliates);
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
	public boolean createAffiliate(HttpServletRequest request) {

		Affiliate affiliateToBeAdded = new Affiliate();
		try {
			affiliateToBeAdded = AffiliateMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAffiliate(affiliateToBeAdded);

	}

	/**
	 * creates a new Affiliate entry in the ofbiz database
	 * 
	 * @param affiliateToBeAdded
	 *            the Affiliate thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAffiliate(Affiliate affiliateToBeAdded) {

		AddAffiliate com = new AddAffiliate(affiliateToBeAdded);
		int usedTicketId;

		synchronized (AffiliateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AffiliateAdded.class,
				event -> sendAffiliateChangedMessage(((AffiliateAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAffiliate(HttpServletRequest request) {

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

		Affiliate affiliateToBeUpdated = new Affiliate();

		try {
			affiliateToBeUpdated = AffiliateMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAffiliate(affiliateToBeUpdated);

	}

	/**
	 * Updates the Affiliate with the specific Id
	 * 
	 * @param affiliateToBeUpdated the Affiliate thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAffiliate(Affiliate affiliateToBeUpdated) {

		UpdateAffiliate com = new UpdateAffiliate(affiliateToBeUpdated);

		int usedTicketId;

		synchronized (AffiliateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AffiliateUpdated.class,
				event -> sendAffiliateChangedMessage(((AffiliateUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Affiliate from the database
	 * 
	 * @param affiliateId:
	 *            the id of the Affiliate thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteaffiliateById(@RequestParam(value = "affiliateId") String affiliateId) {

		DeleteAffiliate com = new DeleteAffiliate(affiliateId);

		int usedTicketId;

		synchronized (AffiliateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AffiliateDeleted.class,
				event -> sendAffiliateChangedMessage(((AffiliateDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAffiliateChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/affiliate/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddFixedAssetStdCost;
import com.skytala.eCommerce.command.DeleteFixedAssetStdCost;
import com.skytala.eCommerce.command.UpdateFixedAssetStdCost;
import com.skytala.eCommerce.entity.FixedAssetStdCost;
import com.skytala.eCommerce.entity.FixedAssetStdCostMapper;
import com.skytala.eCommerce.event.FixedAssetStdCostAdded;
import com.skytala.eCommerce.event.FixedAssetStdCostDeleted;
import com.skytala.eCommerce.event.FixedAssetStdCostFound;
import com.skytala.eCommerce.event.FixedAssetStdCostUpdated;
import com.skytala.eCommerce.query.FindFixedAssetStdCostsBy;

@RestController
@RequestMapping("/api/fixedAssetStdCost")
public class FixedAssetStdCostController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetStdCost>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetStdCostController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetStdCost
	 * @return a List with the FixedAssetStdCosts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetStdCost> findFixedAssetStdCostsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetStdCostsBy query = new FindFixedAssetStdCostsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetStdCostController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetStdCostFound.class,
				event -> sendFixedAssetStdCostsFoundMessage(((FixedAssetStdCostFound) event).getFixedAssetStdCosts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetStdCostsFoundMessage(List<FixedAssetStdCost> fixedAssetStdCosts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetStdCosts);
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
	public boolean createFixedAssetStdCost(HttpServletRequest request) {

		FixedAssetStdCost fixedAssetStdCostToBeAdded = new FixedAssetStdCost();
		try {
			fixedAssetStdCostToBeAdded = FixedAssetStdCostMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetStdCost(fixedAssetStdCostToBeAdded);

	}

	/**
	 * creates a new FixedAssetStdCost entry in the ofbiz database
	 * 
	 * @param fixedAssetStdCostToBeAdded
	 *            the FixedAssetStdCost thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetStdCost(FixedAssetStdCost fixedAssetStdCostToBeAdded) {

		AddFixedAssetStdCost com = new AddFixedAssetStdCost(fixedAssetStdCostToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetStdCostController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetStdCostAdded.class,
				event -> sendFixedAssetStdCostChangedMessage(((FixedAssetStdCostAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetStdCost(HttpServletRequest request) {

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

		FixedAssetStdCost fixedAssetStdCostToBeUpdated = new FixedAssetStdCost();

		try {
			fixedAssetStdCostToBeUpdated = FixedAssetStdCostMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetStdCost(fixedAssetStdCostToBeUpdated);

	}

	/**
	 * Updates the FixedAssetStdCost with the specific Id
	 * 
	 * @param fixedAssetStdCostToBeUpdated the FixedAssetStdCost thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetStdCost(FixedAssetStdCost fixedAssetStdCostToBeUpdated) {

		UpdateFixedAssetStdCost com = new UpdateFixedAssetStdCost(fixedAssetStdCostToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetStdCostController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetStdCostUpdated.class,
				event -> sendFixedAssetStdCostChangedMessage(((FixedAssetStdCostUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetStdCost from the database
	 * 
	 * @param fixedAssetStdCostId:
	 *            the id of the FixedAssetStdCost thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetStdCostById(@RequestParam(value = "fixedAssetStdCostId") String fixedAssetStdCostId) {

		DeleteFixedAssetStdCost com = new DeleteFixedAssetStdCost(fixedAssetStdCostId);

		int usedTicketId;

		synchronized (FixedAssetStdCostController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetStdCostDeleted.class,
				event -> sendFixedAssetStdCostChangedMessage(((FixedAssetStdCostDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetStdCostChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetStdCost/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddMetaDataPredicate;
import com.skytala.eCommerce.command.DeleteMetaDataPredicate;
import com.skytala.eCommerce.command.UpdateMetaDataPredicate;
import com.skytala.eCommerce.entity.MetaDataPredicate;
import com.skytala.eCommerce.entity.MetaDataPredicateMapper;
import com.skytala.eCommerce.event.MetaDataPredicateAdded;
import com.skytala.eCommerce.event.MetaDataPredicateDeleted;
import com.skytala.eCommerce.event.MetaDataPredicateFound;
import com.skytala.eCommerce.event.MetaDataPredicateUpdated;
import com.skytala.eCommerce.query.FindMetaDataPredicatesBy;

@RestController
@RequestMapping("/api/metaDataPredicate")
public class MetaDataPredicateController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<MetaDataPredicate>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public MetaDataPredicateController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a MetaDataPredicate
	 * @return a List with the MetaDataPredicates
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<MetaDataPredicate> findMetaDataPredicatesBy(@RequestParam Map<String, String> allRequestParams) {

		FindMetaDataPredicatesBy query = new FindMetaDataPredicatesBy(allRequestParams);

		int usedTicketId;

		synchronized (MetaDataPredicateController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MetaDataPredicateFound.class,
				event -> sendMetaDataPredicatesFoundMessage(((MetaDataPredicateFound) event).getMetaDataPredicates(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendMetaDataPredicatesFoundMessage(List<MetaDataPredicate> metaDataPredicates, int usedTicketId) {
		queryReturnVal.put(usedTicketId, metaDataPredicates);
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
	public boolean createMetaDataPredicate(HttpServletRequest request) {

		MetaDataPredicate metaDataPredicateToBeAdded = new MetaDataPredicate();
		try {
			metaDataPredicateToBeAdded = MetaDataPredicateMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createMetaDataPredicate(metaDataPredicateToBeAdded);

	}

	/**
	 * creates a new MetaDataPredicate entry in the ofbiz database
	 * 
	 * @param metaDataPredicateToBeAdded
	 *            the MetaDataPredicate thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createMetaDataPredicate(MetaDataPredicate metaDataPredicateToBeAdded) {

		AddMetaDataPredicate com = new AddMetaDataPredicate(metaDataPredicateToBeAdded);
		int usedTicketId;

		synchronized (MetaDataPredicateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MetaDataPredicateAdded.class,
				event -> sendMetaDataPredicateChangedMessage(((MetaDataPredicateAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateMetaDataPredicate(HttpServletRequest request) {

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

		MetaDataPredicate metaDataPredicateToBeUpdated = new MetaDataPredicate();

		try {
			metaDataPredicateToBeUpdated = MetaDataPredicateMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateMetaDataPredicate(metaDataPredicateToBeUpdated);

	}

	/**
	 * Updates the MetaDataPredicate with the specific Id
	 * 
	 * @param metaDataPredicateToBeUpdated the MetaDataPredicate thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateMetaDataPredicate(MetaDataPredicate metaDataPredicateToBeUpdated) {

		UpdateMetaDataPredicate com = new UpdateMetaDataPredicate(metaDataPredicateToBeUpdated);

		int usedTicketId;

		synchronized (MetaDataPredicateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MetaDataPredicateUpdated.class,
				event -> sendMetaDataPredicateChangedMessage(((MetaDataPredicateUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a MetaDataPredicate from the database
	 * 
	 * @param metaDataPredicateId:
	 *            the id of the MetaDataPredicate thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletemetaDataPredicateById(@RequestParam(value = "metaDataPredicateId") String metaDataPredicateId) {

		DeleteMetaDataPredicate com = new DeleteMetaDataPredicate(metaDataPredicateId);

		int usedTicketId;

		synchronized (MetaDataPredicateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MetaDataPredicateDeleted.class,
				event -> sendMetaDataPredicateChangedMessage(((MetaDataPredicateDeleted) event).isSuccess(), usedTicketId));

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

	public void sendMetaDataPredicateChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/metaDataPredicate/\" plus one of the following: "
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

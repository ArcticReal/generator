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
import com.skytala.eCommerce.command.AddTaxAuthorityCategory;
import com.skytala.eCommerce.command.DeleteTaxAuthorityCategory;
import com.skytala.eCommerce.command.UpdateTaxAuthorityCategory;
import com.skytala.eCommerce.entity.TaxAuthorityCategory;
import com.skytala.eCommerce.entity.TaxAuthorityCategoryMapper;
import com.skytala.eCommerce.event.TaxAuthorityCategoryAdded;
import com.skytala.eCommerce.event.TaxAuthorityCategoryDeleted;
import com.skytala.eCommerce.event.TaxAuthorityCategoryFound;
import com.skytala.eCommerce.event.TaxAuthorityCategoryUpdated;
import com.skytala.eCommerce.query.FindTaxAuthorityCategorysBy;

@RestController
@RequestMapping("/api/taxAuthorityCategory")
public class TaxAuthorityCategoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TaxAuthorityCategory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TaxAuthorityCategoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TaxAuthorityCategory
	 * @return a List with the TaxAuthorityCategorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TaxAuthorityCategory> findTaxAuthorityCategorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindTaxAuthorityCategorysBy query = new FindTaxAuthorityCategorysBy(allRequestParams);

		int usedTicketId;

		synchronized (TaxAuthorityCategoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityCategoryFound.class,
				event -> sendTaxAuthorityCategorysFoundMessage(((TaxAuthorityCategoryFound) event).getTaxAuthorityCategorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTaxAuthorityCategorysFoundMessage(List<TaxAuthorityCategory> taxAuthorityCategorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, taxAuthorityCategorys);
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
	public boolean createTaxAuthorityCategory(HttpServletRequest request) {

		TaxAuthorityCategory taxAuthorityCategoryToBeAdded = new TaxAuthorityCategory();
		try {
			taxAuthorityCategoryToBeAdded = TaxAuthorityCategoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTaxAuthorityCategory(taxAuthorityCategoryToBeAdded);

	}

	/**
	 * creates a new TaxAuthorityCategory entry in the ofbiz database
	 * 
	 * @param taxAuthorityCategoryToBeAdded
	 *            the TaxAuthorityCategory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTaxAuthorityCategory(TaxAuthorityCategory taxAuthorityCategoryToBeAdded) {

		AddTaxAuthorityCategory com = new AddTaxAuthorityCategory(taxAuthorityCategoryToBeAdded);
		int usedTicketId;

		synchronized (TaxAuthorityCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityCategoryAdded.class,
				event -> sendTaxAuthorityCategoryChangedMessage(((TaxAuthorityCategoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTaxAuthorityCategory(HttpServletRequest request) {

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

		TaxAuthorityCategory taxAuthorityCategoryToBeUpdated = new TaxAuthorityCategory();

		try {
			taxAuthorityCategoryToBeUpdated = TaxAuthorityCategoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTaxAuthorityCategory(taxAuthorityCategoryToBeUpdated);

	}

	/**
	 * Updates the TaxAuthorityCategory with the specific Id
	 * 
	 * @param taxAuthorityCategoryToBeUpdated the TaxAuthorityCategory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTaxAuthorityCategory(TaxAuthorityCategory taxAuthorityCategoryToBeUpdated) {

		UpdateTaxAuthorityCategory com = new UpdateTaxAuthorityCategory(taxAuthorityCategoryToBeUpdated);

		int usedTicketId;

		synchronized (TaxAuthorityCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityCategoryUpdated.class,
				event -> sendTaxAuthorityCategoryChangedMessage(((TaxAuthorityCategoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TaxAuthorityCategory from the database
	 * 
	 * @param taxAuthorityCategoryId:
	 *            the id of the TaxAuthorityCategory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetaxAuthorityCategoryById(@RequestParam(value = "taxAuthorityCategoryId") String taxAuthorityCategoryId) {

		DeleteTaxAuthorityCategory com = new DeleteTaxAuthorityCategory(taxAuthorityCategoryId);

		int usedTicketId;

		synchronized (TaxAuthorityCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityCategoryDeleted.class,
				event -> sendTaxAuthorityCategoryChangedMessage(((TaxAuthorityCategoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTaxAuthorityCategoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/taxAuthorityCategory/\" plus one of the following: "
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

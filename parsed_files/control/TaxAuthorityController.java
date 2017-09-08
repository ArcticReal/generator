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
import com.skytala.eCommerce.command.AddTaxAuthority;
import com.skytala.eCommerce.command.DeleteTaxAuthority;
import com.skytala.eCommerce.command.UpdateTaxAuthority;
import com.skytala.eCommerce.entity.TaxAuthority;
import com.skytala.eCommerce.entity.TaxAuthorityMapper;
import com.skytala.eCommerce.event.TaxAuthorityAdded;
import com.skytala.eCommerce.event.TaxAuthorityDeleted;
import com.skytala.eCommerce.event.TaxAuthorityFound;
import com.skytala.eCommerce.event.TaxAuthorityUpdated;
import com.skytala.eCommerce.query.FindTaxAuthoritysBy;

@RestController
@RequestMapping("/api/taxAuthority")
public class TaxAuthorityController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TaxAuthority>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TaxAuthorityController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TaxAuthority
	 * @return a List with the TaxAuthoritys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TaxAuthority> findTaxAuthoritysBy(@RequestParam Map<String, String> allRequestParams) {

		FindTaxAuthoritysBy query = new FindTaxAuthoritysBy(allRequestParams);

		int usedTicketId;

		synchronized (TaxAuthorityController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityFound.class,
				event -> sendTaxAuthoritysFoundMessage(((TaxAuthorityFound) event).getTaxAuthoritys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTaxAuthoritysFoundMessage(List<TaxAuthority> taxAuthoritys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, taxAuthoritys);
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
	public boolean createTaxAuthority(HttpServletRequest request) {

		TaxAuthority taxAuthorityToBeAdded = new TaxAuthority();
		try {
			taxAuthorityToBeAdded = TaxAuthorityMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTaxAuthority(taxAuthorityToBeAdded);

	}

	/**
	 * creates a new TaxAuthority entry in the ofbiz database
	 * 
	 * @param taxAuthorityToBeAdded
	 *            the TaxAuthority thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTaxAuthority(TaxAuthority taxAuthorityToBeAdded) {

		AddTaxAuthority com = new AddTaxAuthority(taxAuthorityToBeAdded);
		int usedTicketId;

		synchronized (TaxAuthorityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityAdded.class,
				event -> sendTaxAuthorityChangedMessage(((TaxAuthorityAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTaxAuthority(HttpServletRequest request) {

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

		TaxAuthority taxAuthorityToBeUpdated = new TaxAuthority();

		try {
			taxAuthorityToBeUpdated = TaxAuthorityMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTaxAuthority(taxAuthorityToBeUpdated);

	}

	/**
	 * Updates the TaxAuthority with the specific Id
	 * 
	 * @param taxAuthorityToBeUpdated the TaxAuthority thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTaxAuthority(TaxAuthority taxAuthorityToBeUpdated) {

		UpdateTaxAuthority com = new UpdateTaxAuthority(taxAuthorityToBeUpdated);

		int usedTicketId;

		synchronized (TaxAuthorityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityUpdated.class,
				event -> sendTaxAuthorityChangedMessage(((TaxAuthorityUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TaxAuthority from the database
	 * 
	 * @param taxAuthorityId:
	 *            the id of the TaxAuthority thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetaxAuthorityById(@RequestParam(value = "taxAuthorityId") String taxAuthorityId) {

		DeleteTaxAuthority com = new DeleteTaxAuthority(taxAuthorityId);

		int usedTicketId;

		synchronized (TaxAuthorityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityDeleted.class,
				event -> sendTaxAuthorityChangedMessage(((TaxAuthorityDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTaxAuthorityChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/taxAuthority/\" plus one of the following: "
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

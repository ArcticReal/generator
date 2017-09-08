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
import com.skytala.eCommerce.command.AddTaxAuthorityAssocType;
import com.skytala.eCommerce.command.DeleteTaxAuthorityAssocType;
import com.skytala.eCommerce.command.UpdateTaxAuthorityAssocType;
import com.skytala.eCommerce.entity.TaxAuthorityAssocType;
import com.skytala.eCommerce.entity.TaxAuthorityAssocTypeMapper;
import com.skytala.eCommerce.event.TaxAuthorityAssocTypeAdded;
import com.skytala.eCommerce.event.TaxAuthorityAssocTypeDeleted;
import com.skytala.eCommerce.event.TaxAuthorityAssocTypeFound;
import com.skytala.eCommerce.event.TaxAuthorityAssocTypeUpdated;
import com.skytala.eCommerce.query.FindTaxAuthorityAssocTypesBy;

@RestController
@RequestMapping("/api/taxAuthorityAssocType")
public class TaxAuthorityAssocTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TaxAuthorityAssocType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TaxAuthorityAssocTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TaxAuthorityAssocType
	 * @return a List with the TaxAuthorityAssocTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TaxAuthorityAssocType> findTaxAuthorityAssocTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindTaxAuthorityAssocTypesBy query = new FindTaxAuthorityAssocTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (TaxAuthorityAssocTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityAssocTypeFound.class,
				event -> sendTaxAuthorityAssocTypesFoundMessage(((TaxAuthorityAssocTypeFound) event).getTaxAuthorityAssocTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTaxAuthorityAssocTypesFoundMessage(List<TaxAuthorityAssocType> taxAuthorityAssocTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, taxAuthorityAssocTypes);
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
	public boolean createTaxAuthorityAssocType(HttpServletRequest request) {

		TaxAuthorityAssocType taxAuthorityAssocTypeToBeAdded = new TaxAuthorityAssocType();
		try {
			taxAuthorityAssocTypeToBeAdded = TaxAuthorityAssocTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTaxAuthorityAssocType(taxAuthorityAssocTypeToBeAdded);

	}

	/**
	 * creates a new TaxAuthorityAssocType entry in the ofbiz database
	 * 
	 * @param taxAuthorityAssocTypeToBeAdded
	 *            the TaxAuthorityAssocType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTaxAuthorityAssocType(TaxAuthorityAssocType taxAuthorityAssocTypeToBeAdded) {

		AddTaxAuthorityAssocType com = new AddTaxAuthorityAssocType(taxAuthorityAssocTypeToBeAdded);
		int usedTicketId;

		synchronized (TaxAuthorityAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityAssocTypeAdded.class,
				event -> sendTaxAuthorityAssocTypeChangedMessage(((TaxAuthorityAssocTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTaxAuthorityAssocType(HttpServletRequest request) {

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

		TaxAuthorityAssocType taxAuthorityAssocTypeToBeUpdated = new TaxAuthorityAssocType();

		try {
			taxAuthorityAssocTypeToBeUpdated = TaxAuthorityAssocTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTaxAuthorityAssocType(taxAuthorityAssocTypeToBeUpdated);

	}

	/**
	 * Updates the TaxAuthorityAssocType with the specific Id
	 * 
	 * @param taxAuthorityAssocTypeToBeUpdated the TaxAuthorityAssocType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTaxAuthorityAssocType(TaxAuthorityAssocType taxAuthorityAssocTypeToBeUpdated) {

		UpdateTaxAuthorityAssocType com = new UpdateTaxAuthorityAssocType(taxAuthorityAssocTypeToBeUpdated);

		int usedTicketId;

		synchronized (TaxAuthorityAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityAssocTypeUpdated.class,
				event -> sendTaxAuthorityAssocTypeChangedMessage(((TaxAuthorityAssocTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TaxAuthorityAssocType from the database
	 * 
	 * @param taxAuthorityAssocTypeId:
	 *            the id of the TaxAuthorityAssocType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetaxAuthorityAssocTypeById(@RequestParam(value = "taxAuthorityAssocTypeId") String taxAuthorityAssocTypeId) {

		DeleteTaxAuthorityAssocType com = new DeleteTaxAuthorityAssocType(taxAuthorityAssocTypeId);

		int usedTicketId;

		synchronized (TaxAuthorityAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityAssocTypeDeleted.class,
				event -> sendTaxAuthorityAssocTypeChangedMessage(((TaxAuthorityAssocTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTaxAuthorityAssocTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/taxAuthorityAssocType/\" plus one of the following: "
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

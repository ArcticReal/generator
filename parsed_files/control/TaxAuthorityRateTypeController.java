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
import com.skytala.eCommerce.command.AddTaxAuthorityRateType;
import com.skytala.eCommerce.command.DeleteTaxAuthorityRateType;
import com.skytala.eCommerce.command.UpdateTaxAuthorityRateType;
import com.skytala.eCommerce.entity.TaxAuthorityRateType;
import com.skytala.eCommerce.entity.TaxAuthorityRateTypeMapper;
import com.skytala.eCommerce.event.TaxAuthorityRateTypeAdded;
import com.skytala.eCommerce.event.TaxAuthorityRateTypeDeleted;
import com.skytala.eCommerce.event.TaxAuthorityRateTypeFound;
import com.skytala.eCommerce.event.TaxAuthorityRateTypeUpdated;
import com.skytala.eCommerce.query.FindTaxAuthorityRateTypesBy;

@RestController
@RequestMapping("/api/taxAuthorityRateType")
public class TaxAuthorityRateTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TaxAuthorityRateType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TaxAuthorityRateTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TaxAuthorityRateType
	 * @return a List with the TaxAuthorityRateTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TaxAuthorityRateType> findTaxAuthorityRateTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindTaxAuthorityRateTypesBy query = new FindTaxAuthorityRateTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (TaxAuthorityRateTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityRateTypeFound.class,
				event -> sendTaxAuthorityRateTypesFoundMessage(((TaxAuthorityRateTypeFound) event).getTaxAuthorityRateTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTaxAuthorityRateTypesFoundMessage(List<TaxAuthorityRateType> taxAuthorityRateTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, taxAuthorityRateTypes);
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
	public boolean createTaxAuthorityRateType(HttpServletRequest request) {

		TaxAuthorityRateType taxAuthorityRateTypeToBeAdded = new TaxAuthorityRateType();
		try {
			taxAuthorityRateTypeToBeAdded = TaxAuthorityRateTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTaxAuthorityRateType(taxAuthorityRateTypeToBeAdded);

	}

	/**
	 * creates a new TaxAuthorityRateType entry in the ofbiz database
	 * 
	 * @param taxAuthorityRateTypeToBeAdded
	 *            the TaxAuthorityRateType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTaxAuthorityRateType(TaxAuthorityRateType taxAuthorityRateTypeToBeAdded) {

		AddTaxAuthorityRateType com = new AddTaxAuthorityRateType(taxAuthorityRateTypeToBeAdded);
		int usedTicketId;

		synchronized (TaxAuthorityRateTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityRateTypeAdded.class,
				event -> sendTaxAuthorityRateTypeChangedMessage(((TaxAuthorityRateTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTaxAuthorityRateType(HttpServletRequest request) {

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

		TaxAuthorityRateType taxAuthorityRateTypeToBeUpdated = new TaxAuthorityRateType();

		try {
			taxAuthorityRateTypeToBeUpdated = TaxAuthorityRateTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTaxAuthorityRateType(taxAuthorityRateTypeToBeUpdated);

	}

	/**
	 * Updates the TaxAuthorityRateType with the specific Id
	 * 
	 * @param taxAuthorityRateTypeToBeUpdated the TaxAuthorityRateType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTaxAuthorityRateType(TaxAuthorityRateType taxAuthorityRateTypeToBeUpdated) {

		UpdateTaxAuthorityRateType com = new UpdateTaxAuthorityRateType(taxAuthorityRateTypeToBeUpdated);

		int usedTicketId;

		synchronized (TaxAuthorityRateTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityRateTypeUpdated.class,
				event -> sendTaxAuthorityRateTypeChangedMessage(((TaxAuthorityRateTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TaxAuthorityRateType from the database
	 * 
	 * @param taxAuthorityRateTypeId:
	 *            the id of the TaxAuthorityRateType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetaxAuthorityRateTypeById(@RequestParam(value = "taxAuthorityRateTypeId") String taxAuthorityRateTypeId) {

		DeleteTaxAuthorityRateType com = new DeleteTaxAuthorityRateType(taxAuthorityRateTypeId);

		int usedTicketId;

		synchronized (TaxAuthorityRateTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityRateTypeDeleted.class,
				event -> sendTaxAuthorityRateTypeChangedMessage(((TaxAuthorityRateTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTaxAuthorityRateTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/taxAuthorityRateType/\" plus one of the following: "
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
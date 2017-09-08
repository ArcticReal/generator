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
import com.skytala.eCommerce.command.AddTaxAuthorityAssoc;
import com.skytala.eCommerce.command.DeleteTaxAuthorityAssoc;
import com.skytala.eCommerce.command.UpdateTaxAuthorityAssoc;
import com.skytala.eCommerce.entity.TaxAuthorityAssoc;
import com.skytala.eCommerce.entity.TaxAuthorityAssocMapper;
import com.skytala.eCommerce.event.TaxAuthorityAssocAdded;
import com.skytala.eCommerce.event.TaxAuthorityAssocDeleted;
import com.skytala.eCommerce.event.TaxAuthorityAssocFound;
import com.skytala.eCommerce.event.TaxAuthorityAssocUpdated;
import com.skytala.eCommerce.query.FindTaxAuthorityAssocsBy;

@RestController
@RequestMapping("/api/taxAuthorityAssoc")
public class TaxAuthorityAssocController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TaxAuthorityAssoc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TaxAuthorityAssocController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TaxAuthorityAssoc
	 * @return a List with the TaxAuthorityAssocs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TaxAuthorityAssoc> findTaxAuthorityAssocsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTaxAuthorityAssocsBy query = new FindTaxAuthorityAssocsBy(allRequestParams);

		int usedTicketId;

		synchronized (TaxAuthorityAssocController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityAssocFound.class,
				event -> sendTaxAuthorityAssocsFoundMessage(((TaxAuthorityAssocFound) event).getTaxAuthorityAssocs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTaxAuthorityAssocsFoundMessage(List<TaxAuthorityAssoc> taxAuthorityAssocs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, taxAuthorityAssocs);
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
	public boolean createTaxAuthorityAssoc(HttpServletRequest request) {

		TaxAuthorityAssoc taxAuthorityAssocToBeAdded = new TaxAuthorityAssoc();
		try {
			taxAuthorityAssocToBeAdded = TaxAuthorityAssocMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTaxAuthorityAssoc(taxAuthorityAssocToBeAdded);

	}

	/**
	 * creates a new TaxAuthorityAssoc entry in the ofbiz database
	 * 
	 * @param taxAuthorityAssocToBeAdded
	 *            the TaxAuthorityAssoc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTaxAuthorityAssoc(TaxAuthorityAssoc taxAuthorityAssocToBeAdded) {

		AddTaxAuthorityAssoc com = new AddTaxAuthorityAssoc(taxAuthorityAssocToBeAdded);
		int usedTicketId;

		synchronized (TaxAuthorityAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityAssocAdded.class,
				event -> sendTaxAuthorityAssocChangedMessage(((TaxAuthorityAssocAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTaxAuthorityAssoc(HttpServletRequest request) {

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

		TaxAuthorityAssoc taxAuthorityAssocToBeUpdated = new TaxAuthorityAssoc();

		try {
			taxAuthorityAssocToBeUpdated = TaxAuthorityAssocMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTaxAuthorityAssoc(taxAuthorityAssocToBeUpdated);

	}

	/**
	 * Updates the TaxAuthorityAssoc with the specific Id
	 * 
	 * @param taxAuthorityAssocToBeUpdated the TaxAuthorityAssoc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTaxAuthorityAssoc(TaxAuthorityAssoc taxAuthorityAssocToBeUpdated) {

		UpdateTaxAuthorityAssoc com = new UpdateTaxAuthorityAssoc(taxAuthorityAssocToBeUpdated);

		int usedTicketId;

		synchronized (TaxAuthorityAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityAssocUpdated.class,
				event -> sendTaxAuthorityAssocChangedMessage(((TaxAuthorityAssocUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TaxAuthorityAssoc from the database
	 * 
	 * @param taxAuthorityAssocId:
	 *            the id of the TaxAuthorityAssoc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetaxAuthorityAssocById(@RequestParam(value = "taxAuthorityAssocId") String taxAuthorityAssocId) {

		DeleteTaxAuthorityAssoc com = new DeleteTaxAuthorityAssoc(taxAuthorityAssocId);

		int usedTicketId;

		synchronized (TaxAuthorityAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityAssocDeleted.class,
				event -> sendTaxAuthorityAssocChangedMessage(((TaxAuthorityAssocDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTaxAuthorityAssocChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/taxAuthorityAssoc/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddInvoiceContactMech;
import com.skytala.eCommerce.command.DeleteInvoiceContactMech;
import com.skytala.eCommerce.command.UpdateInvoiceContactMech;
import com.skytala.eCommerce.entity.InvoiceContactMech;
import com.skytala.eCommerce.entity.InvoiceContactMechMapper;
import com.skytala.eCommerce.event.InvoiceContactMechAdded;
import com.skytala.eCommerce.event.InvoiceContactMechDeleted;
import com.skytala.eCommerce.event.InvoiceContactMechFound;
import com.skytala.eCommerce.event.InvoiceContactMechUpdated;
import com.skytala.eCommerce.query.FindInvoiceContactMechsBy;

@RestController
@RequestMapping("/api/invoiceContactMech")
public class InvoiceContactMechController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceContactMech>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceContactMechController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceContactMech
	 * @return a List with the InvoiceContactMechs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceContactMech> findInvoiceContactMechsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceContactMechsBy query = new FindInvoiceContactMechsBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceContactMechController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContactMechFound.class,
				event -> sendInvoiceContactMechsFoundMessage(((InvoiceContactMechFound) event).getInvoiceContactMechs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceContactMechsFoundMessage(List<InvoiceContactMech> invoiceContactMechs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceContactMechs);
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
	public boolean createInvoiceContactMech(HttpServletRequest request) {

		InvoiceContactMech invoiceContactMechToBeAdded = new InvoiceContactMech();
		try {
			invoiceContactMechToBeAdded = InvoiceContactMechMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceContactMech(invoiceContactMechToBeAdded);

	}

	/**
	 * creates a new InvoiceContactMech entry in the ofbiz database
	 * 
	 * @param invoiceContactMechToBeAdded
	 *            the InvoiceContactMech thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceContactMech(InvoiceContactMech invoiceContactMechToBeAdded) {

		AddInvoiceContactMech com = new AddInvoiceContactMech(invoiceContactMechToBeAdded);
		int usedTicketId;

		synchronized (InvoiceContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContactMechAdded.class,
				event -> sendInvoiceContactMechChangedMessage(((InvoiceContactMechAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceContactMech(HttpServletRequest request) {

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

		InvoiceContactMech invoiceContactMechToBeUpdated = new InvoiceContactMech();

		try {
			invoiceContactMechToBeUpdated = InvoiceContactMechMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceContactMech(invoiceContactMechToBeUpdated);

	}

	/**
	 * Updates the InvoiceContactMech with the specific Id
	 * 
	 * @param invoiceContactMechToBeUpdated the InvoiceContactMech thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceContactMech(InvoiceContactMech invoiceContactMechToBeUpdated) {

		UpdateInvoiceContactMech com = new UpdateInvoiceContactMech(invoiceContactMechToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContactMechUpdated.class,
				event -> sendInvoiceContactMechChangedMessage(((InvoiceContactMechUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceContactMech from the database
	 * 
	 * @param invoiceContactMechId:
	 *            the id of the InvoiceContactMech thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceContactMechById(@RequestParam(value = "invoiceContactMechId") String invoiceContactMechId) {

		DeleteInvoiceContactMech com = new DeleteInvoiceContactMech(invoiceContactMechId);

		int usedTicketId;

		synchronized (InvoiceContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContactMechDeleted.class,
				event -> sendInvoiceContactMechChangedMessage(((InvoiceContactMechDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceContactMechChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceContactMech/\" plus one of the following: "
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

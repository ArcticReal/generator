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
import com.skytala.eCommerce.command.AddInvoiceItemAssocType;
import com.skytala.eCommerce.command.DeleteInvoiceItemAssocType;
import com.skytala.eCommerce.command.UpdateInvoiceItemAssocType;
import com.skytala.eCommerce.entity.InvoiceItemAssocType;
import com.skytala.eCommerce.entity.InvoiceItemAssocTypeMapper;
import com.skytala.eCommerce.event.InvoiceItemAssocTypeAdded;
import com.skytala.eCommerce.event.InvoiceItemAssocTypeDeleted;
import com.skytala.eCommerce.event.InvoiceItemAssocTypeFound;
import com.skytala.eCommerce.event.InvoiceItemAssocTypeUpdated;
import com.skytala.eCommerce.query.FindInvoiceItemAssocTypesBy;

@RestController
@RequestMapping("/api/invoiceItemAssocType")
public class InvoiceItemAssocTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceItemAssocType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceItemAssocTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceItemAssocType
	 * @return a List with the InvoiceItemAssocTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceItemAssocType> findInvoiceItemAssocTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceItemAssocTypesBy query = new FindInvoiceItemAssocTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceItemAssocTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemAssocTypeFound.class,
				event -> sendInvoiceItemAssocTypesFoundMessage(((InvoiceItemAssocTypeFound) event).getInvoiceItemAssocTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceItemAssocTypesFoundMessage(List<InvoiceItemAssocType> invoiceItemAssocTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceItemAssocTypes);
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
	public boolean createInvoiceItemAssocType(HttpServletRequest request) {

		InvoiceItemAssocType invoiceItemAssocTypeToBeAdded = new InvoiceItemAssocType();
		try {
			invoiceItemAssocTypeToBeAdded = InvoiceItemAssocTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceItemAssocType(invoiceItemAssocTypeToBeAdded);

	}

	/**
	 * creates a new InvoiceItemAssocType entry in the ofbiz database
	 * 
	 * @param invoiceItemAssocTypeToBeAdded
	 *            the InvoiceItemAssocType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceItemAssocType(InvoiceItemAssocType invoiceItemAssocTypeToBeAdded) {

		AddInvoiceItemAssocType com = new AddInvoiceItemAssocType(invoiceItemAssocTypeToBeAdded);
		int usedTicketId;

		synchronized (InvoiceItemAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemAssocTypeAdded.class,
				event -> sendInvoiceItemAssocTypeChangedMessage(((InvoiceItemAssocTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceItemAssocType(HttpServletRequest request) {

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

		InvoiceItemAssocType invoiceItemAssocTypeToBeUpdated = new InvoiceItemAssocType();

		try {
			invoiceItemAssocTypeToBeUpdated = InvoiceItemAssocTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceItemAssocType(invoiceItemAssocTypeToBeUpdated);

	}

	/**
	 * Updates the InvoiceItemAssocType with the specific Id
	 * 
	 * @param invoiceItemAssocTypeToBeUpdated the InvoiceItemAssocType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceItemAssocType(InvoiceItemAssocType invoiceItemAssocTypeToBeUpdated) {

		UpdateInvoiceItemAssocType com = new UpdateInvoiceItemAssocType(invoiceItemAssocTypeToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceItemAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemAssocTypeUpdated.class,
				event -> sendInvoiceItemAssocTypeChangedMessage(((InvoiceItemAssocTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceItemAssocType from the database
	 * 
	 * @param invoiceItemAssocTypeId:
	 *            the id of the InvoiceItemAssocType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceItemAssocTypeById(@RequestParam(value = "invoiceItemAssocTypeId") String invoiceItemAssocTypeId) {

		DeleteInvoiceItemAssocType com = new DeleteInvoiceItemAssocType(invoiceItemAssocTypeId);

		int usedTicketId;

		synchronized (InvoiceItemAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemAssocTypeDeleted.class,
				event -> sendInvoiceItemAssocTypeChangedMessage(((InvoiceItemAssocTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceItemAssocTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceItemAssocType/\" plus one of the following: "
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

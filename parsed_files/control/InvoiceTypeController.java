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
import com.skytala.eCommerce.command.AddInvoiceType;
import com.skytala.eCommerce.command.DeleteInvoiceType;
import com.skytala.eCommerce.command.UpdateInvoiceType;
import com.skytala.eCommerce.entity.InvoiceType;
import com.skytala.eCommerce.entity.InvoiceTypeMapper;
import com.skytala.eCommerce.event.InvoiceTypeAdded;
import com.skytala.eCommerce.event.InvoiceTypeDeleted;
import com.skytala.eCommerce.event.InvoiceTypeFound;
import com.skytala.eCommerce.event.InvoiceTypeUpdated;
import com.skytala.eCommerce.query.FindInvoiceTypesBy;

@RestController
@RequestMapping("/api/invoiceType")
public class InvoiceTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceType
	 * @return a List with the InvoiceTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceType> findInvoiceTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceTypesBy query = new FindInvoiceTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTypeFound.class,
				event -> sendInvoiceTypesFoundMessage(((InvoiceTypeFound) event).getInvoiceTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceTypesFoundMessage(List<InvoiceType> invoiceTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceTypes);
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
	public boolean createInvoiceType(HttpServletRequest request) {

		InvoiceType invoiceTypeToBeAdded = new InvoiceType();
		try {
			invoiceTypeToBeAdded = InvoiceTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceType(invoiceTypeToBeAdded);

	}

	/**
	 * creates a new InvoiceType entry in the ofbiz database
	 * 
	 * @param invoiceTypeToBeAdded
	 *            the InvoiceType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceType(InvoiceType invoiceTypeToBeAdded) {

		AddInvoiceType com = new AddInvoiceType(invoiceTypeToBeAdded);
		int usedTicketId;

		synchronized (InvoiceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTypeAdded.class,
				event -> sendInvoiceTypeChangedMessage(((InvoiceTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceType(HttpServletRequest request) {

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

		InvoiceType invoiceTypeToBeUpdated = new InvoiceType();

		try {
			invoiceTypeToBeUpdated = InvoiceTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceType(invoiceTypeToBeUpdated);

	}

	/**
	 * Updates the InvoiceType with the specific Id
	 * 
	 * @param invoiceTypeToBeUpdated the InvoiceType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceType(InvoiceType invoiceTypeToBeUpdated) {

		UpdateInvoiceType com = new UpdateInvoiceType(invoiceTypeToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTypeUpdated.class,
				event -> sendInvoiceTypeChangedMessage(((InvoiceTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceType from the database
	 * 
	 * @param invoiceTypeId:
	 *            the id of the InvoiceType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceTypeById(@RequestParam(value = "invoiceTypeId") String invoiceTypeId) {

		DeleteInvoiceType com = new DeleteInvoiceType(invoiceTypeId);

		int usedTicketId;

		synchronized (InvoiceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTypeDeleted.class,
				event -> sendInvoiceTypeChangedMessage(((InvoiceTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceType/\" plus one of the following: "
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

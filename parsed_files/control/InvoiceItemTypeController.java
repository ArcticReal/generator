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
import com.skytala.eCommerce.command.AddInvoiceItemType;
import com.skytala.eCommerce.command.DeleteInvoiceItemType;
import com.skytala.eCommerce.command.UpdateInvoiceItemType;
import com.skytala.eCommerce.entity.InvoiceItemType;
import com.skytala.eCommerce.entity.InvoiceItemTypeMapper;
import com.skytala.eCommerce.event.InvoiceItemTypeAdded;
import com.skytala.eCommerce.event.InvoiceItemTypeDeleted;
import com.skytala.eCommerce.event.InvoiceItemTypeFound;
import com.skytala.eCommerce.event.InvoiceItemTypeUpdated;
import com.skytala.eCommerce.query.FindInvoiceItemTypesBy;

@RestController
@RequestMapping("/api/invoiceItemType")
public class InvoiceItemTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceItemType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceItemTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceItemType
	 * @return a List with the InvoiceItemTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceItemType> findInvoiceItemTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceItemTypesBy query = new FindInvoiceItemTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceItemTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeFound.class,
				event -> sendInvoiceItemTypesFoundMessage(((InvoiceItemTypeFound) event).getInvoiceItemTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceItemTypesFoundMessage(List<InvoiceItemType> invoiceItemTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceItemTypes);
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
	public boolean createInvoiceItemType(HttpServletRequest request) {

		InvoiceItemType invoiceItemTypeToBeAdded = new InvoiceItemType();
		try {
			invoiceItemTypeToBeAdded = InvoiceItemTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceItemType(invoiceItemTypeToBeAdded);

	}

	/**
	 * creates a new InvoiceItemType entry in the ofbiz database
	 * 
	 * @param invoiceItemTypeToBeAdded
	 *            the InvoiceItemType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceItemType(InvoiceItemType invoiceItemTypeToBeAdded) {

		AddInvoiceItemType com = new AddInvoiceItemType(invoiceItemTypeToBeAdded);
		int usedTicketId;

		synchronized (InvoiceItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeAdded.class,
				event -> sendInvoiceItemTypeChangedMessage(((InvoiceItemTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceItemType(HttpServletRequest request) {

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

		InvoiceItemType invoiceItemTypeToBeUpdated = new InvoiceItemType();

		try {
			invoiceItemTypeToBeUpdated = InvoiceItemTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceItemType(invoiceItemTypeToBeUpdated);

	}

	/**
	 * Updates the InvoiceItemType with the specific Id
	 * 
	 * @param invoiceItemTypeToBeUpdated the InvoiceItemType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceItemType(InvoiceItemType invoiceItemTypeToBeUpdated) {

		UpdateInvoiceItemType com = new UpdateInvoiceItemType(invoiceItemTypeToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeUpdated.class,
				event -> sendInvoiceItemTypeChangedMessage(((InvoiceItemTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceItemType from the database
	 * 
	 * @param invoiceItemTypeId:
	 *            the id of the InvoiceItemType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceItemTypeById(@RequestParam(value = "invoiceItemTypeId") String invoiceItemTypeId) {

		DeleteInvoiceItemType com = new DeleteInvoiceItemType(invoiceItemTypeId);

		int usedTicketId;

		synchronized (InvoiceItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeDeleted.class,
				event -> sendInvoiceItemTypeChangedMessage(((InvoiceItemTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceItemTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceItemType/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddInvoiceContentType;
import com.skytala.eCommerce.command.DeleteInvoiceContentType;
import com.skytala.eCommerce.command.UpdateInvoiceContentType;
import com.skytala.eCommerce.entity.InvoiceContentType;
import com.skytala.eCommerce.entity.InvoiceContentTypeMapper;
import com.skytala.eCommerce.event.InvoiceContentTypeAdded;
import com.skytala.eCommerce.event.InvoiceContentTypeDeleted;
import com.skytala.eCommerce.event.InvoiceContentTypeFound;
import com.skytala.eCommerce.event.InvoiceContentTypeUpdated;
import com.skytala.eCommerce.query.FindInvoiceContentTypesBy;

@RestController
@RequestMapping("/api/invoiceContentType")
public class InvoiceContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceContentType
	 * @return a List with the InvoiceContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceContentType> findInvoiceContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceContentTypesBy query = new FindInvoiceContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContentTypeFound.class,
				event -> sendInvoiceContentTypesFoundMessage(((InvoiceContentTypeFound) event).getInvoiceContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceContentTypesFoundMessage(List<InvoiceContentType> invoiceContentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceContentTypes);
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
	public boolean createInvoiceContentType(HttpServletRequest request) {

		InvoiceContentType invoiceContentTypeToBeAdded = new InvoiceContentType();
		try {
			invoiceContentTypeToBeAdded = InvoiceContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceContentType(invoiceContentTypeToBeAdded);

	}

	/**
	 * creates a new InvoiceContentType entry in the ofbiz database
	 * 
	 * @param invoiceContentTypeToBeAdded
	 *            the InvoiceContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceContentType(InvoiceContentType invoiceContentTypeToBeAdded) {

		AddInvoiceContentType com = new AddInvoiceContentType(invoiceContentTypeToBeAdded);
		int usedTicketId;

		synchronized (InvoiceContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContentTypeAdded.class,
				event -> sendInvoiceContentTypeChangedMessage(((InvoiceContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceContentType(HttpServletRequest request) {

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

		InvoiceContentType invoiceContentTypeToBeUpdated = new InvoiceContentType();

		try {
			invoiceContentTypeToBeUpdated = InvoiceContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceContentType(invoiceContentTypeToBeUpdated);

	}

	/**
	 * Updates the InvoiceContentType with the specific Id
	 * 
	 * @param invoiceContentTypeToBeUpdated the InvoiceContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceContentType(InvoiceContentType invoiceContentTypeToBeUpdated) {

		UpdateInvoiceContentType com = new UpdateInvoiceContentType(invoiceContentTypeToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContentTypeUpdated.class,
				event -> sendInvoiceContentTypeChangedMessage(((InvoiceContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceContentType from the database
	 * 
	 * @param invoiceContentTypeId:
	 *            the id of the InvoiceContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceContentTypeById(@RequestParam(value = "invoiceContentTypeId") String invoiceContentTypeId) {

		DeleteInvoiceContentType com = new DeleteInvoiceContentType(invoiceContentTypeId);

		int usedTicketId;

		synchronized (InvoiceContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContentTypeDeleted.class,
				event -> sendInvoiceContentTypeChangedMessage(((InvoiceContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceContentType/\" plus one of the following: "
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

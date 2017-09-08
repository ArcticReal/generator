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
import com.skytala.eCommerce.command.AddInvoiceAttribute;
import com.skytala.eCommerce.command.DeleteInvoiceAttribute;
import com.skytala.eCommerce.command.UpdateInvoiceAttribute;
import com.skytala.eCommerce.entity.InvoiceAttribute;
import com.skytala.eCommerce.entity.InvoiceAttributeMapper;
import com.skytala.eCommerce.event.InvoiceAttributeAdded;
import com.skytala.eCommerce.event.InvoiceAttributeDeleted;
import com.skytala.eCommerce.event.InvoiceAttributeFound;
import com.skytala.eCommerce.event.InvoiceAttributeUpdated;
import com.skytala.eCommerce.query.FindInvoiceAttributesBy;

@RestController
@RequestMapping("/api/invoiceAttribute")
public class InvoiceAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceAttribute
	 * @return a List with the InvoiceAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceAttribute> findInvoiceAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceAttributesBy query = new FindInvoiceAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceAttributeFound.class,
				event -> sendInvoiceAttributesFoundMessage(((InvoiceAttributeFound) event).getInvoiceAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceAttributesFoundMessage(List<InvoiceAttribute> invoiceAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceAttributes);
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
	public boolean createInvoiceAttribute(HttpServletRequest request) {

		InvoiceAttribute invoiceAttributeToBeAdded = new InvoiceAttribute();
		try {
			invoiceAttributeToBeAdded = InvoiceAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceAttribute(invoiceAttributeToBeAdded);

	}

	/**
	 * creates a new InvoiceAttribute entry in the ofbiz database
	 * 
	 * @param invoiceAttributeToBeAdded
	 *            the InvoiceAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceAttribute(InvoiceAttribute invoiceAttributeToBeAdded) {

		AddInvoiceAttribute com = new AddInvoiceAttribute(invoiceAttributeToBeAdded);
		int usedTicketId;

		synchronized (InvoiceAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceAttributeAdded.class,
				event -> sendInvoiceAttributeChangedMessage(((InvoiceAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceAttribute(HttpServletRequest request) {

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

		InvoiceAttribute invoiceAttributeToBeUpdated = new InvoiceAttribute();

		try {
			invoiceAttributeToBeUpdated = InvoiceAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceAttribute(invoiceAttributeToBeUpdated);

	}

	/**
	 * Updates the InvoiceAttribute with the specific Id
	 * 
	 * @param invoiceAttributeToBeUpdated the InvoiceAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceAttribute(InvoiceAttribute invoiceAttributeToBeUpdated) {

		UpdateInvoiceAttribute com = new UpdateInvoiceAttribute(invoiceAttributeToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceAttributeUpdated.class,
				event -> sendInvoiceAttributeChangedMessage(((InvoiceAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceAttribute from the database
	 * 
	 * @param invoiceAttributeId:
	 *            the id of the InvoiceAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceAttributeById(@RequestParam(value = "invoiceAttributeId") String invoiceAttributeId) {

		DeleteInvoiceAttribute com = new DeleteInvoiceAttribute(invoiceAttributeId);

		int usedTicketId;

		synchronized (InvoiceAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceAttributeDeleted.class,
				event -> sendInvoiceAttributeChangedMessage(((InvoiceAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceAttribute/\" plus one of the following: "
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

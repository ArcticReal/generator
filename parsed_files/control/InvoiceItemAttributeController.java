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
import com.skytala.eCommerce.command.AddInvoiceItemAttribute;
import com.skytala.eCommerce.command.DeleteInvoiceItemAttribute;
import com.skytala.eCommerce.command.UpdateInvoiceItemAttribute;
import com.skytala.eCommerce.entity.InvoiceItemAttribute;
import com.skytala.eCommerce.entity.InvoiceItemAttributeMapper;
import com.skytala.eCommerce.event.InvoiceItemAttributeAdded;
import com.skytala.eCommerce.event.InvoiceItemAttributeDeleted;
import com.skytala.eCommerce.event.InvoiceItemAttributeFound;
import com.skytala.eCommerce.event.InvoiceItemAttributeUpdated;
import com.skytala.eCommerce.query.FindInvoiceItemAttributesBy;

@RestController
@RequestMapping("/api/invoiceItemAttribute")
public class InvoiceItemAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceItemAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceItemAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceItemAttribute
	 * @return a List with the InvoiceItemAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceItemAttribute> findInvoiceItemAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceItemAttributesBy query = new FindInvoiceItemAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceItemAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemAttributeFound.class,
				event -> sendInvoiceItemAttributesFoundMessage(((InvoiceItemAttributeFound) event).getInvoiceItemAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceItemAttributesFoundMessage(List<InvoiceItemAttribute> invoiceItemAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceItemAttributes);
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
	public boolean createInvoiceItemAttribute(HttpServletRequest request) {

		InvoiceItemAttribute invoiceItemAttributeToBeAdded = new InvoiceItemAttribute();
		try {
			invoiceItemAttributeToBeAdded = InvoiceItemAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceItemAttribute(invoiceItemAttributeToBeAdded);

	}

	/**
	 * creates a new InvoiceItemAttribute entry in the ofbiz database
	 * 
	 * @param invoiceItemAttributeToBeAdded
	 *            the InvoiceItemAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceItemAttribute(InvoiceItemAttribute invoiceItemAttributeToBeAdded) {

		AddInvoiceItemAttribute com = new AddInvoiceItemAttribute(invoiceItemAttributeToBeAdded);
		int usedTicketId;

		synchronized (InvoiceItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemAttributeAdded.class,
				event -> sendInvoiceItemAttributeChangedMessage(((InvoiceItemAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceItemAttribute(HttpServletRequest request) {

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

		InvoiceItemAttribute invoiceItemAttributeToBeUpdated = new InvoiceItemAttribute();

		try {
			invoiceItemAttributeToBeUpdated = InvoiceItemAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceItemAttribute(invoiceItemAttributeToBeUpdated);

	}

	/**
	 * Updates the InvoiceItemAttribute with the specific Id
	 * 
	 * @param invoiceItemAttributeToBeUpdated the InvoiceItemAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceItemAttribute(InvoiceItemAttribute invoiceItemAttributeToBeUpdated) {

		UpdateInvoiceItemAttribute com = new UpdateInvoiceItemAttribute(invoiceItemAttributeToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemAttributeUpdated.class,
				event -> sendInvoiceItemAttributeChangedMessage(((InvoiceItemAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceItemAttribute from the database
	 * 
	 * @param invoiceItemAttributeId:
	 *            the id of the InvoiceItemAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceItemAttributeById(@RequestParam(value = "invoiceItemAttributeId") String invoiceItemAttributeId) {

		DeleteInvoiceItemAttribute com = new DeleteInvoiceItemAttribute(invoiceItemAttributeId);

		int usedTicketId;

		synchronized (InvoiceItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemAttributeDeleted.class,
				event -> sendInvoiceItemAttributeChangedMessage(((InvoiceItemAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceItemAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceItemAttribute/\" plus one of the following: "
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

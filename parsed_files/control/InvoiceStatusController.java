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
import com.skytala.eCommerce.command.AddInvoiceStatus;
import com.skytala.eCommerce.command.DeleteInvoiceStatus;
import com.skytala.eCommerce.command.UpdateInvoiceStatus;
import com.skytala.eCommerce.entity.InvoiceStatus;
import com.skytala.eCommerce.entity.InvoiceStatusMapper;
import com.skytala.eCommerce.event.InvoiceStatusAdded;
import com.skytala.eCommerce.event.InvoiceStatusDeleted;
import com.skytala.eCommerce.event.InvoiceStatusFound;
import com.skytala.eCommerce.event.InvoiceStatusUpdated;
import com.skytala.eCommerce.query.FindInvoiceStatussBy;

@RestController
@RequestMapping("/api/invoiceStatus")
public class InvoiceStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceStatus
	 * @return a List with the InvoiceStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceStatus> findInvoiceStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceStatussBy query = new FindInvoiceStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceStatusFound.class,
				event -> sendInvoiceStatussFoundMessage(((InvoiceStatusFound) event).getInvoiceStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceStatussFoundMessage(List<InvoiceStatus> invoiceStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceStatuss);
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
	public boolean createInvoiceStatus(HttpServletRequest request) {

		InvoiceStatus invoiceStatusToBeAdded = new InvoiceStatus();
		try {
			invoiceStatusToBeAdded = InvoiceStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceStatus(invoiceStatusToBeAdded);

	}

	/**
	 * creates a new InvoiceStatus entry in the ofbiz database
	 * 
	 * @param invoiceStatusToBeAdded
	 *            the InvoiceStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceStatus(InvoiceStatus invoiceStatusToBeAdded) {

		AddInvoiceStatus com = new AddInvoiceStatus(invoiceStatusToBeAdded);
		int usedTicketId;

		synchronized (InvoiceStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceStatusAdded.class,
				event -> sendInvoiceStatusChangedMessage(((InvoiceStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceStatus(HttpServletRequest request) {

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

		InvoiceStatus invoiceStatusToBeUpdated = new InvoiceStatus();

		try {
			invoiceStatusToBeUpdated = InvoiceStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceStatus(invoiceStatusToBeUpdated);

	}

	/**
	 * Updates the InvoiceStatus with the specific Id
	 * 
	 * @param invoiceStatusToBeUpdated the InvoiceStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceStatus(InvoiceStatus invoiceStatusToBeUpdated) {

		UpdateInvoiceStatus com = new UpdateInvoiceStatus(invoiceStatusToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceStatusUpdated.class,
				event -> sendInvoiceStatusChangedMessage(((InvoiceStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceStatus from the database
	 * 
	 * @param invoiceStatusId:
	 *            the id of the InvoiceStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceStatusById(@RequestParam(value = "invoiceStatusId") String invoiceStatusId) {

		DeleteInvoiceStatus com = new DeleteInvoiceStatus(invoiceStatusId);

		int usedTicketId;

		synchronized (InvoiceStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceStatusDeleted.class,
				event -> sendInvoiceStatusChangedMessage(((InvoiceStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceStatus/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddInvoiceRole;
import com.skytala.eCommerce.command.DeleteInvoiceRole;
import com.skytala.eCommerce.command.UpdateInvoiceRole;
import com.skytala.eCommerce.entity.InvoiceRole;
import com.skytala.eCommerce.entity.InvoiceRoleMapper;
import com.skytala.eCommerce.event.InvoiceRoleAdded;
import com.skytala.eCommerce.event.InvoiceRoleDeleted;
import com.skytala.eCommerce.event.InvoiceRoleFound;
import com.skytala.eCommerce.event.InvoiceRoleUpdated;
import com.skytala.eCommerce.query.FindInvoiceRolesBy;

@RestController
@RequestMapping("/api/invoiceRole")
public class InvoiceRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceRole
	 * @return a List with the InvoiceRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceRole> findInvoiceRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceRolesBy query = new FindInvoiceRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceRoleFound.class,
				event -> sendInvoiceRolesFoundMessage(((InvoiceRoleFound) event).getInvoiceRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceRolesFoundMessage(List<InvoiceRole> invoiceRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceRoles);
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
	public boolean createInvoiceRole(HttpServletRequest request) {

		InvoiceRole invoiceRoleToBeAdded = new InvoiceRole();
		try {
			invoiceRoleToBeAdded = InvoiceRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceRole(invoiceRoleToBeAdded);

	}

	/**
	 * creates a new InvoiceRole entry in the ofbiz database
	 * 
	 * @param invoiceRoleToBeAdded
	 *            the InvoiceRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceRole(InvoiceRole invoiceRoleToBeAdded) {

		AddInvoiceRole com = new AddInvoiceRole(invoiceRoleToBeAdded);
		int usedTicketId;

		synchronized (InvoiceRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceRoleAdded.class,
				event -> sendInvoiceRoleChangedMessage(((InvoiceRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceRole(HttpServletRequest request) {

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

		InvoiceRole invoiceRoleToBeUpdated = new InvoiceRole();

		try {
			invoiceRoleToBeUpdated = InvoiceRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceRole(invoiceRoleToBeUpdated);

	}

	/**
	 * Updates the InvoiceRole with the specific Id
	 * 
	 * @param invoiceRoleToBeUpdated the InvoiceRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceRole(InvoiceRole invoiceRoleToBeUpdated) {

		UpdateInvoiceRole com = new UpdateInvoiceRole(invoiceRoleToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceRoleUpdated.class,
				event -> sendInvoiceRoleChangedMessage(((InvoiceRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceRole from the database
	 * 
	 * @param invoiceRoleId:
	 *            the id of the InvoiceRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceRoleById(@RequestParam(value = "invoiceRoleId") String invoiceRoleId) {

		DeleteInvoiceRole com = new DeleteInvoiceRole(invoiceRoleId);

		int usedTicketId;

		synchronized (InvoiceRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceRoleDeleted.class,
				event -> sendInvoiceRoleChangedMessage(((InvoiceRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceRole/\" plus one of the following: "
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

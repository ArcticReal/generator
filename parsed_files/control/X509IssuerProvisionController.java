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
import com.skytala.eCommerce.command.AddX509IssuerProvision;
import com.skytala.eCommerce.command.DeleteX509IssuerProvision;
import com.skytala.eCommerce.command.UpdateX509IssuerProvision;
import com.skytala.eCommerce.entity.X509IssuerProvision;
import com.skytala.eCommerce.entity.X509IssuerProvisionMapper;
import com.skytala.eCommerce.event.X509IssuerProvisionAdded;
import com.skytala.eCommerce.event.X509IssuerProvisionDeleted;
import com.skytala.eCommerce.event.X509IssuerProvisionFound;
import com.skytala.eCommerce.event.X509IssuerProvisionUpdated;
import com.skytala.eCommerce.query.FindX509IssuerProvisionsBy;

@RestController
@RequestMapping("/api/x509IssuerProvision")
public class X509IssuerProvisionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<X509IssuerProvision>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public X509IssuerProvisionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a X509IssuerProvision
	 * @return a List with the X509IssuerProvisions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<X509IssuerProvision> findX509IssuerProvisionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindX509IssuerProvisionsBy query = new FindX509IssuerProvisionsBy(allRequestParams);

		int usedTicketId;

		synchronized (X509IssuerProvisionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(X509IssuerProvisionFound.class,
				event -> sendX509IssuerProvisionsFoundMessage(((X509IssuerProvisionFound) event).getX509IssuerProvisions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendX509IssuerProvisionsFoundMessage(List<X509IssuerProvision> x509IssuerProvisions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, x509IssuerProvisions);
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
	public boolean createX509IssuerProvision(HttpServletRequest request) {

		X509IssuerProvision x509IssuerProvisionToBeAdded = new X509IssuerProvision();
		try {
			x509IssuerProvisionToBeAdded = X509IssuerProvisionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createX509IssuerProvision(x509IssuerProvisionToBeAdded);

	}

	/**
	 * creates a new X509IssuerProvision entry in the ofbiz database
	 * 
	 * @param x509IssuerProvisionToBeAdded
	 *            the X509IssuerProvision thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createX509IssuerProvision(X509IssuerProvision x509IssuerProvisionToBeAdded) {

		AddX509IssuerProvision com = new AddX509IssuerProvision(x509IssuerProvisionToBeAdded);
		int usedTicketId;

		synchronized (X509IssuerProvisionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(X509IssuerProvisionAdded.class,
				event -> sendX509IssuerProvisionChangedMessage(((X509IssuerProvisionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateX509IssuerProvision(HttpServletRequest request) {

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

		X509IssuerProvision x509IssuerProvisionToBeUpdated = new X509IssuerProvision();

		try {
			x509IssuerProvisionToBeUpdated = X509IssuerProvisionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateX509IssuerProvision(x509IssuerProvisionToBeUpdated);

	}

	/**
	 * Updates the X509IssuerProvision with the specific Id
	 * 
	 * @param x509IssuerProvisionToBeUpdated the X509IssuerProvision thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateX509IssuerProvision(X509IssuerProvision x509IssuerProvisionToBeUpdated) {

		UpdateX509IssuerProvision com = new UpdateX509IssuerProvision(x509IssuerProvisionToBeUpdated);

		int usedTicketId;

		synchronized (X509IssuerProvisionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(X509IssuerProvisionUpdated.class,
				event -> sendX509IssuerProvisionChangedMessage(((X509IssuerProvisionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a X509IssuerProvision from the database
	 * 
	 * @param x509IssuerProvisionId:
	 *            the id of the X509IssuerProvision thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletex509IssuerProvisionById(@RequestParam(value = "x509IssuerProvisionId") String x509IssuerProvisionId) {

		DeleteX509IssuerProvision com = new DeleteX509IssuerProvision(x509IssuerProvisionId);

		int usedTicketId;

		synchronized (X509IssuerProvisionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(X509IssuerProvisionDeleted.class,
				event -> sendX509IssuerProvisionChangedMessage(((X509IssuerProvisionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendX509IssuerProvisionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/x509IssuerProvision/\" plus one of the following: "
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

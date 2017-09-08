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
import com.skytala.eCommerce.command.AddCommunicationEventProduct;
import com.skytala.eCommerce.command.DeleteCommunicationEventProduct;
import com.skytala.eCommerce.command.UpdateCommunicationEventProduct;
import com.skytala.eCommerce.entity.CommunicationEventProduct;
import com.skytala.eCommerce.entity.CommunicationEventProductMapper;
import com.skytala.eCommerce.event.CommunicationEventProductAdded;
import com.skytala.eCommerce.event.CommunicationEventProductDeleted;
import com.skytala.eCommerce.event.CommunicationEventProductFound;
import com.skytala.eCommerce.event.CommunicationEventProductUpdated;
import com.skytala.eCommerce.query.FindCommunicationEventProductsBy;

@RestController
@RequestMapping("/api/communicationEventProduct")
public class CommunicationEventProductController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommunicationEventProduct>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommunicationEventProductController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommunicationEventProduct
	 * @return a List with the CommunicationEventProducts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommunicationEventProduct> findCommunicationEventProductsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommunicationEventProductsBy query = new FindCommunicationEventProductsBy(allRequestParams);

		int usedTicketId;

		synchronized (CommunicationEventProductController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventProductFound.class,
				event -> sendCommunicationEventProductsFoundMessage(((CommunicationEventProductFound) event).getCommunicationEventProducts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommunicationEventProductsFoundMessage(List<CommunicationEventProduct> communicationEventProducts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, communicationEventProducts);
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
	public boolean createCommunicationEventProduct(HttpServletRequest request) {

		CommunicationEventProduct communicationEventProductToBeAdded = new CommunicationEventProduct();
		try {
			communicationEventProductToBeAdded = CommunicationEventProductMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommunicationEventProduct(communicationEventProductToBeAdded);

	}

	/**
	 * creates a new CommunicationEventProduct entry in the ofbiz database
	 * 
	 * @param communicationEventProductToBeAdded
	 *            the CommunicationEventProduct thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommunicationEventProduct(CommunicationEventProduct communicationEventProductToBeAdded) {

		AddCommunicationEventProduct com = new AddCommunicationEventProduct(communicationEventProductToBeAdded);
		int usedTicketId;

		synchronized (CommunicationEventProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventProductAdded.class,
				event -> sendCommunicationEventProductChangedMessage(((CommunicationEventProductAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCommunicationEventProduct(HttpServletRequest request) {

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

		CommunicationEventProduct communicationEventProductToBeUpdated = new CommunicationEventProduct();

		try {
			communicationEventProductToBeUpdated = CommunicationEventProductMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommunicationEventProduct(communicationEventProductToBeUpdated);

	}

	/**
	 * Updates the CommunicationEventProduct with the specific Id
	 * 
	 * @param communicationEventProductToBeUpdated the CommunicationEventProduct thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommunicationEventProduct(CommunicationEventProduct communicationEventProductToBeUpdated) {

		UpdateCommunicationEventProduct com = new UpdateCommunicationEventProduct(communicationEventProductToBeUpdated);

		int usedTicketId;

		synchronized (CommunicationEventProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventProductUpdated.class,
				event -> sendCommunicationEventProductChangedMessage(((CommunicationEventProductUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CommunicationEventProduct from the database
	 * 
	 * @param communicationEventProductId:
	 *            the id of the CommunicationEventProduct thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommunicationEventProductById(@RequestParam(value = "communicationEventProductId") String communicationEventProductId) {

		DeleteCommunicationEventProduct com = new DeleteCommunicationEventProduct(communicationEventProductId);

		int usedTicketId;

		synchronized (CommunicationEventProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventProductDeleted.class,
				event -> sendCommunicationEventProductChangedMessage(((CommunicationEventProductDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCommunicationEventProductChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/communicationEventProduct/\" plus one of the following: "
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

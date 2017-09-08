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
import com.skytala.eCommerce.command.AddProdCatalogCategoryType;
import com.skytala.eCommerce.command.DeleteProdCatalogCategoryType;
import com.skytala.eCommerce.command.UpdateProdCatalogCategoryType;
import com.skytala.eCommerce.entity.ProdCatalogCategoryType;
import com.skytala.eCommerce.entity.ProdCatalogCategoryTypeMapper;
import com.skytala.eCommerce.event.ProdCatalogCategoryTypeAdded;
import com.skytala.eCommerce.event.ProdCatalogCategoryTypeDeleted;
import com.skytala.eCommerce.event.ProdCatalogCategoryTypeFound;
import com.skytala.eCommerce.event.ProdCatalogCategoryTypeUpdated;
import com.skytala.eCommerce.query.FindProdCatalogCategoryTypesBy;

@RestController
@RequestMapping("/api/prodCatalogCategoryType")
public class ProdCatalogCategoryTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProdCatalogCategoryType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProdCatalogCategoryTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProdCatalogCategoryType
	 * @return a List with the ProdCatalogCategoryTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProdCatalogCategoryType> findProdCatalogCategoryTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProdCatalogCategoryTypesBy query = new FindProdCatalogCategoryTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProdCatalogCategoryTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogCategoryTypeFound.class,
				event -> sendProdCatalogCategoryTypesFoundMessage(((ProdCatalogCategoryTypeFound) event).getProdCatalogCategoryTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProdCatalogCategoryTypesFoundMessage(List<ProdCatalogCategoryType> prodCatalogCategoryTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, prodCatalogCategoryTypes);
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
	public boolean createProdCatalogCategoryType(HttpServletRequest request) {

		ProdCatalogCategoryType prodCatalogCategoryTypeToBeAdded = new ProdCatalogCategoryType();
		try {
			prodCatalogCategoryTypeToBeAdded = ProdCatalogCategoryTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProdCatalogCategoryType(prodCatalogCategoryTypeToBeAdded);

	}

	/**
	 * creates a new ProdCatalogCategoryType entry in the ofbiz database
	 * 
	 * @param prodCatalogCategoryTypeToBeAdded
	 *            the ProdCatalogCategoryType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProdCatalogCategoryType(ProdCatalogCategoryType prodCatalogCategoryTypeToBeAdded) {

		AddProdCatalogCategoryType com = new AddProdCatalogCategoryType(prodCatalogCategoryTypeToBeAdded);
		int usedTicketId;

		synchronized (ProdCatalogCategoryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogCategoryTypeAdded.class,
				event -> sendProdCatalogCategoryTypeChangedMessage(((ProdCatalogCategoryTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProdCatalogCategoryType(HttpServletRequest request) {

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

		ProdCatalogCategoryType prodCatalogCategoryTypeToBeUpdated = new ProdCatalogCategoryType();

		try {
			prodCatalogCategoryTypeToBeUpdated = ProdCatalogCategoryTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProdCatalogCategoryType(prodCatalogCategoryTypeToBeUpdated);

	}

	/**
	 * Updates the ProdCatalogCategoryType with the specific Id
	 * 
	 * @param prodCatalogCategoryTypeToBeUpdated the ProdCatalogCategoryType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProdCatalogCategoryType(ProdCatalogCategoryType prodCatalogCategoryTypeToBeUpdated) {

		UpdateProdCatalogCategoryType com = new UpdateProdCatalogCategoryType(prodCatalogCategoryTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProdCatalogCategoryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogCategoryTypeUpdated.class,
				event -> sendProdCatalogCategoryTypeChangedMessage(((ProdCatalogCategoryTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProdCatalogCategoryType from the database
	 * 
	 * @param prodCatalogCategoryTypeId:
	 *            the id of the ProdCatalogCategoryType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteprodCatalogCategoryTypeById(@RequestParam(value = "prodCatalogCategoryTypeId") String prodCatalogCategoryTypeId) {

		DeleteProdCatalogCategoryType com = new DeleteProdCatalogCategoryType(prodCatalogCategoryTypeId);

		int usedTicketId;

		synchronized (ProdCatalogCategoryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogCategoryTypeDeleted.class,
				event -> sendProdCatalogCategoryTypeChangedMessage(((ProdCatalogCategoryTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProdCatalogCategoryTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/prodCatalogCategoryType/\" plus one of the following: "
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

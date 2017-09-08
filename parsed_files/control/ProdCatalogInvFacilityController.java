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
import com.skytala.eCommerce.command.AddProdCatalogInvFacility;
import com.skytala.eCommerce.command.DeleteProdCatalogInvFacility;
import com.skytala.eCommerce.command.UpdateProdCatalogInvFacility;
import com.skytala.eCommerce.entity.ProdCatalogInvFacility;
import com.skytala.eCommerce.entity.ProdCatalogInvFacilityMapper;
import com.skytala.eCommerce.event.ProdCatalogInvFacilityAdded;
import com.skytala.eCommerce.event.ProdCatalogInvFacilityDeleted;
import com.skytala.eCommerce.event.ProdCatalogInvFacilityFound;
import com.skytala.eCommerce.event.ProdCatalogInvFacilityUpdated;
import com.skytala.eCommerce.query.FindProdCatalogInvFacilitysBy;

@RestController
@RequestMapping("/api/prodCatalogInvFacility")
public class ProdCatalogInvFacilityController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProdCatalogInvFacility>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProdCatalogInvFacilityController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProdCatalogInvFacility
	 * @return a List with the ProdCatalogInvFacilitys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProdCatalogInvFacility> findProdCatalogInvFacilitysBy(@RequestParam Map<String, String> allRequestParams) {

		FindProdCatalogInvFacilitysBy query = new FindProdCatalogInvFacilitysBy(allRequestParams);

		int usedTicketId;

		synchronized (ProdCatalogInvFacilityController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogInvFacilityFound.class,
				event -> sendProdCatalogInvFacilitysFoundMessage(((ProdCatalogInvFacilityFound) event).getProdCatalogInvFacilitys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProdCatalogInvFacilitysFoundMessage(List<ProdCatalogInvFacility> prodCatalogInvFacilitys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, prodCatalogInvFacilitys);
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
	public boolean createProdCatalogInvFacility(HttpServletRequest request) {

		ProdCatalogInvFacility prodCatalogInvFacilityToBeAdded = new ProdCatalogInvFacility();
		try {
			prodCatalogInvFacilityToBeAdded = ProdCatalogInvFacilityMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProdCatalogInvFacility(prodCatalogInvFacilityToBeAdded);

	}

	/**
	 * creates a new ProdCatalogInvFacility entry in the ofbiz database
	 * 
	 * @param prodCatalogInvFacilityToBeAdded
	 *            the ProdCatalogInvFacility thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProdCatalogInvFacility(ProdCatalogInvFacility prodCatalogInvFacilityToBeAdded) {

		AddProdCatalogInvFacility com = new AddProdCatalogInvFacility(prodCatalogInvFacilityToBeAdded);
		int usedTicketId;

		synchronized (ProdCatalogInvFacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogInvFacilityAdded.class,
				event -> sendProdCatalogInvFacilityChangedMessage(((ProdCatalogInvFacilityAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProdCatalogInvFacility(HttpServletRequest request) {

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

		ProdCatalogInvFacility prodCatalogInvFacilityToBeUpdated = new ProdCatalogInvFacility();

		try {
			prodCatalogInvFacilityToBeUpdated = ProdCatalogInvFacilityMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProdCatalogInvFacility(prodCatalogInvFacilityToBeUpdated);

	}

	/**
	 * Updates the ProdCatalogInvFacility with the specific Id
	 * 
	 * @param prodCatalogInvFacilityToBeUpdated the ProdCatalogInvFacility thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProdCatalogInvFacility(ProdCatalogInvFacility prodCatalogInvFacilityToBeUpdated) {

		UpdateProdCatalogInvFacility com = new UpdateProdCatalogInvFacility(prodCatalogInvFacilityToBeUpdated);

		int usedTicketId;

		synchronized (ProdCatalogInvFacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogInvFacilityUpdated.class,
				event -> sendProdCatalogInvFacilityChangedMessage(((ProdCatalogInvFacilityUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProdCatalogInvFacility from the database
	 * 
	 * @param prodCatalogInvFacilityId:
	 *            the id of the ProdCatalogInvFacility thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteprodCatalogInvFacilityById(@RequestParam(value = "prodCatalogInvFacilityId") String prodCatalogInvFacilityId) {

		DeleteProdCatalogInvFacility com = new DeleteProdCatalogInvFacility(prodCatalogInvFacilityId);

		int usedTicketId;

		synchronized (ProdCatalogInvFacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogInvFacilityDeleted.class,
				event -> sendProdCatalogInvFacilityChangedMessage(((ProdCatalogInvFacilityDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProdCatalogInvFacilityChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/prodCatalogInvFacility/\" plus one of the following: "
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

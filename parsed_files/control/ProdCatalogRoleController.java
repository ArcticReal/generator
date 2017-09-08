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
import com.skytala.eCommerce.command.AddProdCatalogRole;
import com.skytala.eCommerce.command.DeleteProdCatalogRole;
import com.skytala.eCommerce.command.UpdateProdCatalogRole;
import com.skytala.eCommerce.entity.ProdCatalogRole;
import com.skytala.eCommerce.entity.ProdCatalogRoleMapper;
import com.skytala.eCommerce.event.ProdCatalogRoleAdded;
import com.skytala.eCommerce.event.ProdCatalogRoleDeleted;
import com.skytala.eCommerce.event.ProdCatalogRoleFound;
import com.skytala.eCommerce.event.ProdCatalogRoleUpdated;
import com.skytala.eCommerce.query.FindProdCatalogRolesBy;

@RestController
@RequestMapping("/api/prodCatalogRole")
public class ProdCatalogRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProdCatalogRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProdCatalogRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProdCatalogRole
	 * @return a List with the ProdCatalogRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProdCatalogRole> findProdCatalogRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProdCatalogRolesBy query = new FindProdCatalogRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProdCatalogRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogRoleFound.class,
				event -> sendProdCatalogRolesFoundMessage(((ProdCatalogRoleFound) event).getProdCatalogRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProdCatalogRolesFoundMessage(List<ProdCatalogRole> prodCatalogRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, prodCatalogRoles);
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
	public boolean createProdCatalogRole(HttpServletRequest request) {

		ProdCatalogRole prodCatalogRoleToBeAdded = new ProdCatalogRole();
		try {
			prodCatalogRoleToBeAdded = ProdCatalogRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProdCatalogRole(prodCatalogRoleToBeAdded);

	}

	/**
	 * creates a new ProdCatalogRole entry in the ofbiz database
	 * 
	 * @param prodCatalogRoleToBeAdded
	 *            the ProdCatalogRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProdCatalogRole(ProdCatalogRole prodCatalogRoleToBeAdded) {

		AddProdCatalogRole com = new AddProdCatalogRole(prodCatalogRoleToBeAdded);
		int usedTicketId;

		synchronized (ProdCatalogRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogRoleAdded.class,
				event -> sendProdCatalogRoleChangedMessage(((ProdCatalogRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProdCatalogRole(HttpServletRequest request) {

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

		ProdCatalogRole prodCatalogRoleToBeUpdated = new ProdCatalogRole();

		try {
			prodCatalogRoleToBeUpdated = ProdCatalogRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProdCatalogRole(prodCatalogRoleToBeUpdated);

	}

	/**
	 * Updates the ProdCatalogRole with the specific Id
	 * 
	 * @param prodCatalogRoleToBeUpdated the ProdCatalogRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProdCatalogRole(ProdCatalogRole prodCatalogRoleToBeUpdated) {

		UpdateProdCatalogRole com = new UpdateProdCatalogRole(prodCatalogRoleToBeUpdated);

		int usedTicketId;

		synchronized (ProdCatalogRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogRoleUpdated.class,
				event -> sendProdCatalogRoleChangedMessage(((ProdCatalogRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProdCatalogRole from the database
	 * 
	 * @param prodCatalogRoleId:
	 *            the id of the ProdCatalogRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteprodCatalogRoleById(@RequestParam(value = "prodCatalogRoleId") String prodCatalogRoleId) {

		DeleteProdCatalogRole com = new DeleteProdCatalogRole(prodCatalogRoleId);

		int usedTicketId;

		synchronized (ProdCatalogRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogRoleDeleted.class,
				event -> sendProdCatalogRoleChangedMessage(((ProdCatalogRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProdCatalogRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/prodCatalogRole/\" plus one of the following: "
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

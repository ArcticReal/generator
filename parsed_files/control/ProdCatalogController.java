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
import com.skytala.eCommerce.command.AddProdCatalog;
import com.skytala.eCommerce.command.DeleteProdCatalog;
import com.skytala.eCommerce.command.UpdateProdCatalog;
import com.skytala.eCommerce.entity.ProdCatalog;
import com.skytala.eCommerce.entity.ProdCatalogMapper;
import com.skytala.eCommerce.event.ProdCatalogAdded;
import com.skytala.eCommerce.event.ProdCatalogDeleted;
import com.skytala.eCommerce.event.ProdCatalogFound;
import com.skytala.eCommerce.event.ProdCatalogUpdated;
import com.skytala.eCommerce.query.FindProdCatalogsBy;

@RestController
@RequestMapping("/api/prodCatalog")
public class ProdCatalogController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProdCatalog>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProdCatalogController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProdCatalog
	 * @return a List with the ProdCatalogs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProdCatalog> findProdCatalogsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProdCatalogsBy query = new FindProdCatalogsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProdCatalogController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogFound.class,
				event -> sendProdCatalogsFoundMessage(((ProdCatalogFound) event).getProdCatalogs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProdCatalogsFoundMessage(List<ProdCatalog> prodCatalogs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, prodCatalogs);
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
	public boolean createProdCatalog(HttpServletRequest request) {

		ProdCatalog prodCatalogToBeAdded = new ProdCatalog();
		try {
			prodCatalogToBeAdded = ProdCatalogMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProdCatalog(prodCatalogToBeAdded);

	}

	/**
	 * creates a new ProdCatalog entry in the ofbiz database
	 * 
	 * @param prodCatalogToBeAdded
	 *            the ProdCatalog thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProdCatalog(ProdCatalog prodCatalogToBeAdded) {

		AddProdCatalog com = new AddProdCatalog(prodCatalogToBeAdded);
		int usedTicketId;

		synchronized (ProdCatalogController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogAdded.class,
				event -> sendProdCatalogChangedMessage(((ProdCatalogAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProdCatalog(HttpServletRequest request) {

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

		ProdCatalog prodCatalogToBeUpdated = new ProdCatalog();

		try {
			prodCatalogToBeUpdated = ProdCatalogMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProdCatalog(prodCatalogToBeUpdated);

	}

	/**
	 * Updates the ProdCatalog with the specific Id
	 * 
	 * @param prodCatalogToBeUpdated the ProdCatalog thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProdCatalog(ProdCatalog prodCatalogToBeUpdated) {

		UpdateProdCatalog com = new UpdateProdCatalog(prodCatalogToBeUpdated);

		int usedTicketId;

		synchronized (ProdCatalogController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogUpdated.class,
				event -> sendProdCatalogChangedMessage(((ProdCatalogUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProdCatalog from the database
	 * 
	 * @param prodCatalogId:
	 *            the id of the ProdCatalog thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteprodCatalogById(@RequestParam(value = "prodCatalogId") String prodCatalogId) {

		DeleteProdCatalog com = new DeleteProdCatalog(prodCatalogId);

		int usedTicketId;

		synchronized (ProdCatalogController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogDeleted.class,
				event -> sendProdCatalogChangedMessage(((ProdCatalogDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProdCatalogChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/prodCatalog/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddProdCatalogCategory;
import com.skytala.eCommerce.command.DeleteProdCatalogCategory;
import com.skytala.eCommerce.command.UpdateProdCatalogCategory;
import com.skytala.eCommerce.entity.ProdCatalogCategory;
import com.skytala.eCommerce.entity.ProdCatalogCategoryMapper;
import com.skytala.eCommerce.event.ProdCatalogCategoryAdded;
import com.skytala.eCommerce.event.ProdCatalogCategoryDeleted;
import com.skytala.eCommerce.event.ProdCatalogCategoryFound;
import com.skytala.eCommerce.event.ProdCatalogCategoryUpdated;
import com.skytala.eCommerce.query.FindProdCatalogCategorysBy;

@RestController
@RequestMapping("/api/prodCatalogCategory")
public class ProdCatalogCategoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProdCatalogCategory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProdCatalogCategoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProdCatalogCategory
	 * @return a List with the ProdCatalogCategorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProdCatalogCategory> findProdCatalogCategorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindProdCatalogCategorysBy query = new FindProdCatalogCategorysBy(allRequestParams);

		int usedTicketId;

		synchronized (ProdCatalogCategoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogCategoryFound.class,
				event -> sendProdCatalogCategorysFoundMessage(((ProdCatalogCategoryFound) event).getProdCatalogCategorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProdCatalogCategorysFoundMessage(List<ProdCatalogCategory> prodCatalogCategorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, prodCatalogCategorys);
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
	public boolean createProdCatalogCategory(HttpServletRequest request) {

		ProdCatalogCategory prodCatalogCategoryToBeAdded = new ProdCatalogCategory();
		try {
			prodCatalogCategoryToBeAdded = ProdCatalogCategoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProdCatalogCategory(prodCatalogCategoryToBeAdded);

	}

	/**
	 * creates a new ProdCatalogCategory entry in the ofbiz database
	 * 
	 * @param prodCatalogCategoryToBeAdded
	 *            the ProdCatalogCategory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProdCatalogCategory(ProdCatalogCategory prodCatalogCategoryToBeAdded) {

		AddProdCatalogCategory com = new AddProdCatalogCategory(prodCatalogCategoryToBeAdded);
		int usedTicketId;

		synchronized (ProdCatalogCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogCategoryAdded.class,
				event -> sendProdCatalogCategoryChangedMessage(((ProdCatalogCategoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProdCatalogCategory(HttpServletRequest request) {

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

		ProdCatalogCategory prodCatalogCategoryToBeUpdated = new ProdCatalogCategory();

		try {
			prodCatalogCategoryToBeUpdated = ProdCatalogCategoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProdCatalogCategory(prodCatalogCategoryToBeUpdated);

	}

	/**
	 * Updates the ProdCatalogCategory with the specific Id
	 * 
	 * @param prodCatalogCategoryToBeUpdated the ProdCatalogCategory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProdCatalogCategory(ProdCatalogCategory prodCatalogCategoryToBeUpdated) {

		UpdateProdCatalogCategory com = new UpdateProdCatalogCategory(prodCatalogCategoryToBeUpdated);

		int usedTicketId;

		synchronized (ProdCatalogCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogCategoryUpdated.class,
				event -> sendProdCatalogCategoryChangedMessage(((ProdCatalogCategoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProdCatalogCategory from the database
	 * 
	 * @param prodCatalogCategoryId:
	 *            the id of the ProdCatalogCategory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteprodCatalogCategoryById(@RequestParam(value = "prodCatalogCategoryId") String prodCatalogCategoryId) {

		DeleteProdCatalogCategory com = new DeleteProdCatalogCategory(prodCatalogCategoryId);

		int usedTicketId;

		synchronized (ProdCatalogCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdCatalogCategoryDeleted.class,
				event -> sendProdCatalogCategoryChangedMessage(((ProdCatalogCategoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProdCatalogCategoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/prodCatalogCategory/\" plus one of the following: "
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

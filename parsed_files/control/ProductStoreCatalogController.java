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
import com.skytala.eCommerce.command.AddProductStoreCatalog;
import com.skytala.eCommerce.command.DeleteProductStoreCatalog;
import com.skytala.eCommerce.command.UpdateProductStoreCatalog;
import com.skytala.eCommerce.entity.ProductStoreCatalog;
import com.skytala.eCommerce.entity.ProductStoreCatalogMapper;
import com.skytala.eCommerce.event.ProductStoreCatalogAdded;
import com.skytala.eCommerce.event.ProductStoreCatalogDeleted;
import com.skytala.eCommerce.event.ProductStoreCatalogFound;
import com.skytala.eCommerce.event.ProductStoreCatalogUpdated;
import com.skytala.eCommerce.query.FindProductStoreCatalogsBy;

@RestController
@RequestMapping("/api/productStoreCatalog")
public class ProductStoreCatalogController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreCatalog>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreCatalogController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreCatalog
	 * @return a List with the ProductStoreCatalogs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreCatalog> findProductStoreCatalogsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreCatalogsBy query = new FindProductStoreCatalogsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreCatalogController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreCatalogFound.class,
				event -> sendProductStoreCatalogsFoundMessage(((ProductStoreCatalogFound) event).getProductStoreCatalogs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreCatalogsFoundMessage(List<ProductStoreCatalog> productStoreCatalogs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreCatalogs);
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
	public boolean createProductStoreCatalog(HttpServletRequest request) {

		ProductStoreCatalog productStoreCatalogToBeAdded = new ProductStoreCatalog();
		try {
			productStoreCatalogToBeAdded = ProductStoreCatalogMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreCatalog(productStoreCatalogToBeAdded);

	}

	/**
	 * creates a new ProductStoreCatalog entry in the ofbiz database
	 * 
	 * @param productStoreCatalogToBeAdded
	 *            the ProductStoreCatalog thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreCatalog(ProductStoreCatalog productStoreCatalogToBeAdded) {

		AddProductStoreCatalog com = new AddProductStoreCatalog(productStoreCatalogToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreCatalogController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreCatalogAdded.class,
				event -> sendProductStoreCatalogChangedMessage(((ProductStoreCatalogAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreCatalog(HttpServletRequest request) {

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

		ProductStoreCatalog productStoreCatalogToBeUpdated = new ProductStoreCatalog();

		try {
			productStoreCatalogToBeUpdated = ProductStoreCatalogMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreCatalog(productStoreCatalogToBeUpdated);

	}

	/**
	 * Updates the ProductStoreCatalog with the specific Id
	 * 
	 * @param productStoreCatalogToBeUpdated the ProductStoreCatalog thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreCatalog(ProductStoreCatalog productStoreCatalogToBeUpdated) {

		UpdateProductStoreCatalog com = new UpdateProductStoreCatalog(productStoreCatalogToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreCatalogController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreCatalogUpdated.class,
				event -> sendProductStoreCatalogChangedMessage(((ProductStoreCatalogUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreCatalog from the database
	 * 
	 * @param productStoreCatalogId:
	 *            the id of the ProductStoreCatalog thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreCatalogById(@RequestParam(value = "productStoreCatalogId") String productStoreCatalogId) {

		DeleteProductStoreCatalog com = new DeleteProductStoreCatalog(productStoreCatalogId);

		int usedTicketId;

		synchronized (ProductStoreCatalogController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreCatalogDeleted.class,
				event -> sendProductStoreCatalogChangedMessage(((ProductStoreCatalogDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreCatalogChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreCatalog/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddProductCategoryLink;
import com.skytala.eCommerce.command.DeleteProductCategoryLink;
import com.skytala.eCommerce.command.UpdateProductCategoryLink;
import com.skytala.eCommerce.entity.ProductCategoryLink;
import com.skytala.eCommerce.entity.ProductCategoryLinkMapper;
import com.skytala.eCommerce.event.ProductCategoryLinkAdded;
import com.skytala.eCommerce.event.ProductCategoryLinkDeleted;
import com.skytala.eCommerce.event.ProductCategoryLinkFound;
import com.skytala.eCommerce.event.ProductCategoryLinkUpdated;
import com.skytala.eCommerce.query.FindProductCategoryLinksBy;

@RestController
@RequestMapping("/api/productCategoryLink")
public class ProductCategoryLinkController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCategoryLink>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCategoryLinkController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCategoryLink
	 * @return a List with the ProductCategoryLinks
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCategoryLink> findProductCategoryLinksBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCategoryLinksBy query = new FindProductCategoryLinksBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCategoryLinkController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryLinkFound.class,
				event -> sendProductCategoryLinksFoundMessage(((ProductCategoryLinkFound) event).getProductCategoryLinks(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCategoryLinksFoundMessage(List<ProductCategoryLink> productCategoryLinks, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCategoryLinks);
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
	public boolean createProductCategoryLink(HttpServletRequest request) {

		ProductCategoryLink productCategoryLinkToBeAdded = new ProductCategoryLink();
		try {
			productCategoryLinkToBeAdded = ProductCategoryLinkMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCategoryLink(productCategoryLinkToBeAdded);

	}

	/**
	 * creates a new ProductCategoryLink entry in the ofbiz database
	 * 
	 * @param productCategoryLinkToBeAdded
	 *            the ProductCategoryLink thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCategoryLink(ProductCategoryLink productCategoryLinkToBeAdded) {

		AddProductCategoryLink com = new AddProductCategoryLink(productCategoryLinkToBeAdded);
		int usedTicketId;

		synchronized (ProductCategoryLinkController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryLinkAdded.class,
				event -> sendProductCategoryLinkChangedMessage(((ProductCategoryLinkAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCategoryLink(HttpServletRequest request) {

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

		ProductCategoryLink productCategoryLinkToBeUpdated = new ProductCategoryLink();

		try {
			productCategoryLinkToBeUpdated = ProductCategoryLinkMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCategoryLink(productCategoryLinkToBeUpdated);

	}

	/**
	 * Updates the ProductCategoryLink with the specific Id
	 * 
	 * @param productCategoryLinkToBeUpdated the ProductCategoryLink thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCategoryLink(ProductCategoryLink productCategoryLinkToBeUpdated) {

		UpdateProductCategoryLink com = new UpdateProductCategoryLink(productCategoryLinkToBeUpdated);

		int usedTicketId;

		synchronized (ProductCategoryLinkController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryLinkUpdated.class,
				event -> sendProductCategoryLinkChangedMessage(((ProductCategoryLinkUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCategoryLink from the database
	 * 
	 * @param productCategoryLinkId:
	 *            the id of the ProductCategoryLink thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCategoryLinkById(@RequestParam(value = "productCategoryLinkId") String productCategoryLinkId) {

		DeleteProductCategoryLink com = new DeleteProductCategoryLink(productCategoryLinkId);

		int usedTicketId;

		synchronized (ProductCategoryLinkController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryLinkDeleted.class,
				event -> sendProductCategoryLinkChangedMessage(((ProductCategoryLinkDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCategoryLinkChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCategoryLink/\" plus one of the following: "
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

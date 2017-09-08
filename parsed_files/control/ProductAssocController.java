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
import com.skytala.eCommerce.command.AddProductAssoc;
import com.skytala.eCommerce.command.DeleteProductAssoc;
import com.skytala.eCommerce.command.UpdateProductAssoc;
import com.skytala.eCommerce.entity.ProductAssoc;
import com.skytala.eCommerce.entity.ProductAssocMapper;
import com.skytala.eCommerce.event.ProductAssocAdded;
import com.skytala.eCommerce.event.ProductAssocDeleted;
import com.skytala.eCommerce.event.ProductAssocFound;
import com.skytala.eCommerce.event.ProductAssocUpdated;
import com.skytala.eCommerce.query.FindProductAssocsBy;

@RestController
@RequestMapping("/api/productAssoc")
public class ProductAssocController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductAssoc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductAssocController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductAssoc
	 * @return a List with the ProductAssocs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductAssoc> findProductAssocsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductAssocsBy query = new FindProductAssocsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductAssocController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAssocFound.class,
				event -> sendProductAssocsFoundMessage(((ProductAssocFound) event).getProductAssocs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductAssocsFoundMessage(List<ProductAssoc> productAssocs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productAssocs);
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
	public boolean createProductAssoc(HttpServletRequest request) {

		ProductAssoc productAssocToBeAdded = new ProductAssoc();
		try {
			productAssocToBeAdded = ProductAssocMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductAssoc(productAssocToBeAdded);

	}

	/**
	 * creates a new ProductAssoc entry in the ofbiz database
	 * 
	 * @param productAssocToBeAdded
	 *            the ProductAssoc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductAssoc(ProductAssoc productAssocToBeAdded) {

		AddProductAssoc com = new AddProductAssoc(productAssocToBeAdded);
		int usedTicketId;

		synchronized (ProductAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAssocAdded.class,
				event -> sendProductAssocChangedMessage(((ProductAssocAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductAssoc(HttpServletRequest request) {

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

		ProductAssoc productAssocToBeUpdated = new ProductAssoc();

		try {
			productAssocToBeUpdated = ProductAssocMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductAssoc(productAssocToBeUpdated);

	}

	/**
	 * Updates the ProductAssoc with the specific Id
	 * 
	 * @param productAssocToBeUpdated the ProductAssoc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductAssoc(ProductAssoc productAssocToBeUpdated) {

		UpdateProductAssoc com = new UpdateProductAssoc(productAssocToBeUpdated);

		int usedTicketId;

		synchronized (ProductAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAssocUpdated.class,
				event -> sendProductAssocChangedMessage(((ProductAssocUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductAssoc from the database
	 * 
	 * @param productAssocId:
	 *            the id of the ProductAssoc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductAssocById(@RequestParam(value = "productAssocId") String productAssocId) {

		DeleteProductAssoc com = new DeleteProductAssoc(productAssocId);

		int usedTicketId;

		synchronized (ProductAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAssocDeleted.class,
				event -> sendProductAssocChangedMessage(((ProductAssocDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductAssocChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productAssoc/\" plus one of the following: "
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

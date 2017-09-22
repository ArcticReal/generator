package parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class ServiceSearcher {

	private String pathToOfbiz = "/home/work/ofbiz/";
	private String pathCopyTo = "/home/work/workspace/ControllingParser/service_xmls/";
	private Set<String> serviceXmlPaths = new HashSet<String>();
	private final Charset ENCODING = StandardCharsets.UTF_8;
	private int level = 0;
	
	
	public void doIt() throws IOException {
		
		File f = new File(pathCopyTo);
		f.mkdirs();
		searchComponent(new File(pathToOfbiz));
		Iterator<String> it = serviceXmlPaths.iterator();
		
		while(it.hasNext()) {
			String path = it.next();

			f = new File(path);
			
			if(f.exists()) {
				Files.copy(Paths.get(path), Paths.get(pathCopyTo + path.split("/work/ofbiz")[1].replaceAll("/", "_")), StandardCopyOption.REPLACE_EXISTING);
			}
			
		}
		
		System.out.println(serviceXmlPaths.size());
	}
	
	public void searchComponent(File file) throws IOException {
		
		if(!file.isDirectory()) {
			if(file.getAbsolutePath().contains("ofbiz-component.xml")) {
				for(int i=0;i<level;i++) {
					System.out.print("  ");
				}
				System.out.println("Component found: " + file.getAbsolutePath());
				extractServiceFiles(file.getAbsolutePath());				
			}
		}else {
			int count = 0;
			for(File f: file.listFiles()) {
				level++;
				searchComponent(f);
				count++;
				
			}
			
			for(int i=0;i<level;i++) {
				System.out.print("  ");
			}
			System.out.println(count);
		}
		
		
		level--;
	}
	
	
	
	public void extractServiceFiles(String path) throws IOException {

		File f = new File(path);
		
		Scanner scanner = new Scanner(f, ENCODING.name());
		
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			
			if(line.contains("<service-resource ")) {
				String[] splittedString = line.split("\"");
				
				for(int i = 0; i < splittedString.length-1; i++) {
					if(splittedString[i].contains("location=")) {
						serviceXmlPaths.add(path.split("ofbiz-c")[0] + splittedString[i+1]);

					}
				}
			}
			
		}
		
		scanner.close();
	
	}
	
	
}

package org.jahia.utils.maven.plugin.contentgenerator.wise.bo;

import org.jahia.utils.maven.plugin.contentgenerator.properties.ContentGeneratorCst;
import org.jdom.Element;

public class TaskBO {
	private Element task;
	
	private String name = "new-task";
	
	private String title = "New task";
	
	private String assigneeUserKey = "root";
	
	private String description = "This task has been created by the Content generator";
	
	private String priority = "low";
	
	private String state = "active";
	
	private String type = "docspace";
	
	private String dueDate = "2013-07-05T00:00:00.000+02:00";
		
	public TaskBO(String title, String assigneeUserKey, String description) {
		this.title = title;
		this.assigneeUserKey = assigneeUserKey;
		this.description = description;
	}
	
	public Element getElement() {
		if (task == null) {
			task = new Element(name);
			task.setAttribute("assigneeUserKey", assigneeUserKey);
			task.setAttribute("description", description);
			task.setAttribute("assigneeUserKey", assigneeUserKey);
			task.setAttribute("dueDate", dueDate);
			task.setAttribute("originWS", "default", ContentGeneratorCst.NS_J);
			task.setAttribute("primaryType", "jnt:task", ContentGeneratorCst.NS_JCR);
			task.setAttribute("title", title, ContentGeneratorCst.NS_JCR);
			task.setAttribute("priority", priority);
			task.setAttribute("state", state);
			task.setAttribute("type", type);
		}
		return task;
	}
}
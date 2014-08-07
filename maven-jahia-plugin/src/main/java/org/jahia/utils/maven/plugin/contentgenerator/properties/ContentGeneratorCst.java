package org.jahia.utils.maven.plugin.contentgenerator.properties;

import org.jdom.Namespace;


public final class ContentGeneratorCst {
	
	public static String DATE_RANGE_FORMAT = "yyyy-MM-dd kk:mm";
	/**
	 * Configuration
	 */
	public static final String PROPERTIES_FILE_NAME = "jahiaContentGenerator.properties";
	public static final Integer SQL_RECORDSET_SIZE = Integer.valueOf(10000);
	public static final Integer MAX_TOTAL_PAGES = Integer.valueOf(200000);
	
	public static final float PERCENTAGE_PAGES_WITH_EXTERNAL_FILE_REF = 0.25f;
	public static final float PERCENTAGE_PAGES_WITH_INTERNAL_FILE_REF = 0.25f;
		
	public static final String PAGE_PATH_SEPARATOR = "/"; 
	
	/**
	 * Default value
	 */
	public static final String MYSQL_HOST_DEFAULT = "localhost";
	public static final String MYSQL_TABLE_DEFAULT = "articles";
	
	public static final Integer NB_PAGES_TOP_LEVEL_DEFAULT = Integer.valueOf(1);
	public static final Integer NB_SUB_LEVELS_DEFAULT = Integer.valueOf(2);
	public static final Integer NB_SUBPAGES_PER_PAGE_DEFAULT = Integer.valueOf(3);
	public static final String OUTPUT_FILE_DEFAULT = "jahia-cg.output.xml";
	public static final String OUTPUT_MAP_DEFAULT = "jahia-cg.output.csv";
	public static final Boolean HAS_VANITY_DEFAULT = Boolean.TRUE;
	public static final Boolean CREATE_MAP_DEFAULT = Boolean.FALSE;
	public static final String SITE_KEY_DEFAULT = "testSite";
	public static final String SITE_SERVER_NAME_DEFAULT = "localhost";
	public static final String DESCRIPTION_DEFAULT = "Site generated by Jahia content generator";
	public static final String TEMPLATE_SET_DEFAULT = "templates-web-blue-qa";
	
	public static final String ROOT_PAGE_NAME = "home";
	public static final String PAGE_NAME_PREFIX = "page";
	
	public static final String REPOSITORY_FILENAME = "repository.xml";
	public static final String SITE_PROPERTIES_FILENAME = "site.properties";
	public static final String EXPORT_PROPERTIES_FILENAME = "export.properties";
	
	public static final Integer NB_NEWS_IN_QALIST = 100;
	
	/**
	 * Namespaces
	 */	
	public static Namespace NS_JCR = Namespace.getNamespace("jcr", "http://www.jcp.org/jcr/1.0");
	public static Namespace NS_JNT = Namespace.getNamespace("jnt", "http://www.jahia.org/jahia/nt/1.0"); 
	public static Namespace NS_JMIX = Namespace.getNamespace("jmix", "http://www.jahia.org/jahia/mix/1.0"); 
	public static Namespace NS_J = Namespace.getNamespace("j", "http://www.jahia.org/jahia/1.0");
	
	public static Namespace NS_NT = Namespace.getNamespace("nt", "http://www.jcp.org/jcr/nt/1.0"); 
	public static Namespace NS_TEST = Namespace.getNamespace("test", "http://www.apache.org/jackrabbit/test"); 
	public static Namespace NS_SV = Namespace.getNamespace("sv", "http://www.jcp.org/jcr/sv/1.0"); 
	public static Namespace NS_MIX = Namespace.getNamespace("mix", "http://www.jcp.org/jcr/mix/1.0");
	public static Namespace NS_REP = Namespace.getNamespace("rep", "internal");
	
	public static Namespace NS_DOCNT = Namespace.getNamespace("docnt", "http://www.jahia.org/jahia/docspace/nt/1.0");
	
	public static Namespace NS_CMIS = Namespace.getNamespace("cmis", "http://docs.oasis-open.org/ns/cmis/ws/200908/"); 
	
	
	/**
	 * Wise
	 */
	public static String OFTEN_USED_DESCRIPTION_WORDS = "Lorem,Ipsum,simply,dummy,text,printing,typesetting,industry";
	public static String SELDOM_USED_DESCRIPTION_WORDS = "Wikipedia,free,collaboratively,edited,multilingual,Internet,encyclopedia,supported,non-profit,Wikimedia,Foundation";
	public static Integer OFTEN_USED_DESCRIPTION_WORDS_COUNTER = 100;
	public static Integer SELDOM_USED_DESCRIPTION_WORDS_COUNTER = 10;
	
	/**
	 *  Directory names
	 */
	public static final String TMP_DIR_WISE_FILES = "wise_files";
	
	public static final String TMP_DIR_TOP_FOLDERS = "top_folders";
	
	/**
	 * Templates
	 */
	public static final String PAGE_TPL_DEFAULT = "events";
	public static final String PAGE_TPL_QALIST = "qa-list";
	public static final String PAGE_TPL_QAQUERY = "qa-query";
	public static final String PAGE_TPL_QAEXTERNAL = "qa-external";
	public static final String PAGE_TPL_QAINTERNAL = "qa-internal";
	
	/**
	 * Mounts
	 */
	public static final String MOUNT_POINT_CMIS = "cmis";
	public static final String MOUNT_POINT_VFS = "vfs";
	public static final String MOUNT_POINT_NAME = "cmisMountPoint";
	public static final String CMIS_PICTURES_DIR = "pictures";
	public static final String CMIS_TEXT_DIR = "text";
	
	// jcr jnt jmix j
	private ContentGeneratorCst() {

	}
}


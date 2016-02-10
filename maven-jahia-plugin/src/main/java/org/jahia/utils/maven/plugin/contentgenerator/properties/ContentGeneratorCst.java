/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
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
    public static final Integer NB_NEWS_PER_PAGE_IN_QALIST = 10;

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
    public static Namespace NS_WEM = Namespace.getNamespace("wem", "http://www.jahia.org/jahia/wem/1.0");

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
    public static final String CMIS_MOUNT_POINT_NAME = "cmisMountPoint";
    public static final String CMIS_PICTURES_DIR = "pictures";
    public static final String CMIS_TEXT_DIR = "text";

    /**
     * DMF segment IDs
     */
    public static final String[][] SEGMENTS = {
        new String[] {"gender_male", "gender_female"},
        new String[] {"age__20", "age_20_30", "age_30_40", "age_40_50", "age_50_60", "age_60_70", "age_70_"},
        new String[] {"nationality_austria", "nationality_canada", "nationality_france", "nationality_kenia", "nationality_russia", "nationality_switzerland", "nationality_ukraine", "nationality_usa"}
    };

    private ContentGeneratorCst() {
    }
}

package org.jahia.utils.maven.plugin.contentgenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jahia.utils.maven.plugin.contentgenerator.bo.ArticleBO;
import org.jahia.utils.maven.plugin.contentgenerator.bo.ExportBO;
import org.jahia.utils.maven.plugin.contentgenerator.bo.PageBO;
import org.jahia.utils.maven.plugin.contentgenerator.properties.ContentGeneratorCst;
import org.jdom.Document;

public class PageService {
	private Log logger = new SystemStreamLog();

	String sep;
    private Random random = new Random();
    
	private static List<String> oftenUsedDescriptionWords;

	private static List<String> seldomUsedDescriptionWords;
	
	private static Integer nbOftenKeywordsAlreadyAssigned;
	
	private static Integer nbSeldomKeywordsAlreadyAssigned;
	
	private Map cmisFilePaths;
	
    public PageService() {
		sep = System.getProperty("file.separator");
		oftenUsedDescriptionWords = Arrays.asList(ContentGeneratorCst.OFTEN_USED_DESCRIPTION_WORDS.split("\\s*,\\s*"));
		seldomUsedDescriptionWords = Arrays.asList(ContentGeneratorCst.SELDOM_USED_DESCRIPTION_WORDS.split("\\s*,\\s*"));
		
		nbOftenKeywordsAlreadyAssigned = 0;
		nbSeldomKeywordsAlreadyAssigned = 0;
		
		initCmisFilePath();
	}

	/**
	 * Main method, create top pages and for each calls the sub pages
	 * generation. Each time a top page and its sub pages have been generated,
	 * they are sent to the writer to avoid out of memory error with a big
	 * number of pages.
	 * 
	 * @param export
	 *            Export BO contains all the parameters chose by the user to
	 *            configure his/her content generation
	 * @param articles
	 *            List of articles selected from database
	 * @throws IOException
	 *             Error while writing generated XML to the output file
	 */
	public void createTopPages(ExportBO export, List<ArticleBO> articles) throws IOException {
		OutputService os = new OutputService();
		ArticleService articleService = ArticleService.getInstance();

		logger.info("Creating top pages");
		PageBO pageTopLevel = null;

        Map<String,ArticleBO> articlesMap = new HashMap<String, ArticleBO>();
        for (String language : export.getSiteLanguages()) {
            articlesMap.put(language,articleService.getArticle(articles));
        }

		String rootPageName = export.getRootPageName();

		OutputService outService = new OutputService();
        outService.initOutputFile(export.getOutputFile());
		outService.appendStringToFile(export.getOutputFile(), export.toString());
		
		List<PageBO> listeTopPages = new ArrayList<PageBO>();
		for (int i = 1; i <= export.getNbPagesTopLevel().intValue(); i++) {
            for (String language : export.getSiteLanguages()) {
                articlesMap.put(language, articleService.getArticle(articles));
            }
            
			pageTopLevel = createNewPage(export, null, articlesMap, export.getNbSubLevels() + 1,
					createSubPages(export, articles, export.getNbSubLevels(), export.getMaxArticleIndex()));
			
			outService.appendPageToFile(export.getOutputFile(), pageTopLevel);

            // path
            listeTopPages.add(pageTopLevel);
		}
		
        logger.info("Pages path are being written to the map file");
        List<String> pagesPath = getPagesPath(listeTopPages, "/sites/" + export.getSiteKey() + "/" + rootPageName);
        outService.appendPathToFile(export.getMapFile(), pagesPath);
        
		PageBO rootPage = createNewPage(export, rootPageName, articlesMap, export.getNbSubLevels() + 1, listeTopPages);
		outService.appendStringToFile(export.getOutputFile(), rootPage.getJcrXml());
		
		Document pagesDocument = new Document(rootPage.getElement());
		os.writeJdomDocumentToFile(pagesDocument, export.getOutputFile());
	}

	/**
	 * Recursive method that will generate all sub pages of a page, and call
	 * itself as much as necessary to reach the number of levels requested
	 * 
	 * @param export
	 *            Export BO contains all the parameters chose by the user to
	 *            configure his/her content generation
	 * @param articles
	 *            List of articles selected from database
	 * @param level
	 *            Current level in the tree decrease each time, top level ==
	 *            number of levels requested by the user)
	 * @param maxArticleIndex
	 *            Index of the last article
	 * @return A list of Page BO, containing their sub pages (if they have some)
	 */
	public List<PageBO> createSubPages(ExportBO export, List<ArticleBO> articles, Integer level, Integer maxArticleIndex) {
		ArticleService articleService = ArticleService.getInstance();
		List<PageBO> listePages = new ArrayList<PageBO>();
		Map<String,ArticleBO> articlesMap;

		int nbPagesPerLevel = export.getNbSubPagesPerPage();

		listePages.clear();
		if (level.intValue() > 0) {
			for (int i = 0; i < nbPagesPerLevel; i++) {
                articlesMap = new HashMap<String, ArticleBO>();
                for (String language : export.getSiteLanguages()) {
                    articlesMap.put(language,articleService.getArticle(articles));
                }
				PageBO page = createNewPage(export, null, articlesMap, level,
						createSubPages(export, articles, level.intValue() - 1, maxArticleIndex + 1));
				listePages.add(page);
			}
		}
		return listePages;
	}

	/**
	 * Create a new page object
	 * 
	 *
     * @param export
     *            Export BO contains all the parameters chose by the user to
     *            configure his/her content generation
     * @param pageName
     *            Used only for root page, or to specify a page name If null,
     *            creates a unique page name from concatenation of page name
     *            prefix and unique ID
     * @param articlesMap
     *@param level
     *            Current level in the tree
     * @param subPages
*            List of sub pages related   @return
	 */
	public PageBO createNewPage(ExportBO export, String pageName, Map<String, ArticleBO> articlesMap, int level,
                                List<PageBO> subPages) {
		ContentGeneratorService.currentPageIndex = ContentGeneratorService.currentPageIndex + 1;
		

		// choose template (query, list, external)
        String template = ContentGeneratorCst.PAGE_TPL_DEFAULT;
        Integer indexPagesWithList = export.getNbPagesWithTplList();
        Integer indexPagesWithQuery = indexPagesWithList + export.getNbPagesWithTplQuery();
        
        // the remaining is distributed between pages with external file references, internal file references and just text
        Integer indexPagesWithExternalFileReference = 0;
        Integer remainingNbPages = export.getTotalPages() - (export.getNbPagesWithTplList() + export.getNbPagesWithTplQuery());
        if (! export.isDisableExternalFileReference()) {
	        Integer nbPagesWithExternalFileReference = Math.round(remainingNbPages * ContentGeneratorCst.PERCENTAGE_PAGES_WITH_EXTERNAL_FILE_REF);
	        indexPagesWithExternalFileReference = indexPagesWithQuery + nbPagesWithExternalFileReference;
        }
        
        Integer indexPagesWithInternalFileReference = 0;
        if (! export.isDisableInternalFileReference()) {
	        Integer nbPagesWithInternalFileReference = Math.round(remainingNbPages * ContentGeneratorCst.PERCENTAGE_PAGES_WITH_INTERNAL_FILE_REF);
	        indexPagesWithInternalFileReference = indexPagesWithExternalFileReference + nbPagesWithInternalFileReference;
        }
        
        // pages with qa-list template
        if (ContentGeneratorService.currentPageIndex <= indexPagesWithList) {
        	template = ContentGeneratorCst.PAGE_TPL_QALIST;
        }
        
        // pages with qa-query template
        if (ContentGeneratorService.currentPageIndex > indexPagesWithList && ContentGeneratorService.currentPageIndex <= indexPagesWithQuery) {
        	template = ContentGeneratorCst.PAGE_TPL_QAQUERY;
        }
        
        List externalFilePaths = new ArrayList();
        if (ContentGeneratorService.currentPageIndex > indexPagesWithQuery && ContentGeneratorService.currentPageIndex <= indexPagesWithExternalFileReference) {
        	pageName = ContentGeneratorCst.PAGE_TPL_QAEXTERNAL + ContentGeneratorService.currentPageIndex;
        	externalFilePaths.add(getRandomCmisFilePath(ContentGeneratorCst.CMIS_PICTURES_DIR));
        	externalFilePaths.add(getRandomCmisFilePath(ContentGeneratorCst.CMIS_TEXT_DIR));
        }
        
        String fileName = null;
        if (ContentGeneratorService.currentPageIndex > indexPagesWithExternalFileReference && ContentGeneratorService.currentPageIndex <= indexPagesWithInternalFileReference) {
        	pageName = ContentGeneratorCst.PAGE_TPL_QAINTERNAL + ContentGeneratorService.currentPageIndex;
			FileService fileService = new FileService();
			fileName = fileService.getFileName(export.getFileNames());
        }
        
		if (pageName == null) {
			pageName = template + ContentGeneratorService.currentPageIndex;
		}
		
		logger.debug("		Creating new page level " + level + ": " + pageName);

        HashMap<String, List<String>> acls = new HashMap<String, List<String>>();
        if (random.nextFloat() < export.getGroupAclRatio() && export.getNumberOfGroups() > 0) {
            acls.put("g:group"+random.nextInt(export.getNumberOfGroups()), Arrays.asList("editor"));
        }
        if (random.nextFloat() < export.getUsersAclRatio() && export.getNumberOfUsers() > 0) {
            acls.put("u:user"+random.nextInt(export.getNumberOfUsers()), Arrays.asList("editor"));
        }

        // mapping to category
        Integer idCategory = null;
        float firstThird = export.getTotalPages() / 3;
        if (ContentGeneratorService.currentPageIndex <= firstThird && export.getNumberOfCategories() > 0) {
        	idCategory = random.nextInt(export.getNumberOfCategories());
        	logger.debug("Add " + pageName + " to category " +idCategory);
        }
        
        Integer idTag = null;
        if (ContentGeneratorService.currentPageIndex <= firstThird && export.getNumberOfTags() > 0) {
        	idTag = random.nextInt(export.getNumberOfTags());
        	logger.debug("Tag " + pageName + " with tag " + idTag);
        }
        
        // visibility
        Boolean visibilityOnPage = Boolean.FALSE;
        if (ContentGeneratorService.currentPageIndex <= firstThird && export.getVisibilityEnabled()) {
        	visibilityOnPage = Boolean.TRUE;
        	logger.debug("Visibility enabled");
        }
        
        // add description for the first pages
        String oftenKeywords = getOftenKeywords(export.getTotalPages());
        String seldomKeywords = getSeldomKeywords(export.getTotalPages());        
        String description = oftenKeywords + " " + seldomKeywords;
        
        PageBO page = new PageBO(pageName, articlesMap, level, subPages,
				export.getPagesHaveVanity(), export.getSiteKey(), fileName, export.getNumberOfBigTextPerPage(), acls, idCategory, idTag,  visibilityOnPage, export.getVisibilityStartDate(), export.getVisibilityEndDate(), description, template, export.getCmisSiteName(), externalFilePaths);
        
		return page;
	}

	/**
	 * getPagesPathrecursively retrieves absolute paths for each page, from the
	 * top page. If choosen by the user, a map of this path will be generated.
	 * It can be used to run performance tests.
	 * 
	 * @param pages
	 *            list of the top pages
	 * @param path
	 *            this method is recursive, this is the path generated for the
	 *            pages above
	 * @return String containing all the generated paths, one per line
	 */
	public List<String> getPagesPath(List<PageBO> pages, String path) {
		List<String> siteMap = new ArrayList<String>();

		if (path == null) {
			path = "";
		}
		for (Iterator<PageBO> iterator = pages.iterator(); iterator.hasNext();) {
			PageBO page = iterator.next();
			String newPath = path + ContentGeneratorCst.PAGE_PATH_SEPARATOR + page.getUniqueName();
			siteMap.add(newPath);
			if (page.getSubPages() != null) {
				siteMap.addAll(getPagesPath(page.getSubPages(), newPath));
			}
		}

		return siteMap;
	}
		
	private String getOftenKeywords(int nbOfPagesToCreate) {
		Integer nbKeywordsAvailable = oftenUsedDescriptionWords.size();
		double ratio =  ((double) (ContentGeneratorCst.OFTEN_USED_DESCRIPTION_WORDS_COUNTER * (double) nbKeywordsAvailable) / (double) nbOfPagesToCreate);
		
		double nbKeywordsToGet = ((ContentGeneratorService.currentPageIndex + 1) * ratio) - nbOftenKeywordsAlreadyAssigned;
		nbKeywordsToGet = Math.floor(nbKeywordsToGet);
		

		Set<String> keywords = new HashSet<String>();
		if (nbKeywordsAvailable <= nbKeywordsToGet) {
			keywords = new HashSet<String>(oftenUsedDescriptionWords);
		} else {
			int i = 1;
			Random r = new Random();
			while (i <= nbKeywordsToGet) {
				int randomId = r.nextInt(nbKeywordsAvailable - 1);
				boolean added = keywords.add(oftenUsedDescriptionWords.get(randomId));
				if (added) {
					i++;
				}
			}
		}
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iterator = keywords.iterator(); iterator.hasNext();) {
			sb = sb.append(iterator.next() + " ");			
		}

		PageService.nbOftenKeywordsAlreadyAssigned += (int) nbKeywordsToGet;
		return sb.toString();
	}
	
	private String getSeldomKeywords(int nbOfPagesToCreate) {
		Integer nbKeywordsAvailable = seldomUsedDescriptionWords.size();
		double ratio =  ((double) (ContentGeneratorCst.SELDOM_USED_DESCRIPTION_WORDS_COUNTER * (double) nbKeywordsAvailable) / (double) nbOfPagesToCreate);
		
		double nbKeywordsToGet = ((ContentGeneratorService.currentPageIndex + 1) * ratio) - nbSeldomKeywordsAlreadyAssigned;
		nbKeywordsToGet = Math.floor(nbKeywordsToGet);
		
		Set<String> keywords = new HashSet<String>();if (nbKeywordsAvailable <= nbKeywordsToGet) {
			keywords = new HashSet<String>(seldomUsedDescriptionWords);
		} else {
			int i = 1;
			Random r = new Random();
			while (i <= nbKeywordsToGet) {
				int randomId = r.nextInt(nbKeywordsAvailable - 1);
				boolean added = keywords.add(seldomUsedDescriptionWords.get(randomId));
				if (added) {
					i++;
				}
			}			
		}
		
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iterator = keywords.iterator(); iterator.hasNext();) {
			sb = sb.append(iterator.next() + " ");			
		}

		PageService.nbSeldomKeywordsAlreadyAssigned += (int) nbKeywordsToGet;
		return sb.toString();
	}
	
	private String getRandomCmisFilePath(String type) {
		String randomFilePath;
		List pathsList = (ArrayList) cmisFilePaths.get(type);
		int randomPathIndex = random.nextInt(pathsList.size() - 1);
		CmisDirectoryPath pathObject = (CmisDirectoryPath) pathsList.get(randomPathIndex);
		
		int randomFileId = random.nextInt(pathObject.getNbFiles() - 1);
		String randomFileName = randomFileId + pathObject.getFileSuffix();
		randomFilePath = pathObject.getDirectoryPath() + "/" + randomFileName;
		return randomFilePath;
	}
	
	private void initCmisFilePath() {		
		List textPaths = new ArrayList();
		List picturesPaths = new ArrayList();
		int nbFiles = 100;
		for (int i = 0; i < 10; i++) {
			textPaths = new ArrayList();
			textPaths.add(new CmisDirectoryPath("/" + ContentGeneratorCst.CMIS_TEXT_DIR + "/directory-" + nbFiles + "-" + i, ".sample.txt", nbFiles));
			picturesPaths.add(new CmisDirectoryPath("/" + ContentGeneratorCst.CMIS_PICTURES_DIR + "/directory-" + nbFiles + "-" + i, ".sample.png", nbFiles));
		}

		nbFiles = 500;
		textPaths.add(new CmisDirectoryPath("/" + ContentGeneratorCst.CMIS_TEXT_DIR + "/directory-" + nbFiles + "-0", ".sample.txt", nbFiles));
		picturesPaths.add(new CmisDirectoryPath("/" + ContentGeneratorCst.CMIS_PICTURES_DIR + "/directory-" + nbFiles + "-0", ".sample.png", nbFiles));

		nbFiles = 1000;
		textPaths.add(new CmisDirectoryPath("/" + ContentGeneratorCst.CMIS_TEXT_DIR + "/directory-" + nbFiles + "-0", ".sample.txt", nbFiles));
		picturesPaths.add(new CmisDirectoryPath("/" + ContentGeneratorCst.CMIS_PICTURES_DIR + "/directory-" + nbFiles + "-0", ".sample.png", nbFiles));
		
		cmisFilePaths = new HashMap();
		cmisFilePaths.put(ContentGeneratorCst.CMIS_PICTURES_DIR, textPaths);
		cmisFilePaths.put(ContentGeneratorCst.CMIS_TEXT_DIR, picturesPaths);
	}
	
	private class CmisDirectoryPath {
		private String directoryPath;
		private String fileSuffix;
		private int nbFiles;
		
		private CmisDirectoryPath(String directoryPath, String fileSuffix, int nbFiles) {
			this.directoryPath = directoryPath;
			this.fileSuffix = fileSuffix;
			this.nbFiles = nbFiles;
		}
		
		public String getDirectoryPath() {
			return directoryPath;
		}

		public String getFileSuffix() {
			return fileSuffix;
		}

		public int getNbFiles() {
			return nbFiles;
		}
	}

}

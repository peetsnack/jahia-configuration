package org.jahia.utils.maven.plugin.contentgenerator.wise;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import org.jahia.utils.maven.plugin.contentgenerator.wise.bo.FileBO;
import org.jahia.utils.maven.plugin.contentgenerator.wise.bo.FolderBO;

public class FileAndFolderService {
	
	private static FileAndFolderService instance;
	
	String sep = System.getProperty("file.separator");
	
	private FileAndFolderService() {

	}

	public static FileAndFolderService getInstance() {
		if (instance == null) {
			instance = new FileAndFolderService();
		}
		return instance;
	}

	public List<FolderBO> generateFolders(Integer foldersDepth,  Integer nbFoldersPerLevel, Integer filesPerFolder, List<String> fileNames, String outputDirPath, String wiseInstanceName, String docpaceKey,  File filesDirectory) {
		return generateFolders(1, nbFoldersPerLevel, foldersDepth, filesPerFolder, fileNames, initializeContentFolder(outputDirPath, wiseInstanceName, docpaceKey), filesDirectory);
	}
	
	private List<FolderBO> generateFolders(Integer currentDepth, Integer nbFoldersPerLevel, Integer foldersDepth, Integer filesPerFolder, List<String> fileNames, String currentPath, File filesDirectory) {
		String depthName;
		
		switch (currentDepth) {
		case 1:
			depthName = "aaa";
			break;
		case 2:
			depthName = "bbb";
			break;
		case 3:
			depthName = "ccc";
			break;
		case 4:
			depthName = "ddd";
			break;
		case 5:
			depthName = "eee";
			break;
		case 6:
			depthName = "fff";
			break;
		case 7:
			depthName = "ggg";
			break;
		case 8:
			depthName = "hhh";
			break;
		case 9:
			depthName = "iii";
			break;
		default:
			depthName = "aaa";
			break;
		}
		
		List<FolderBO> folders = new ArrayList<FolderBO>();
		for (int i = 0; i < nbFoldersPerLevel; i++) {
			List<FolderBO> subFolders = null;
			List<FileBO> files = generateFiles(filesPerFolder, fileNames);
			if (currentDepth < foldersDepth) {
				subFolders =  generateFolders(currentDepth + 1, nbFoldersPerLevel, foldersDepth, filesPerFolder, fileNames, currentPath + sep + depthName + i, filesDirectory);
			}
			folders.add(new FolderBO(depthName + i, subFolders, files));
			
			// create physical folder
			File newFolder = new File(currentPath + sep + depthName + i);
			newFolder.mkdirs();
			
			// copy files into the new folder
			for (Iterator<FileBO> iterator = files.iterator(); iterator.hasNext();) {
				FileBO fileBO = (FileBO) iterator.next();
				File sourceFile = new File(filesDirectory + sep + fileBO.getFileName());
				File targetDirectory = new File(newFolder + sep + fileBO.getFileName());
				// each file is contained in its own directory
				targetDirectory.mkdir();
				File targetFile = new File(targetDirectory + sep + fileBO.getFileName());
				try {
					FileUtils.copyFile(sourceFile, targetFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return folders;
	}
	
	public List<FileBO> generateFiles(Integer nbFiles, List<String> fileNames) {
		List<FileBO> files = new ArrayList<FileBO>();
		Random rand  = new Random();
		
		for (int i = 0; i < nbFiles; i++) {
			String fileName = fileNames.get(rand.nextInt(fileNames.size() - 1));
			files.add(new FileBO(fileName)); 
		}
		return files;
	}
	
	public String initializeContentFolder(String outputDirPath, String wiseInstanceName, String docpaceKey) {
		File contentdirectory = new File(outputDirPath + sep + "content" + sep + "sites" + sep + wiseInstanceName + sep + "files" + sep + "docspaces" + sep + docpaceKey);
		contentdirectory.mkdirs();
		return contentdirectory.getAbsolutePath();
	}
}
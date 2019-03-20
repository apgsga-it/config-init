package com.apgsga.patch.service.bootstrap.config

import java.nio.file.Files

import groovy.io.FileType
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.NodeChild
import groovy.xml.XmlUtil

class PatchInitConfigClient {
	
	ConfigObject initConfig
	
	boolean dryRun
	
	def final dryRunFileExt = ".dryrun"
	
	public PatchInitConfigClient(def initConfig, def dryRun) {
		this.initConfig = initConfig
		this.dryRun = dryRun
	}
	
	def initAll() {
		initTargetSystemMapping()
		initPiperProperties()
		initJenkinsConfig()
		initMavenSettings()
		initGradleSettings()
		initYumRepoConfig()
	}
	
	def initYumRepoConfig() {
		println "Initialisation of Yum Repo configuration started ..."
		adaptYumRepoConfig()
		println "Initialisation of Yum Repo configuration done!"
	}
	
	def initJenkinsConfig() {
		println "Initialisation of Jenkins config.xml started ..."
		backupFile(initConfig.jenkins.jenkinsConfigFileLocation)
		adaptJenkinsConfig()
		backupFile(initConfig.jenkins.jenkinsModelFileLocaltion)
		adaptJenkinsModelConfig()	
		removeJenkinsNodes()	
		println "Initialisation of Jenkins config.xml done!"
	}

	def initTargetSystemMapping() {
		println "Initialisation of targetSystemMapping started ..."
		backupFile(initConfig.target.system.mapping.file.path)
		changeTargetSystemMappingContent()
		println "Initialisation of targetSystemMapping done!"
	}

	def initPiperProperties() {
		println "Initialisation of patch service properties started ..."
		def dir = new File(initConfig.piper.config.path)
		dir.traverse(type: FileType.DIRECTORIES, nameFilter: ~/${initConfig.piper.config.folder.prefix}.*/, excludeNameFilter:"apg-patch-target-configinit") {
			it.traverse(type: FileType.FILES, nameFilter: ~/.*\.${initConfig.piper.config.file.suffix}/) {
				backupFile(it.getPath())
				adaptContentForPiperPropertiesFile(it)
			}
		}
		println "Initialisation of patch service properties done!"
	}
	
	def adaptContentForPiperPropertiesFile(File file) {
		
		println "Processing file : " + file.getPath()
		
		def piperPropertiesFromInitConfig = initConfig.flatten()
		Properties propsToBeUpdated = new Properties()
		file.withInputStream{ stream ->
			propsToBeUpdated.load(stream)
		}
		
		def needToUpdateFile = false
		Properties newProps = new Properties()
		propsToBeUpdated.each({key,value -> 
			def String searchedKey = "piper.${key}"
			if(piperPropertiesFromInitConfig.containsKey(searchedKey)) {
				def newValue = piperPropertiesFromInitConfig.get(searchedKey)
				newProps.put(key, newValue)
				needToUpdateFile = true
			}
		})

		if(needToUpdateFile || dryRun) {
			propsToBeUpdated.putAll(newProps)
			PrintWriter pw
			
			if(!dryRun) {
				pw = new PrintWriter(file)
			}
			else {
				File dryRunFile = new File(file.getAbsolutePath() + dryRunFileExt)
				pw = new PrintWriter(dryRunFile)
			}
			
			pw.write("")
			
			propsToBeUpdated.each({key,value -> 
				pw.write("${key}=${value}")
				pw.write(System.getProperty("line.separator"))
			})

			pw.close()			
		}
		
		println "Update from ${file.getPath()} done."
	}
	
	def initMavenSettings() {
		println "Initialisation of maven settings started ..."
		backupFile(initConfig.maven.config.file.path)
		adaptMavenSettings()
		println "Initialisation of maven settings done!"
	}
	
	def initGradleSettings() {
		println "Initialisation of graddle settings started ..."
		backupFile(initConfig.gradle.properties.file.path)
		adaptGradleSettings()
		println "Initialisation of graddle settings done!"
	}
	
	private def adaptYumRepoConfig() {
		if(Files.exists(new File(initConfig.yum.artifactory.repo.config.file.path).toPath())) {
			backupFile(initConfig.yum.artifactory.repo.config.file.path)
			adaptArtifactoryRepo()
		}
		if(Files.exists(new File(initConfig.yum.artifactory.patch.repo.config.file.path).toPath())) {
			backupFile(initConfig.yum.artifactory.patch.repo.config.file.path)
			adaptArtifactoryPatchRepo()
		}
	}
	
	// TODO JHE: Quickly implemented so that it worked, but to be refactored
	private def adaptArtifactoryRepo() {
		new File( "${initConfig.yum.artifactory.repo.config.file.path}.new" ).withWriter { w ->
			def lineNumber = 1
			new File( initConfig.yum.artifactory.repo.config.file.path ).eachLine { line ->
				if(lineNumber == 1) {
					w << initConfig.yum.artifactory.repo.header + System.getProperty("line.separator")
				}
				else {
					if(line.contains("name=")) {
						w << "name=" + initConfig.yum.artifactory.repo.name + System.getProperty("line.separator")
					}
					else if(line.contains("baseurl=")) {
						w << "baseurl=" + initConfig.yum.artifactory.repo.baseurl + System.getProperty("line.separator")
					}
					else {
						w << line + System.getProperty("line.separator")
					}
				}
				lineNumber++
			}
		}
		
		if(dryRun) {
			new File("${initConfig.yum.artifactory.repo.config.file.path}.dryrun").delete()
			Files.copy(new File("${initConfig.yum.artifactory.repo.config.file.path}.new").toPath(), new File("${initConfig.yum.artifactory.repo.config.file.path}.dryrun").toPath() )
			new File("${initConfig.yum.artifactory.repo.config.file.path}.new").delete()
		}
		else {
			new File("${initConfig.yum.artifactory.repo.config.file.path}").delete()
			Files.copy(new File("${initConfig.yum.artifactory.repo.config.file.path}.new").toPath(), new File("${initConfig.yum.artifactory.repo.config.file.path}").toPath() )
			new File("${initConfig.yum.artifactory.repo.config.file.path}.new").delete()
		}
	}

	// TODO JHE: Quickly implemented so that it worked, but to be refactored
	private def adaptArtifactoryPatchRepo() {
		new File( "${initConfig.yum.artifactory.patch.repo.config.file.path}.new" ).withWriter { w ->
			def lineNumber = 1
			new File( initConfig.yum.artifactory.patch.repo.config.file.path ).eachLine { line ->
				if(lineNumber == 1) {
					w << initConfig.yum.artifactory.patch.repo.header + System.getProperty("line.separator")
				}
				else {
					if(line.contains("name=")) {
						w << "name=" + initConfig.yum.artifactory.patch.repo.name + System.getProperty("line.separator")
					}
					else if(line.contains("baseurl=")) {
						w << "baseurl=" + initConfig.yum.artifactory.patch.repo.baseurl + System.getProperty("line.separator")
					}
					else {
						w << line + System.getProperty("line.separator")
					}
				}
				lineNumber++
			}
		}
		
		if(dryRun) {
			new File("${initConfig.yum.artifactory.patch.repo.config.file.path}.dryrun").delete()
			Files.copy(new File("${initConfig.yum.artifactory.patch.repo.config.file.path}.new").toPath(), new File("${initConfig.yum.artifactory.patch.repo.config.file.path}.dryrun").toPath())
			new File("${initConfig.yum.artifactory.patch.repo.config.file.path}.new").delete()
		}
		else {
			new File("${initConfig.yum.artifactory.patch.repo.config.file.path}").delete()
			Files.copy(new File("${initConfig.yum.artifactory.patch.repo.config.file.path}.new").toPath(), new File("${initConfig.yum.artifactory.patch.repo.config.file.path}").toPath())
			new File("${initConfig.yum.artifactory.patch.repo.config.file.path}.new").delete()
		}
	}
	
	private def removeJenkinsNodes() {
		println "Deletion of Jenkins Nodes starting ..."
		def dirsToDelete = []
		def dir = new File("${initConfig.jenkins.home.folder}/nodes")
		dir.traverse(type: FileType.DIRECTORIES) {
			if(dryRun) {
				println "${it} folder would have been added to list of directories to be deleted."
			}
			else {
				// it.deleteDir() : doesn't work, probably because of the way Groovy iterates over. Once it has been deleted, we get a NPE at next iter.
				dirsToDelete.add(it)
				println "${it} added to list of directories to be deleted."
			}
		}
		
		dirsToDelete.each { d ->
			if(dryRun) {
				println "${d} and all its content would have been deleted."
			}
			else {
				d.deleteDir()
			}
			
		}
		println "Deletion of Jenkins Nodes done!"
	}
	
	private def adaptGradleSettings() {
		def propertiesFromInitConfig = initConfig.flatten()
		Properties propsToBeUpdated = new Properties()
		def gradlePropertyFile = new File(propertiesFromInitConfig.get("gradle.properties.file.path"))
		gradlePropertyFile.withInputStream{ stream ->
			propsToBeUpdated.load(stream)
		}
		
		def needToUpdateFile = false
		Properties newProps = new Properties()
		
		propsToBeUpdated.each({key,value ->
			def String searchedKey = "gradle.${key}"
			if(propertiesFromInitConfig.containsKey(searchedKey)) {
				def newValue = propertiesFromInitConfig.get(searchedKey)
				newProps.put(key, newValue)
				needToUpdateFile = true
			}
		})

		if(needToUpdateFile) {
			propsToBeUpdated.putAll(newProps)
			PrintWriter pw
			
			if(!dryRun) {		
				pw = new PrintWriter(gradlePropertyFile)
			}
			else {
				File gradlePropertyFileDryRun = new File(propertiesFromInitConfig.get("gradle.properties.file.path") + dryRunFileExt)
				pw = new PrintWriter(gradlePropertyFileDryRun)
			}
			pw.write("")
			
			propsToBeUpdated.each({key,value ->
				pw.write("${key}=${value}")
				pw.write(System.getProperty("line.separator"))
			})

			pw.close()
		}
		
		
	}
	
	private def adaptMavenSettings() {
		def mavenSettings = new XmlSlurper().parse(new File(initConfig.maven.config.file.path))
		adaptMavenSettingsServer(mavenSettings)
		adaptMavenSettingsRepository(mavenSettings)
		adaptMavenSettingsPluginRepository(mavenSettings)
		adaptMavenSettingsProfileIds(mavenSettings)
		adaptMavenSettingsProperties(mavenSettings)
		saveXmlConfiguration(mavenSettings, initConfig.maven.config.file.path)
	}
	
	private def adaptMavenSettingsProperties(def mavenSettings) {
		NodeChild defaultProfile = mavenSettings.profiles.getAt(0)
		def String newDistributionManagementRepositoryUrl = "${defaultProfile.profile.properties.distributionManagementRepositoryUrl}${initConfig.maven.repository.suffix}"
		def String newSnapshotRepositoryUrl = "${defaultProfile.profile.properties.snapshotRepositoryUrl}${initConfig.maven.repository.suffix}"
		defaultProfile.profile.properties.distributionManagementRepositoryUrl = newDistributionManagementRepositoryUrl
		defaultProfile.profile.properties.snapshotRepositoryUrl = newSnapshotRepositoryUrl
	}
	
	private def adaptMavenSettingsProfileIds(def mavenSettings) {
		NodeChild defaultProfile = mavenSettings.profiles.getAt(0)
		def String newProfileId = "${defaultProfile.profile.id}${initConfig.maven.profile.id.suffix}"
		def String newActiveProfileId = "${mavenSettings.activeProfiles.getAt(0).activeProfile}${initConfig.maven.profile.id.suffix}"
		defaultProfile.profile.id = newProfileId
		mavenSettings.activeProfiles.getAt(0).activeProfile = newActiveProfileId
	}
	
	private def adaptMavenSettingsPluginRepository(def mavenSettings) {
		NodeChild defaultProfile = mavenSettings.profiles.getAt(0)
		defaultProfile.profile.pluginRepositories.pluginRepository.each({NodeChild pluginRepo ->
			def String newId = "${pluginRepo.id}${initConfig.maven.plugin.repository.suffix}"
			def String newName = "${pluginRepo.name}${initConfig.maven.plugin.repository.suffix}"
			def String newUrl = "${pluginRepo.url}${initConfig.maven.plugin.repository.suffix}"
			pluginRepo.id = newId
			pluginRepo.name = newName
			pluginRepo.url = newUrl
		})
	}
	
	private def adaptMavenSettingsRepository(def mavenSettings) {
		NodeChild defaultProfile = mavenSettings.profiles.getAt(0)
		defaultProfile.profile.repositories.repository.each({repo ->
			def String newId = "${repo.id}${initConfig.maven.repository.suffix}"
			def String newName = "${repo.name}${initConfig.maven.repository.suffix}"
			def String newUrl = "${repo.url}${initConfig.maven.repository.suffix}"
			repo.id = newId
			repo.name = newName
			repo.url = newUrl
		})
	}
	
	private def adaptMavenSettingsServer(def mavenSettings) {
		mavenSettings.servers.server.each({s ->
			s.username = initConfig.maven.servers.server.username
			s.password = initConfig.maven.servers.server.password
		})
	}
	
	private def saveXmlConfiguration(def xmlContent, def fileName) {
		if(dryRun) {
			fileName += dryRunFileExt
		}		
		
		FileOutputStream fos = new FileOutputStream(new File(fileName))
		XmlUtil xmlUtil = new XmlUtil()
		xmlUtil.serialize(xmlContent,fos)
		fos.close()
	}
	
	private def adaptJenkinsModelConfig() {
		def jenkinsModelConfig = new XmlSlurper().parse(new File(initConfig.jenkins.jenkinsModelFileLocaltion))
		jenkinsModelConfig.jenkinsUrl = initConfig.jenkins.jenkinsUrl
		saveXmlConfiguration(jenkinsModelConfig,initConfig.jenkins.jenkinsModelFileLocaltion)
	}
	
	private def adaptJenkinsConfig() {
		
		def jenkinsConfig = new XmlSlurper().parse(new File(initConfig.jenkins.jenkinsConfigFileLocation))
		
		jenkinsConfig.numExecutors = 5

		boolean iscvsFwRootOnNextIter = false
		boolean iscvsRootOnNextIter = false
		boolean isRepoRoPasswdOnNextIter = false
		boolean isDbPatchRepoOnNextIter = false
		
		// JHE: Well, rather bad to iterate like below ... but we need to deal with such a list:
		/*
		 *<string>ARTIFACTORY_SERVER_ID</string>
          <string>artifactory4t4apgsga</string>
          <string>CVS_FW_ROOT</string>
          <string>:ext:svcCvsClient@cvs.apgsga.ch:/var/local/cvs/root</string>
          <string>CVS_ROOT</string>
          <string>:ext:svcCvsClient@cvs.apgsga.ch:/var/local/cvs/root</string>
          <string>CVS_RSH</string>
          <string>ssh</string>
          <string>GITHUB_JENKINS_VERSION</string>
          <string>refs/heads/1.0.x</string>
		 * 
		 */
		jenkinsConfig.globalNodeProperties."hudson.slaves.EnvironmentVariablesNodeProperty".envVars."tree-map".string.each({NodeChild p ->

			if(iscvsFwRootOnNextIter) {
				p.replaceBody(initConfig.jenkins.cvsFwRoot)
				iscvsFwRootOnNextIter = false
			}
			
			if(iscvsRootOnNextIter) {
				p.replaceBody(initConfig.jenkins.cvsRoot)
				iscvsRootOnNextIter = false
			}
			
			if(isRepoRoPasswdOnNextIter) {
				p.replaceBody(initConfig.jenkins.repo_ro_password)
				isRepoRoPasswdOnNextIter = false
			}
			
			if(isDbPatchRepoOnNextIter) {
				p.replaceBody(initConfig.jenkins.dbPatchRepo)
				isDbPatchRepoOnNextIter = false
			}
			
			if(p.equals("CVS_FW_ROOT")) {
				iscvsFwRootOnNextIter = true
			}
			
			if(p.equals("CVS_ROOT")) {
				iscvsRootOnNextIter = true
			}
			
			if(p.equals("REPO_RO_PASSWD")) {
				isRepoRoPasswdOnNextIter = true
			}
			
			if(p.equals("DB_PATCH_REPO")) {
				isDbPatchRepoOnNextIter = true
			}
		})
		
		saveXmlConfiguration(jenkinsConfig, initConfig.jenkins.jenkinsConfigFileLocation)
	}
	
	private def changeTargetSystemMappingContent() {
		
		def targetSystemMappingFile = new File(initConfig.target.system.mapping.file.path)
		def targetSystemMappingContent = new JsonSlurper().parse(targetSystemMappingFile)

		updateTargetSystemMapping(targetSystemMappingContent,"Entwicklung")
		updateTargetSystemMapping(targetSystemMappingContent,"Informatiktest")
		updateTargetSystemMapping(targetSystemMappingContent,"Produktion")
		updateTargetSystemMappingOtherInstance(targetSystemMappingContent)

		if(!dryRun) {		
			targetSystemMappingFile.delete()
			targetSystemMappingFile.write(new JsonBuilder(targetSystemMappingContent).toPrettyString())
		}
		else {
			def targetSystemMappingFileDryRun = new File(initConfig.target.system.mapping.file.path + dryRunFileExt)
			targetSystemMappingFileDryRun.write(new JsonBuilder(targetSystemMappingContent).toPrettyString())
		}
	}
	
	private def updateTargetSystemMappingOtherInstance(def targetSystemMappingContent) {
		targetSystemMappingContent.otherTargetInstances = []
		def newInstancesList = initConfig.target.system.mapping.otherTargetInstances.new
		newInstancesList.split(",").each({instance ->
			targetSystemMappingContent.otherTargetInstances.add(instance)
		})
	}
	
	private def updateTargetSystemMapping(def targetSystemMappingContent, def targetName) {
		targetSystemMappingContent.targetSystems.each({targetSystem ->
			if (targetSystem.name.equals(targetName)) {
				targetSystem.target = getNewTarget(targetName)
			}
		})
	}
	
	private getNewTarget(String targetName) {
		def targetNameLowerCase = targetName.toLowerCase()
		return initConfig.target.system.mapping."${targetNameLowerCase}".new
	}
	
	private def backupFile(def originalFileName) {
		def originalFile = new File(originalFileName)
		def backupFile = new File("${originalFileName}.backupFromConfigInit")
		if(!dryRun) {
			if(!Files.exists(backupFile.toPath())) {
				Files.copy(originalFile.toPath(), backupFile.toPath())
				println "Backup created for ${originalFileName} : ${backupFile.getPath()}"
			}
		}
		else {
			println "Dryrun = true ... : ${backupFile.getPath()} would have been created as backup for ${originalFileName}"
		}
	}

}
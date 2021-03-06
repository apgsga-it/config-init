package com.apgsga.patch.service.initconfig.test

import java.nio.file.Files
import java.nio.file.Paths

import javax.imageio.ImageIO.ContainsFilter

import org.apache.commons.io.FileUtils
import org.spockframework.util.Assert

import com.apgsga.patch.service.bootstrap.config.PatchInitConfigCli
import com.apgsga.patch.service.configinit.util.ConfigInitUtil

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.NodeChild
import groovy.xml.XmlUtil
import spock.lang.Specification

class PatchInitConfigTest extends Specification {
	
	def backupExtension = ".backupFromConfigInit"
	
	def usageString = "usage: patchinitcli.sh -[h|i|]"
	
	def etcOptPath = "src/test/resources/etc/opt"
	
	def varJenkinsPath = "src/test/resources/var/jenkins"
	
	def mavenSettingPath = "src/test/resources/home/jenkins/.m2"
	
	def graddlePropertiesPath = "src/test/resources/var/jenkins/gradle/home"
	
	def yumConfigPath = "src/test/resources/etc/yum.repos.d"
	
	def artifactoryRepoConfigFileName = "${yumConfigPath}/apg-artifactory.repo"
	
	def artifactoryPatchRepoConfigFileName = "${yumConfigPath}/apg-artifactory-patch.repo"
	
	def artifactoryRepoConfigBackupFileName = "${artifactoryRepoConfigFileName}${backupExtension}"
	
	def artifactoryPatchRepoConfigBackupFileName = "${artifactoryPatchRepoConfigFileName}${backupExtension}"
	
	def artifactoryRepoConfigDryRunFileName = "${artifactoryRepoConfigFileName}.dryrun"
	
	def artifactoryPatchRepoConfigDryRunFileName = "${artifactoryPatchRepoConfigFileName}.dryrun"
	
	def targetSystemMappingFileName = "${etcOptPath}/apg-patch-common/TargetSystemMappings.json"
	
	def targetSystemMappingBackupFileName = "${targetSystemMappingFileName}${backupExtension}"
	
	def targetSystemMappingDryRunFileName = "${targetSystemMappingFileName}.dryrun"
	
	def patchCliApplicationPropertiesFileName = "${etcOptPath}/apg-patch-cli/application.properties"
	
	def patchCliApplicationPropertiesBackupFileName = "${patchCliApplicationPropertiesFileName}${backupExtension}"
	
	def patchCliApplicationPropertiesDryRunFileName = "${patchCliApplicationPropertiesFileName}.dryrun"
	
	def patchCliOpsPropertiesFileName = "${etcOptPath}/apg-patch-cli/ops.properties"
	
	def patchCliOpsPropertiesBackupFileName = "${patchCliOpsPropertiesFileName}${backupExtension}"
	
	def patchCliOpsPropertiesDryRunFileName = "${patchCliOpsPropertiesFileName}.dryrun"
	
	def patchServerApplicationPropertiesFileName = "${etcOptPath}/apg-patch-service-server/application.properties"
	
	def patchServerApplicationPropertiesBackupFileName = "${patchServerApplicationPropertiesFileName}${backupExtension}"
	
	def patchServerApplicationPropertiesDryRunFileName = "${patchServerApplicationPropertiesFileName}.dryrun"
	
	def patchServerOpsPropertiesFileName = "${etcOptPath}/apg-patch-service-server/ops.properties"
	
	def patchServerOpsPropertiesBackupFileName = "${patchServerOpsPropertiesFileName}${backupExtension}"
	
	def patchServerOpsPropertiesDryRunFileName = "${patchServerOpsPropertiesFileName}.dryrun"
	
	def jenkinsConfigXmlFileName = "${varJenkinsPath}/config.xml"
	
	def jenkinsConfigXmlBackupFileName = "${jenkinsConfigXmlFileName}${backupExtension}"
	
	def jenkinsConfigXmlDryRunFileName = "${jenkinsConfigXmlFileName}.dryrun"
	
	def jenkinsModelXmlFileName = "${varJenkinsPath}/jenkins.model.JenkinsLocationConfiguration.xml"
	
	def jenkinsModelXmlBackupFileName = "${jenkinsModelXmlFileName}${backupExtension}"
	
	def jenkinsModelXmlDryRunFileName = "${jenkinsModelXmlFileName}.dryrun"
	
	def mavenSettingFileName = "${mavenSettingPath}/settings.xml"
	
	def mavenSettingBackupFileName = "${mavenSettingFileName}${backupExtension}"
	
	def mavenSettingDryRunFileName = "${mavenSettingFileName}.dryrun"
	
	def graddlePropertiesFileName =  "${graddlePropertiesPath}/gradle.properties"
	
	def gradlePropertiesBackupFileName = "${graddlePropertiesFileName}${backupExtension}"
	
	def gradlePropertiesDryRunFileName = "${graddlePropertiesFileName}.dryrun"
	
	def targetSystemMappingOriContent
	
	def ConfigObject patchCliOriApplicationPropsOriContent
	
	def ConfigObject patchCliOpsPropsOriContent
	
	def ConfigObject patchServerApplicationPropsOriContent
	
	def ConfigObject patchServerOpsPropsOriContent
	
	def jenkinsConfixXmlOriContent
	
	def jenkinsModelXmlOriContent
	
	def mavenSettingsOriContent
	
	def gradlePropertiesOriContent
	
	def artifactoryRepoPropertiesOriContent
	
	def artifactoryPatchRepoPropertiesOriContent
	
	def jenkinsNodesPath = "src/test/resources/var/jenkins/nodes"
	
	def firstNodePath = "${jenkinsNodesPath}/n1"
	
	def secondNodePath = "${jenkinsNodesPath}/n2"
	
	def setup() {
		saveContentOfTestConfigFile()
		createFakeJenkinsNodes()
	}
	
	def cleanup() {
		cleanAllBackupFiles()
		cleanAllDryrunFile()
		restoreContentOfOriginalTestFiles()
		deleteFakeJenkinsNodes() // Because some test won't call the method which do it
	}
	
	def "PatchInitConfig validate behavior when no option has been passed"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			PrintStream oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process([])
			System.setOut(oldStream)
		then:
			notThrown(RuntimeException)
			result.returnCode == 0
			buffer.toString().contains(usageString)
	}
	
	def "PatchInitConfig validate help"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			PrintStream oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-h"])
			System.setOut(oldStream)
		then:
			notThrown(RuntimeException)
			result.returnCode == 0
			buffer.toString().contains(usageString)
	}
	
	def "PatchInitConfig validate init without providing initConfigFile"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			PrintStream oldStream = System.out;
			def buffer = new ByteArrayOutputStream()
			System.setOut(new PrintStream(buffer))
			def result = cli.process(["-i"])
			System.setOut(oldStream)
		then:
			notThrown(Exception)
			result.returnCode == 0
			buffer.toString().contains(usageString)
	}
	
	def "PatchInitConfig validate init with a non existing init config file"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-i","aWrongfile"])
		then:
			notThrown(Exception)
			result.returnCode == 1
	}
	
	def "PatchInitConfig validate init did the job for TargetSystemMapping File"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def targetSystemMappingFile = new File(targetSystemMappingFileName)
			def result = cli.process(["-dr", "false", "-i","src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
		then:
			result.returnCode == 0
			def targetSystemMappingBackupFile = new File(targetSystemMappingBackupFileName)
			targetSystemMappingBackupFile.exists()
			targetSystemMappingFile.exists()
			
			// Validate content of backup file
			def targetSystemMappingBackupFileContent = new JsonSlurper().parse(targetSystemMappingBackupFile)
			targetSystemMappingBackupFileContent.targetSystems.each({targetSystem -> 
				switch(targetSystem.name) {
					case "Entwicklung":
						Assert.that(targetSystem.target.equals("CHEI212"), "Target for Entwicklung should be CHEI212!")
						break
					case "Informatiktest":
						Assert.that(targetSystem.target.equals("CHTI211"), "Target for Informatiktest should be CHTI211!")
						break
					case "Produktion":
						Assert.that(targetSystem.target.equals("CHPI211"), "Target for Produktion should be CHPI211!")
						break
					default:
						Assert.fail("Default case is invalid, all target system should be known!")
						break
				}
			})
			targetSystemMappingBackupFileContent.otherTargetInstances.size == 6
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHEI211")
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHTI212")
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHTI216")
			targetSystemMappingBackupFileContent.otherTargetInstances.contains("CHTI215")
						
			// validate that content of newly saved file has been adapted accordingly
			def targetSystemMappingFileContent = new JsonSlurper().parse(targetSystemMappingFile)
			targetSystemMappingFileContent.targetSystems.each({targetSystem -> 
				switch(targetSystem.name) {
					case "Entwicklung":
						Assert.that(targetSystem.target.equals("CHEI212"), "New target for Entwicklung should be CHEI212!")
						break
					case "Informatiktest":
						Assert.that(targetSystem.target.equals("CHEI211"), "New target for Informatiktest should be CHEI211!")
						break
					case "Produktion":
						Assert.that(targetSystem.target.equals("CHEI211"), "New target for Produktion should be CHEI211!")
						break
					default:
						Assert.fail("Default case is invalid, all target system should be known!")
						break
				}
			})
			targetSystemMappingFileContent.otherTargetInstances.size == 3
			targetSystemMappingFileContent.otherTargetInstances.contains("CHEI211")
			targetSystemMappingFileContent.otherTargetInstances.contains("CHTI212")
			targetSystemMappingFileContent.otherTargetInstances.contains("CHTI213")
	}
	
	def "PatchInitConfig validate init did the job for all patch Service *.properties files"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def cliApplicationPropertiesFile = new File(patchCliApplicationPropertiesFileName)
			def cliApplicationPropertiesBackupFile = new File(patchCliApplicationPropertiesBackupFileName)
			def cliOpsPropertiesFile = new File(patchCliOpsPropertiesFileName)
			def cliOpsPropertiesBackupFile = new File(patchCliOpsPropertiesBackupFileName)
			def serverApplicationPropertiesFile = new File(patchServerApplicationPropertiesFileName)
			def serverApplicationPropertiesBackupFile = new File(patchServerApplicationPropertiesBackupFileName)
			def serveropsPropertiesFile = new File(patchServerOpsPropertiesFileName)
			def serveropsPropertiesBackupFile = new File(patchServerOpsPropertiesBackupFileName)
			def result = cli.process(["-dr", "false", "-i","src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
		then:
			result.returnCode == 0
			cliApplicationPropertiesFile.exists()
			cliApplicationPropertiesBackupFile.exists()
			cliOpsPropertiesFile.exists()
			cliOpsPropertiesBackupFile.exists()
			serverApplicationPropertiesFile.exists()
			serverApplicationPropertiesBackupFile.exists()
			serveropsPropertiesFile.exists()
			serveropsPropertiesBackupFile.exists()
		
			// Some files shouldn't have change at all
			FileUtils.contentEquals(serveropsPropertiesFile, serveropsPropertiesBackupFile)
			
			// apg-patch-cli application.properties + backup
			def cliAppProps = slurpProperties(cliApplicationPropertiesFile)
			cliAppProps.artifactory.dbpatch.repo.name == "dbpatch-test"
			cliAppProps.artifactory.release.repo.name == "releases-test"
			cliAppProps.patchRepoName == "yumpatchrepo-test"
			cliAppProps.mavenRepoUser == "dev-test"
			cliAppProps.mavenRepoName == "repo-test"
			def cliAppBackupProps = slurpProperties(cliApplicationPropertiesBackupFile)
			cliAppBackupProps.artifactory.dbpatch.repo.name == "dbpatch"
			cliAppBackupProps.artifactory.release.repo.name == "releases"
			cliAppBackupProps.patchRepoName == "yumpatchrepo"
			cliAppBackupProps.mavenRepoUser == "oldMavenRepoUser"
			cliAppBackupProps.mavenRepoName == "repo"
						
			
			// apg-patch-cli ops.properties + backup	
			def cliOpsProps = slurpProperties(cliOpsPropertiesFile)
			cliOpsProps.db.url == "jdbc:oracle:thin:@test.apgsga.ch:1521:test"
			cliOpsProps.db.user == "newDbUser"
			cliOpsProps.db.passwd == "newDbPasswd"
			def cliOpsBackupProps = slurpProperties(cliOpsPropertiesBackupFile)
			cliOpsBackupProps.db.url == "jdbc:oracle:thin:@prod.apgsga.ch:1521:prod"
			cliOpsBackupProps.db.user == "oldUser"
			cliOpsBackupProps.db.passwd == "oldPassword"
			
			// apg-patch-service-server application.properties + backup
			def serverApplicationProps = slurpProperties(serverApplicationPropertiesFile)
			serverApplicationProps.vcs.host == "cvs-t.apgsga.ch"
			serverApplicationProps.vcs.user == "newVcsUser"
			serverApplicationProps.jenkins.host == "https://jenkins-t.apgsga.ch/"
			serverApplicationProps.jenkins.user == "newJenkinsUser"
			serverApplicationProps.jenkins.authkey == "newJenkinsAuthKey"
			def serverApplicationBackupProps = slurpProperties(serverApplicationPropertiesBackupFile)
			serverApplicationBackupProps.vcs.host == "cvs.apgsga.ch"
			serverApplicationBackupProps.vcs.user == "cvsProdUser"
			serverApplicationBackupProps.jenkins.host == "https://jenkins.apgsga.ch/"
			serverApplicationBackupProps.jenkins.user == "jenkinsProdUser"
			serverApplicationBackupProps.jenkins.authkey == "jenkinsProdAuthKey"
	}
	
	def "PatchInitConfig validate init did the job for jenkins config.xml"() {
		when:
			def jenkinsConfigOriFile = new File(jenkinsConfigXmlFileName)
			def startConfig = new XmlSlurper().parse(jenkinsConfigOriFile)
		then:
			// Ensure we start from the original configuration ....
			Assert.that(startConfig.numExecutors.equals("12") , "Seems the config.xml file has not been re-initiliased.!")
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-dr", "false", "-i","src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
			def jenkinsConfigBackupFile = new File(jenkinsConfigXmlBackupFileName)
		then:
		    result.returnCode == 0
			jenkinsConfigOriFile.exists()
			jenkinsConfigBackupFile.exists()
			
			def newConfig = new XmlSlurper().parse(jenkinsConfigOriFile)
			
			Assert.that(newConfig.numExecutors.equals("5") , "numExecutor within Jenkins config.xml wrong!") 
			
			boolean iscvsFwRootOnNextIter = false
			boolean iscvsRootOnNextIter = false
			boolean isRepoRoPasswdOnNextIter = false
			boolean isDbPatchRepoOnNextIter = false
			boolean isReleasesPatchRepoOnNextIter = false
			newConfig.globalNodeProperties."hudson.slaves.EnvironmentVariablesNodeProperty".envVars."tree-map".string.size() > 0
			newConfig.globalNodeProperties."hudson.slaves.EnvironmentVariablesNodeProperty".envVars."tree-map".string.each({p -> 
				
				if(iscvsFwRootOnNextIter) {
					Assert.that(p.equals("ext:svcCvsClient@cvs-t.apgsga.ch:/var/local/cvs/root") , "CVS_FW_ROOT within Jenkins config.xml wrong!")
					iscvsFwRootOnNextIter = false
				}
				
				if(iscvsRootOnNextIter) {
					Assert.that(p.equals(":ext:svcCvsClient@cvs-t.apgsga.ch:/var/local/cvs/root") , "CVS_ROOT within Jenkins config.xml wrong!")
					iscvsRootOnNextIter = false
				}
				
				if(isRepoRoPasswdOnNextIter) {
					Assert.that(p.equals("newRepoRoPwd") , "REPO_RO_PASSWD within Jenkins config.xml wrong!")
					isRepoRoPasswdOnNextIter = false
				}
				
				if(isDbPatchRepoOnNextIter) {
					Assert.that(p.equals("dbpatch-test/") , "DB_PATCH_REPO within Jenkins config.xml wrong!")
					isDbPatchRepoOnNextIter = false
				}
				
				if(isReleasesPatchRepoOnNextIter) {
					Assert.that(p.equals("releases-test/") , "RELEASES_PATCH_REPO within Jenkins config.xml wrong!")
					isReleasesPatchRepoOnNextIter = false
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
				
				if(p.equals("RELEASES_PATCH_REPO")) {
					isReleasesPatchRepoOnNextIter = true
				}
			})
	}
	
	def "PatchInitConfig validate init job did the job for jenkins.model.JenkinsLocationConfiguration.xml"() {
		when:
			def jenkinsModelOriFile = new File(jenkinsModelXmlFileName)
			def startConfig = new XmlSlurper().parse(jenkinsModelOriFile)
		then:
			// Ensure we start from the original configuration ....
			Assert.that(startConfig.jenkinsUrl.equals("https://jenkins.apgsga.ch/") , "Seems the jenkins.model.JenkinsLocationConfiguration.xml file has not been re-initiliased.!")
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-dr", "false", "-i","src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
			def jenkinsModelBackupFile = new File(jenkinsModelXmlBackupFileName)
		then:
			result.returnCode == 0
			jenkinsModelBackupFile.exists()
			jenkinsModelOriFile.exists()
			
			def newConfig = new XmlSlurper().parse(jenkinsModelOriFile)
			
			Assert.that(newConfig.jenkinsUrl.equals("http://jenkins-t.apgsga.ch/"))
			
	}
	
	def "PatchInitConfig validate init job deleted all Jenkins Nodes"() {
		when:
			println "First don't do anything, but just verify that nodes exists before calling init cli ..."
		then:
			new File(firstNodePath).exists()
			new File(secondNodePath).exists()
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-dr", "false", "-i","src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
		then:
			result.returnCode == 0
			!(new File(firstNodePath).exists())
			!(new File(secondNodePath).exists())
	}
	
	def "PatchInitConfig validate init did the job for maven settings.xml"() {
		when:
			def mavenSettingFile = new File(mavenSettingFileName)
			def startConfig = new XmlSlurper().parse(mavenSettingFile)
		then:
			// Ensure we start from the original configuration ....
			Assert.that(startConfig.servers.server[0].password.equals("prodPass") , "Seems the settings.xml file has not been re-initiliased.!")
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-dr", "false", "-i","src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
			def mavenSettingBackupFile = new File(mavenSettingBackupFileName)
			def mavenSettings = new XmlSlurper().parse(mavenSettingFile)
		then:
			result.returnCode == 0
			mavenSettingFile.exists()
			mavenSettingBackupFile.exists()
			
			mavenSettings.servers.server.each({s -> 
				Assert.that(s.password.equals("newMavenUserpwd"), "Maven settings.xml, server pwd wrongly set for ${s.id}")
				Assert.that(s.username.equals("newMavenUsername"), "Maven settings.xml, server pwd wrongly set for ${s.id}")
				Assert.that(s.id in ["central","snapshots","releases"], "Maven settings.xml, id wrongly set for ${s.id}")
			})
			
			mavenSettings.profiles.size() == 1
			NodeChild defaultProfile = mavenSettings.profiles.getAt(0)
			Assert.that(defaultProfile != null)
			defaultProfile.profile.repositories.repository.each({NodeChild repo ->
				Assert.that(repo.id in ["snapshots-test","central-test"])
				Assert.that(repo.name in ["public-test","public-snapshots-test"])
				Assert.that(repo.url in ["https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/public-snapshots-test","https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/public-test"])
			})			

			defaultProfile.profile.pluginRepositories.pluginRepository.each({NodeChild pluginRepo ->
				Assert.that(pluginRepo.id in ["central-test","snapshots-test"])
				Assert.that(pluginRepo.name in ["public-test","public-snapshots-test"])
				Assert.that(pluginRepo.url in ["https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/public-test","https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/public-snapshots-test"])
			})
			
			defaultProfile.profile.id == "artifactory-test"
			mavenSettings.activeProfiles.getAt(0).activeProfile == "artifactory-test"
			
			defaultProfile.profile.properties.distributionManagementRepositoryUrl.equals("https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/releases-test")
			defaultProfile.profile.properties.snapshotRepositoryUrl.equals("https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/snapshots-test")
	}
	
	def "PatchInitConfig validate init did the job for gradle.properties"() {
		when:
			def gradlePropertiesFile = new File(graddlePropertiesFileName)
			def startConfig = slurpProperties(gradlePropertiesFile)
		then:
			// Ensure we start from the original configuration ....
			Assert.that(startConfig.patchRepoName.equals("yumpatchrepo"), "Seems the gradle.properties file has not been re-initiliased.!")
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-dr", "false", "-i","src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
			def gradlePropertiesBackupFile = new File(gradlePropertiesBackupFileName)
			def gradleProperties = slurpProperties(gradlePropertiesFile)
		then:
			gradlePropertiesBackupFile.exists()
			gradlePropertiesFile.exists()
			result.returnCode == 0
			Assert.that(gradleProperties.systemProp.http.connectionTimeout.equals("30000"))
			Assert.that(gradleProperties.systemProp.http.socketTimeout.equals("30000"))
			Assert.that(gradleProperties.mavenRepoUser.equals("newMavenRepoUser"))
			Assert.that(gradleProperties.mavenRepoBaseUrl.equals("https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga"))
			Assert.that(gradleProperties.mavenRepoName.equals("repo-test"))
			Assert.that(gradleProperties.mavenRepoPwd.equals("newMavenRepoPwd"))
			Assert.that(gradleProperties.patchRepoName.equals("yumpatchrepo-test"))
			Assert.that(gradleProperties.org.gradle.caching.equals("false"))
			Assert.that(gradleProperties.mavenReleasesRepoName.equals("releases-test"))
			Assert.that(gradleProperties.mavenSnapshotsRepoName.equals("snapshots-test"))
			Assert.that(gradleProperties.yumRepoDevName.equals("yumrepodev-test"))
	}
	
	def "PatchInitConfig validate dryRun option"() {
		when:
			// First scenario, we provide the -dr option
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-dr", "true", "-i", "src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
		then:
			// We simply validate that no backup file have been created, but that dryRun files exist
			result.returnCode == 0
			Assert.that(!(new File(targetSystemMappingBackupFileName).exists()))
			Assert.that(!(new File(patchCliApplicationPropertiesBackupFileName).exists()))
			Assert.that(!(new File(patchCliOpsPropertiesBackupFileName).exists()))
			Assert.that(!(new File(patchServerApplicationPropertiesBackupFileName).exists()))
			Assert.that(!(new File(patchServerOpsPropertiesBackupFileName).exists()))
			Assert.that(!(new File(jenkinsConfigXmlBackupFileName).exists()))
			Assert.that(!(new File(mavenSettingBackupFileName).exists()))
			Assert.that(!(new File(gradlePropertiesBackupFileName).exists()))
			Assert.that(!(new File(artifactoryPatchRepoConfigBackupFileName).exists()))
			Assert.that(!(new File(artifactoryRepoConfigBackupFileName).exists()))
			Assert.that(!(new File(jenkinsModelXmlBackupFileName).exists()))
			
			Assert.that((new File(targetSystemMappingDryRunFileName).exists()))
			Assert.that((new File(patchCliApplicationPropertiesDryRunFileName).exists()))
			Assert.that((new File(patchCliOpsPropertiesDryRunFileName).exists()))
			Assert.that((new File(patchServerApplicationPropertiesDryRunFileName).exists()))
			Assert.that((new File(patchServerOpsPropertiesDryRunFileName).exists()))
			Assert.that((new File(jenkinsConfigXmlDryRunFileName).exists()))
			Assert.that((new File(mavenSettingDryRunFileName).exists()))
			Assert.that((new File(gradlePropertiesDryRunFileName).exists()))
			Assert.that((new File(artifactoryPatchRepoConfigDryRunFileName).exists()))
			Assert.that((new File(artifactoryRepoConfigDryRunFileName).exists()))
			Assert.that((new File(jenkinsModelXmlDryRunFileName).exists()))
		when:
			// Second scenario, we don't provide -dr option, should be dryRun by default
			result = cli.process(["-i", "src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
		then:
			// We simply validate that no backup file have been created, but that dryRun files exist
			result.returnCode == 0
			Assert.that(!(new File(targetSystemMappingBackupFileName).exists()))
			Assert.that(!(new File(patchCliApplicationPropertiesBackupFileName).exists()))
			Assert.that(!(new File(patchCliOpsPropertiesBackupFileName).exists()))
			Assert.that(!(new File(patchServerApplicationPropertiesBackupFileName).exists()))
			Assert.that(!(new File(patchServerOpsPropertiesBackupFileName).exists()))
			Assert.that(!(new File(jenkinsConfigXmlBackupFileName).exists()))
			Assert.that(!(new File(mavenSettingBackupFileName).exists()))
			Assert.that(!(new File(gradlePropertiesBackupFileName).exists()))
			Assert.that(!(new File(artifactoryPatchRepoConfigBackupFileName).exists()))
			Assert.that(!(new File(artifactoryRepoConfigBackupFileName).exists()))
			Assert.that(!(new File(jenkinsModelXmlBackupFileName).exists()))
			
			Assert.that((new File(targetSystemMappingDryRunFileName).exists()))
			Assert.that((new File(patchCliApplicationPropertiesDryRunFileName).exists()))
			Assert.that((new File(patchCliOpsPropertiesDryRunFileName).exists()))
			Assert.that((new File(patchServerApplicationPropertiesDryRunFileName).exists()))
			Assert.that((new File(patchServerOpsPropertiesDryRunFileName).exists()))
			Assert.that((new File(jenkinsConfigXmlDryRunFileName).exists()))
			Assert.that((new File(mavenSettingDryRunFileName).exists()))
			Assert.that((new File(gradlePropertiesDryRunFileName).exists()))
			Assert.that((new File(artifactoryPatchRepoConfigDryRunFileName).exists()))
			Assert.that((new File(artifactoryRepoConfigDryRunFileName).exists()))
			Assert.that((new File(jenkinsModelXmlDryRunFileName).exists()))
	}
	
	def "PatchInitConfig validate yum repo configuration"() {
		when:
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-dr", "false", "-i", "src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])
		then:
			result.returnCode == 0
			Assert.that((new File(artifactoryPatchRepoConfigBackupFileName).exists()))
			Assert.that((new File(artifactoryRepoConfigBackupFileName).exists()))
			Assert.that((new File(artifactoryPatchRepoConfigFileName).exists()))
			Assert.that((new File(artifactoryRepoConfigFileName).exists()))
		when:
			def artifactoryPatchRepoConfigBackupFileContent = new File(artifactoryPatchRepoConfigBackupFileName).readLines()
			def artifactoryRepoConfigBackupFileContent = new File(artifactoryRepoConfigBackupFileName).readLines()
			def artifactoryPatchRepoConfigFileContent = new File(artifactoryPatchRepoConfigFileName).readLines()
			def artifactoryRepoConfigFileContent = new File(artifactoryRepoConfigFileName).readLines()
		then:
			Assert.that(artifactoryPatchRepoConfigBackupFileContent.contains("[apg-artifactory-patch]"))
			Assert.that(artifactoryPatchRepoConfigBackupFileContent.contains("gpgcheck=0"))
			Assert.that(artifactoryPatchRepoConfigBackupFileContent.contains("name=APG Artifactory Patch Repository"))
			Assert.that(artifactoryPatchRepoConfigBackupFileContent.contains("enabled=1"))
			Assert.that(artifactoryPatchRepoConfigBackupFileContent.contains("baseurl=https://ops:prodPassword@artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/yumpatchrepo"))
			
			Assert.that(artifactoryRepoConfigBackupFileContent.contains("[apg-artifactory]"))
			Assert.that(artifactoryRepoConfigBackupFileContent.contains("gpgcheck=0"))
			Assert.that(artifactoryRepoConfigBackupFileContent.contains("name=APG Artifactory Repository"))
			Assert.that(artifactoryRepoConfigBackupFileContent.contains("enabled=1"))
			Assert.that(artifactoryRepoConfigBackupFileContent.contains("baseurl=https://ops:prodPassword@artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/yumrepoprod"))
			
		
			Assert.that(artifactoryPatchRepoConfigFileContent.contains("[apg-artifactory-patch-test]"))
			Assert.that(artifactoryPatchRepoConfigFileContent.contains("gpgcheck=0"))
			Assert.that(artifactoryPatchRepoConfigFileContent.contains("name=APG Artifactory Patch Repository Test"))
			Assert.that(artifactoryPatchRepoConfigFileContent.contains("enabled=1"))
			Assert.that(artifactoryPatchRepoConfigFileContent.contains("baseurl=https://ops-test:newPassword@artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/yumpatchrepo-test"))
			
			Assert.that(artifactoryRepoConfigFileContent.contains("[apg-artifactory-test]"))
			Assert.that(artifactoryRepoConfigFileContent.contains("gpgcheck=0"))
			Assert.that(artifactoryRepoConfigFileContent.contains("name=APG Artifactory Repository Test"))
			Assert.that(artifactoryRepoConfigFileContent.contains("enabled=1"))
			Assert.that(artifactoryRepoConfigFileContent.contains("baseurl=https://ops-test:newPassword@artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/yumrepoprod-test"))
	}
	
	def "PatchInitConfig validate no IO Error when Backup file already exists"() {
		when:
			File f = new File(mavenSettingBackupFileName)
			Files.createFile(f.toPath())
			PatchInitConfigCli cli = PatchInitConfigCli.create()
			def result = cli.process(["-dr", "false", "-i", "src/test/resources/etc/opt/apg-patch-target-configinit/initconfig.properties"])

		then:
			notThrown(java.nio.file.FileAlreadyExistsException)
			result.returnCode == 0
	}
	
	private def slurpProperties(def propertyFile) {
		ConfigSlurper cs = new ConfigSlurper()
		
		def props = new Properties()
		propertyFile.withInputStream{ stream ->
			props.load(stream)
		}
		
		def config = cs.parse(props)
		config
	}
	
	private def saveContentOfTestConfigFile() {
		targetSystemMappingOriContent = new JsonSlurper().parse(new File(targetSystemMappingFileName))
		patchCliOriApplicationPropsOriContent = ConfigInitUtil.slurpProperties(new File(patchCliApplicationPropertiesFileName))
		patchCliOpsPropsOriContent = ConfigInitUtil.slurpProperties(new File(patchCliOpsPropertiesFileName))
		patchServerApplicationPropsOriContent = ConfigInitUtil.slurpProperties(new File(patchServerApplicationPropertiesFileName))
		patchServerOpsPropsOriContent = ConfigInitUtil.slurpProperties(new File(patchServerOpsPropertiesFileName))
		jenkinsConfixXmlOriContent = new XmlSlurper().parse(new File(jenkinsConfigXmlFileName))
		jenkinsModelXmlOriContent = new XmlSlurper().parse(new File(jenkinsModelXmlFileName))
		mavenSettingsOriContent = new XmlSlurper().parse(new File(mavenSettingFileName))
		gradlePropertiesOriContent = ConfigInitUtil.slurpProperties(new File(graddlePropertiesFileName))
		backupArtifactoryRepoFiles()
	}
	
	private def backupArtifactoryRepoFiles() {
		Files.copy(new File(artifactoryRepoConfigFileName).toPath(), new File("${artifactoryRepoConfigFileName}.save").toPath())
		Files.copy(new File(artifactoryPatchRepoConfigFileName).toPath(), new File("${artifactoryPatchRepoConfigFileName}.save").toPath())
	}
	
	private def restoreArtifactoryRepoFiles() {
		Files.delete(new File(artifactoryRepoConfigFileName).toPath())
		Files.move(new File("${artifactoryRepoConfigFileName}.save").toPath(), new File(artifactoryRepoConfigFileName).toPath())
		Files.delete(new File(artifactoryPatchRepoConfigFileName).toPath())
		Files.move(new File("${artifactoryPatchRepoConfigFileName}.save").toPath(), new File(artifactoryPatchRepoConfigFileName).toPath())
	}
	
	private def restoreContentOfOriginalTestFiles() {
		def targetSystemMappingFile = new File(targetSystemMappingFileName)
		targetSystemMappingFile.write(new JsonBuilder(targetSystemMappingOriContent).toPrettyString())
		
		Map<ConfigObject,File> configToBeRestored = [:]
		configToBeRestored.put(patchCliOriApplicationPropsOriContent, new File(patchCliApplicationPropertiesFileName))
		configToBeRestored.put(patchCliOpsPropsOriContent, new File(patchCliOpsPropertiesFileName))	
		configToBeRestored.put(patchServerApplicationPropsOriContent, new File(patchServerApplicationPropertiesFileName))
		configToBeRestored.put(patchServerOpsPropsOriContent, new File(patchServerOpsPropertiesFileName))
		configToBeRestored.put(gradlePropertiesOriContent, new File(graddlePropertiesFileName))
		
		configToBeRestored.each({configObj,file -> 
			PrintWriter pw = new PrintWriter(file)
			pw.write("")
			
			Properties props = new Properties()
			props.putAll(configObj.flatten())
			
			props.each({key,value ->
				pw.write("${key}=${value}")
				pw.write(System.getProperty("line.separator"))
			})
	
			pw.close()
		})
		
		XmlUtil xmlUtil = new XmlUtil()
		
		FileOutputStream jenkinsConfigXmlFos = new FileOutputStream(new File(jenkinsConfigXmlFileName))
		xmlUtil.serialize(jenkinsConfixXmlOriContent,jenkinsConfigXmlFos)
		jenkinsConfigXmlFos.close()
		
		FileOutputStream jenkinsModelXmlFos = new FileOutputStream(new File(jenkinsModelXmlFileName))
		xmlUtil.serialize(jenkinsModelXmlOriContent,jenkinsModelXmlFos)
		jenkinsModelXmlFos.close()
		
		FileOutputStream mavenFos = new FileOutputStream(new File(mavenSettingFileName))
		xmlUtil.serialize(mavenSettingsOriContent,mavenFos)
		mavenFos.close()
		
		restoreArtifactoryRepoFiles()
		
	}
	
	private def createFakeJenkinsNodes() {
		new File(firstNodePath).mkdirs()
		new File("${firstNodePath}/test1.txt").createNewFile()
		new File(secondNodePath).mkdirs()
		new File("${secondNodePath}/test2.txt").createNewFile()
	}
	
	private def deleteFakeJenkinsNodes() {
		new File(jenkinsNodesPath).deleteDir()
	}
	
	private def cleanAllBackupFiles() {
		new File(targetSystemMappingBackupFileName).delete()
		new File(patchCliApplicationPropertiesBackupFileName).delete()
		new File(patchCliOpsPropertiesBackupFileName).delete()
		new File(patchServerApplicationPropertiesBackupFileName).delete()
		new File(patchServerOpsPropertiesBackupFileName).delete()
		new File(jenkinsConfigXmlBackupFileName).delete()
		new File(mavenSettingBackupFileName).delete()
		new File(gradlePropertiesBackupFileName).delete()
		new File(artifactoryRepoConfigBackupFileName).delete()
		new File(artifactoryPatchRepoConfigBackupFileName).delete()
		new File(jenkinsModelXmlBackupFileName).delete()
	}
	
	private def cleanAllDryrunFile() {
		new File(targetSystemMappingDryRunFileName).delete()
		new File(patchCliApplicationPropertiesDryRunFileName).delete()
		new File(patchCliOpsPropertiesDryRunFileName).delete()
		new File(patchServerApplicationPropertiesDryRunFileName).delete()
		new File(patchServerOpsPropertiesDryRunFileName).delete()
		new File(jenkinsConfigXmlDryRunFileName).delete()
		new File(mavenSettingDryRunFileName).delete()
		new File(gradlePropertiesDryRunFileName).delete()
		new File(artifactoryRepoConfigDryRunFileName).delete()
		new File(artifactoryPatchRepoConfigDryRunFileName).delete()
		new File(jenkinsModelXmlDryRunFileName).delete()
	}
	
}
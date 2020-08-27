package com.cerner.automation;

import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.builder.DiffBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.*;
import java.nio.charset.StandardCharsets;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
//import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import org.xml.sax.SAXException;
import org.custommonkey.xmlunit.*;

/*SFTP package*/
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import difflib.*;


class Engine{
    //public int processRequestCont;
    //public String processDescr;
    //public String processUnitDescr;
    //public String processNum;
    //public String processUnitNum;
    // test comment RH025
    private Dictionary dict = new Hashtable();
    private Map<String,String> bindValue = new HashMap<String, String>();
    private String [] colKey = new String[]{"prcsID","prcsDescr","prcsSeqNum","prcsSeqDescr","driver","action","type","match","parameter","active","screenShot","onError"};
    //public String colValue;
    public String errorString = "NA";
    public String errorStringLong = "NA";
    public String screenShotName = " ";
    private String colDriver;
    private String colAction;
    private String colType;
    private String screenShotPath;
    private int prcsStatus;

    private String key = "eMxzpUKnLmW1qfOeb9cEeg==";
    WebDriver webDriver;
    private Map<String,String> workdayIntEvent = new HashMap<String, String>();

    // Test Upate

    public  Engine ()
    {

    }

    //private int XMLComparator(Dictionary dict) throws FileNotFoundException, SAXException, IOException {
    private int XMLComparator(String file1,String file2, String resultFile) throws FileNotFoundException, SAXException, IOException {
        //String files, file1,file2,resultFile;
        int mismatch=0;
        try{
          /*  files= dict.get("parameter").toString();
            List<String> para= new ArrayList<String>(Arrays.asList(files.split(",")));
            file1= para.get(0);
            file2= para.get(1);
            resultFile=para.get(2);*/
            // reading two xml file to compare in Java program
            FileInputStream inputStream1= new FileInputStream(file1);
            FileInputStream inputStream2= new FileInputStream(file2);
            File file = new File(resultFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            //FileOutputStream outputStream= new FileOutputStream(resultFile);-- RAC Updated as the file instance was not passed to output stream
            FileOutputStream outputStream= new FileOutputStream(file);
            System.out.println("resultFile:"+resultFile);

            // using BufferedReader for improved performance
            BufferedReader source = new BufferedReader(new InputStreamReader(inputStream1));
            BufferedReader target = new BufferedReader(new InputStreamReader(inputStream2));

            //configuring XMLUnit to ignore white spaces
            XMLUnit.setIgnoreWhitespace(true);

            //comparing two XML using XMLUnit in Java
            List differences =  compareXML(source, target);

            //showing differences found in two xml files
            printDifferences(differences, outputStream);

            if (!differences.isEmpty()) {
                throw new Exception("File Mismatch, Compare report is placed at " + resultFile);
            }
            // outputStream.close();
        }catch(Exception e){
            System.out.println(e);
            errorString = "Files do not match. Please review result file for details";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }

    private  List compareXML(Reader source, Reader target) throws SAXException, IOException{
        //System.out.println("INSIDE compareXML");
        //creating Diff instance to compare two XML files
        Diff xmlDiff = new Diff(source, target);

        //for getting detailed differences between two xml files
        DetailedDiff detailXmlDiff = new DetailedDiff(xmlDiff);

        return detailXmlDiff.getAllDifferences();

    }

    private  void printDifferences(List differences, FileOutputStream outputStream) throws IOException {

        int totalDifferences = differences.size();
        String newLine = System.getProperty("line.separator");
        byte[] nextLine = newLine.getBytes();

        System.out.println("===============================");
        System.out.println("Total differences : " + totalDifferences);
        System.out.println("================================");
        for(Object difference : differences){
            System.out.println("Difference :"+difference);
            String data=difference.toString();
            //System.out.println("Data"+data);
            byte[] dataDiffereces= data.getBytes(StandardCharsets.UTF_8);
            outputStream.write(dataDiffereces);
            outputStream.write(nextLine);


        }

    }


    public void setWebDriver(String driverType,String driverProp,String driverPath) throws MalformedURLException{
        System.out.println("---Setting up Web driver---");
        System.out.println(driverType+" "+driverPath+" "+driverProp);
        WebDriverFactory webDriverFactory = new WebDriverFactory();
        webDriver = webDriverFactory.setWebDriver(driverType,driverProp,driverPath);
    }

    public void setFileCompare(String fileType,String fileAPath,String fileBPath, String resultFilePath) throws MalformedURLException{
        System.out.println("---Setting up file driver---");
        System.out.println(fileType+" "+fileAPath+" "+fileBPath+" "+resultFilePath);
        FileDriverFactory fileDriverFactory = new FileDriverFactory();
        fileDriverFactory.setFileCompare(fileType,fileAPath,fileBPath,resultFilePath);
    }


    public void setScreenShotPath(String path){
        //System.out.println("--Setting up ScreenShot File path---");
        screenShotPath = path;
    }

    public int processRequest(ProcessData processData) throws Exception {
        copyHashTable(processData);
        prcsStatus = 1;

        //System.out.println(dict.get("prcsID")+" "+dict.get("prcsSeqNum"));
        colDriver = (String) dict.get("driver");
        colAction = (String) dict.get("action");
        colType = (String) dict.get("type");
        String driverProp = null;
        String driverPath = null;
        String logString = "======>"+dict.get("prcsSeqNum")+" : "+dict.get("prcsSeqDescr")+" : "+dict.get("driver");
        logString = logString +" : "+dict.get("action")+dict.get("type")+" : "+dict.get("match");
        logString = logString +" : "+dict.get("parameter")+" : "+dict.get("active");
        logString = logString +" : "+dict.get("screenShot")+" : "+dict.get("onError")+"<=====";


        //Navigate..
        //System.out.println("Driver : "+colDriver+" colAction : "+colAction);
        if(colDriver.equals("Web") || colDriver.equals("Mobile")) {
            //Setting up WebDriver
            if (dict.get("action").toString().equals("SetDriver")) {
                ClassLoader classLoader = getClass().getClassLoader();
                Properties prop = new Properties();
                FileInputStream input = null;
                try{
                    //input = new FileInputStream(classLoader.getResource("DriverConfig/WebDriverConfig.properties").getFile());
                    input = new FileInputStream("WebDriverConfig.properties");
                    //TempFix
                    //input = new FileInputStream("C:\\Users\\VC024129\\IdeaProjects\\Automation\\src\\Web.properties");
                    //input = new FileInputStream(new File("src\\main\\resources\\DriverConfig\\WebDriverConfig.properties").getAbsolutePath());
                    prop.load(input);
                }catch (Exception e){
                    System.out.println("Missing WebDriver Property file");
                    e.printStackTrace();
                }

                //Chrome
                if(dict.get("type").toString().equals("Chrome")){
                    driverProp = "webdriver.chrome.driver";
                    driverPath = prop.get("CHROME").toString();
                    System.out.println(driverPath);
                    setWebDriver("CHROME",driverProp,driverPath);
                }
                //FireFox
                if(dict.get("type").toString().equals("FireFox")){
                    driverProp = "webdriver.gecko.driver";
                    driverPath = prop.get("FIREFOX").toString();
                    setWebDriver("FIREFOX",driverProp,driverPath);
                }
                //MSFTEDGE
                if(dict.get("type").toString().equals("Edge")){

                    driverProp = "webdriver.edge.driver";
                    driverPath = prop.get("MSFTEDGE").toString();
                    setWebDriver("MSFTEDGE",driverProp,driverPath);

                }
                //MSFTIE
                if(dict.get("type").toString().equals("IE")){

                    driverProp = "webdriver.ie.driver";
                    driverPath = prop.get("MSFTIE").toString();
                    setWebDriver("MSFTIE",driverProp,driverPath);

                }
                //OPERA
                if(dict.get("type").toString().equals("Opera")){

                    driverProp = "webdriver.opera.driver";
                    driverPath = prop.get("OPERA").toString();
                    setWebDriver("OPERA",driverProp,driverPath);

                }
                //Safari
                // AndroidChrome
                if(dict.get("type").toString().equals("AndroidChrome")){
                    driverProp = "NA";
                    driverPath = "NA";
                    try {
                        setWebDriver("ANDROIDCHROME", driverProp, driverPath);
                    }catch (Exception e){
                        errorStringLong = e.getStackTrace().toString();
                        errorString = "Unable to initialize Mobile Chrome driver";
                        prcsStatus = 1;
                        return prcsStatus;
                    }
                }
                prcsStatus = 0;
            }
            if (dict.get("action").toString().equals("CloseDriver")) {
                System.out.println("In Close Driver");
                try {
                    webDriver.close();
                    prcsStatus = 0;
                }catch (Exception e){
                    errorString = "Unable to close webdriver";
                    errorStringLong = e.getStackTrace().toString();
                }

            }
            //System.out.println("In Web ***");
            if (dict.get("action").toString().equals("Navigate")) {
                //webDriver.get(dict.get("match").toString());
                //prcsStatus = webNavigate(webDriver, dict.get("match").toString(), dict.get("screenShot").toString());
                prcsStatus = webNavigateHandler(webDriver, dict);
            }
            //Window Event
            if (colAction.equals("Window")) {
                //System.out.println("In Window even handler");
                prcsStatus = windowEventHandler(webDriver, dict);
            }
            //Send Keys
            if (colAction.equals("SendKeys")) {
                //System.out.println("In Send keys handler");
                prcsStatus = sendKeysEventHandler(webDriver, dict);
            }

            //Send Keys Encrypted
            if (colAction.equals("SendKeysEncrypted")) {
                //System.out.println("In Send keys handler");
                prcsStatus = sendKeysEncryptionEventHandler(webDriver, dict);
            }
            // Send Text/Input
            //if (colAction.equals("SendKeys")) {
            // System.out.println("In Send keys handler");
            //    prcsStatus = sendKeysEventHandler(webDriver, key);
            //}
            //Clear
            if(colAction.equals("Clear")){
                //System.out.println("In Clear event");
                prcsStatus = clearEventHandler(webDriver,dict);
            }
            //Click Event
            if (colAction.equals("Click")) {
                //System.out.println("In Click handler");
                prcsStatus = clickEventHandler(webDriver, dict);
            }
            //Compare Event
            if (colAction.equals("Compare") && !dict.get("type").toString().equals("PageTitle")) {
                //System.out.println("In Compare handler");
                prcsStatus = compareEventHandler(webDriver, dict);
            }
            //Compare Page Title
            if (colAction.equals("Compare") && dict.get("type").toString().equals("PageTitle") ) {
                //System.out.println("In Compare Page Title");
                prcsStatus = comparePageTitle(webDriver, dict);
            }
            if(colAction.equals("CheckMinificaiton")){
                //System.out.println("In CheckMinificaiton");
                prcsStatus = checkMinification(webDriver,dict);
            }
            if(colAction.equals("CheckImageLoad")){
                //System.out.println("In CheckImageLoad");
                prcsStatus = checkImageLoad(webDriver,dict);
            }
            if(colAction.equals("CheckPageImageLoad")){
                //System.out.println("In CheckImageLoad");
                prcsStatus = checkPageImages(webDriver);
            }
            if(colAction.equals("Crawl")){
                prcsStatus = crawlLinks(webDriver);
            }

            if (colAction.equals("Hover")) {
                //System.out.println("In Compare handler");
                prcsStatus = hoverEventHandler(webDriver, dict);
            }
            if (colAction.equals("SwitchTab")) {
                //System.out.println("In Compare handler");
                prcsStatus = switchTab(webDriver, dict);
            }
            if (colAction.equals("IsDisplayed")) {
                prcsStatus = checkFieldIsPresent(webDriver, dict);
            }
            if (colAction.equals("CheckDropDownOptions")) {
                prcsStatus = checkDropDownOptions(webDriver, dict);
            }
            if (colAction.equals("Store")) {
                prcsStatus = storeBindValue(webDriver, dict);
            }
            //Accept Alert -
            if(colAction.equals("AcceptAlert")){
                prcsStatus = acceptAlert(dict);
            }
            //Dismiss Alert
            if(colAction.equals("DismissAlert")){
                prcsStatus = dismissAlert(dict);
            }
            //IsEnabled
            if(colAction.equals("IsEnabled")){
                prcsStatus=isElementEnabled(webDriver,dict);
            }
            //IsEnabled
            if(colAction.equals("SendText")){
                prcsStatus=sendTextEventHandler(webDriver,dict);
            }

            //IsEnabled
            if(colAction.equals("IsSelected")){
                prcsStatus=isElementSelected(webDriver,dict);
            }


            //GetTagName
            if(colAction.equals("CompareTagName")){
                prcsStatus= getTagName(webDriver,dict);
            }

            //GetText
            if(colAction.equals("CompareText")){
                //prcsStatus= getTagName(webDriver,dict);
            }

            // Submit
            if(colAction.equals("Submit")){
                //prcsStatus= submitEvent(webDriver,dict);
            }

            // GetAttribute and Compare
            if(colAction.equals("CompareAttribute")){
                prcsStatus= compareAttribute(webDriver,dict);
            }

            // GetSize and Compare
            if(colAction.equals("CheckSize")){
                //prcsStatus= compareSize(webDriver,dict);
            }
            // GetLocation and Compare
            if(colAction.equals("CheckLocation")){
                //prcsStatus= compareLocation(webDriver,dict);
            }
            // Get CSS Value
            if(colAction.equals("CheckCssValue")){
                //prcsStatus= compareCssValue(webDriver,dict);
            }


        }

        if(colDriver.equals("Time")){
            if(colAction.equals("DelayBy")){
                prcsStatus = timeDelayBy(dict);
            }
            if(colAction.equals("PrintDateTime")){
                prcsStatus = printDateTime(dict);
            }
        }

      /* RH025179 17/4/2018 if(colDriver.equals("File")){
            if(colAction.equals("FileCompare")) {
                if(colType.equals("XML")) {
                    prcsStatus =  XMLComparator(dict);
                }
            }
        }

        if(colDriver.equals("File")){
            if(colAction.equals("FileCompare")) {
                if(!(colType.equals("XML"))) {
                    prcsStatus = fileCompare(dict);
                }
            }
        }*/

        if(colDriver.equals("WebService")){
            if(colAction.equals("SOAPAPI")) {
                if(colType.equals("Request")) {
                    prcsStatus = SOAPWebservice(dict);
                }
            }
        }

       /* if(colDriver.equals("WebService")){
            if(colAction.equals("SOAPAPI")) {
                if(colType.equals("Response")) {
                    prcsStatus = GetIntStatus(dict);
                }
            }
        }*/

        if(colDriver.equals("SFTP")){
            if(colAction.equals("Download")) {
                prcsStatus = DownloadFilesSFTP(dict);

            }
        }

        if(colDriver.equals("SFTP")){
            if(colAction.equals("Upload"))  {
                prcsStatus = UploadFilesSFTP(dict);
            }
        }


        if(colDriver.equals("WebService")){
            if(colAction.equals("SOAPAPI")) {
                if(colType.equals("WorkdayIntegration")) {
                    prcsStatus = WorkdayIntegration(dict);
                    //   System.out.println("TESTING: TCDESCR  " +dict.get("match").toString());
                    //  System.out.println("TESTING: TCDESCR  " +dict.get("prcsDescr").toString());
                }
            }
        }

        if(colDriver.equals("File")){
            if(colAction.equals("FileCompare")) {
                if(colType.equals("folder")) {
                    prcsStatus = fileCompareFolder(dict);
                }
            }
        }

        if(colDriver.equals("File")){
            if(colAction.equals("FileCompare")) {
                if(colType.equals("downloadAndCompare")) {
                    Boolean diffCompare = false;
                    prcsStatus = fileDownloadAndCompare(dict, diffCompare);
                }
            }
        }

        if(colDriver.equals("File")){
            if(colAction.equals("diffFileCompare")) {
                if(colType.equals("downloadAndCompare")) {
                    Boolean diffCompare = true;
                    prcsStatus = fileDownloadAndCompare(dict, diffCompare);
                }
            }
        }

        if(colDriver.equals("Database")){
            if(colAction.equals("TableDiff")) {
                prcsStatus = tableDiff(dict);
            }
        }



        if(colDriver.equals("Encrypt")){
            try {
                System.out.println("----------------------------------------------------------");
                System.out.println(dict.get("parameter").toString() + " -> Encrypted String to -> "+encryptString(dict.get("parameter").toString(), stringToKey(key)));
                System.out.println("----------------------------------------------------------");
            }
            catch (Exception e){
                errorString = "Unable to Encrypt the given string";
                errorStringLong = e.toString();
                return 1;
            }


        }
        System.out.println(logString+"===>Return : "+prcsStatus);
        if(prcsStatus==1){
            if(webDriver != null)
                webDriver.navigate().refresh();
        }
        return prcsStatus;
    }

    private int tableDiff(Dictionary dict) throws Exception {

        String param = dict.get("parameter").toString();
        List<String> paramList = new ArrayList<String>(Arrays.asList(param.split(";")));
        System.out.println("param : "+param);

        String commandParam = paramList.get(0);
        String src = paramList.get(1);
        String srcPassword = paramList.get(2);
        String dest = paramList.get(3);
        String destPassword = paramList.get(4);

        srcPassword = decryptString(stringToByte(srcPassword),stringToKey(key));
        destPassword = decryptString(stringToByte(destPassword),stringToKey(key));

        System.out.println("Command param from input file:" + commandParam);

        commandParam = commandParam + " " + src + " " + srcPassword + " " + dest + " " + destPassword;

        //  System.out.println("Table Difference using command:" + commandParam);

        try {
            Process process = Runtime.getRuntime().exec(commandParam);
            process.waitFor();
            System.out.println("TableDiff Completed");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Difference exception");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return 0;
    }



    private int fileDownloadAndCompare(Dictionary dict, boolean diffCompare) throws Exception {
        Boolean download=true, upload=false;
        //read data from match and param

        String match = dict.get("match").toString();
        List<String> matchList = new ArrayList<String>(Arrays.asList(match.split(";")));
        System.out.println("match : "+match);

        String params = dict.get("parameter").toString();
        List<String> paramList = new ArrayList<String>(Arrays.asList(params.split(";")));
        System.out.println("params : "+params);

        String file1From = matchList.get(0);
        String file1Host = matchList.get(1);
        String file1Username = matchList.get(2);
        String file1Password = matchList.get(3);
        String file1SourcePath = matchList.get(4);
        String file1Path = matchList.get(5);
        String resFilePath = matchList.get(6);
        String compareApp = matchList.get(7);

        String file2From = paramList.get(0);
        String file2Host = paramList.get(1);
        String file2Username = paramList.get(2);
        String file2Password = paramList.get(3);
        String file2SourcePath = paramList.get(4);
        String file2Path = paramList.get(5);

        //Create folder integration name with date time stamp in destination file path
        DateFormat fileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date fileDate = new Date();
        String dynFolderName = fileDateFormat.format(fileDate);
        dynFolderName = "Int_Deliverable_file_"+dynFolderName;

        file1Path = file1Path+dynFolderName+"\\";
        file2Path = file2Path+dynFolderName+"\\";
        resFilePath = resFilePath+dynFolderName+"\\";
        System.out.println("file1Path :"+file1Path+"\nfile2Path :"+file2Path+"\nresFilePath :"+resFilePath);
        Thread.sleep(2000);

        if(new File(file1Path).mkdirs() && new File(file2Path).mkdirs() && new File(resFilePath).mkdirs() ) {
            System.out.println("Directory " + dynFolderName + " created at " + file1Path + " and " + file2Path + " and " + resFilePath);
        }
        else {
            System.out.println("Failed to create Directory ");
        }


        //download files to newly created folders
        System.out.println("file1Host"+file1Host+"\nfile1Username:"+file1Username+"\nfile1Password:"+file1Password+"\nfile1SourcePath:"+file1SourcePath+"\nfile1Path:"+file1Path);
        int ret = SFTPCall(file1Host,file1Username,file1Password,file1SourcePath,file1Path,upload,download);

        int ret2 = SFTPCall(file2Host,file2Username,file2Password,file2SourcePath,file2Path,upload,download);

        //compare downloaded files
        int ret3 = multiFileCompare(file1Path,file2Path,resFilePath,file1From,file2From,diffCompare, compareApp);
        int retval =0;
        //if(ret ==1 || ret2==1 || ret3==1 ){ retval =1;}
        if(ret ==1 || ret2==1 || ret3!=0 ){ retval =1;}
        return retval;

    }

    private int     fileCompareFolder(Dictionary dict) throws InterruptedException {

        int ret=0;
        //Get parameters

        String compareParam = dict.get("parameter").toString();
        List<String> paramList = new ArrayList<String>(Arrays.asList(compareParam.split(";")));

        String sourceFolder = paramList.get(0).trim();
        String targetFolder = paramList.get(1).trim();
        String resultFolder = paramList.get(2).trim();
        String file1 = "File1";
        String file2 = "File2";


        ret = multiFileCompare(sourceFolder,targetFolder,resultFolder,file1,file2,false," " );
        System.out.println("RET_Multifile:"+ret);
        // for testing one file     ret = fileCompare(sourceFolder,targetFolder,resultFolder);

        return ret;
    }

    private int multiFileCompare(String sourceFolder, String targetFolder, String resultFolder, String file1From, String file2From, boolean diffCompare, String compareApp) {
        //System.out.println("Inside multiFileCompare");
        int ret=0;
        try{
            //Validate if the above path are directory or file -- Pending

            //navigate to source folder path and get list of files
            // for each file in source folder get the file and check if same file exists in target folder and call file compare function based on file type
            // pass source file path, target file ath, and result file path to the file compare function
            String srcFilePath, targetFilePath, resFilePath;

            File srcFolder = new File(sourceFolder);
            File[] listOfFiles = srcFolder.listFiles();
            System.out.println("List of files:"+listOfFiles.length);
            File tarFolder= new File(targetFolder);
            File[] listofTFiles=  tarFolder.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {

                    srcFilePath = sourceFolder + listOfFiles[i].getName();
                    System.out.println("Source File " + i + ":  " + srcFilePath);
                    // targetFilePath = targetFolder + listOfFiles[i].getName();
                    targetFilePath = targetFolder + listofTFiles[i].getName();
                    System.out.println("Target FilePath " + i + ":  " + targetFilePath);
                    File targetFile = new File(targetFilePath);
                    File srcFile = new File(srcFilePath);
                    boolean targetFileExists = targetFile.exists();

                    System.out.println("targetFile.exists() = "+targetFile.exists());
                    String filename = listOfFiles[i].getName();
                    //System.out.println("FileName:"+filename);
                    //  Added the below lines of code to accommodate files with NO Extensions - Rachithra
                    if(filename.contains("\\.")){
                        System.out.println("Extension Present");
                        if (!diffCompare) {
                            resFilePath = resultFolder + filename.substring(0, filename.lastIndexOf(".")) + ".txt";
                            //resFilePath = resultFolder + filename.substring(0,filename.lastIndexOf("."))+".html";
                        }else {
                            resFilePath = resultFolder + filename.substring(0,filename.lastIndexOf("."))+".html";
                        }
                    }
                    else {
                        //System.out.println("NO Extension ");
                        if (!diffCompare) {
                            resFilePath = resultFolder + filename + ".txt";
                            //resFilePath = resultFolder + filename.substring(0,filename.lastIndexOf("."))+".html";
                        }else {
                            resFilePath = resultFolder + filename+".html";
                            System.out.println("Result File Path inside else: "+resFilePath);
                        }
                    }

                    //  Added the below lines of code in the above if-else condition to accommodate files with NO Extensions
                 /* if (!diffCompare) {
                        resFilePath = resultFolder + filename.substring(0, filename.lastIndexOf(".")) + ".txt";
                        //resFilePath = resultFolder + filename.substring(0,filename.lastIndexOf("."))+".html";
                    }else {
                        resFilePath = resultFolder + filename.substring(0,filename.lastIndexOf("."))+".html";
                        System.out.println("Result File Path inside else: "+resFilePath);
                    }
                 */   System.out.println("Result File Path: "+resFilePath);

                    if (targetFileExists) {
                        System.out.println("Target File " + i + ":  " + targetFilePath);
                        StringBuffer srcHash = SHAChecksum(srcFilePath);
                        StringBuffer targetHash = SHAChecksum(targetFilePath);

                        if(srcHash!=targetHash){
                            System.out.println("File Extension:"+srcFilePath.toUpperCase());
                            //check file extension and call respective file compare function
                            if (srcFilePath.toUpperCase().contains(".XML") ) {
                                //call xmlcompare
                                ret= XMLComparator(srcFilePath,targetFilePath,resFilePath);

                            }
                            // if (!srcFilePath.toUpperCase().contains(".xml")) { Removed as condition is redundant

                            if (!srcFilePath.toUpperCase().contains(".XML") ) {  //Replaced with else to accommodate no Extension files
                                //else{
                                //call flat file compare
                                if (!diffCompare) {
                                    System.out.println("diffCompare: " + diffCompare);
                                    ret = fileCompare(srcFilePath, targetFilePath, resFilePath, file1From, file2From);
                                }else {
                                    System.out.println("diffCompare - " + diffCompare);
                                    ret = diffFileCompare(srcFilePath, targetFilePath, resFilePath, file1From, file2From, compareApp);
                                    System.out.println("retDiff:"+ret);
                                }
                            }
                        } else{
                            errorStringLong = errorStringLong+"|| File "+filename+"  match in source and target";
                            System.out.println("errorStringLong:   "+errorStringLong);
                        }
                    } else {
                        String CompareError = "File does not exist in target file path: " + targetFolder;
                        System.out.println(CompareError);
                        throw new Exception(CompareError);
                    }

                    if(ret!=0){
                        errorStringLong = errorStringLong+"|| File "+filename+"  don't match in source and target";
                        System.out.println("errorStringLong***********:   "+errorStringLong);
                        return ret;
                    }

                } else if (listOfFiles[i].isDirectory()) {
                    System.out.println("Directory :" + listOfFiles[i].getName());
                }
            }

        } catch(Exception e){
            System.out.println("Received error on compare: "+e);
            errorString = "Compare Failed";
            errorStringLong = e.toString();
            return 1;
        }

        return ret;
    }

    private int diffFileCompare(String srcFilePath, String targetFilePath, String resFilePath, String file1From, String file2From, String compareApp) {
        int returnValue=-1;
		//String command=compareApp+" "+srcFilePath+" "+targetFilePath+" "+resFilePath; ---> Command updated as below
        String command=compareApp+" "+srcFilePath+" "+targetFilePath+" -minimize -noninteractive -noprefs -cfg ReportFiles/ReportType=2 -cfg ReportFiles/IncludeFileCmpReport=1 -r -u -or "+resFilePath;
        System.out.println("Command :"+command);

       // try {
            /* Process process = Runtime.getRuntime().exec(command);
            //Executing a code block of a synchronized statement that synchronizes on the object.
            //synchronized (process) {
            process.waitFor();
            //process.wait();
            System.out.println("Compare report in html format is generated and saved at " + resFilePath);
            System.out.println("process.exitValue()"+process.exitValue());
            returnValue= process.exitValue();
            return returnValue;
            //}*/

            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                System.out.println("Compare report in html format is generated and saved at " + resFilePath );
            }
            catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 0;
        }


        private StringBuffer SHAChecksum(String filePath) throws IOException, NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(filePath);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<mdbytes.length;i++) {
                hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
            }

            System.out.println("Hash for file : "+filePath+" Hex format : " + hexString.toString());
            fis.close();

            return hexString;
        }

        //RH025179 6/12
        private int WorkdayIntegration(Dictionary dict) {

            //First: Web service request to Run the main integration and store reponse in unique file
            String filePath, reqXML = "", wdFilePath, wdWebServRespFile, currDate, webServGetIntStatus;
            String stsFilePath, intEventStatus="", intEventMessage="";
            String sftpServer, sftpUser,sftpPassword,sftpIDFilePath;

            try {

                String match = dict.get("match").toString();
                List<String> matchList = new ArrayList<String>(Arrays.asList(match.split(";")));

                reqXML =  dict.get("parameter").toString();

                String url = matchList.get(0);
                wdFilePath = matchList.get(1).trim();
                webServGetIntStatus =  matchList.get(2).trim();
                sftpServer= matchList.get(3).trim();
                sftpUser= matchList.get(4).trim();
                sftpPassword=matchList.get(5).trim();
                sftpIDFilePath=matchList.get(6).trim();

                System.out.println("Workday URL: "+url );
                System.out.println("Workday FilePath: "+wdFilePath);

                reqXML =  dict.get("parameter").toString();

                DateFormat fileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                Date fileDate = new Date();
                currDate = fileDateFormat.format(fileDate);
                wdWebServRespFile = wdFilePath+"WorkdayIntegration_"+currDate+".txt";
                stsFilePath = wdFilePath+"WorkdayIntegration Status_"+currDate+".txt";
                System.out.println("Integration Web Service Response File Name : "+wdWebServRespFile);


                String newLine = System.getProperty("line.separator");
                byte[] nextLine = newLine.getBytes();

                File intRespFile = new File(wdWebServRespFile);
                File intStsFile = new File(stsFilePath);

                FileOutputStream outputStream = new FileOutputStream(wdWebServRespFile);

                SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
                SOAPConnection soapConnection = soapConnectionFactory.createConnection();
                SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(reqXML), url);
                System.out.print("Response SOAP Message:");
                soapResponse.writeTo(System.out);
                SOAPBody responseBody = soapResponse.getSOAPBody();

                if (!responseBody.hasFault()){
                    soapResponse.writeTo(outputStream);
                    System.out.println("Integration launched successfully");

                    //Second: Web Service Request to get integration details for the above run integration using the unique response file generated and store this request's response in another unique file

                    //get the integration event ID from the input file that has main int event web service response message
                    String intEventID = getNodeValue(intRespFile,"wd:ID","wd:type","Background_Process_Instance_ID");
                    String intSys = getNodeValue(intRespFile,"wd:ID","wd:type","Integration_System_ID");

                    List<String> intSysID = new ArrayList<String>(Arrays.asList(intSys.split("/")));
                    String intSysName = intSysID.get(0);

                    System.out.println("Integration Event ID :" + intEventID);

                    if (intEventID!="") {
                        //replace the string with actual int event ID in the SOAP Request message to get int event status
                        webServGetIntStatus = webServGetIntStatus.replace("AAAAA", intEventID);

                        Calendar cal = Calendar.getInstance();
                        Calendar cal1 = Calendar.getInstance();
                        cal.add(Calendar.MINUTE, 120);
                        Date aheadTime = cal.getTime();
                        Date now = cal1.getTime();
                        System.out.println(" time added " + cal.getTime());
                        System.out.println(" current time " + now);

                        while (now.before(aheadTime)) {

                            FileOutputStream outputStream1 = new FileOutputStream(stsFilePath);

                            cal1 = Calendar.getInstance();
                            now = cal1.getTime();
                            System.out.println("inside while loop now" + now);
                            System.out.println("inside while loop" + aheadTime);
                            Thread.sleep(60000);

                            SOAPMessage soapResponse1 = soapConnection.call(createSOAPRequest(webServGetIntStatus), url);
                            System.out.println("Get integration status SOAP Response:");
                            soapResponse1.writeTo(System.out);
                            SOAPBody responseBody1 = soapResponse1.getSOAPBody();
                            soapResponse1.writeTo(outputStream1);
                            intEventStatus = getNodeValue(intStsFile, "wd:ID","wd:type", "Background_Process_Instance_Status_ID");
                            intEventMessage = getNodeValue(intStsFile, "wd:Message_Summary", "", "");
                            System.out.println("STS:"+intEventStatus+":END");
                            if (!responseBody1.hasFault()) {
                                if (!"Processing".equals(intEventStatus)) {
                                    System.out.println("Integration Completed with status: " + intEventStatus);
                                    errorString = "Integration -"+intSysName+"- Completed with status: " + intEventStatus;
                                    errorStringLong = intEventMessage;
                                    outputStream1.close();
                                    break;
                                }

                            } else {

                                throw new Exception(responseBody.getFault().getFaultString());

                            }
                            outputStream1.close();

                        }
                        //Third: If the integration is completed successfully, store the int event ID and status to a CSV file
                        //write int ID and int status to a csv file with int name
                        if (!"Processing".equals(intEventStatus)) {
                            String intFileName, intDetails;
                            intDetails = intEventID + "," + intEventStatus + "," + intSysName;
                            byte[] b = intDetails.getBytes(StandardCharsets.UTF_8);

                   /* intSys = getNodeValue(intRespFile,"wd:ID","wd:type","Integration_System_ID");

                    List<String> intSysID = new ArrayList<String>(Arrays.asList(intSys.split("/")));*/
                            intFileName = intSysName;
                            intFileName = wdFilePath + intFileName + ".csv";

                            File idFile = new File(intFileName);
                            if (!idFile.exists()) {
                                idFile.createNewFile();
                            }

                            FileOutputStream outputIDFile = new FileOutputStream(intFileName);
                            outputIDFile.write(b);

                            outputIDFile.close();

                            //Fourth: Upload the CSV file to respective SFTP path , all integrations except the download file integration
                            if (!intFileName.contains("INT_STU_FTP_INTEGRATION_OUTPUT")) {

                                int ret = SFTPCall(sftpServer, sftpUser, sftpPassword, intFileName, sftpIDFilePath, true, false);

                                System.out.println("SFTP Upload return: " + ret);
                            }
                        } else {

                            throw new Exception("Exceeded the time limit and integration status is "+ intEventStatus);
                        }

                    }

                } else {

                    throw new Exception(responseBody.getFault().getFaultString());
                    // SOAPFault fault = responseBody.getFault();
                    //   logger.error("Received SOAP Fault");
                    //   logger.error("SOAP Fault Code :" + fault.getFaultCode());
                    //   logger.error("SOAP Fault String :" + fault.getFaultString());

                }
                outputStream.close();

                soapConnection.close();


            }catch(Exception e){
                System.out.println("Integration Run Failed: "+e);
                errorString = "Integration Run Failed";
                errorStringLong = e.toString();
                return 1;
            }

            if (!"Completed".equals(intEventStatus)){
                return 1;
            }else{
                return 0;
            }
        }

        private int UploadFilesSFTP(Dictionary dict) throws Exception {
            String username,host,password,source,destination;
            Boolean download=false, upload=true;

            String params = dict.get("match").toString();
            List<String> paramList = new ArrayList<String>(Arrays.asList(params.split(";")));

            host = paramList.get(0);
            username = paramList.get(1);
            password = paramList.get(2);
            source = paramList.get(3);
            destination = paramList.get(4);

            int ret = SFTPCall(host,username,password,source,destination,upload,download);

            return ret;
        }

        private int DownloadFilesSFTP(Dictionary dict) throws Exception {
            String username,host,password,source,destination;
            Boolean download=true, upload=false;

            String params = dict.get("match").toString();
            List<String> paramList = new ArrayList<String>(Arrays.asList(params.split(";")));
            System.out.println("params : "+params);

            host = paramList.get(0);
            username = paramList.get(1);
            password = paramList.get(2);
            source = paramList.get(3);
            destination = paramList.get(4);

            int ret = SFTPCall(host,username,password,source,destination,upload,download);

            return ret;
        }


        private int SFTPCall(String host, String username, String password, String source, String destination, Boolean upload, Boolean download) throws Exception{

            JSch jsch = new JSch();
            Session session = null;
            try {
                System.out.println("SFTP Details:");
                System.out.println("SFTP Server:"+host);
                System.out.println("SFTP user:"+username);
                System.out.println("SFTP Password:"+password);
                System.out.println("SFTP Source:"+source);
                System.out.println("SFTP Destination:"+destination);
                System.out.println("SFTP Upload:"+upload);
                System.out.println("SFTP Download:"+download);

                session = jsch.getSession(username, host, 22);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(password);
                session.connect();

                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftpChannel = (ChannelSftp) channel;
                if(upload) {
                    sftpChannel.put(source, destination);
                }
                try {
                    if (download) {
                        //System.out.println("Inside SFTPcall method download");
                        sftpChannel.get(source, destination);
                    }
                }catch (Exception e)
                {
                    System.out.println("Exception :"+e);
                    e.printStackTrace();
                }


                sftpChannel.exit();
                channel.disconnect();
                session.disconnect();
            } catch (JSchException e) {
                e.printStackTrace();
                return 1;
            } catch (SftpException e) {
                e.printStackTrace();
                return 1;
            }

            return 0;
        }



        private int GetIntStatus(Dictionary dict) {
            String intFilePath,testFilePath,filePath, reqXML = "", resFilePath,Attr ="", Tag ="", intEventID, attrVal="",url="",intEventStatus="",intEventMessage="";

            try {
                String match = dict.get("match").toString();
                List<String> matchList = new ArrayList<String>(Arrays.asList(match.split(";")));

                reqXML =  dict.get("parameter").toString();

                filePath = matchList.get(0); //this file holds the main int event ID for which we need to get status of.
                Tag = matchList.get(1).trim();
                Attr = matchList.get(2).trim();
                attrVal = matchList.get(3).trim();
                resFilePath = matchList.get(4).trim(); //this is to store the response of the get integration event soap request
                url = matchList.get(5).trim();
                testFilePath = matchList.get(6).trim();
                intFilePath = matchList.get(7).trim();

                System.out.println("filePath: "+filePath );
                System.out.println("Tag: "+Tag );
                System.out.println("Attr: "+Attr );
                System.out.println("attrVal: "+attrVal );
                System.out.println("resFilePath: "+resFilePath);
                System.out.println("url: "+url);

                String newLine = System.getProperty("line.separator");
                byte[] nextLine = newLine.getBytes();

                File inputFile = new File(filePath); //holds the int event ID for main test integration
                if (!inputFile.exists() || inputFile.length()==0) {
                    throw new Exception("\r\n Input File "+ inputFile+ " doesn't exist or is empty to fetch Integration Event ID \r\n");
                }

                File resFile = new File(resFilePath); //holds response message for "get int detail" web service request.
                if (!resFile.exists()) {
                    resFile.createNewFile();
                }


                //get the integration event ID from the input file that has main int event web service response message
                intEventID = getNodeValue(inputFile,Tag,Attr,attrVal);

                if (intEventID!="") {
                    //replace the string with actual int event ID in the SOAP Request message to get int event status
                    reqXML = reqXML.replace("AAAAA", intEventID);

                    Calendar cal = Calendar.getInstance();
                    Calendar cal1 = Calendar.getInstance();
                    ;
                    cal.add(Calendar.MINUTE, 35);
                    Date aheadTime = cal.getTime();
                    Date now = cal1.getTime();
                    System.out.println(" time added " + cal.getTime());
                    System.out.println(" current time " + now);

                    while (now.before(aheadTime)) {

                        FileOutputStream outputStream = new FileOutputStream(resFilePath);
                        cal1 = Calendar.getInstance();
                        now = cal1.getTime();
                        System.out.println("inside while loop now" + now);
                        System.out.println("inside while loop" + aheadTime);
                        Thread.sleep(60000);

                        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
                        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
                        SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(reqXML), url);
                        System.out.print("Response SOAP Message:");
                        soapResponse.writeTo(System.out);
                        SOAPBody responseBody = soapResponse.getSOAPBody();
                        soapResponse.writeTo(outputStream);
                        intEventStatus = getNodeValue(resFile, Tag, Attr, "Background_Process_Instance_Status_ID");
                        intEventMessage = getNodeValue(resFile, "wd:Message_Summary", "", "");
                        System.out.println("STS:"+intEventStatus+":END");
                        soapConnection.close();
                        if (!responseBody.hasFault()) {
                            if (!"Processing".equals(intEventStatus)) {
                                System.out.println("Integration Completed with status: " + intEventStatus);
                                errorString = "Integration -"+intEventID+"- Completed with status: " + intEventStatus;
                                errorStringLong = intEventMessage;
                                outputStream.close();
                                break;
                            }

                        } else {

                            throw new Exception(responseBody.getFault().getFaultString());

                        }
                        outputStream.close();
                    }

                }
                //write int ID and int status to a csv file with int name
                String intSys,intFileName,intDetails ;
                intDetails = intEventID+","+intEventStatus;
                byte[] b = intDetails.getBytes(StandardCharsets.UTF_8);

                intSys = getNodeValue(inputFile,"wd:ID","wd:type","Integration_System_ID");

                List<String> intSysID = new ArrayList<String>(Arrays.asList(intSys.split("/")));
                intFileName= intSysID.get(0);
                intFilePath = intFilePath+intFileName+".csv";

                File idFile = new File(intFilePath);
                if (!idFile.exists()) {
                    idFile.createNewFile();
                }

                FileOutputStream outputIDFile = new FileOutputStream(intFilePath);
                outputIDFile.write(b);

                outputIDFile.close();
//this needs to be rewritten to call upload function
                JSch jsch = new JSch();
                Session session = null;
                try {
                    session = jsch.getSession("svcWorkday", "ftp3.cerner.com", 22);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.setPassword("test1234");
                    session.connect();

                    Channel channel = session.openChannel("sftp");
                    channel.connect();
                    ChannelSftp sftpChannel = (ChannelSftp) channel;
                    sftpChannel.put(intFilePath, testFilePath);
                    sftpChannel.exit();
                    channel.disconnect();
                    session.disconnect();
                } catch (JSchException e) {
                    e.printStackTrace();
                    // return 1;
                } catch (SftpException e) {
                    e.printStackTrace();
                    //return 1;
                }



            }catch(Exception e){
                System.out.println("\r\n Error getting integration event status: "+e);
                errorString = "Error Getting integration event status";
                errorStringLong = e.toString();
                return 1;
            }

            if (!"Completed".equals(intEventStatus)){
                return 1;
            }else{


                return 0;
            }


        }

        private String getNodeValue(File resFile, String tag, String attr, String attrVal) throws IOException, SAXException, ParserConfigurationException {
            String intEventID="";


            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            try {
                Document doc = builder.parse(resFile);
                NodeList nodeList = doc.getElementsByTagName(tag);

                if(nodeList != null && nodeList.getLength() > 0){
                    for (int j = 0; j < nodeList.getLength(); j++) {
                        Element el = (Element) nodeList.item(j);

                        System.out.println("NodeList Element Content: "+nodeList.item(j).getTextContent()+"\r\n");

                        if (el.hasAttribute(attr) && el.getAttribute(attr).equals(attrVal)) {
                            intEventID = el.getTextContent();
                            System.out.println(" Integration Event ID : "+intEventID);
                            return intEventID;
                        }
                        if(attr==""){

                            intEventID = intEventID + ", " + el.getTextContent();

                        }
                    }
                    return intEventID;
                }
                return intEventID;

            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return intEventID;

        }

        private int SOAPWebservice(Dictionary dict) throws Exception {
            String filePath, reqXML = "", resFilePath;

            try {

                String match = dict.get("match").toString();
                List<String> matchList = new ArrayList<String>(Arrays.asList(match.split(";")));

                reqXML =  dict.get("parameter").toString();



                String url = matchList.get(0);
                resFilePath = matchList.get(1).trim();

                System.out.println("URL: "+url );
                System.out.println("resFilePath: "+resFilePath);

                String newLine = System.getProperty("line.separator");
                byte[] nextLine = newLine.getBytes();

                File resFile = new File(resFilePath);
                if (!resFile.exists()) {
                    resFile.createNewFile();
                }

                FileOutputStream outputStream = new FileOutputStream(resFilePath);

                SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
                SOAPConnection soapConnection = soapConnectionFactory.createConnection();
                SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(reqXML), url);
                System.out.print("Response SOAP Message:");
                soapResponse.writeTo(System.out);
                SOAPBody responseBody = soapResponse.getSOAPBody();



                if (!responseBody.hasFault()){
                    soapResponse.writeTo(outputStream);
                    System.out.print("\r\n Integration launched successfully \r\n");
                } else {

                    throw new Exception(responseBody.getFault().getFaultString());
                    // SOAPFault fault = responseBody.getFault();
                    //   logger.error("Received SOAP Fault");
                    //   logger.error("SOAP Fault Code :" + fault.getFaultCode());
                    //   logger.error("SOAP Fault String :" + fault.getFaultString());

                }
                outputStream.close();
                soapConnection.close();


            }catch(Exception e){
                System.out.println("\r\n Received SOAP Fault: "+e);
                errorString = "Received SOAP Fault";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;


        }



        private SOAPMessage createSOAPRequest(String reqXML) throws Exception {
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage(null,new ByteArrayInputStream(reqXML.getBytes()));
            soapMessage.saveChanges();
            System.out.println("Request SOAP Message:");
            soapMessage.writeTo(System.out);
            System.out.println();
            return soapMessage;
        }



        private int fileCompare(String file1,String file2, String resultFile, String file1From, String file2From){

            try {

                System.out.println("Comparing files : " +file1+ " and "+file2+ ". Placing the results of compare at : " +resultFile);
                long start = System.nanoTime();

                File file = new File(resultFile);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream outputStream = new FileOutputStream(resultFile);

                String newLine = System.getProperty("line.separator");
                byte[] NL = newLine.getBytes();

                // Compute diff. Get the Patch object. Patch is the container for computed deltas.

                List<String> file1_lines = fileToLines(file1);
                List<String> file2_lines  = fileToLines(file2);

                difflib.Patch patch = DiffUtils.diff(file1_lines, file2_lines);

                String data = null,data1=null;

                for (difflib.Delta delta : patch.getDeltas()) {
                    System.out.println("RESULT :" + delta.getOriginal());
                    data =  file1From + " - "+(delta.getOriginal()).toString();
                    data1 = file2From + " - "+(delta.getRevised()).toString();
                    byte[] b = data.getBytes(StandardCharsets.UTF_8);
                    byte[] b1 = data1.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(b);
                    outputStream.write(NL);
                    outputStream.write(b1);
                    outputStream.write(NL);
                    outputStream.write(NL);

                }

                outputStream.close();
                long end = System.nanoTime();
                System.out.println("File Compare Execution time: " + (end - start) / 1000000 + "ms");

                if ( data != null) {
                    throw new Exception("Files Mismatch, Compare report is placed at " + resultFile);
                }

            }catch(Exception e){
                System.out.println(e);
                errorString = "Files do not match. Please review result file for details";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;
        }


        private List<String> fileToLines(String filename) {
            List<String> lines = new LinkedList<String>();
            String line = "";
            try {
                BufferedReader in = new BufferedReader(new FileReader(filename));
                while ((line = in.readLine()) != null) {
                    lines.add(line);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lines;
        }

   /* private int fileCompare(Dictionary dict){
        String files, file1,file2,resultFile,string1,string2;
        int mismatch=0;
        try{
            files= dict.get("parameter").toString();
            List<String>para= new ArrayList<String>(Arrays.asList(files.split(",")));
            file1= para.get(0);
            file2= para.get(1);
            resultFile=para.get(2);
            File file = new File(resultFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            FileInputStream inputStream1= new FileInputStream(file1);
            FileInputStream inputStream2= new FileInputStream(file2);
            Scanner scanner1=new Scanner(inputStream1);
            Scanner scanner2= new Scanner(inputStream2);
            System.out.println("Scanner initialized");
            int lineNum = 1;
            while(scanner1.hasNext() || scanner2.hasNext()) {
                if (scanner1.hasNext() != false)
                    string1 = scanner1.nextLine();
                else string1 ="";
                if (scanner2.hasNext() != false)
                    string2 = scanner2.nextLine();
                else string2 ="";
                System.out.println("S1 :" + string1);
                System.out.println("S2 : " + string2);
                if(!(string1.equals(string2))){
                    mismatch+=1;
                    System.out.println(string1+" != "+string2);
                    bw.append("Mismatched string on line number : "+lineNum+" And the Line from first file is: '" +string1+"', Line from second file is: '"+string2+"' "+"\r\n");
                    //bw.close();
                   // break;
                }
                lineNum++;
            }

            bw.close();

            //System.out.println("Mismatch :"+mismatch);
            if (mismatch>0) {
                throw new Exception("File Mismatch");
            }
            //   if(dict.get("screenShot").toString().equals("Y")){
            //     takeScreenshot(webdr);

            //}
        }catch(Exception e){
            errorString = "File doesn not match. Please review the files for the test case ";
            errorStringLong = e.toString();
            return 1;
        }
        return 0;
    }*/

        private void copyHashTable(ProcessData pd){


            dict.put(colKey[0],pd.testCaseID );
            dict.put(colKey[1],pd.testCaseDescr );
            dict.put(colKey[2],pd.seqNo );
            dict.put(colKey[3],pd.testDescr );
            dict.put(colKey[4],pd.driver );
            dict.put(colKey[5],pd.action );
            dict.put(colKey[6],pd.type );
            dict.put(colKey[7],pd.match );
            dict.put(colKey[8],pd.param );
            dict.put(colKey[9],pd.active );
            dict.put(colKey[10],pd.screenShot );
            dict.put(colKey[11],pd.onError );



        }


    /*private void copyHashTable(Row row){

        for (int i=0; i<row.getLastCellNum();i++)
        {
            dict.put(colKey[i],row.getCell(i).getRichStringCellValue().getString());
        }
    }*/

        private int webNavigateHandler(WebDriver webdr, Dictionary dict) throws InterruptedException{
            int returnFlag = 0;
            errorString = " ";
            errorStringLong =" ";
            //System.out.println("InWebNavigate");

            try {
                if(dict.get("type").toString().equals("Get")){
                    //webDriver.get(dict.get("match").toString());
                    webdr.get(dict.get("parameter").toString());
                }
                if(dict.get("type").toString().equals("Refresh")){
                    webdr.navigate().refresh();
                }

                if(dict.get("type").toString().equals("Forward")){
                    webdr.navigate().forward();
                }
                if(dict.get("type").toString().equals("Backward")){
                    webdr.navigate().back();
                }

                if (dict.get("screenShot").toString().equals("Y")) {
                    takeScreenshot(webdr);
                }

            }
            catch (Exception e){
                errorString = "Web Navigation Error";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            return returnFlag;
        }

        private int windowEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
            Dimension dimension;
            String jScript;
            try{

                if(dict.get("type").toString().equals("Maximize")){
                    webdr.manage().window().maximize();
                }
                if(dict.get("type").toString().equals("Minimize")){
                    int width=0,height =0;
                    String[] dimen = dict.get("parameter").toString().split(",");
                    width = Integer.parseInt(dimen[0]) ;
                    height = Integer.parseInt(dimen[1]) ;
                    dimension = new Dimension(width,height);
                    webdr.manage().window().setSize(dimension);
                }

                if(dict.get("type").toString().equals("Scroll")){
                    int width=0,height =0;
                    String[] dimen = dict.get("parameter").toString().split(",");
                    width = Integer.parseInt(dimen[0]) ;
                    height = Integer.parseInt(dimen[1]) ;
                    dimension = new Dimension(width,height);
                    JavascriptExecutor scroll = (JavascriptExecutor)webdr;
                    jScript = "scroll("+dimen[0]+","+dimen[1]+")";
                    scroll.executeScript(jScript,"");
                }

                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Window Max/Min Error";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;
        }

        private int isElementEnabled(WebDriver webdr, Dictionary dict) throws InterruptedException{
            boolean element;
            int returnFlag = 1;
            try{
                element = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString()).isEnabled();
                if(element==true){
                    System.out.println("====>Element is Enabled<====");
                    returnFlag = 0;
                }
                else {
                    System.out.println("====>Element NOT Enabled<===");
                    errorString = "====>Element NOT Enabled<===";
                    errorStringLong =  "====>Element NOT Enabled<===";
                    returnFlag = 1;
                }
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element is not enabled";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            return  returnFlag;
        }

        private int isElementSelected(WebDriver webdr, Dictionary dict) throws InterruptedException{
            boolean element;
            int returnFlag = 1;
            try{
                element = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString()).isSelected();
                if(element==true){
                    System.out.println("====>Element is Selected<====");
                    returnFlag = 0;
                }
                else {
                    System.out.println("====>Element NOT Selected<===");
                    errorString = "====>Element NOT Selected<===";
                    errorStringLong =  "====>Element NOT Selected<===";
                    returnFlag = 1;
                }
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element is not enabled";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            return  returnFlag;
        }

        private int submitEvent(WebDriver webdr, Dictionary dict) throws InterruptedException{

            WebElement webElement;
            int returnFlag = 1;
            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());

                if(webElement!=null){
                    webElement.submit();
                    System.out.println("====>Submitted successfully<====");
                    returnFlag = 0;
                }
                else {
                    System.out.println("====>Element NOT Found<===");
                    errorString = "====>Element NOT Found<===";
                    errorStringLong =  "====>Element NOT Found<===";
                    returnFlag = 1;
                }
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element is not enabled";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            return  returnFlag;
        }
        private int sendTextEventHandler(WebDriver webdr, Dictionary dict) throws InterruptedException {
            WebElement webElement;
            try {
                webElement = getWebElement(webdr, dict.get("type").toString(), dict.get("match").toString());
                webElement.sendKeys(dict.get("parameter").toString() + Keys.ENTER);
                // Take ScreenShot
                if (dict.get("screenShot").toString().equals("Y")) {
                    takeScreenshot(webdr);
                }
            } catch (Exception e) {
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;
        }

        private int sendKeysEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
            WebElement webElement;
            String param;
            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
                // Check if bind variable is passed in parameter
                if (dict.get("parameter").toString().substring(0,1).equals(":")){

                    param = bindValue.get(dict.get("parameter").toString());
                    System.out.println("sendKeysEventHandler -- param bind -- : "+param);
                }else{
                    param = dict.get("parameter").toString();
                    System.out.println("sendKeysEventHandler -- param non bind -- : "+param);
                }
                webElement.sendKeys(param);
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
        /*try{
            webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
            webElement.sendKeys(dict.get("parameter").toString());
            // Take ScreenShot
            if(dict.get("screenShot").toString().equals("Y")){
                takeScreenshot(webdr);
            }
        }catch (Exception e){
            errorString = "Element not found exception";
            errorStringLong = e.toString();
            return 1;
        }*/
            return 0;
        }

        private int sendKeysEncryptionEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
            WebElement webElement;
            String param;
            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
                param = dict.get("parameter").toString();
                webElement.sendKeys(decryptString(stringToByte(param),stringToKey(key)));
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }

            return 0;
        }

        private int clearEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
            WebElement webElement;
            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
                webElement.clear();
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;
        }

        private int clickEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
            WebElement webElement;
            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
                webElement.click();
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;
        }

        private int hoverEventHandler(WebDriver webdr, Dictionary dict)throws InterruptedException{
            WebElement webElement;
            Actions action= new Actions(webDriver);
            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
                action.moveToElement(webElement).build().perform();
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;
        }

        private int switchTab(WebDriver webdr, Dictionary dict)throws InterruptedException{
            WebElement webElement;
            try{
                for(String windowHandle: webdr.getWindowHandles())
                {
                    webdr.switchTo().window(windowHandle);
                }
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;
        }

        private int comparePageTitle(WebDriver webdr, Dictionary dict) throws InterruptedException{
            WebElement webElement;
            int returnFlag = 1;
            try{
                // Check for Page Title
                if(dict.get("type").toString().equals("PageTitle")) {
                    if (webdr.getTitle().equals(dict.get("parameter").toString())) {
                        System.out.println("====>Title Matched<====");
                        returnFlag = 0;
                    } else {
                        System.out.println("====>Title NOT Matched<===");
                        errorString = "====>Title NOT Matched<===";
                        errorStringLong =  "====>Title NOT Matched<===";
                        returnFlag = 1;
                    }
                    if(dict.get("screenShot").toString().equals("Y")){
                        takeScreenshot(webdr);
                    }
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            return  returnFlag;
        }

        private int compareEventHandler(WebDriver webdr, Dictionary dict) throws InterruptedException{
            WebElement webElement;
            int returnFlag = 1;
            String param;
            System.out.println("CompareEventHandler");
            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
                // Check if bind variable is passed in parameter
                if (dict.get("parameter").toString().substring(0,1).equals(":")){

                    param = bindValue.get(dict.get("parameter").toString());
                    System.out.println("CompareEventHandler -- param bind -- : "+param);
                }else{
                    param = dict.get("parameter").toString();
                    System.out.println("CompareEventHandler -- param non bind -- : "+param);
                }
                if(webElement.getText().equals(param)){
                    System.out.println("====>String Matched<====");
                    returnFlag = 0;
                }
                else {
                    System.out.println("====>String NOT Matched<===");
                    errorString = "====>Compare string NOT Matched<===";
                    errorStringLong =  "====>Compare string NOT Matched<===";
                    returnFlag = 1;
                }
            /*if(webElement.getText().equals(dict.get("parameter").toString())){
                System.out.println("====>String Matched<====");
                returnFlag = 0;
            }
            else {
                System.out.println("====>String NOT Matched<===");
                errorString = "====>Compare string NOT Matched<===";
                errorStringLong =  "====>Compare string NOT Matched<===";
                returnFlag = 1;
            }*/
                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            return  returnFlag;
        }


        private int compareAttribute(WebDriver webdr, Dictionary dict) throws InterruptedException{
            WebElement webElement;
            int returnFlag = 1;
            String param;
            String[] attrib;
            System.out.println("CompareAttribute");
            try{

                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());

                // Check if bind variable is passed in parameter
                param = dict.get("parameter").toString();

                attrib = param.split(":");

                if(webElement.getAttribute(attrib[0]).equals(attrib[1])){
                    System.out.println("====>Attribute Matched<====");
                    returnFlag = 0;
                }
                else {
                    System.out.println("====>Attribute NOT Matched<===");
                    errorString = "====>Attribute NOT Matched<===";
                    errorStringLong =  "====>Attribute NOT Matched<===";
                    returnFlag = 1;
                }

                // Take ScreenShot
                if(dict.get("screenShot").toString().equals("Y")){
                    takeScreenshot(webdr);
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            return  returnFlag;
        }




        private int checkMinification(WebDriver webdr, Dictionary dict) throws MalformedURLException{
            String pageSource = " ";
            String returnMinfiResult = " ";
            String currentURL = " ";
            String assetURLForMsg = " ";
            List resourceURL = new ArrayList();
            int returnFlag = 0;
            int newLineCoutn = 0;
            currentURL = webdr.getCurrentUrl();
            String originalUrl = currentURL;
            System.out.print("Current URl : "+currentURL);
            currentURL = getDomainURL(currentURL);
            System.out.println("Current Domain URL :"+currentURL);
            //currentURL = "https://"+currentURL;

            // Read pageSource and find .css and .js files belong to Cerner.
            // load each item to list.
            // Get source for each list item and check for minification.
            // append the result to errorString.
            try {
                pageSource = webdr.getPageSource();
                System.out.println("Before getResourceURL");
                resourceURL = getResourceURL(pageSource);
                System.out.println("After getResourceURL");
                System.out.println("Resource URL "+ resourceURL);
                Iterator iterator = resourceURL.iterator();
                while (iterator.hasNext()){
                    String assetURL = iterator.next().toString();

                    if(assetURL.contains(".com")){
                        System.out.println("Contains .com");
                        //continue;
                    }
                    assetURLForMsg = assetURL;
                    assetURL = currentURL+assetURL;
                    System.out.println("Asset URL :"+ assetURL);
                    System.out.println("Navigat to Asset URL");
                    webdr.get(assetURL);
                    pageSource = webdr.getPageSource();
                    for (String str : pageSource.split("\n|\r")) {
                        newLineCoutn++;
                    }
                    returnMinfiResult = returnMinfiResult+assetURLForMsg +": "+"There are(is) "+ String.valueOf(newLineCoutn)+" CRLF >>>";

                    if(newLineCoutn > 2){

                        returnFlag = 1;
                    }
                }

                //returnMinfiResult = "There are(is) "+ String.valueOf(newLineCoutn)+" new line/carriage return character found";

            }catch (Exception e){
                errorString = "Minification issue";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            webdr.get(originalUrl);
            pageSource = webdr.getPageSource();
            for (String str : pageSource.split("\n|\r")) {
                newLineCoutn++;
            }
            returnMinfiResult = returnMinfiResult+assetURLForMsg +": "+"There are(is) "+ String.valueOf(newLineCoutn)+" new line/carriage return character found >>";
            errorString = "Minification Result";
            errorStringLong = returnMinfiResult;
            return returnFlag;
        }

        private List getResourceURL(String pageSource){
            System.out.println("Inside getResourceURL");
            List resultResourceURL = new ArrayList();
            String getCss ="href(\\s+=|=)(\\s+\"/|\"/).*?\\.css";
            String getJs ="src(\\s+=|=)(\\s+\"/|\"/).*?\\.js";
            String getQuotes = ".*=\"";
            Pattern css = Pattern.compile(getCss);
            Pattern js =  Pattern.compile(getJs);
            Pattern quotes = Pattern.compile(getQuotes);
            Matcher matchCss = css.matcher(pageSource);
            Matcher matchJs = js.matcher(pageSource);

            while (matchCss.find()){
                Matcher matchQuote = quotes.matcher(matchCss.group());
                resultResourceURL.add(matchQuote.replaceFirst(""));

                //resultResourceURL.add(matchCss.group());
            }
            while (matchJs.find()){
                Matcher matchQuote = quotes.matcher(matchJs.group());
                resultResourceURL.add(matchQuote.replaceFirst(""));
                //resultResourceURL.add(matchJs.group());
            }
            System.out.println("before return resultResourceURL "+resultResourceURL);
            return resultResourceURL;
        }

        private List getImageResourceURL(String pageSource){
            System.out.println("Inside getImageResourceURL");
            List resultImgResourceURL = new ArrayList();
            //String getImgUrl ="img.*src(\\s+=|=)(\\s+\"/|\"/|\").*?\\\"";
            String getImgUrl ="img(.*?)src=(.*?)\"(.*?)\"";
            String getSrc = "src.*=\".*?\"";
            String getQuote = "\".*?\"";
            Pattern imgUrlpattern = Pattern.compile(getImgUrl);
            Pattern imgSrc = Pattern.compile(getSrc);
            Pattern imgQuote = Pattern.compile(getQuote);
            Matcher imgMatch = imgUrlpattern.matcher(pageSource);
            while(imgMatch.find()){
                System.out.println("Inside Img page match");
                Matcher imgSrcMatch = imgSrc.matcher(imgMatch.group());
                System.out.println("Matched img : "+imgMatch.group().toString());
                System.out.println("Matched img scr : "+imgSrcMatch.find());
                System.out.println("Matched img scr group: "+imgSrcMatch.group());
                Matcher imgQuoteMatch = imgQuote.matcher(imgSrcMatch.group());
                System.out.println("Matched img scr quote -Find:"+ imgQuoteMatch.find());
                System.out.println("Match quote : "+ imgQuoteMatch.group());
                //System.out.println("Replace First : "+ imgQuoteMatch.group().replaceAll("\"",""));
                System.out.println("-----------------");

                resultImgResourceURL.add(imgQuoteMatch.group().replaceAll("\"",""));

            }

            return  resultImgResourceURL;

        }

        private String getDomainURL(String currentURL) throws MalformedURLException{
            URL domainURL=null;
            String hostUrl=" ";
            try{
                domainURL = new URL(currentURL);


            }catch (MalformedURLException e){
                System.out.println(e.getMessage());
            }
            hostUrl =  domainURL.getProtocol()+"://"+domainURL.getHost();
            System.out.println("hostUrl : "+hostUrl);
            return hostUrl;
        }

        private int checkImageLoad(WebDriver webdr,Dictionary dict){
            int returnFlag = 0;
            WebElement webElement;
            Boolean imageLodeStatus = Boolean.FALSE;
            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
                imageLodeStatus = (Boolean) ((JavascriptExecutor)webdr).executeScript("return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0",webElement);
                if(imageLodeStatus==Boolean.TRUE){
                    return 0;
                }else{
                    errorString = "Image not loaded";
                    errorStringLong = "Image not loaded";
                    returnFlag = 1;
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            return returnFlag;
        }

        private int checkPageImages(WebDriver webdr) throws MalformedURLException{
            String pageSource = " ";
            //String returnMinfiResult = " ";
            String currentURL = " ";
            //String assetURLForMsg = " ";
            List imgResourceURL = new ArrayList();
            URL  urlImage = null;
            HttpURLConnection httpImge = null;
            int returnFlag = 0;
            currentURL = webdr.getCurrentUrl();
            String originalUrl = currentURL;
            System.out.print("Current URl : "+currentURL);
            currentURL = getDomainURL(currentURL);
            System.out.println("Current Domain URL :"+currentURL);
            try {
                pageSource = webdr.getPageSource();
                //System.out.println(pageSource);
                System.out.println("Before getResourceURL");
                imgResourceURL = getImageResourceURL(pageSource);
                Iterator imgIterator = imgResourceURL.iterator();
                while(imgIterator.hasNext()){
                    String imgAssetURL = imgIterator.next().toString();
                    System.out.println("Substring 0,2"+imgAssetURL.substring(0,2));
                    if(imgAssetURL.substring(0,2).equals("//")){
                        imgAssetURL = imgAssetURL.replaceFirst("//","http://");
                        System.out.println("after stripping "+imgAssetURL);

                    }
                    //System.out.println("Image Asset : "+imgAssetURL);
                    if(imgAssetURL.contains(".com")){
                        System.out.println("Contains .com");
                        //continue;
                        // webdr.get(imgAssetURL);
                        //System.out.println("-----------------");
                        //System.out.println(webdr.getPageSource());
                        //System.out.println("-----------------");
                    }
                    else {
                        imgAssetURL = currentURL + imgAssetURL;
                        //System.out.println("Asset URL :" + imgAssetURL);
                        //System.out.println("Navigat to Asset URL");
                        //webdr.get(imgAssetURL);
                        //System.out.println("-----------------");
                        //System.out.println(webdr.getPageSource());
                        //System.out.println("-----------------");
                    }

                    urlImage = new URL(imgAssetURL);
                    httpImge = (HttpURLConnection)urlImage.openConnection();
                    int imgStagus = httpImge.getResponseCode();
                    System.out.println("Image Status for URL "+imgAssetURL+" Status is "+imgStagus);
                    if(imgStagus != 200){
                        errorString = "Image Load issue";
                        errorStringLong = errorStringLong + "Image Status for URL "+imgAssetURL+" Status is "+"NOT LOADED"+"---";
                        returnFlag = 1;
                    }
                }


            }catch (Exception e){
                errorString = "Image Load issue";
                errorStringLong = e.toString();
                returnFlag = 1;
            }
            webdr.get(originalUrl);
            return returnFlag;
        }

        private int checkFieldIsPresent(WebDriver webdr, Dictionary dict) throws InterruptedException {
            WebElement element;
            int returnFlag = 1;
            try {
                element = getWebElement(webdr, dict.get("type").toString(), dict.get("match").toString());
                if (element.isDisplayed()) {
                    returnFlag = 0;
                } else {
                    errorString = "Field does not exist";
                    returnFlag = 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return returnFlag;

        }

        // Added By KV031709
        private int checkDropDownOptions(WebDriver webdr, Dictionary dict)
            throws InterruptedException {
            int returnFlag = 1, flag = 0;
            int noOfOptions = 0;
            String[] expectedOptions = null;
            try {
                String[] options = webdr.findElement(By.xpath(dict.get("match").toString())).getText().split("\n");
                noOfOptions = options.length;
                System.out.println("number=" + noOfOptions);
                expectedOptions = dict.get("parameter").toString().split(",");
                for (int j = 0; j < expectedOptions.length; j++) {
                    System.out.println(expectedOptions[j]);
                    if (expectedOptions[j].contains(options[j]))
                        flag = 0;
                    else
                        flag = flag + 1;
                }
                System.out.println("count=" + flag);
                if (flag == 0) {
                    returnFlag = 0;
                } else {
                    errorString = "Options does not match";
                    returnFlag = 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return returnFlag;
        }

        private int storeBindValue(WebDriver  webdr, Dictionary dict){
            WebElement webElement;
            String bindKey=null;

            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
                System.out.println(webElement.getTagName());
                System.out.println("Parameter : "+dict.get("parameter").toString());
                System.out.println("TextValue : "+ webElement.getText());
                bindKey = dict.get("parameter").toString();
                if(bindKey.substring(0,1).equalsIgnoreCase(":")){
                    bindValue.put(bindKey,webElement.getText());
                }else {
                    System.out.println("Invalid bind value, Valid Bind should start with ':' like :Bind1");
                }
                System.out.println(bindValue.get(bindKey));
                for(Map.Entry map: bindValue.entrySet()){
                    System.out.println(map.getKey()+" "+map.getValue());
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;

        }

        private int getTagName(WebDriver  webdr, Dictionary dict){
            WebElement webElement;
            String bindKey=null;

            try{
                webElement = getWebElement(webdr,dict.get("type").toString(),dict.get("match").toString());
                System.out.println(webElement.getTagName());
                System.out.println("Parameter : "+dict.get("parameter").toString());
                System.out.println("TextValue : "+ webElement.getText());
                bindKey = dict.get("parameter").toString();
                if(bindKey.substring(0,1).equalsIgnoreCase(":")){
                    bindValue.put(bindKey,webElement.getText());
                }else {
                    System.out.println("Invalid bind value, Valid Bind should start with ':' like :Bind1");
                }
                System.out.println(bindValue.get(bindKey));
                for(Map.Entry map: bindValue.entrySet()){
                    System.out.println(map.getKey()+" "+map.getValue());
                }
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;

        }

        //Alert handing - Accept Alert - Rachithra
        private int acceptAlert(Dictionary dict)throws InterruptedException{
            WebElement webElement;
            try{
                Alert alert= webDriver.switchTo().alert();
                System.out.println(alert.getText());
                alert.accept();
            }catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;
        }

        //Alert handing - Dismiss Alert - Rachithra
        private int dismissAlert(Dictionary dict)throws InterruptedException{
            WebElement webElement;
            try{
                Alert alert= webDriver.switchTo().alert();
                System.out.println(alert.getText());
                alert.dismiss();
            }
            catch (Exception e){
                errorString = "Element not found exception";
                errorStringLong = e.toString();
                return 1;
            }
            return 0;
        }

        private WebElement getWebElement(WebDriver webdr,String searchBy,String match){
            WebDriverWait wait = new WebDriverWait(webdr, 50);
            WebElement webElement = null;
            if(searchBy.equals("Xpath")){
                webElement = webdr.findElement(By.xpath(match));

                if (wait.until(ExpectedConditions.elementToBeClickable(webElement)).isDisplayed()) {
                    return webElement;
                }

            }
            if(searchBy.equals("ID")){
                webElement = webdr.findElement(By.id(match));
                if (wait.until(ExpectedConditions.elementToBeClickable(webElement)).isDisplayed()) {
                    return webElement;
                }
            }
            if(searchBy.equals("CSSSelector")){
                webElement = webdr.findElement(By.cssSelector(match));
                if (wait.until(ExpectedConditions.elementToBeClickable(webElement)).isDisplayed()) {
                    return webElement;
                }
            }
            if(searchBy.equals("ClassName")){
                webElement = webdr.findElement(By.className(match));
                if (wait.until(ExpectedConditions.elementToBeClickable(webElement)).isDisplayed()) {
                    return webElement;
                }
            }
            if(searchBy.equals("LinkText")){
                webElement = webdr.findElement(By.linkText(match));
                return webElement;
            }

            return webElement;
        }

        private int crawlLinks(WebDriver webdr) throws MalformedURLException,ParserConfigurationException, TransformerException, IOException {
            String pageSource;
            String currentURL;
            int URLResponse =0;
            int returnFlag = 0;
            String xmlCrawlFilePath,xslCrawlFilePath,htmlCrawlFilePath;
            DocumentBuilderFactory crawldbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder crawlDocBuilder = crawldbFactory.newDocumentBuilder();
            Document crawlDocument = crawlDocBuilder.newDocument();
            Element crawlRootElement = crawlDocument.createElement("CrawlResult");
            crawlDocument.appendChild(crawlRootElement);
            List crawlURLList = new ArrayList();
            URL crawlURL = null;
            HttpURLConnection httpCrawlink = null;
            DateFormat autoDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            DateFormat htmlFileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date fileDate = new Date();
            String autoFileName = htmlFileDateFormat.format(fileDate).toString();
            currentURL = webdr.getCurrentUrl();
            String originalUrl = currentURL;
            currentURL = getDomainURL(currentURL);
            //---
            Properties prop = new Properties();
            FileInputStream input = new FileInputStream("config.properties");
            prop.load(input);
            xmlCrawlFilePath = prop.getProperty("XMLCRAWL");
            xslCrawlFilePath = prop.getProperty("XSLTCRAWL");
            htmlCrawlFilePath = prop.getProperty("HTMLFILE");
            htmlCrawlFilePath = htmlCrawlFilePath+"/"+"Crawl_"+autoFileName+".html";
            input.close();
            //---

            try{
                pageSource = webdr.getPageSource();
                crawlURLList = getCrawlURLList(pageSource);
                Iterator crawlURLIterator = crawlURLList.iterator();
                while (crawlURLIterator.hasNext()){
                    String crawlLink = crawlURLIterator.next().toString();
                    System.out.println(crawlLink);
                    if(crawlLink.contains(".com")|crawlLink.contains("http:")|crawlLink.contains("https:")){

                    }else {
                        crawlLink = currentURL+crawlLink;
                    }
                    crawlURL = new URL(crawlLink);
                    //webdr.get(crawlLink);
                    try{
                        httpCrawlink = (HttpURLConnection)crawlURL.openConnection();
                        URLResponse = httpCrawlink.getResponseCode();
                        System.out.println("Status Code is : "+URLResponse);
                        if(URLResponse != 200){
                            errorString = "URL Crawl Error";
                            errorStringLong = errorStringLong + "URL "+crawlLink+" Status is "+"NOT LOADED"+"---";
                            returnFlag = 1;
                        }
                        Element xmlURLElement = crawlDocument.createElement("URL");
                        Element xmlURLLink = crawlDocument.createElement("URLLink");
                        Element xmlURLResponse = crawlDocument.createElement("Response");
                        xmlURLLink.appendChild(crawlDocument.createTextNode(crawlLink));
                        xmlURLResponse.appendChild(crawlDocument.createTextNode(String.valueOf(URLResponse)));
                        xmlURLElement.appendChild(xmlURLLink);
                        xmlURLElement.appendChild(xmlURLResponse);
                        crawlRootElement.appendChild(xmlURLElement);
                    }catch (Exception e){
                        Element xmlURLElement = crawlDocument.createElement("URL");
                        Element xmlURLLink = crawlDocument.createElement("URLLink");
                        Element xmlURLResponse = crawlDocument.createElement("Response");
                        xmlURLLink.appendChild(crawlDocument.createTextNode(crawlLink));
                        xmlURLResponse.appendChild(crawlDocument.createTextNode(String.valueOf(URLResponse)));
                        xmlURLElement.appendChild(xmlURLLink);
                        xmlURLElement.appendChild(xmlURLResponse);
                        crawlRootElement.appendChild(xmlURLElement);
                        System.out.println(e.getStackTrace().toString());
                        errorString = "Error" ;
                        errorStringLong = e.getStackTrace().toString();
                        e.printStackTrace();
                        returnFlag = 1;

                    }



                }
            }catch (Exception e){
                System.out.println(e.getStackTrace().toString());
                errorString = "Error" ;
                errorStringLong = e.getStackTrace().toString();
                e.printStackTrace();
                returnFlag = 1;
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(crawlDocument);
            //StreamResult crawlResult = new StreamResult(new File("C:\\Users\\VC024129\\Documents\\Vijay\\TestFrameWork\\ApplicationReports\\Crawl.xml"));
            StreamResult crawlResult = new StreamResult(new File(xmlCrawlFilePath));
            transformer.transform(source,crawlResult);
            TransformerFactory transformerFactoryHTML = TransformerFactory.newInstance();
            Transformer transformerHTMLCrawl = transformerFactoryHTML.newTransformer(new javax.xml.transform.stream.StreamSource(xslCrawlFilePath));
            transformerHTMLCrawl.transform(new javax.xml.transform.stream.StreamSource(xmlCrawlFilePath), new javax.xml.transform.stream.StreamResult(htmlCrawlFilePath));
            errorString = htmlCrawlFilePath;
            errorStringLong = "View the Crawl result by clicking the link";
            webdr.get(originalUrl);
            return returnFlag;
        }

        private List getCrawlURLList(String pageSource){
            List resultCrawlURL = new ArrayList();
            Set<String> tempSet = new HashSet<String>();
            String getAnchor = "<a(.*?)href=(.*?)\"(.*?)\"";
            String getHref = "href=(.*?)\"(.*?)\"";
            String getLink = "\"(.*?)\"";
            Pattern crawlURLAnchor = Pattern.compile(getAnchor);
            Pattern crawlURLHref = Pattern.compile(getHref);
            Pattern crawlURLLink = Pattern.compile(getLink);
            //System.out.println(pageSource);
            Matcher matchAnchor = crawlURLAnchor.matcher(pageSource);
            try{
                while (matchAnchor.find() ){
                    //System.out.println("Anchor : "+matchAnchor.group());
                    Matcher matchHref = crawlURLHref.matcher(matchAnchor.group());
                    matchHref.find();
                    //System.out.println("Href :"+matchHref.group());
                    Matcher matchURLLink = crawlURLLink.matcher(matchHref.group());
                    matchURLLink.find();
                    resultCrawlURL.add(matchURLLink.group().replaceAll("\"",""));
                }
                // Remove duplicates from the List
                tempSet.addAll(resultCrawlURL);
                resultCrawlURL.clear();
                resultCrawlURL.addAll(tempSet);
                // Remove invalid items
                Iterator iterator = resultCrawlURL.iterator();
                while (iterator.hasNext()){
                    String tmpStr = iterator.next().toString();
                    if(tmpStr.equals("#")| tmpStr.equals("#footer")|tmpStr.equals("#menu")|tmpStr.equals("#header")|tmpStr.equals("#main-cont")|tmpStr.equals("/")){
                        iterator.remove();
                    }
                }


            }catch (Exception e){
                e.printStackTrace();
            }


            return resultCrawlURL;
        }


        private int timeDelayBy(Dictionary dict)throws InterruptedException{
            //System.out.println("In DelayBy");
            String varTime;
            if(dict.get("action").toString().equals("DelayBy")){
                varTime = dict.get("parameter").toString();
                Thread.sleep(Long.valueOf(varTime));

            }
            return 0;
        }

        private int printDateTime(Dictionary dict){
            //System.out.println("In Print Date and Time");
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            errorString = dateFormat.format(date);
            errorStringLong = errorString;
            return 0;
        }

        private void takeScreenshot(WebDriver webdr)throws InterruptedException {
            Thread.sleep(4000);
            DateFormat fileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date fileDate = new Date();
            String autoFileName = fileDateFormat.format(fileDate).toString();
            File src = ((TakesScreenshot) webdr).getScreenshotAs(OutputType.FILE);
            try{
                screenShotName = dict.get("prcsID").toString()+"_"+ dict.get("prcsSeqNum")+"_screenShot_"+autoFileName+".png";
                String screenShotFileName = screenShotPath+"\\"+screenShotName;
                FileUtils.copyFile(src,new File(screenShotFileName));
            }catch (IOException e){
                errorString ="Unable to take ScreenShot";
                errorStringLong = e.toString();
            }

        }

        private byte[] stringToByte(String str){
            byte [] byteString;
            byteString = Base64.getDecoder().decode(str);
            return byteString;

        }

        private  String byteToString(byte[] bytes){
            String str;
            str = Base64.getEncoder().encodeToString(bytes);
            return  str;

        }

        private SecretKey stringToKey(String keyStr){
            byte[] decodeKey = stringToByte(keyStr);
            SecretKey secretKey = new SecretKeySpec(decodeKey,0,decodeKey.length,"AES");
            return  secretKey;
        }

        private String decryptString(byte[] byteCipherString, SecretKey secretKey) throws Exception{
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE,secretKey);
            byte[] decodedByte = aesCipher.doFinal(byteCipherString);
            return new String(decodedByte);
        }

        public String getBinvalue(String bValue){
            return bindValue.get(bValue);
        }

        public String encryptString(String str, SecretKey secretKey) throws Exception{
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] byteCipherText = aesCipher.doFinal(str.getBytes());
            return byteToString(byteCipherText);
        }

        public static String getEncryptedString(byte[] secKey){
            byte encoded[] = secKey;
            String encodedKey = Base64.getEncoder().encodeToString(encoded);
            return encodedKey;
        }

        public Node getXMLProcessNode(Document document,ProcessData processData,int returnValue,long execTime,String errorString,String errorStringLong,String screenShotName ){
            copyHashTable(processData);
            String activeRow,result = "NORUN";
            String shortErrorMsg = " ";
            activeRow =  dict.get("active").toString();
            if (activeRow.equals("A")){
                if(returnValue==0)
                    result = "PASS";
                else
                    result = "FAIL";

            }

            Element unitCase = document.createElement("TestUnit");
            Element seqNum = document.createElement("SeqNum");
            Element unitDescr = document.createElement("UnitDescr");
            Element unitActive = document.createElement("Active");
            Element unitResult = document.createElement("Result");
            Element errorStr = document.createElement("Exception");
            Element errorMsgStr = document.createElement("ExceptionMsg");
            Element screenShotNameElement = document.createElement("ScreenShotImageName");
            Element executionTime = document.createElement("ExecutionTime");
            seqNum.appendChild(document.createTextNode(dict.get("prcsSeqNum").toString()));
            unitDescr.appendChild(document.createTextNode(dict.get("prcsSeqDescr").toString()));
            unitActive.appendChild(document.createTextNode(dict.get("active").toString()));
            unitResult.appendChild(document.createTextNode(result));
            errorStr.appendChild(document.createTextNode(errorString));
            errorMsgStr.appendChild(document.createTextNode(errorStringLong));
            screenShotNameElement.appendChild(document.createTextNode(screenShotName));
            executionTime.appendChild(document.createTextNode((String.valueOf(execTime))));

            unitCase.appendChild(seqNum);
            unitCase.appendChild(unitDescr);
            unitCase.appendChild(unitActive);
            unitCase.appendChild(unitResult);
            unitCase.appendChild(errorStr);
            unitCase.appendChild(errorMsgStr);
            unitCase.appendChild(screenShotNameElement);
            unitCase.appendChild(executionTime);
            return unitCase;
        }

        public Node generateSummary(Document document){

            String summaryTestCaseID = " ";
            String summaryTestCaseDescr = " ";
            String summaryTestResult = " ";
            String summaryDTTM = " ";
            String summaryExecTimeStr = " ";

            long summaryExecTimeLong = 0;
            double totalExecTime = 0;
            int totalTestCase = 0;
            int passTestCase = 0;
            int noRunTestCase = 0;
            int failTestCase = 0;
            Element summaryElement = document.createElement("Summary");
            NodeList nodeList = document.getElementsByTagName("TestCase");
            System.out.println("===>Generating Test Execution Summary<===");
            //Reset Execution Time var

            for(int temp=0;temp < nodeList.getLength(); temp++){
                summaryExecTimeLong = 0;
                Element summaryTSTElement = document.createElement("SummaryTestCase");
                Node node = nodeList.item(temp);
                Element summaryElementTest=  (Element) node;
                summaryTestCaseID = summaryElementTest.getElementsByTagName("PrcsID").item(0).getTextContent();
                summaryTestCaseDescr = summaryElementTest.getElementsByTagName("PrcsDescr").item(0).getTextContent();
                summaryDTTM = summaryElementTest.getElementsByTagName("StartDTTM").item(0).getTextContent();

                //System.out.println(node.getNodeName());
                if( node.getNodeType() == Node.ELEMENT_NODE){
                    Element summaryElementTU = (Element) node;
                    NodeList summaryNodeList = summaryElementTU.getElementsByTagName("TestUnit");
                    for(int count=0; count< summaryNodeList.getLength();count++){
                        Node summaryNode = summaryNodeList.item(count);
                        if(summaryNode.getNodeType() == Node.ELEMENT_NODE){
                            Element resultElement = (Element) summaryNode;
                            summaryTestResult = resultElement.getElementsByTagName("Result").item(0).getTextContent();
                            if (summaryTestResult.equals("FAIL")|| summaryTestResult.equals("NORUN") ){
                                break;
                            }
                        }
                    }
                }
                // Sum up total execution time for Test Case
                if( node.getNodeType() == Node.ELEMENT_NODE){
                    Element summaryElementTU = (Element) node;
                    NodeList summaryNodeList = summaryElementTU.getElementsByTagName("TestUnit");
                    for(int count=0; count< summaryNodeList.getLength();count++){
                        Node summaryNode = summaryNodeList.item(count);
                        if(summaryNode.getNodeType() == Node.ELEMENT_NODE){
                            Element resultElement = (Element) summaryNode;
                            summaryExecTimeStr = resultElement.getElementsByTagName("ExecutionTime").item(0).getTextContent();
                            summaryExecTimeLong = summaryExecTimeLong + Long.parseLong(summaryExecTimeStr);
                        }
                    }
                }
                totalExecTime += summaryExecTimeLong;

                //System.out.println("TestCaseID : "+summaryTestCaseID+" TestCaseDescr : "+summaryTestCaseDescr+" TestResult : "+summaryTestResult);
                Element sumSeqNum = document.createElement("TestCaseSeqNum");
                Element sumTestCaseID = document.createElement("TestCaseID");
                Element sumTestCaseDescr = document.createElement("TestCaseDescr");
                Element sumResult = document.createElement("Result");
                Element sumDTTM = document.createElement("StartDTTM");
                Element sumExecTime = document.createElement("ExecutionTime");
                sumSeqNum.appendChild(document.createTextNode(( String.valueOf(temp+1))));
                sumTestCaseID.appendChild(document.createTextNode(summaryTestCaseID));
                sumTestCaseDescr.appendChild(document.createTextNode(summaryTestCaseDescr));
                sumResult.appendChild(document.createTextNode(summaryTestResult));
                sumDTTM.appendChild(document.createTextNode(summaryDTTM));
                sumExecTime.appendChild(document.createTextNode(String.valueOf(summaryExecTimeLong)));
                summaryTSTElement.appendChild(sumSeqNum);
                summaryTSTElement.appendChild(sumTestCaseID);
                summaryTSTElement.appendChild(sumTestCaseDescr);
                summaryTSTElement.appendChild(sumResult);
                summaryTSTElement.appendChild(sumDTTM);
                summaryTSTElement.appendChild(sumExecTime);
                summaryElement.appendChild(summaryTSTElement);
                //totalTestCase = temp + 1;
                if (summaryTestResult.equals("PASS")) passTestCase += 1;
                if(summaryTestResult.equals("FAIL")) failTestCase +=1;
                if(summaryTestResult.equals("NORUN")) noRunTestCase +=1;

            }
            totalTestCase = passTestCase + failTestCase + noRunTestCase;
            //totalExecTime =  (totalExecTime%3600)/60;
            int totalExecTimeWholeNum = (int)(totalExecTime % 3600)/60;
            double totalExecTimeFraction = (totalExecTime % 60)/100;
            //System.out.println("totalExecTime : "+totalExecTime);
            //System.out.println("totalExecTimeWholeNum : "+ totalExecTimeWholeNum);
            //System.out.println("totalExecTimeFraction : "+ totalExecTimeFraction);
            //totalExecTimeFraction = totalExecTimeFraction * 60;
            totalExecTime = totalExecTimeWholeNum + totalExecTimeFraction;
            System.out.println("==>totalExecTime : "+totalExecTime);
            DecimalFormat decimalFormat = new DecimalFormat("####.##");

            Element sumTotalTestCase = document.createElement("TotalTestCases");
            Element sumPassedTestCase = document.createElement("PassedTestCases");
            Element sumFailedTestCase = document.createElement("FailedTestCases");
            Element sumNoRunTestCase = document.createElement("NoRunTestCases");
            Element sumTotalExecTime = document.createElement("TotalExecutionTime");
            sumTotalTestCase.appendChild(document.createTextNode(String.valueOf(totalTestCase)));
            sumPassedTestCase.appendChild(document.createTextNode(String.valueOf(passTestCase)));
            sumFailedTestCase.appendChild(document.createTextNode(String.valueOf(failTestCase)));
            sumNoRunTestCase.appendChild(document.createTextNode(String.valueOf(noRunTestCase)));
            sumTotalExecTime.appendChild(document.createTextNode(decimalFormat.format(totalExecTime)));
            summaryElement.appendChild(sumTotalTestCase);
            summaryElement.appendChild(sumPassedTestCase);
            summaryElement.appendChild(sumFailedTestCase);
            summaryElement.appendChild(sumNoRunTestCase);
            summaryElement.appendChild(sumTotalExecTime);
            return summaryElement;
        }
    }


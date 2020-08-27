package com.cerner.automation;

import java.net.MalformedURLException;

/**
 * Created by RH025179 on 2/22/2018.
 */
class FileDriverFactory  {

    public void setFileCompare(String fileType, String fileAPath, String fileBPath, String resultPath) throws MalformedURLException {
        if (fileType.equals("XML")) {
            XMLCompare xmlCompare = new XMLCompare();

            xmlCompare.setFileCompareDriver(fileAPath, fileBPath, resultPath);
        }


    }
}
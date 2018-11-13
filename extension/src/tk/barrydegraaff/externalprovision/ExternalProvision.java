/*

Copyright (C) 2018  Barry de Graaff

The MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package tk.barrydegraaff.externalprovision;


import com.zimbra.cs.extension.ExtensionHttpHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.sql.*;

import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;

import org.json.JSONObject;
import org.json.XML;
import org.apache.commons.codec.digest.DigestUtils;

/*
 * Example request:
 * curl -k -d 'secret=iow4yae9ahdah6eruichahxahng7Oonahghei0ae&property=com_example_zimlet&username=user1@zimbradev.barrydegraaff.tk&value=com_example_properties:{"Username":"user1@email.com","Password":"apasswordhere","DelaySend":12}' -H "Content-Type: application/x-www-form-urlencoded" -X POST https://zimbradev/service/extension/externalprovision -v
 * */

public class ExternalProvision extends ExtensionHttpHandler {

    /**
     * The path under which the handler is registered for an extension.
     * * @return path
     */
    @Override
    public String getPath() {
        return "/externalprovision";
    }


    /**
     * Processes HTTP GET requests.
     *
     * @param req  request message
     * @param resp response message
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.getOutputStream().print("tk.barrydegraaff.externalprovision is installed. HTTP GET method is not supported");
    }

    /**
     * Processes HTTP POST requests.
     *
     * @param req  request message
     * @param resp response message
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {

            //Get the extension properties
            Properties prop = new Properties();
            String secret;
            String[] allowedProps;
            String[] allowedIps;
            try {
                FileInputStream input = new FileInputStream("/opt/zimbra/lib/ext/externalProvision/config.properties");
                prop.load(input);
                secret = prop.getProperty("secret");
                allowedProps = prop.getProperty("allowedProps").split(",");
                allowedIps = prop.getProperty("allowedIps").split(",");
                input.close();
                if (secret.length() < 10) {
                    responseWriter(resp, "Please set a secret > 9 characters in /opt/zimbra/lib/ext/externalProvision/config.properties");
                    return;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            if (Arrays.asList(allowedIps).contains(req.getRemoteAddr()) || Arrays.asList(allowedIps).contains("*")) {
                if (secret.equals(req.getParameter("secret"))) {
                    if (Arrays.asList(allowedProps).contains(req.getParameter("property"))) {

                        Account acct = Provisioning.getInstance().getAccountByName(req.getParameter("username"));
                        String[] userProps = acct.getZimletUserProperties();
                        ArrayList<String> newUserProps = new ArrayList<String>();
                        for (String userProp : userProps) {
                            if (!userProp.contains(req.getParameter("property"))) {
                                newUserProps.add(userProp);
                            }
                        }

                        newUserProps.add(req.getParameter("property") + ":" + req.getParameter("value"));
                        acct.setZimletUserProperties(newUserProps.toArray(new String[0]));
                        //responseWriter(resp, "old>" + Arrays.toString(userProps) + "\r\n\r\nnew>" + newUserProps.toString());
                        responseWriter(resp, "OK");
                    } else {
                        responseWriter(resp, "Property not allowed");
                    }
                } else {
                    responseWriter(resp, "Invalid secret");
                }
            } else {
                responseWriter(resp, "IP not allowed");
            }


        } catch (
                Exception ex) {
            responseWriter(resp, "Exception occurred");
            ex.printStackTrace();
        }

    }

    private void responseWriter(HttpServletResponse resp, String message) {
        try {

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setHeader("Content-Type", "text/html");
            resp.getWriter().write(message);

            resp.getWriter().flush();
            resp.getWriter().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

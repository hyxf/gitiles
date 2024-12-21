// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gitiles;


import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * main cli
 *
 * @author hyxf
 * @date 2018/7/20 pm 9:54
 */
public class MainCli {
    public static final String CMD = "gitiles";
    private static final int DEFAULT_PORT = 8088;
    private static final String ARG_IP = "ip";
    private static final String ARG_PORT = "port";
    private static final String ARG_DIR = "dir";
    private static final String ARG_TITLE = "title";
    private static final String ARG_URL = "url";
    private static final String ARG_PUSH = "push";


    public static void main(String[] args) {
//        run(new String[]{"-d", "/Users/seven/Mirror/gerrit", "-i", "127.0.0.1", "--push"});
        run(args);
    }

    private static void run(String[] args) {
        Options options = new Options();
        options.addOption("i", ARG_IP, true, "ip");
        options.addOption("p", ARG_PORT, true, "port");
        options.addRequiredOption("d", ARG_DIR, true, "git mirror directory");
        options.addOption("t", ARG_TITLE, true, "Web title");
        options.addOption("u", ARG_URL, true, "git url");
        options.addOption("s", ARG_PUSH, false, "support push?");
        //-----
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);

        CommandLineParser parser = new DefaultParser();
        try {
            Params params = new Params();
            CommandLine cmd = parser.parse(options, args);
            params.setDir(cmd.getOptionValue(ARG_DIR));
            String ip = cmd.getOptionValue(ARG_IP);
            if (StringUtils.isEmpty(ip)) {
                ip = getIP();
            }
            params.setIp(ip);
            int port = DEFAULT_PORT;
            if (cmd.hasOption(ARG_PORT)) {
                port = Integer.parseInt(cmd.getOptionValue(ARG_PORT));
            }
            params.setPort(port);
            String title = cmd.getOptionValue(ARG_TITLE);
            if (StringUtils.isEmpty(title)) {
                title = String.format("%s - %s", CMD, "git");
            }
            params.setTitle(title);
            String url = cmd.getOptionValue(ARG_URL);
            if (StringUtils.isEmpty(url)) {
                url = String.format("http://%s:%d/git/", ip, port);
            }
            params.setUrl(url);
            params.setPush(cmd.hasOption(ARG_PUSH));

            new DevServer(params).start();
        } catch (Exception e) {
            hf.printHelp(CMD, String.format("\n%s\n\n", CMD), options, "\nmake it easy!", true);
        }
    }

    /**
     * get local IP
     *
     * @return
     */
    public static String getIP() {
        InetAddress address = getLocalHostLANAddress();
        return address != null ? address.getHostAddress() : "127.0.0.1";
    }

    /**
     * get local InetAddress
     *
     * @return
     */
    private static InetAddress getLocalHostLANAddress() {
        try {
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        }
                    }
                }
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            return jdkSuppliedAddress;
        } catch (Exception e) {
            ///e.printStackTrace();
        }
        return null;
    }

    public static final class Params {
        private String dir;
        private String ip;
        private boolean push;

        public boolean isPush() {
            return push;
        }

        public void setPush(boolean push) {
            this.push = push;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        private int port;
        private String title;
        private String url;

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}

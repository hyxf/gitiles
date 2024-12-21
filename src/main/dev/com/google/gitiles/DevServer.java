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

import com.google.common.base.Strings;
import com.google.common.html.types.UncheckedConversions;
import com.google.gitiles.doc.HtmlSanitizer;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.gitiles.GitilesServlet.STATIC_PREFIX;
import static com.google.gitiles.Renderer.fileUrlMapper;
import static java.util.stream.Collectors.toList;

/**
 * Dev server
 */
class DevServer {
    private static final Logger log = LoggerFactory.getLogger(PathServlet.class);

    private static Config defaultConfig(MainCli.Params params) {
        Config cfg = new Config();
        String cwd = params.getDir();
        cfg.setString("gitiles", null, "basePath", cwd);
        cfg.setBoolean("gitiles", null, "exportAll", true);
        cfg.setString("gitiles", null, "baseGitUrl", params.getUrl());
        cfg.setString("gitiles", null, "siteTitle", params.getTitle());
        cfg.setString("gitiles", null, "canonicalHostName", new File(cwd).getName());
        return cfg;
    }

    private final Config cfg;
    private final Server httpd;
    private MainCli.Params params;

    DevServer(MainCli.Params params) throws IOException {
        Config cfg = defaultConfig(params);
        this.params = params;
        this.cfg = cfg;
        httpd = new Server(params.getPort());
        httpd.setHandler(handler());
    }

    void start() throws Exception {
        httpd.start();
        log.info(String.format("running at http://%s:%d/", params.getIp(), params.getPort()));
        httpd.join();
    }

    private Handler handler() throws IOException {
        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.addHandler(staticHandler());
        handlers.addHandler(appHandler());
        return handlers;
    }

    private Handler appHandler() {
        DefaultRenderer renderer = new DefaultRenderer(STATIC_PREFIX, Arrays.stream(cfg.getStringList("gitiles", null, "customTemplates")).map(fileUrlMapper()).collect(toList()), firstNonNull(cfg.getString("gitiles", null, "siteTitle"), "Gitiles"));
        String docRoot = cfg.getString("gitiles", null, "docroot");
        Servlet servlet;
        if (!Strings.isNullOrEmpty(docRoot)) {
            servlet = createRootedDocServlet(renderer, docRoot);
        } else {
            servlet = new GitilesServlet(cfg, renderer, null, null, null, null, null, null, null, new BranchRedirect());

        }

        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("");
        handler.addServlet(new ServletHolder(servlet), "/*");
        //----- support git http server
        GitServlet gitServlet = new GitServlet();
        gitServlet.setReceivePackFactory(new ReceivePackFactory<HttpServletRequest>() {
            @Override
            public ReceivePack create(HttpServletRequest req, Repository db) throws ServiceNotEnabledException {
                if (params.isPush()) {
                    String user = req.getRemoteUser();
                    if (StringUtils.isEmpty(user)) {
                        user = "anonymous";
                    }
                    return createFor(req, db, user);
                }
                throw new ServiceNotEnabledException();
            }

            private ReceivePack createFor(final HttpServletRequest req, final Repository db, final String user) {
                final ReceivePack rp = new ReceivePack(db);
                rp.setRefLogIdent(toPersonIdent(req, user));
                return rp;
            }

            private PersonIdent toPersonIdent(HttpServletRequest req, String user) {
                return new PersonIdent(user, user + "@" + req.getRemoteHost());
            }
        });
        ServletHolder servletHolder = new ServletHolder(gitServlet);
        Map<String, String> params = new HashMap<String, String>();
        params.put("base-path", cfg.getString("gitiles", null, "basePath"));
        params.put("export-all", cfg.getString("gitiles", null, "exportAll"));
        servletHolder.setInitParameters(params);
        handler.addServlet(servletHolder, "/git/*");
        //-----
        return handler;
    }

    private Handler staticHandler() throws IOException {
        ResourceHandler rh = new ResourceHandler();
        URL url = DevServer.class.getResource("/com/google/gitiles/static");
        Resource resource = null;
        try {
            resource = new PathResource(url);
        } catch (Exception e) {
            resource = JarResource.newResource(url);
        }
        rh.setBaseResource(resource);
        rh.setWelcomeFiles(new String[]{});
        rh.setDirectoriesListed(false);
        ContextHandler handler = new ContextHandler("/+static");
        handler.setHandler(rh);
        return handler;
    }

    private Servlet createRootedDocServlet(DefaultRenderer renderer, String docRoot) {
        File docRepo = new File(docRoot);
        FileKey repoKey = FileKey.exact(docRepo, FS.DETECTED);

        RepositoryResolver<HttpServletRequest> resolver = (req, name) -> {
            try {
                return RepositoryCache.open(repoKey, true);
            } catch (IOException e) {
                throw new RepositoryNotFoundException(repoKey.getFile(), e);
            }
        };

        HtmlSanitizer.Factory htmlSanitizer = HtmlSanitizer.DISABLED_FACTORY;
        if (cfg.getBoolean("markdown", "unsafeAllowUserContentHtmlInDevMode", false)) {
            log.warn("!!! Allowing unsafe user content HTML in Markdown !!!");
            htmlSanitizer = request -> rawUnsafeHtml ->
                    // Yes, this is evil. It's not known the input was safe.
                    // I'm a development server to test Gitiles, not a cop.
                    UncheckedConversions.safeHtmlFromStringKnownToSatisfyTypeContract(rawUnsafeHtml);
        }
        return new RootedDocServlet(resolver, new RootedDocAccess(docRepo), renderer, htmlSanitizer);
    }

    private class RootedDocAccess implements GitilesAccess.Factory {
        private final String repoName;

        RootedDocAccess(File docRepo) {
            if (Constants.DOT_GIT.equals(docRepo.getName())) {
                repoName = docRepo.getParentFile().getName();
            } else {
                repoName = docRepo.getName();
            }
        }

        @Override
        public GitilesAccess forRequest(HttpServletRequest req) {
            return new GitilesAccess() {
                @Override
                public Map<String, RepositoryDescription> listRepositories(String prefix, Set<String> branches) {
                    return Collections.emptyMap();
                }

                @Override
                public Object getUserKey() {
                    return null;
                }

                @Override
                public String getRepositoryName() {
                    return repoName;
                }

                @Override
                public RepositoryDescription getRepositoryDescription() {
                    RepositoryDescription d = new RepositoryDescription();
                    d.name = getRepositoryName();
                    return d;
                }

                @Override
                public Config getConfig() {
                    return cfg;
                }
            };
        }
    }
}

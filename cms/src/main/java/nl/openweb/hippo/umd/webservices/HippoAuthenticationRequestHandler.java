/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Modified by Ebrahim Aharpour
 * to integrate the authentication of the user with the CMS
 */

package nl.openweb.hippo.umd.webservices;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.jcr.LoginException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.ext.ResponseHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.session.ISessionStore;
import org.hippoecm.frontend.model.UserCredentials;
import org.hippoecm.frontend.session.PluginUserSession;
import org.onehippo.forge.webservices.AuthenticationConstants;
import org.onehippo.forge.webservices.jaxrs.exception.UnauthorizedException;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrSessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class HippoAuthenticationRequestHandler implements RequestHandler, ResponseHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HippoAuthenticationRequestHandler.class);
    private Session session = null;
    public static final String[] STREAMING_OUTPUT_SERVICES = new String[] { "getGroupsOverview", "getUsersOverview" };

    public Response handleRequest(Message m, ClassResourceInfo resourceClass) {
        ISessionStore sessionStore = Application.get().getSessionStore();
        Serializable attribute = sessionStore.getAttribute(
                new ServletWebRequest((HttpServletRequest) m.get(AbstractHTTPDestination.HTTP_REQUEST), ""), "session");
        if (attribute instanceof PluginUserSession) {
            UserCredentials userCredentials = ((PluginUserSession) attribute).getUserCredentials();
            if (userCredentials != null) {
                SimpleCredentials jcrCredentials = (SimpleCredentials) userCredentials.getJcrCredentials();
                String username = jcrCredentials.getUserID();
                String password = new String(jcrCredentials.getPassword());
                try {
                    session = JcrSessionUtil.createSession(username, password);
                    if (isAuthenticated()) {
                        HttpServletRequest request = (HttpServletRequest) m.get(AbstractHTTPDestination.HTTP_REQUEST);
                        request.setAttribute(AuthenticationConstants.HIPPO_SESSION, session);
                        return null;
                    } else {
                        throw new UnauthorizedException();
                    }
                } catch (LoginException e) {
                    LOG.debug("Login failed: {}", e);
                    throw new UnauthorizedException(e.getMessage());
                }
            }
        }
        throw new UnauthorizedException();
    }

    @Override
    public Response handleResponse(final Message m, final OperationResourceInfo ori, final Response response) {
        if (session != null && session.isLive()) {
            if (!(ori != null && ori.getMethodToInvoke() != null && isStreamingOutputServices(ori.getMethodToInvoke()))) {
                session.logout();
            }
            session = null;
        }
        return null;
    }

    public boolean isStreamingOutputServices(Method methodToInvoke) {
        boolean result = false;
        if (methodToInvoke != null) {
            String calledMethod = methodToInvoke.getName();
            for (String serviceMethod : STREAMING_OUTPUT_SERVICES) {
                if (serviceMethod.equals(calledMethod)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private boolean isAuthenticated() {
        return session != null;
    }
}
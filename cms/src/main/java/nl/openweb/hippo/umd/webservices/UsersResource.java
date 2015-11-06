package nl.openweb.hippo.umd.webservices;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.query.InvalidQueryException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import nl.openweb.hippo.umd.beans.CopyRequest;
import nl.openweb.hippo.umd.beans.GroupCopyResults;
import nl.openweb.hippo.umd.beans.UserBean;
import nl.openweb.hippo.umd.beans.UsersBean;
import nl.openweb.hippo.umd.utils.UserUtils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.cxf.annotations.GZIP;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrSessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordnik.swagger.annotations.Api;

@GZIP
@Api(value = "Users", description = "Users API", position = 7)
@Path("users/")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class UsersResource {

    private static final Logger LOG = LoggerFactory.getLogger(UsersResource.class);
    public static final CSVFormat CSV_FILE_FORMAT = CSVFormat.DEFAULT.withRecordSeparator("\n");

    @GET
    @Path("/users/overview")
    @Produces("text/csv")
    public Response getUsersOverview(@Context HttpServletRequest request) throws RepositoryException {
        final Session session = JcrSessionUtil.getSessionFromRequest(request);
        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try (CSVPrinter csvFilePrinter = new CSVPrinter(new PrintStream(output), CSV_FILE_FORMAT)) {
                    printUsersOverviewHeaders(csvFilePrinter);
                    List<String> defaultGroupMembers = UserUtils.getDefaultGroupMembers(session);
                    for (String member : defaultGroupMembers) {
                        NodeIterator userNodes = UserUtils.getUserNodeById(member, session);
                        printUsers(session, csvFilePrinter, userNodes);
                    }
                } catch (RepositoryException e) {
                    LOG.error(e.getMessage(), e);
                    throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
                } finally {
                    session.logout();
                }

            }

        };
        return Response.ok().header("Content-Disposition", "attachment; filename=Users-Overview.csv").entity(output)
                .build();
    }

    @GET
    @Path("/groups/overview")
    @Produces("text/csv")
    public Response getGroupsOverview(@Context HttpServletRequest request) throws RepositoryException {
        final Session session = JcrSessionUtil.getSessionFromRequest(request);
        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try (CSVPrinter csvFilePrinter = new CSVPrinter(new PrintStream(output), CSV_FILE_FORMAT)) {
                    printGroupsOverviewHeaders(csvFilePrinter);
                    NodeIterator groups = UserUtils.getGroups(session);
                    while (groups.hasNext()) {
                        Node group = groups.nextNode();
                        printGroup(group, session, csvFilePrinter);
                    }
                } catch (RepositoryException e) {
                    LOG.error(e.getMessage(), e);
                    throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
                } finally {
                    session.logout();
                }

            }

        };
        return Response.ok().header("Content-Disposition", "attachment; filename=Groups-Overview.csv").entity(output)
                .build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTargetUsers(@Context HttpServletRequest request) {
        Session session = JcrSessionUtil.getSessionFromRequest(request);
        try {
            List<UserBean> targetUser = getTargetUsers(session);
            List<UserBean> sourceUser = getSourceUsers(session);
            return Response.ok().entity(new UsersBean(sourceUser, targetUser)).build();
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("copy/groups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response copyUserGroup(CopyRequest copyRequest, @Context HttpServletRequest request) {
        Session session = JcrSessionUtil.getSessionFromRequest(request);
        try {
            Response result;
            Node source = session.getNode(copyRequest.getSource());
            Node target = session.getNode(copyRequest.getTarget());
            if (UserUtils.isUser(source) && UserUtils.isUser(target)) {
                GroupCopyResults report = UserUtils.copyUserGroups(source.getName(), target.getName(), session);
                result = Response.ok().entity(report).build();
            } else {
                result = Response.status(Status.BAD_REQUEST).build();
            }
            return result;
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    private List<UserBean> getSourceUsers(Session session) throws PathNotFoundException, RepositoryException,
            ValueFormatException, InvalidQueryException {
        List<UserBean> result = new ArrayList<>();
        List<String> members = UserUtils.getDefaultGroupMembers(session);
        for (String member : members) {
            NodeIterator userNodes = UserUtils.getUserNodeById(member, session);
            while (userNodes.hasNext()) {
                Node user = userNodes.nextNode();
                result.add(new UserBean(user.getPath(), user.getName()));
            }
        }
        return result;
    }

    private List<UserBean> getTargetUsers(Session session) throws RepositoryException {
        List<UserBean> result = new ArrayList<>();
        NodeIterator userNodes = UserUtils.getAllActiveUsers(session);
        while (userNodes.hasNext()) {
            Node user = userNodes.nextNode();
            result.add(new UserBean(user.getPath(), user.getName()));
        }
        return result;
    }

    private void printGroup(Node group, Session session, CSVPrinter csvFilePrinter) throws IOException,
            RepositoryException, InvalidQueryException {
        csvFilePrinter.print(group.getName());
        List<String> members = UserUtils.getMembers(group);
        for (String member : members) {
            NodeIterator userNodes = UserUtils.getUserNodeById(member, session);
            printGroupsUsers(csvFilePrinter, userNodes, session);
        }
        csvFilePrinter.println();
    }

    private void printGroupsUsers(CSVPrinter csvFilePrinter, NodeIterator userNodes, Session session)
            throws RepositoryException, IOException {
        if (userNodes != null) {
            List<String> members = UserUtils.getDefaultGroupPath(session) != null ? UserUtils
                    .getDefaultGroupMembers(session) : null;

            while (userNodes.hasNext()) {
                Node userNode = userNodes.nextNode();
                if (UserUtils.isActiveNoneSystemUser(userNode)
                        && (members == null || UserUtils.isAlreadyMember(userNode.getName(), members))) {
                    csvFilePrinter.print(userNode.getName());
                }
            }
        }
    }

    private void printUsers(final Session session, CSVPrinter csvFilePrinter, NodeIterator userNodes)
            throws RepositoryException, IOException {
        if (userNodes != null) {
            while (userNodes.hasNext()) {
                Node userNode = userNodes.nextNode();
                if (userNode != null && UserUtils.isActiveNoneSystemUser(userNode)) {
                    printUser(userNode, session, csvFilePrinter);
                }
            }
        }
    }

    private void printUser(Node user, final Session session, CSVPrinter csvFilePrinter) throws RepositoryException,
            IOException {
        String userId = user.getName();
        csvFilePrinter.print(userId);
        NodeIterator groups = UserUtils.getGroups(user.getName(), session);
        while (groups.hasNext()) {
            Node group = groups.nextNode();
            csvFilePrinter.print(group.getName());

        }
        csvFilePrinter.println();
    }

    private void printUsersOverviewHeaders(CSVPrinter csvFilePrinter) throws IOException {
        csvFilePrinter.printRecord("User ID", "Groups");
    }

    private void printGroupsOverviewHeaders(CSVPrinter csvFilePrinter) throws IOException {
        csvFilePrinter.printRecord("Groups", "Users");

    }

}

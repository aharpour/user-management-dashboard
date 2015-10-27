package nl.openweb.hippo.umd.webservices;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

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
                    List<String> algemeenEditors = UserUtils.getAlgemeenEditorsGroupMembers(session);
                    for (String user : algemeenEditors) {
                        Node userNode = UserUtils.getUserNodeById(user, session);
                        if (userNode != null && UserUtils.isActiveNoneSystemUser(userNode)) {
                            printUser(userNode, session, csvFilePrinter);
                        }
                    }
                } catch (RepositoryException e) {
                    LOG.error(e.getMessage(), e);
                    throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
                } finally {
                    session.logout();
                }

            }
        };
        return Response.ok().header("Content-Disposition", "attachment; filename=Users.csv").entity(output).build();
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
        return Response.ok().header("Content-Disposition", "attachment; filename=Users.csv").entity(output).build();
    }

    @POST
    @Path("copy/groups/{source}/{target}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response copyUserGroup(@PathParam("source") String source, @PathParam("target") String target,
            @Context HttpServletRequest request) {
        Session session = JcrSessionUtil.getSessionFromRequest(request);

        return Response.ok().entity("test").build();
    }

    private void printGroup(Node group, Session session, CSVPrinter csvFilePrinter) throws IOException,
            RepositoryException, InvalidQueryException {
        csvFilePrinter.print(group);
        List<String> members = UserUtils.getMembers(group);
        for (String member : members) {
            Node userNode = UserUtils.getUserNodeById(member, session);
            if (UserUtils.isActiveNoneSystemUser(userNode)) {
                csvFilePrinter.print(userNode.getName());
            }
        }
        csvFilePrinter.println();
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

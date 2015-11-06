package nl.openweb.hippo.umd.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import nl.openweb.hippo.umd.beans.GroupCopyResults;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.api.HippoNodeType;

public class UserUtils {

    private static final String DEFAULT_GROUP_PROPERTY = "defaultGroup";
    private static final String CONFIGURATION_NODE_PATH = "hippo:configuration/hippo:modules/umd";

    private UserUtils() {
    }
    
    public static GroupCopyResults copyUserGroups(String sourceUserId, String targetUserId, Session session) throws RepositoryException {
        GroupCopyResults result = new GroupCopyResults();
        NodeIterator nodes = getGroups(sourceUserId, session);
        while(nodes.hasNext()) {
            Node group = nodes.nextNode();
            List<String> members = getMembers(group);
            if (isAlreadyMember(targetUserId, members)) {
                result.getAlreadyMemberOf().add(group.getName());
            } else {
                members.add(targetUserId);
                group.setProperty(HippoNodeType.HIPPO_MEMBERS, members.toArray(new String[members.size()]));
                result.getCopiedGroups().add(group.getName());
            }
        }
        session.save();
        return result;
    }

    public static NodeIterator getUserNodeById(String userId, Session session) throws InvalidQueryException,
            RepositoryException {
        NodeIterator result = null;
        if (userId != null) {
            StringBuilder stringBuilder = new StringBuilder("SELECT * FROM [hipposys:user] AS u ");
            if (!"*".equals(userId)) {
                stringBuilder.append("WHERE NAME(u) = '").append(jcrSql2Escape(userId)).append("'");
            }
            stringBuilder.append(" order by Name(u)");
            result = executeQuery(session, stringBuilder.toString());

        }
        return result;
    }

    private static String jcrSql2Escape(String userId) {
        return userId.replaceAll("['\\*]", "");
    }

    public static boolean isAlreadyMember(String user, List<String> members) throws RepositoryException {
        boolean result = false;
        for (String member : members) {
            if (user.equals(member) || "*".endsWith(member)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static List<String> getDefaultGroupMembers(Session session) throws PathNotFoundException,
            RepositoryException, ValueFormatException {
        List<String> result;
        String defaultGroup = getDefaultGroupPath(session);
        if (StringUtils.isNotBlank(defaultGroup)) {
            Node node = session.getNode(defaultGroup);
            result = getMembers(node);
            Collections.sort(result);
        } else {
            result = new ArrayList<>();
            NodeIterator allActiveUsers = getAllActiveUsers(session);
            while (allActiveUsers.hasNext()) {
                result.add(allActiveUsers.nextNode().getName());
            }
        }
        return result;
    }
    
    public static boolean isUser(Node node) throws RepositoryException {
        return node != null && node.isNodeType(HippoNodeType.NT_USER);
    }

    public static String getDefaultGroupPath(Session session) throws PathNotFoundException, RepositoryException,
            ValueFormatException {
        String defaultGroup = null;
        Node rootNode = session.getRootNode();
        if (rootNode.hasNode(CONFIGURATION_NODE_PATH)) {
            Node configuration = rootNode.getNode(CONFIGURATION_NODE_PATH);
            if (configuration.hasProperty(DEFAULT_GROUP_PROPERTY)) {
                defaultGroup = configuration.getProperty(DEFAULT_GROUP_PROPERTY).getString();
            }
        }
        return defaultGroup;
    }

    public static List<String> getMembers(Node group) throws RepositoryException {
        List<String> values = new ArrayList<>();
        if (group != null && group.hasProperty(HippoNodeType.HIPPO_MEMBERS)) {
            Property property = group.getProperty(HippoNodeType.HIPPO_MEMBERS);
            Value[] members = property.getValues();
            for (Value member : members) {
                values.add(member.getString());
            }
        }
        return values;
    }

    public static NodeIterator getAllActiveUsers(Session session) throws RepositoryException {
        return executeQuery(
                session,
                "SELECT * FROM [hipposys:user] AS u WHERE u.[hipposys:active] = 'true' AND (u.[hipposys:system] IS NULL OR u.[hipposys:system] = 'false') order by Name(u)");
    }

    public static NodeIterator getGroups(Session session) throws RepositoryException {
        return executeQuery(session, "select * from [hipposys:group] as g order by Name(g)");
    }

    public static NodeIterator getGroups(String userId, Session session) throws RepositoryException {
        StringBuilder stringBuilder = new StringBuilder(
                "SELECT * FROM [hipposys:group] AS group  WHERE group.[hipposys:members]='").append(userId).append("' order by Name(group)");
        return executeQuery(session, stringBuilder.toString());
    }

    private static NodeIterator executeQuery(Session session, String sql2Query) throws RepositoryException,
            InvalidQueryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(sql2Query, Query.JCR_SQL2);
        QueryResult queryResult = query.execute();
        NodeIterator nodes = queryResult.getNodes();
        return nodes;
    }

    public static boolean isActiveNoneSystemUser(Node userNode) throws RepositoryException {
        boolean result = userNode.hasProperty(HippoNodeType.HIPPO_ACTIVE) ? userNode.getProperty(
                HippoNodeType.HIPPO_ACTIVE).getBoolean() : false;
        if (result && userNode.hasProperty(HippoNodeType.HIPPO_SYSTEM)) {
            result = !userNode.getProperty(HippoNodeType.HIPPO_SYSTEM).getBoolean();
        }
        return result;
    }
}

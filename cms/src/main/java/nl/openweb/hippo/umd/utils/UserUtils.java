package nl.openweb.hippo.umd.utils;

import java.util.ArrayList;
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

import org.hippoecm.repository.api.HippoNodeType;

public class UserUtils {
    

    private UserUtils() {
    }
    
    public static Node getUserNodeById(String userId, Session session) throws InvalidQueryException, RepositoryException {
        Node result = null;
        NodeIterator nodeIterator = executeQuery(session, "SELECT * FROM [hipposys:user] AS u WHERE NAME(u) = '" + userId + "'");
        if (nodeIterator.hasNext()) {
            result = nodeIterator.nextNode();
        }
        return result;
    }

    public static boolean isAlreadyMember(String user, List<Value> members) throws RepositoryException {
        boolean result = false;
        for (Value value : members) {
            String member = value.getString();
            if (user.equals(member)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static List<String> getAlgemeenEditorsGroupMembers(Session session) throws PathNotFoundException, RepositoryException,
            ValueFormatException {
        Node node = session.getNode("/hippo:configuration/hippo:groups/algemeen/algemeen-editors");
        return getMembers(node);
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
                "SELECT * FROM [hipposys:user] AS u WHERE u.[hipposys:active] = 'true' AND (u.[hipposys:system] IS NULL OR u.[hipposys:system] = 'false')");
    }

    public static NodeIterator getGroups(Session session) throws RepositoryException {
        return executeQuery(session, "select * from [hipposys:group]");
    }
    
    public static NodeIterator getGroups(String userId, Session session) throws RepositoryException {
        StringBuilder stringBuilder = new StringBuilder(
                "select * from [hipposys:group] as group  WHERE group.[hipposys:members]='").append(userId).append("'");
        return executeQuery(session, stringBuilder.toString());
    }

    private static NodeIterator executeQuery(Session session, String xpathQuery) throws RepositoryException,
            InvalidQueryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(xpathQuery, Query.JCR_SQL2);
        QueryResult queryResult = query.execute();
        NodeIterator nodes = queryResult.getNodes();
        return nodes;
    }

    public static boolean isActiveNoneSystemUser(Node userNode) throws RepositoryException {
        boolean result = userNode.hasProperty(HippoNodeType.HIPPO_ACTIVE) ? userNode.getProperty(HippoNodeType.HIPPO_ACTIVE).getBoolean() : false;
        if (result && userNode.hasProperty(HippoNodeType.HIPPO_SYSTEM)) {
            result = !userNode.getProperty(HippoNodeType.HIPPO_SYSTEM).getBoolean();
        }
        return result;
    }
}
